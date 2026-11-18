package com.comp2042;

import static com.comp2042.PowerUpState.Effect.*;

/** Owns one playable run independently of JavaFX stages, input events and rendering. */
public final class GameSession {
    public enum State { READY, RUNNING, PAUSED, RECOVERY, OVER }
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

    public GameSession(Game game) {
        this(game, new Obstacle(game.getWidth(), game.getHeight()), HighScoreStore.forGame(game));
    }

    GameSession(Game game, Obstacle obstacles, HighScoreStore records) {
        this.game = game;
        this.obstacles = obstacles;
        this.records = records;
        startingBest = records.best();
        snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacles);
        coin = new Coin(game.getWidth(), game.getHeight(), snake, obstacles);
    }

    /** Fixture/scripted-board constructor; the caller supplies mutually consistent occupied cells. */
    GameSession(Game game, Obstacle obstacles, HighScoreStore records, Snake snake, Coin coin) {
        this.game = game;
        this.obstacles = obstacles;
        this.records = records;
        this.snake = snake;
        this.coin = coin;
        startingBest = records.best();
    }

    public Game game() { return game; }
    public Snake snake() { return snake; }
    public Coin coin() { return coin; }
    public Obstacle obstacles() { return obstacles; }
    public PowerUpState effects() { return effects; }
    public State state() { return state; }
    public int best() { return Math.max(records.best(), game.getScorePoints()); }
    public boolean newRecord() { return game.getScorePoints() > startingBest; }
    public boolean savingAvailable() { return records.available(); }
    public String notice() { return notice; }
    public double speed() {
        double continuing = game.level() == 3 ? ((game.getScorePoints() - 80) / 80) * .5 : 0;
        double progressive = Math.min(14, game.getSpeed() + game.level() - 1 + continuing);
        return Math.min(17, progressive + (effects.active(RUSH) ? 3 : 0));
    }

    public void resume() { if (state != State.OVER) state = State.RUNNING; }

    /** Any arrow can start a fresh or recovered snake; a paused long snake keeps its orientation. */
    public void resume(char direction) {
        if (state == State.RECOVERY || (state == State.READY && direction != 'L')) {
            try { snake.findRespawn(coin.getPos(), direction); }
            catch (IllegalStateException noLane) { return; }
        }
        resume();
    }
    public void pause() { if (state == State.RUNNING) state = State.PAUSED; }
    public void save() { records.record(game.getScorePoints()); }

    /** Advances exactly one logical step; returns true only for a successful pickup. */
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
