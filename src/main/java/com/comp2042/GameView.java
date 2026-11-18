package com.comp2042;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import java.util.List;
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

    private List<Point> previousBody = List.of();
    private Point previousHead = new Point();

    private double progress = 1;

    void beginStep(Snake snake) {
        previousHead = new Point(snake.getHead());
        previousBody = List.copyOf(snake.getBody());
    }

    void setProgress(double fraction) {
        progress = fraction;
    }

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

        scoreLabel = new Label();
        scoreLabel.setId("scoreLabel");
        scoreLabel.getStylesheets().add(css);
        scoreLabel.setStyle("-fx-font-size: 16px;");
        scoreLabel.setVisible(true);
        updateScoreLabel();
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

        double cell = game.getMultiplier() * fullScale;
        List<javafx.geometry.Point2D> path = bodyPath(body, head, previousBody,
                previousHead, game.getWidth(), game.getHeight(), progress);
        drawConnectedBody(path, cell, snek);
        var tip = path.get(path.size() - 1);
        paintWrapped(snek.getImageViewHead(), snek.getSnakeDir(), tip.getX(), tip.getY(), cell);
    }

    private void drawConnectedBody(List<javafx.geometry.Point2D> path, double cell, Snake snake) {
        gc.save();
        gc.scale(cell, cell);
        gc.setImageSmoothing(false);
        gc.setFill(javafx.scene.paint.Color.GREEN);
        SnakeBodyPainter.paint(new SnakeBodyPainter.Surface() {
            public void joint(double x, double y, double radius) {
                gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
            }

            public void strip(double x, double y, double angle, double length, double offset, boolean tail) {
                var image = (tail ? snake.getImageViewTail() : snake.getImageViewBody()).getImage();
                // The final 5% of the original tail bitmap is transparent padding.
                double h = image.getHeight() * (tail ? .95 : 1);
                gc.save();
                gc.translate(x, y);
                gc.rotate(angle);
                gc.drawImage(image, 0, Math.max(0, 1 - offset - length) * h, image.getWidth(), length * h,
                        -.5, -length, 1, length);
                gc.restore();
            }
        }, path, game.getWidth(), game.getHeight());
        gc.restore();
    }

    private void paintWrapped(ImageView image, int rotation, double x, double y, double cell) {
        paintSprite(image, rotation, x, y, cell);
        if (x < 0) paintSprite(image, rotation, x + game.getWidth(), y, cell);
        if (x > game.getWidth() - 1) paintSprite(image, rotation, x - game.getWidth(), y, cell);
        if (y < 0) paintSprite(image, rotation, x, y + game.getHeight(), cell);
        if (y > game.getHeight() - 1) paintSprite(image, rotation, x, y - game.getHeight(), cell);
    }

    /** Keeps occupied bends fixed while the two endpoints advance along grid edges. */
    static List<javafx.geometry.Point2D> bodyPath(List<Point> body, Point head,
            List<Point> oldBody, Point oldHead, int width, int height, double fraction) {
        var path = new java.util.ArrayList<javafx.geometry.Point2D>();
        if (fraction < 1 && !oldBody.isEmpty() && oldBody.size() == body.size()) {
            Point tail = oldBody.get(0);
            Point next = body.get(0);
            path.add(new javafx.geometry.Point2D(interpolate(tail.x, next.x, width, fraction),
                    interpolate(tail.y, next.y, height, fraction)));
        }
        for (Point cell : body) path.add(new javafx.geometry.Point2D(cell.x, cell.y));
        path.add(new javafx.geometry.Point2D(interpolate(oldHead.x, head.x, width, fraction),
                interpolate(oldHead.y, head.y, height, fraction)));
        return path;
    }

    private void paintSprite(ImageView sprite, int rotation, double x, double y, double cell) {
        gc.save();
        gc.translate((x + 0.5) * cell, (y + 0.5) * cell);
        gc.rotate(rotation);
        gc.drawImage(sprite.getImage(), -cell / 2, -cell / 2, cell, cell);
        gc.restore();
    }

    static double interpolate(int from, int to, int size, double progress) {
        if (progress >= 1) return to;
        int delta = to - from;
        if (delta > 1) delta -= size;
        if (delta < -1) delta += size;
        return from + delta * progress;
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

    void showRecovery() {
        gameOverLabel.setText("Life lost — press an arrow");
        gameOverLabel.setVisible(true);
    }

    void hideRecovery() {
        gameOverLabel.setText("GAME OVER");
        gameOverLabel.setVisible(false);
    }

    public void updateScoreLabel() {
        String next = switch (game.level()) {
            case 1 -> "/40";
            case 2 -> "/80";
            default -> "";
        };
        scoreLabel.setText("Lv " + game.level() + "  Score " + game.getScorePoints()
                + next + "  Lives " + game.hearts());
    }

}

