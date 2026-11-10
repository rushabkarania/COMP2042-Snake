package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;



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
    private boolean keyPressed = false;

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
        stage.setOnHidden(event -> animation.stop());
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> advanceRound()));

    private boolean roundFinished() {
        return snek.getDead() || game.gameWon();
    }

    private void advanceRound() {
        char direction = switch (key) {
            case LEFT -> 'L';
            case RIGHT -> 'R';
            case UP -> 'U';
            case DOWN -> 'D';
            default -> throw new IllegalStateException("Invalid movement key: " + key);
        };
        boolean collectedCoin = snek.move(direction, coin.getPos());

        if (collectedCoin) {
            coinSound.play();
            powerCoin();
            game.incScore();
            if (!game.gameWon()) {
                newCoin();
            }
        }

        keyPressed = false;

        gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);

        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();
        if (roundFinished()) {
            animation.stop();
        }
    }

    public void handle(KeyEvent event) {

        if (event.getCode().isArrowKey() && !roundFinished()) {
            animation.play();

            if (isDirectionAllowed(key, event.getCode()) && !keyPressed) {
                key = event.getCode();
                keyPressed = true;
            }
        }

        if (event.getCode().equals(KeyCode.P)) {
            animation.stop();
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

        animation.stop();

        obstacle.reset();
        coinSequence.reset();
        game.resetScore();
        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        animation.setRate(game.getSpeed());
        key = KeyCode.LEFT;
        keyPressed = false;

        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();

        gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
    }

    private void powerCoin() {
        animation.setRate(game.getSpeed());
        snek.ignoreBorders(false);
        if (coin.getClass().equals(RainbowCoin.class)) {
            RainbowCoin tempCoin = (RainbowCoin) coin;
            animation.setRate(game.getSpeed() + tempCoin.getSpeedBonus());
        } else if (coin.getClass().equals(SilverCoin.class)){
            int added = obstacle.newObstacles(snek, coin, 4);
            game.decMaxScore(added);
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
