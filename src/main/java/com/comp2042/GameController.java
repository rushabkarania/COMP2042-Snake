package com.comp2042;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.util.ArrayDeque;
import java.util.function.LongSupplier;

/** Connects keyboard/buttons and the animation clock to a testable game session. */
public class GameController {
    private final GameSession session;
    private final GameView view;
    private final Stage stage;
    private final Runnable onMenu;
    private final TurnBuffer turns = new TurnBuffer();
    private final CoinSound sound = new CoinSound();
    private KeyCode key = KeyCode.LEFT;
    private long lastStep;
    private boolean muted;
    private final LongSupplier nanoTime;

    static final class TurnBuffer {
        private final ArrayDeque<KeyCode> queue = new ArrayDeque<>(2);
        void offer(KeyCode current, KeyCode requested) {
            KeyCode previous = queue.isEmpty() ? current : queue.getLast();
            if (queue.size() < 2 && requested != previous && isDirectionAllowed(previous, requested)) queue.addLast(requested);
        }
        KeyCode take(KeyCode current) { return queue.isEmpty() ? current : queue.removeFirst(); }
        void clear() { queue.clear(); }
    }

    public GameController(GameSession session, GameView view, Stage stage, Runnable onMenu) {
        this(session, view, stage, onMenu, System::nanoTime);
    }

    GameController(GameSession session, GameView view, Stage stage, Runnable onMenu, LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
        this.session = session;
        this.view = view;
        this.stage = stage;
        this.onMenu = onMenu;
        view.bindActions(this::play, this::togglePause, this::reset,
                stage::close, this::toggleMute, () -> stage.setFullScreen(!stage.isFullScreen()));
        stage.setOnHidden(event -> { stopClock(); session.save(); });
        stage.setOnCloseRequest(event -> returnToMenu());
        stage.focusedProperty().addListener((property, before, focused) -> {
            if (!focused && session.state() == GameSession.State.RUNNING) pause();
        });
    }

    private final AnimationTimer animation = new AnimationTimer() {
        @Override public void handle(long now) { pulse(now); }
    };

    /** Uses one clock for the real animation pulse and deterministic input-timing checks. */
    void pulse(long now) {
        if (session.state() != GameSession.State.RUNNING) { stopClock(); return; }
        long interval = (long) (1_000_000_000 / session.speed());
        if (now - lastStep >= interval) {
            lastStep = stepStart(lastStep, now, interval);
            advanceOneStep();
        }
        view.setProgress(session.state() == GameSession.State.RUNNING
                ? Math.max(0, Math.min(1, (now - lastStep) * session.speed() / 1_000_000_000)) : 1);
        view.render();
    }

    private void advanceOneStep() {
        view.beginStep(session.snake());
        key = turns.take(key);
        boolean collected = session.step(direction(key));
        if (collected && !muted) sound.play();
        if (session.state() != GameSession.State.RUNNING) {
            stopClock();
            key = KeyCode.LEFT;
            view.beginStep(session.snake());
        }
        view.updateHud();
    }

    /** Preserves the fractional step remainder, discarding long stalls to avoid teleportation. */
    static long stepStart(long previous, long now, long interval) {
        return previous == 0 || now - previous >= 2 * interval ? now : previous + interval;
    }

    private static char direction(KeyCode code) {
        return switch (code) {
            case UP -> 'U'; case RIGHT -> 'R'; case DOWN -> 'D'; case LEFT -> 'L';
            default -> throw new IllegalArgumentException("Not a movement key: " + code);
        };
    }

    private void stopClock() {
        animation.stop();
        turns.clear();
        lastStep = 0;
        view.setProgress(1);
    }

    /** Starts in the key handler instead of waiting for the next timer tick. */
    private void startOrResume() {
        if (session.state() == GameSession.State.OVER) return;
        if (session.state() != GameSession.State.RUNNING) {
            session.resume(direction(key));
            if (session.state() != GameSession.State.RUNNING) return;
            lastStep = nanoTime.getAsLong();
            view.setProgress(0);
            advanceOneStep();
            if (session.state() == GameSession.State.RUNNING) animation.start();
            view.render();
        }
        if (!view.root().isFocused()) view.root().requestFocus();
    }

    /** Play reveals the board; a fresh or recovered snake starts only on an arrow key. */
    private void play() {
        if (session.state() == GameSession.State.PAUSED) startOrResume();
        else if (session.state() == GameSession.State.READY) view.waitForArrow();
        view.root().requestFocus();
    }

    private void pause() {
        session.pause();
        stopClock();
        view.refresh();
    }

    private void togglePause() {
        if (session.state() == GameSession.State.RUNNING) pause();
        else if (session.state() == GameSession.State.PAUSED) startOrResume();
        view.root().requestFocus();
    }

    private void toggleMute() {
        muted = !muted;
        view.setMuted(muted);
        view.root().requestFocus();
    }

    private void reset() {
        stopClock();
        session.reset();
        key = KeyCode.LEFT;
        view.beginStep(session.snake());
        view.showStartScreen();
        view.refresh();
        view.root().requestFocus();
    }

    void showStart() { reset(); }
    private void returnToMenu() { stopClock(); session.save(); onMenu.run(); }

    /** Dispatches shortcuts at scene level so buttons never steal movement keys. */
    public void handle(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code.isArrowKey()) {
            var state = session.state();
            if (state == GameSession.State.READY || state == GameSession.State.RECOVERY) {
                turns.clear();
                key = code;
                startOrResume();
            } else if (state == GameSession.State.RUNNING || state == GameSession.State.PAUSED) {
                if (state == GameSession.State.PAUSED && !isDirectionAllowed(key, code)) { event.consume(); return; }
                turns.offer(key, code);
                if (state == GameSession.State.PAUSED) startOrResume();
            }
        } else switch (code) {
            case P, SPACE -> togglePause();
            case ENTER -> {
                if (session.state() == GameSession.State.OVER) reset();
                else if (session.state() != GameSession.State.RECOVERY) play();
            }
            case TAB, R -> reset();
            case M -> toggleMute();
            case F, F11 -> stage.setFullScreen(!stage.isFullScreen());
            case PLUS, ADD, EQUALS -> stage.setFullScreen(true);
            case MINUS, SUBTRACT -> stage.setFullScreen(false);
            case ESCAPE -> { if (stage.isFullScreen()) stage.setFullScreen(false); else returnToMenu(); }
            default -> { return; }
        }
        event.consume();
    }

    /** Rejects direct reversals and keys that are not movement directions. */
    static boolean isDirectionAllowed(KeyCode currentDirection, KeyCode requestedDirection) {
        if (!requestedDirection.isArrowKey()) return false;
        return switch (requestedDirection) {
            case LEFT -> currentDirection != KeyCode.RIGHT;
            case RIGHT -> currentDirection != KeyCode.LEFT;
            case DOWN -> currentDirection != KeyCode.UP;
            case UP -> currentDirection != KeyCode.DOWN;
            default -> false;
        };
    }
}
