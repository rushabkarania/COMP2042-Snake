package com.comp2042;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in checks against native JavaFX windows; requires DISPLAY on Linux. */
@EnabledIfSystemProperty(named="snake.ui.tests", matches="true")
class GardenUiTest {
    @TempDir Path directory;
    private static <T> T fx(Callable<T> action) throws Exception {
        var future = new FutureTask<>(action); Platform.runLater(future); return future.get(12, TimeUnit.SECONDS);
    }
    private static void key(Stage stage, KeyCode code) throws Exception {
        fx(() -> { Event.fireEvent(stage.getScene(), new KeyEvent(KeyEvent.KEY_PRESSED,"","",code,false,false,false,false));return null; });
    }
    private static void await(BooleanSupplier condition) throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(6);
        while (!fx(condition::getAsBoolean)) {
            if(System.nanoTime()>deadline) fail("UI state transition timed out");
            Thread.sleep(30);
        }
        Thread.sleep(120);
    }
    private static void capture(Stage stage, String name) throws Exception {
        Path directory=Path.of(System.getProperty("snake.screenshots","target/ui-screenshots"));Files.createDirectories(directory);
        Thread.sleep(150);
        fx(() -> {
            var image=stage.getScene().snapshot(null);var pixels=image.getPixelReader();
            var buffered=new BufferedImage((int)image.getWidth(),(int)image.getHeight(),BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<buffered.getHeight();y++) for(int x=0;x<buffered.getWidth();x++) buffered.setRGB(x,y,pixels.getArgb(x,y));
            javax.imageio.ImageIO.write(buffered,"png",directory.resolve(name+".png").toFile());return null;
        });
    }

    @Test void nativeMenuGameplayRecoveryGameOverAndFullscreen() throws Exception {
        System.setProperty("snake.data.dir",directory.toString());
        var startup=new java.util.concurrent.CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); startup.countDown(); });
        assertTrue(startup.await(10,TimeUnit.SECONDS));
        try {
            var manager=fx(() -> { new Main().start(new Stage());return Main.getViewManager(); });
            Stage realGame=fx(manager::getPrimaryStage);
            assertSame(GardenAssets.logo(32),fx(() -> realGame.getIcons().get(1)));
            assertNotNull(fx(() -> realGame.getScene().lookup("#menu-snake-sprites")));
            assertTrue(fx(() -> realGame.getScene().lookup("#menu-snake-sprites") instanceof javafx.scene.canvas.Canvas));
            assertSame(GardenAssets.image("points"),fx(() -> ((javafx.scene.image.ImageView)realGame.getScene().lookup("#score-icon")).getImage()));
            assertEquals("Snake",fx(() -> ((Label)realGame.getScene().lookup("#state-title")).getText()));
            assertTrue(fx(() -> realGame.getScene().getRoot().lookupAll(".difficulty-button").isEmpty()));
            capture(realGame,"01-start");
            assertTrue(fx(() -> {
                var illustration=realGame.getScene().lookup("#menu-snake-sprites");
                var grass=realGame.getScene().lookup("#menu-grass");
                return Math.abs(illustration.localToScene(illustration.getLayoutBounds()).getMaxY()
                    -grass.localToScene(grass.getLayoutBounds()).getMinY())<1;
            }),"Snake illustration must sit directly against the grass strip");
            fx(() -> {
                ((Button)realGame.getScene().getRoot().lookupAll(".secondary-button").iterator().next()).fire();
                return null;
            });
            assertEquals("How to play",fx(() -> ((Label)realGame.getScene().lookup("#state-title")).getText()));
            capture(realGame,"02-help");
            fx(() -> { ((Button)realGame.getScene().lookup("#primary-action")).fire();return null; });
            assertEquals("Snake",fx(() -> ((Label)realGame.getScene().lookup("#state-title")).getText()));
            // Play reveals the board, without advancing until the first arrow.
            fx(() -> { ((Button)realGame.getScene().lookup("#primary-action")).fire();return null; });
            assertFalse(fx(() -> realGame.getScene().lookup("#state-overlay").isVisible()));
            assertTrue(fx(() -> realGame.getScene().lookup("#direction-prompt").isVisible()));
            Thread.sleep(600);
            assertTrue(fx(() -> realGame.getScene().lookup("#direction-prompt").isVisible()));
            capture(realGame,"03-ready");
            key(realGame,KeyCode.LEFT);
            assertFalse(fx(() -> realGame.getScene().lookup("#direction-prompt").isVisible()));
            key(realGame,KeyCode.P);
            assertEquals("Paused",fx(() -> ((Label)realGame.getScene().lookup("#state-title")).getText()));
            capture(realGame,"03-paused");
            key(realGame,KeyCode.ESCAPE);
            assertTrue(fx(realGame::isShowing));
            assertEquals("Snake",fx(() -> ((Label)realGame.getScene().lookup("#state-title")).getText()));
            fx(() -> { realGame.hide();return null; });

            // Scripted starting score positions the next ordinary pickup at the level boundary.
            Game game=new Game(20,20,false,'N');game.addPoints(35);
            Obstacle rocks=new Obstacle(20,20);Snake snake=new Snake(20,20,false,rocks);
            var session=new GameSession(game,rocks,new HighScoreStore(directory.resolve("ui-best.txt")),snake,new Coin(new Point(9,10),Coin.Sprite.GOLD));
            GameView[] view=new GameView[1];
            Stage stage=fx(() -> {
                Stage result=new Stage();view[0]=new GameView(session);
                var controller=new GameController(session,view[0],result,()->result.hide());
                Scene scene=new Scene(view[0].root(),640,724);
                result.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
                scene.getStylesheets().add(Main.class.getResource("/resources/views/garden.css").toExternalForm());
                scene.addEventFilter(KeyEvent.KEY_PRESSED,controller::handle);result.setScene(scene);result.show();return result;
            });
            // Virtual displays have no audio device; mute before the scripted pickup.
            key(stage,KeyCode.M);
            fx(() -> {
                Event.fireEvent(stage.getScene(),new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.LEFT,false,false,false,false));
                assertEquals(50,game.getScorePoints(),"Pickup starts in the arrow-key handler");
                Event.fireEvent(stage.getScene(),new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.P,false,false,false,false));
                return null;
            });
            assertEquals(GameSession.State.PAUSED,fx(session::state));
            assertTrue(rocks.getObstacles().isEmpty());assertEquals(8,rocks.pendingCells().size());
            assertEquals(12,rocks.warningMoves());
            assertEquals("50",fx(() -> ((Label)stage.getScene().lookup("#score-value")).getText()));
            fx(() -> { session.resume();view[0].refresh();return null; });
            capture(stage,"04-rock-garden");
            fx(() -> { session.pause();view[0].refresh();return null; });
            Point position=fx(() -> session.snake().getHead());Thread.sleep(220);
            assertEquals(position,fx(() -> session.snake().getHead()));
            assertTrue(fx(() -> stage.getScene().lookup("#sound-toggle").getAccessibleText().startsWith("Sound off")));
            key(stage,KeyCode.F);await(stage::isFullScreen);
            await(() -> stage.getScene().getWidth() > 1000);
            capture(stage,"05-fullscreen");
            key(stage,KeyCode.ESCAPE);await(() -> !stage.isFullScreen());
            await(() -> stage.getScene().getWidth() < 1000);
            fx(() -> { stage.setWidth(560);stage.setHeight(700);return null; });
            capture(stage,"06-small-window");
            assertTrue(fx(() -> {
                var board=stage.getScene().lookup("#game-board");
                var bounds=board.localToScene(board.getLayoutBounds());
                var header=view[0].root().getTop();
                var footer=view[0].root().getBottom();
                return Math.abs(bounds.getWidth()-bounds.getHeight()) < 1
                    && bounds.getMinY() >= header.localToScene(header.getLayoutBounds()).getMaxY()
                    && bounds.getMaxY() <= footer.localToScene(footer.getLayoutBounds()).getMinY();
            }), "The square board must fit between the HUD and footer");
            assertTrue(fx(() -> stage.getScene().lookup("#lives-icons").getBoundsInParent().getMaxX() < 560));
            fx(() -> { stage.setWidth(640);stage.setHeight(724);return null; });
            for(int life=0;life<3;life++) {
                key(stage,KeyCode.LEFT);
                await(() -> session.state()!=GameSession.State.RUNNING);
                assertEquals(2-life,fx(game::hearts));
                if(life<2) {
                    assertEquals(GameSession.State.RECOVERY,fx(session::state));
                    assertFalse(fx(() -> stage.getScene().lookup("#state-overlay").isVisible()));
                    assertTrue(fx(() -> stage.getScene().lookup("#direction-prompt").isVisible()));
                    Point resting=fx(() -> session.snake().getHead());
                    assertTrue(Math.abs(resting.x-10)+Math.abs(resting.y-10)<=2,"Prefer the centre, moving only for an occupied lane");
                    int keptScore=fx(game::getScorePoints);
                    key(stage,KeyCode.ENTER);key(stage,KeyCode.SPACE);Thread.sleep(240);
                    assertEquals(GameSession.State.RECOVERY,fx(session::state));
                    assertEquals(resting,fx(() -> session.snake().getHead()));
                    assertEquals(keptScore,fx(game::getScorePoints));
                }
                if(life==0) capture(stage,"07-life-lost");
            }
            assertEquals(GameSession.State.OVER,fx(session::state));
            assertEquals("Game over",fx(() -> ((Label)stage.getScene().lookup("#state-title")).getText()));
            capture(stage,"08-game-over");
            fx(() -> { stage.setWidth(560);stage.setHeight(700);return null; });
            capture(stage,"09-small-game-over");
            assertTrue(fx(() -> {
                var button=stage.getScene().lookup("#primary-action");
                var bounds=button.localToScene(button.getBoundsInLocal());
                return bounds.getMaxY() < stage.getScene().getHeight() && bounds.getMinY() > 0;
            }));
            assertTrue(Files.exists(directory.resolve("ui-best.txt")));
            key(stage,KeyCode.LEFT);assertEquals(GameSession.State.OVER,fx(session::state));
            fx(() -> { ((Button)stage.getScene().lookup("#primary-action")).fire();return null; });
            assertEquals(GameSession.State.READY,fx(session::state));
            assertTrue(fx(() -> stage.getScene().lookup("#direction-prompt").isVisible()));
            assertEquals(3,fx(game::hearts));assertEquals(0,fx(game::getScorePoints));
            key(stage,KeyCode.RIGHT);
            assertEquals(GameSession.State.RUNNING,fx(session::state));
            key(stage,KeyCode.P);key(stage,KeyCode.TAB);
            assertEquals(GameSession.State.READY,fx(session::state));
            assertTrue(fx(() -> rocks.getObstacles().isEmpty()));
            fx(() -> { stage.hide();return null; });
            verifyHedgePresentation(directory);
            fx(() -> { verifyImmediateInputAndTurnQueue(directory); return null; });
        } finally {
            fx(() -> { for(var window:javafx.stage.Window.getWindows().toArray(javafx.stage.Window[]::new))window.hide();return null; });
            Platform.exit();
        }
    }
    private static void verifyHedgePresentation(Path directory) throws Exception {
        Game game=new Game(20,20,true,'N');game.addPoints(50);
        Obstacle field=new Obstacle(20,20);Snake snake=new Snake(20,20,false,field);Point silver=new Point(9,10);
        // A scripted already-warned layout isolates artwork/gate checks from random future pickups.
        field.requestLayout(40,snake,silver);for(int i=0;i<12;i++)field.advance(snake,silver,0);
        var session=new GameSession(game,field,new HighScoreStore(directory.resolve("hedges.txt")),snake,new Coin(silver,Coin.Sprite.SILVER));
        GameView[] view=new GameView[1];
        Stage stage=fx(() -> {
            Stage result=new Stage();view[0]=new GameView(session);Scene scene=new Scene(view[0].root(),640,724);
            scene.getStylesheets().add(Main.class.getResource("/resources/views/garden.css").toExternalForm());
            result.setScene(scene);result.show();session.resume();view[0].refresh();return result;
        });
        capture(stage,"11-hedges");
        fx(() -> {assertTrue(session.step('L'));view[0].beginStep(session.snake());view[0].refresh();return null;});
        assertEquals(2,field.gateCells().size());assertEquals(6,field.getObstacles().size());
        capture(stage,"12-open-gap");fx(() -> {stage.hide();return null;});
    }

    private static void verifyImmediateInputAndTurnQueue(Path directory) {
        Game game=new Game(20,20,true,'N');Obstacle obstacles=new Obstacle(20,20);
        Snake snake=new Snake(20,20,false,obstacles);
        var session=new GameSession(game,obstacles,new HighScoreStore(directory.resolve("timing.txt")),snake,
                new Coin(new Point(17,17),Coin.Sprite.RED));
        GameView view=new GameView(session);Stage stage=new Stage();
        var now=new java.util.concurrent.atomic.AtomicLong(1_000_000_000L);
        var controller=new GameController(session,view,stage,()->{},now::get);
        Scene scene=new Scene(view.root(),640,724);stage.setScene(scene);
        scene.addEventFilter(KeyEvent.KEY_PRESSED,controller::handle);
        java.util.function.Consumer<KeyCode> press=code->Event.fireEvent(scene,
                new KeyEvent(KeyEvent.KEY_PRESSED,"","",code,false,false,false,false));
        long interval=(long)(1_000_000_000/session.speed());
        press.accept(KeyCode.M);
        ((Button)scene.lookup("#primary-action")).fire();
        assertEquals(GameSession.State.READY,session.state(),"Play must wait for an arrow");
        Point waiting=snake.getHead();
        now.addAndGet(50*interval);controller.pulse(now.get());
        press.accept(KeyCode.ENTER);press.accept(KeyCode.SPACE);press.accept(KeyCode.P);
        assertEquals(GameSession.State.READY,session.state(),"Non-arrow keys cannot start a ready snake");
        assertEquals(waiting,snake.getHead(),"Even many elapsed intervals must not move the waiting snake");
        assertTrue(scene.lookup("#direction-prompt").isVisible());
        press.accept(KeyCode.LEFT);
        assertFalse(scene.lookup("#direction-prompt").isVisible(),"First arrow dismisses the hint synchronously");
        assertEquals(new Point(9,10),snake.getHead(),"Start must move before the key handler returns");
        controller.pulse(now.get()-1);
        assertEquals(10,view.renderedHead().getX(),.001,"An earlier pulse timestamp must not move backwards");
        controller.pulse(now.get()+interval/4);
        assertEquals(9.75,view.renderedHead().getX(),.001,"The first partial frame must already move");
        assertEquals(new Point(9,10),snake.getHead(),"A quarter pulse must not add a second grid step");
        press.accept(KeyCode.UP);press.accept(KeyCode.RIGHT);
        controller.pulse(now.get()+interval);
        assertEquals(new Point(9,9),snake.getHead(),"First buffered turn executes on the next grid boundary");
        controller.pulse(now.get()+2*interval);
        assertEquals(new Point(10,9),snake.getHead(),"Second quick turn executes on the following boundary");
        press.accept(KeyCode.P);
        Point paused=snake.getHead();
        now.addAndGet(10*interval);
        press.accept(KeyCode.DOWN);
        assertEquals(new Point(paused.x,paused.y+1),snake.getHead(),"Resume must not wait for another pulse");
        press.accept(KeyCode.P);
        press.accept(KeyCode.TAB);
        press.accept(KeyCode.RIGHT);
        assertEquals(new Point(11,10),session.snake().getHead(),"Right must also work from a fresh start");
        press.accept(KeyCode.P);
        stage.hide();
    }

}
