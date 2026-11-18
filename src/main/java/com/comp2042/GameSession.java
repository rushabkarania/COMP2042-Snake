package com.comp2042;

import static com.comp2042.PowerUpState.Effect.*;

/** Owns one playable run independently of JavaFX stages, input events and rendering. */
public final class GameSession {
    /**
     * Lifecycle states used by the controller and view.
     */
    public enum State {
        /** Fresh run waiting to start. */ READY,
        /** Movement and timers are active. */ RUNNING,
        /** Run paused with positions preserved. */ PAUSED,
        /** Life lost; a fresh snake waits for an arrow. */ RECOVERY,
        /** Run ended; further steps are ignored. */ OVER
    }
    private final Game game;
    private final Obstacle obstacles;
    private final HighScoreStore records;
    private final CoinSequence sequence = new CoinSequence();
    private final PowerUpState effects = new PowerUpState();
    private Snake snake;
    private Coin coin;
    private State state = State.READY;
    private int startingBest;
    private String notice = "A fresh start. Make it a good one.";

    /**
     * Creates a fresh run using default obstacles and persistent high-score storage.
     *
     * @param game score, lives and difficulty model
     */
    public GameSession(Game game) {
        this(game, new Obstacle(game.getWidth(), game.getHeight()), HighScoreStore.forGame(game));
    }

    /**
     * Creates a fresh run with supplied obstacle and record services.
     *
     * @param game score, lives and difficulty model
     * @param obstacles collision and layout state
     * @param records personal-best storage
     */
    GameSession(Game game, Obstacle obstacles, HighScoreStore records) {
        this.game = game;
        this.obstacles = obstacles;
        this.records = records;
        startingBest = records.best();
        snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacles);
        coin = new Coin(game.getWidth(), game.getHeight(), snake, obstacles);
    }

    /**
     * Creates a scripted run; the caller supplies mutually consistent occupied cells.
     *
     * @param game score, lives and difficulty model
     * @param obstacles collision and layout state
     * @param records personal-best storage
     * @param snake initial snake
     * @param coin initial collectible
     */
    GameSession(Game game, Obstacle obstacles, HighScoreStore records, Snake snake, Coin coin) {
        this.game = game;
        this.obstacles = obstacles;
        this.records = records;
        this.snake = snake;
        this.coin = coin;
        startingBest = records.best();
    }

    /**
     * Exposes the session game model.
     *
     * @return live score, lives and settings model
     */
    public Game game() { return game; }
    /**
     * Exposes the current snake, which may change after recovery.
     *
     * @return current snake model
     */
    public Snake snake() { return snake; }
    /**
     * Exposes the current collectible.
     *
     * @return current pickup
     */
    public Coin coin() { return coin; }
    /**
     * Exposes the session obstacle state.
     *
     * @return live layout and collision model
     */
    public Obstacle obstacles() { return obstacles; }
    /**
     * Exposes the independent effect timers.
     *
     * @return live timer state
     */
    public PowerUpState effects() { return effects; }
    /**
     * Returns the current lifecycle state.
     *
     * @return current session state
     */
    public State state() { return state; }
    /**
     * Returns the greater of the stored best and the current score.
     *
     * @return best known score
     */
    public int best() { return Math.max(records.best(), game.getScorePoints()); }
    /**
     * Compares the current score with the best at the start of the run.
     *
     * @return true when this run has exceeded its starting record
     */
    public boolean newRecord() { return game.getScorePoints() > startingBest; }
    /**
     * Reports whether the last storage operation was available.
     *
     * @return false after a storage access failure
     */
    public boolean savingAvailable() { return records.available(); }
    /**
     * Returns the latest gameplay message.
     *
     * @return notice text for the view
     */
    public String notice() { return notice; }
    /**
     * Combines base speed, score progression and rush, capped at 14 normally or 17 with rush.
     *
     * @return movement steps per second
     */
    public double speed() {
        double continuing = game.level() == 3 ? ((game.getScorePoints() - 80) / 80) * .5 : 0;
        double progressive = Math.min(14, game.getSpeed() + game.level() - 1 + continuing);
        return Math.min(17, progressive + (effects.active(RUSH) ? 3 : 0));
    }

    /**
     * Sets the state to RUNNING unless the run is already over.
     */
    public void resume() { if (state != State.OVER) state = State.RUNNING; }

    /**
     * Resumes in the requested direction; fresh or recovered snakes are repositioned when needed.
     *
     * @param direction U, D, L or R; a paused snake keeps its existing orientation
     */
    public void resume(char direction) {
        if (state == State.RECOVERY || (state == State.READY && direction != 'L')) {
            try { snake.findRespawn(coin.getPos(), direction); }
            catch (IllegalStateException noLane) { return; }
        }
        resume();
    }
    /**
     * Pauses a running session; other states are unchanged.
     */
    public void pause() { if (state == State.RUNNING) state = State.PAUSED; }
    /**
     * Records the current score; storage failures remain available through savingAvailable().
     */
    public void save() { records.record(game.getScorePoints()); }

    /**
     * Advances one logical move, resolving collisions, pickups, timers and layout transitions.
     *
     * @param direction U, D, L or R
     * @return true only for a successful pickup; false outside RUNNING or after a fatal step
     */
    public boolean step(char direction) {
        if (state != State.RUNNING) return false;
        snake.ignoreBorders(effects.active(WRAP));
        boolean collected = snake.move(direction, coin.getPos(), coin.type() != CoinSequence.Type.SHRINK);
        if (snake.getDead()) {
            effects.clear();
            if (game.loseHeart()) {
                snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacles);
                try {
                    snake.findRespawn(coin.getPos());
                    state = State.RECOVERY;
                    notice = "Your score stays. Your snake gets a fresh start.";
                } catch (IllegalStateException noLane) {
                    state = State.OVER;
                    notice = "No safe space to respawn.";
                    save();
                }
            } else {
                state = State.OVER;
                notice = "A good run. There is always room for one more.";
                save();
            }
            return false;
        }
        effects.tick();
        if (collected) {
            int oldLevel = game.level();
            game.addPoints(coin.getPoints());
            switch (coin.type()) {
                case BLUE -> { effects.activate(WRAP, 48); notice = "Wrap active! Travel through the board edges."; }
                case RAINBOW -> { effects.activate(RUSH, 40); notice = "Rush! A little faster for 40 steps."; }
                case SILVER -> { effects.activate(GAP, 48); notice = "Gate open! A two-cell hedge gap for 48 moves."; }
                case SHRINK -> {
                    int removed = snake.shrink(3);
                    notice = removed == 0 ? "Already at your shortest. +5 points!" : "Room to move! Tail shortened by " + removed + ".";
                }
                case GOLD -> notice = "Golden! +15 points.";
                default -> notice = "+5 points. Keep growing.";
            }
            announceLevel(oldLevel);
            if (Coin.choosePosition(game.getWidth(), game.getHeight(), snake, obstacles).isEmpty()) {
                int beforeBonus = game.level();
                snake.shrink(Math.max(3, snake.getBody().size() / 2));game.addPoints(25);announceLevel(beforeBonus);
                notice = "Garden cleared! +25 bonus and a shorter tail. Keep going.";
            }
            spawnCoin();
        }
        // Validate against the replacement pickup as well as the snake's current cells.
        obstacles.advance(snake, coin.getPos(), effects.steps(GAP));
        obstacles.requestLayout(game.getScorePoints(), snake, coin.getPos());
        return collected;
    }

    private void announceLevel(int previous) {
        if (game.level() != previous) notice = game.level() == 2
                ? "Level 2 · Hedge borders. Watch for the outlines." : "Level 3 · New hedge layouts. Plan your route.";
    }

    private void spawnCoin() {
        int width = game.getWidth(), height = game.getHeight();
        CoinSequence.Type type = game.isPowerUp() ? sequence.next() : CoinSequence.Type.RED;
        coin = switch (type) {
            case RED -> new Coin(width, height, snake, obstacles);
            case GOLD -> new GoldCoin(width, height, snake, obstacles);
            case BLUE -> new BlueCoin(width, height, snake, obstacles);
            case SILVER -> new SilverCoin(width, height, snake, obstacles);
            case RAINBOW -> new RainbowCoin(width, height, snake, obstacles);
            case SHRINK -> new ShrinkCoin(width, height, snake, obstacles);
        };
    }

    /** Saves the previous run and restores all transient state for a new attempt. */
    public void reset() {
        save();
        startingBest = records.best();
        game.resetScore();
        obstacles.reset();
        effects.clear();
        sequence.reset();
        snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacles);
        coin = new Coin(game.getWidth(), game.getHeight(), snake, obstacles);
        state = State.READY;
        notice = "A fresh start. Make it a good one.";
    }
}
