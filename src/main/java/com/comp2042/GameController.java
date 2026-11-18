package com.comp2042;

import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.util.ArrayDeque;



public class GameController {
    private Snake snek;
    private Coin coin;
    private GameView gameView;
    private Game game;
    private Stage stage;
    private final CoinSequence coinSequence = new CoinSequence();
    private final CoinSound coinSound = new CoinSound();
    private Obstacle obstacle;

    private KeyCode key = KeyCode.LEFT;
    private final TurnBuffer turns = new TurnBuffer();
    private boolean running;
    private long lastStep;
    private double speed;

    static final class TurnBuffer {
        private final ArrayDeque<KeyCode> queue = new ArrayDeque<>(2);

        void offer(KeyCode current, KeyCode requested) {
            KeyCode previous = queue.isEmpty() ? current : queue.getLast();
            if (queue.size() < 2 && requested != previous && isDirectionAllowed(previous, requested)) {
                queue.addLast(requested);
            }
        }

        KeyCode take(KeyCode current) {
            return queue.isEmpty() ? current : queue.removeFirst();
        }

        void clear() {
            queue.clear();
        }
    }

    private int legacyScoreMultiplier = 2;

    private void calculateBonusScore() {
        int bonus = game.getScorePoints() * legacyScoreMultiplier;
        System.out.println("Bonus score: " + bonus);
    }

    public GameController(Snake snek, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.snek = snek;
        this.coin = coin;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;

        this.obstacle = obstacle;
        stage.setOnHidden(event -> stopMovement());
        
        speed = game.getSpeed();
    }

    private final AnimationTimer animation = new AnimationTimer() {
        @Override
        public void handle(long now) {
            long interval = (long) (1_000_000_000 / speed);
            if (lastStep == 0 || now - lastStep >= interval) {
                lastStep = stepStart(lastStep, now, interval);
                gameView.beginStep(snek);
                advanceRound();
            }
            gameView.setProgress(running ? Math.min(1.0, (now - lastStep) * speed / 1_000_000_000) : 1);
            gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
        }
    };

    /** Preserve the fractional step time on a late pulse; resume a stalled window without a jump. */
    static long stepStart(long previous, long now, long interval) {
        if (previous == 0 || now - previous >= 2 * interval) return now;
        return previous + interval;
    }

    private void stopMovement() {
        animation.stop();
        running = false;
        turns.clear();
        lastStep = 0;
        gameView.setProgress(1);
    }

    private boolean roundFinished() {
        return snek.getDead() || game.gameWon();
    }

    private void advanceRound() {
        key = turns.take(key);
        char direction = switch (key) {
            case LEFT -> 'L';
            case RIGHT -> 'R';
            case UP -> 'U';
            case DOWN -> 'D';
            default -> throw new IllegalStateException("Invalid movement key: " + key);
        };
        boolean collectedCoin = snek.move(direction, coin.getPos());

        if (snek.getDead()) {
            stopMovement();
            if (game.loseHeart()) {
                snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
                snek.findRespawn(coin.getPos());
                gameView.beginStep(snek);
                key = KeyCode.LEFT;
                speed = game.getSpeed() + game.level() - 1;
                gameView.showRecovery();
            }
        } else if (game.level() == 3) {
            obstacle.advancePatrol(snek, coin.getPos());
        }

        if (collectedCoin) {
            coinSound.play();
            int previousLevel = game.level();
            powerCoin();
            if (previousLevel < 2 && game.level() >= 2) {
                game.decMaxScore(obstacle.placeObstacles(snek, coin.getPos(), 4));
            }
            if (previousLevel < 3 && game.level() == 3) obstacle.startPatrol();
            speed += game.level() - 1;
            game.incScore();
            if (!game.gameWon()) {
                newCoin();
            }
        }


        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();
        if (roundFinished()) {
            stopMovement();
        }
    }

    public void handle(KeyEvent event) {

        if (event.getCode().isArrowKey() && !roundFinished()) {
            if (!isDirectionAllowed(key, event.getCode()) && !running) return;
            gameView.hideRecovery();
            turns.offer(key, event.getCode());
            if (!running) {
                running = true;
                animation.start();
            }
        }

        if (event.getCode().equals(KeyCode.P)) {
            stopMovement();
        }

        if (event.getCode().equals(KeyCode.TAB)) {
            reset();
        }

        if (event.getCode() == KeyCode.PLUS
                || event.getCode() == KeyCode.ADD
                || event.getCode() == KeyCode.EQUALS) {
            gameView.scaleToFullscreen(true, stage, obstacle, snek, coin);
            gameView.gameOverLabel.setStyle("-fx-font-size: 50;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 50;");
            gameView.scoreLabel.setStyle("-fx-font-size: 35;");
        }

        if (event.getCode() == KeyCode.MINUS
                || event.getCode() == KeyCode.SUBTRACT) {
            gameView.scaleToFullscreen(false, stage, obstacle, snek, coin);
            gameView.gameOverLabel.setStyle("-fx-font-size: 15;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 15;");
            gameView.scoreLabel.setStyle("-fx-font-size: 15;");
        }

        if (event.getCode().equals(KeyCode.ESCAPE)) {
            MainMenuController mainMenuController = new MainMenuController();
            mainMenuController.getViewManager().getPrimaryStage().show();
            mainMenuController.getViewManager().getGameStage().close();
        }
    }

        /**
         * Checks whether a requested direction is accepted.
         *
         * @param currentDirection the current movement direction
         * @param requestedDirection the requested keyboard direction
         * @return true if the direction is accepted; otherwise false
         */
        static boolean isDirectionAllowed(
                KeyCode currentDirection,
                KeyCode requestedDirection) {

            if (!requestedDirection.isArrowKey()) {
                return false;
            }

            return switch (requestedDirection) {
                case LEFT -> currentDirection != KeyCode.RIGHT;
                case RIGHT -> currentDirection != KeyCode.LEFT;
                case DOWN -> currentDirection != KeyCode.UP;
                case UP -> currentDirection != KeyCode.DOWN;
                default -> false;
            };
        }


    private void reset() {

        stopMovement();

        gameView.hideRecovery();
        obstacle.reset();
        coinSequence.reset();
        game.resetScore();
        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        speed = game.getSpeed();
        key = KeyCode.LEFT;

        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();

        gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
    }

    private void powerCoin() {
        speed = game.getSpeed();
        snek.ignoreBorders(false);
        if (coin.getClass().equals(RainbowCoin.class)) {
            RainbowCoin tempCoin = (RainbowCoin) coin;
            speed = game.getSpeed() + tempCoin.getSpeedBonus();
        } else if (coin.getClass().equals(SilverCoin.class)){
            if (game.level() == 2) {
                game.decMaxScore(obstacle.newObstacles(snek, coin, 4));
            } else if (game.level() == 3) {
                obstacle.freezePatrol();
            }
        } else if (coin.getClass().equals(BlueCoin.class)) {
            snek.ignoreBorders(true);
        }
        game.addPoints(coin.getPoints());
    }

    private void newCoin() {
        int width = game.getWidth();
        int height = game.getHeight();
        CoinSequence.Type type = game.isPowerUp() ? coinSequence.next() : CoinSequence.Type.RED;
        coin = switch (type) {
            case RED -> new Coin(width, height, snek, obstacle);
            case GOLD -> new GoldCoin(width, height, snek, obstacle);
            case BLUE -> new BlueCoin(width, height, snek, obstacle);
            case RAINBOW -> new RainbowCoin(width, height, snek, obstacle);
            case SILVER -> new SilverCoin(width, height, snek, obstacle);
        };
    }
}

