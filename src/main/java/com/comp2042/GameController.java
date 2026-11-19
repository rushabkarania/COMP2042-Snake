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

    /**
     * Queues up to two valid turns while rejecting duplicate and opposite directions.
     */
    static final class TurnBuffer {
        /** Creates an empty turn queue. */
        TurnBuffer() { }

        private final ArrayDeque<KeyCode> queue = new ArrayDeque<>(2);
        /**
         * Queues a valid change relative to the last queued or current direction.
         *
         * @param current current movement direction
         * @param requested requested arrow key
         */
        void offer(KeyCode current, KeyCode requested) {
            KeyCode previous = queue.isEmpty() ? current : queue.getLast();
            if (queue.size() < 2 && requested != previous && isDirectionAllowed(previous, requested)) queue.addLast(requested);
        }
        /**
         * Consumes the oldest queued turn, if present.
         *
         * @param current direction to retain when the queue is empty
         * @return next movement direction
         */
        KeyCode take(KeyCode current) { return queue.isEmpty() ? current : queue.removeFirst(); }
        /**
         * Discards all queued turns.
         */
        void clear() { queue.clear(); }
    }

    /**
     * Connects controls and window lifecycle events using the system monotonic clock.
     *
     * @param session session to control
     * @param view view receiving state and animation updates
     * @param stage application stage
     * @param onMenu callback for returning to the start panel
     */
    public GameController(GameSession session, GameView view, Stage stage, Runnable onMenu) {
        this(session, view, stage, onMenu, System::nanoTime);
    }

    /**
     * Connects controls using an injectable monotonic clock for deterministic timing tests.
     *
     * @param session session to control
     * @param view view receiving state and animation updates
     * @param stage application stage
     * @param onMenu callback for returning to the start panel
     * @param nanoTime supplier of monotonic timestamps in nanoseconds
     */
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

    /**
     * Advances at most one logical step and renders the fractional movement progress.
     *
     * @param now animation timestamp in nanoseconds on the injected clock time base
     */
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

    /**
     * Preserves a fractional step remainder, discarding long stalls to avoid teleportation.
     *
     * @param previous previous step start timestamp, or zero before startup
     * @param now current timestamp in nanoseconds
     * @param interval positive movement interval in nanoseconds
     * @return start timestamp for the next rendered movement interval
     */
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

    /**
     * Resets the run and displays the start panel.
     */
    void showStart() { reset(); }
    private void returnToMenu() { stopClock(); session.save(); onMenu.run(); }

    /**
     * Dispatches scene-level shortcuts and consumes recognised keys.
     *
     * @param event keyboard event from the scene filter
     */
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

    /**
     * Rejects direct reversals and requests that are not arrow keys.
     *
     * @param currentDirection current movement direction
     * @param requestedDirection proposed movement key
     * @return true when the requested arrow is not opposite to the current direction
     */
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
