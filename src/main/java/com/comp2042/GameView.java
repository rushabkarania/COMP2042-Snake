package com.comp2042;

import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;

public class GameView {

    public Canvas gridCanvas;
    public GraphicsContext gc;

    public Label gameOverLabel;
    public Label gameWonLabel;
    public Label scoreLabel;
    private boolean gameOverVis = false;
    private boolean gameWonVis = false;

    private Game game;
    private double initX;
    private double initY;
    private double fullScale = 1;
    private boolean fullsize = false;

    public GameView(Game game) {
        this.game = game;
    }

    public void makeScene(Scene scene, Snake snek, Coin coin) {
        Scene gameScene = scene;
        gridCanvas = new Canvas(gameScene.getWidth(), gameScene.getHeight());
        initX = scene.getWidth();
        initY = scene.getHeight();
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(coin, snek, gc);

        gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setId("gameOverLabel");
        String css = "resources/views/styling.css";
        gameOverLabel.getStylesheets().add(css);
        gameOverLabel.setVisible(false);

        gameWonLabel = new Label("YOU WON!");
        gameWonLabel.setId("gameWonLabel");
        gameWonLabel.getStylesheets().add(css);
        gameWonLabel.setVisible(false);

        scoreLabel = new Label("Score: " + game.getScorePoints());
        scoreLabel.setId("scoreLabel");
        scoreLabel.getStylesheets().add(css);
        scoreLabel.setFont(new Font("Arial", 20));
        scoreLabel.setVisible(true);
    }

    public void scaleToFullscreen(boolean fullScreen, Stage primaryStage, Obstacle obstacle, Snake snek, Coin coin) {
        primaryStage.setFullScreen(fullScreen);

        double xScaling = Screen.getPrimary().getVisualBounds().getHeight() / initY;
        double yScaling = Screen.getPrimary().getVisualBounds().getWidth() / initX;

        if (fullScreen && !fullsize) {
            if (xScaling < yScaling) {
                fullScale = xScaling;
            } else {
                fullScale = yScaling;
            }
            gridCanvas.setWidth(gridCanvas.getWidth() * fullScale);
            gridCanvas.setHeight(gridCanvas.getHeight() * fullScale);
            fullsize = true;
        } else if (!fullScreen) {
            fullScale = 1;
            gridCanvas.setWidth(initX);
            gridCanvas.setHeight(initY);
            fullsize = false;
        }

        gameOverLabel.setFont(new Font("Arial", 25 * fullScale));
        gameWonLabel.setFont(new Font("Arial", 25 * fullScale));
        drawGrid(coin, snek, obstacle, gc, false, 0);
    }

    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle, GraphicsContext gc, boolean a, int b) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        drawCoin(coin, gc);

        drawSnake(snek);

        gc.setFill(obstacle.getColor());
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(point.x * game.getMultiplier() * fullScale, point.y * game.getMultiplier() * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
        }
    }

    private void drawCoin(Coin coin, GraphicsContext gc) {
        if (game.gameWon()) return;
        double cell = game.getMultiplier() * fullScale;
        double size = cell - 3 * fullScale;
        var position = coin.getPos();
        gc.drawImage(coin.getImage(), position.x * cell, position.y * cell, size, size);
    }

    private void drawGrid(Coin coin, Snake snek, GraphicsContext gc) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        drawCoin(coin, gc);

        drawSnake(snek);
    }

    /**
     * Draws the snake using its current positions rather than turn history.
     *
     * @param snek the snake to draw
     */
    private void drawSnake(Snake snek) {
        Point head = snek.getHead();
        List<Point> body = SnakeSpriteSelector.drawingBody(
                snek.getBody(), head, snek.getDead());

        for (int index = 0; index < body.size(); index++) {
            drawBody(snek, body, head, index);
        }

        drawSprite(
                snek.getImageViewHead(),
                head,
                snek.getSnakeDir());
    }

    /**
     * Draws one tail or body segment.
     *
     * @param snek the snake supplying the sprite images
     * @param body the ordered drawing positions, starting at the tail
     * @param head the current head position
     * @param index the segment index
     */
    private void drawBody(
            Snake snek, List<Point> body, Point head, int index) {

        Point current = body.get(index);
        Point next = index + 1 < body.size()
                ? body.get(index + 1)
                : head;

        if (index == 0) {
            drawSprite(
                    snek.getImageViewTail(),
                    current,
                    SnakeSpriteSelector.tailRotation(
                            current, next, game.getWidth(), game.getHeight()));
            return;
        }

        Point previous = body.get(index - 1);

        SnakeSpriteSelector.Sprite sprite = SnakeSpriteSelector.select(
                previous,
                current,
                next,
                game.getWidth(),
                game.getHeight());

        ImageView image = sprite.corner()
                ? snek.getImageViewTurn()
                : snek.getImageViewBody();

        drawSprite(image, current, sprite.rotation());
    }

    /**
     * Draws a rotated image within one scaled board cell.
     *
     * @param image the image view containing the sprite
     * @param position the board position
     * @param rotation clockwise rotation in degrees
     */
    private void drawSprite(
            ImageView image, Point position, int rotation) {

        image.setRotate(rotation);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image rotatedImage = image.snapshot(params, null);

        double cellSize = game.getMultiplier() * fullScale;

        gc.drawImage(
                rotatedImage,
                position.x * cellSize,
                position.y * cellSize,
                cellSize,
                cellSize);
    }



    public void gameWon() {
        if (game.gameWon() && !gameWonVis) {
            gameWonVis = true;
            gameWonLabel.setVisible(true);
        } else if (!game.gameWon() && gameWonVis) {
            gameWonVis = false;
            gameWonLabel.setVisible(false);
        }
    }

    /**
     * Updates the game-over message according to the snake's state.
     *
     * @param snek the snake whose state determines message visibility
     */
    public void gameOver(Snake snek) {
        if (snek.getDead() && !gameOverVis) {
            gameOverVis = true;
            gameOverLabel.setVisible(true);
        } else if (!snek.getDead() && gameOverVis) {
            gameOverLabel.setVisible(false);
            gameOverVis = false;
        }
    }

    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getScorePoints());
    }

}

