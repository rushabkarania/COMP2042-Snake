package com.comp2042;

import java.awt.Point;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static com.comp2042.PowerUpState.Effect.*;
import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {
    @TempDir Path directory;
    private GameSession fixture(Coin.Sprite type, int points, int growth) {
        Game game = new Game(20,20,true,'N'); game.addPoints(points);
        Obstacle obstacles = new Obstacle(20,20);
        Snake snake = new Snake(20,20,false,obstacles);
        for (int i = 0; i < growth; i++) {
            Point next = snake.getHead(); next.translate(-1,0); assertTrue(snake.move('L',next));
        }
        Point next = snake.getHead(); next.translate(-1,0);
        return new GameSession(game,obstacles,new HighScoreStore(directory.resolve("best.txt")),snake,new Coin(next,type));
    }
    @Test void readyAndPausedRoundsCannotMoveOrConsumeEffects() {
        var session = fixture(Coin.Sprite.RED,0,0);
        Point before = session.snake().getHead();
        assertFalse(session.step('L')); assertEquals(before,session.snake().getHead());
        session.resume(); session.effects().activate(WRAP,10); session.pause();
        assertFalse(session.step('L')); assertEquals(10,session.effects().steps(WRAP));
        assertEquals(before,session.snake().getHead());
    }
    @Test void redGrowsButShrinkRemovesThreeWithoutGrowingFirst() {
        var regular = fixture(Coin.Sprite.RED,0,0); regular.resume();
        assertTrue(regular.step('L')); assertEquals(2,regular.snake().getBody().size());
        var shrink = fixture(Coin.Sprite.SHRINK,0,5); shrink.resume();
        assertTrue(shrink.step('L')); assertEquals(3,shrink.snake().getBody().size());
        assertEquals(5,shrink.game().getScorePoints());
    }
    @Test void shrinkHasAMinimumLengthAndDoesNotDeductEarnedPoints() {
        var session = fixture(Coin.Sprite.SHRINK,20,0); session.resume(); session.step('L');
        assertEquals(1,session.snake().getBody().size()); assertEquals(25,session.game().getScorePoints());
        assertThrows(IllegalArgumentException.class, () -> session.snake().shrink(-1));
    }
    @Test void pickupsActivateTheirDocumentedEffectAndGoldCrossesThreshold() {
        for (var type : new Coin.Sprite[]{Coin.Sprite.BLUE,Coin.Sprite.RAINBOW,Coin.Sprite.SILVER}) {
            var session = fixture(type,0,0); session.resume(); assertTrue(session.step('L'));
            var effect = switch (type) { case BLUE -> WRAP; case RAINBOW -> RUSH; default -> GAP; };
            assertEquals(type == Coin.Sprite.RAINBOW ? 40 : 48, session.effects().steps(effect));
        }
        var gold = fixture(Coin.Sprite.GOLD,35,0); gold.resume(); gold.step('L');
        assertEquals(50,gold.game().getScorePoints()); assertEquals(2,gold.game().level());
        assertTrue(gold.obstacles().getObstacles().isEmpty());
        assertEquals(8,gold.obstacles().pendingCells().size());assertEquals(12,gold.obstacles().warningMoves());
    }
    @Test void wrapWorksOnTheLastActiveStepThenRestoresTheWalls() {
        var game = new Game(20,20,true,'N'); var obstacles = new Obstacle(20,20);
        var snake = new Snake(20,20,false,obstacles);
        for(int i=0;i<10;i++) snake.move('L',null);
        var session = new GameSession(game,obstacles,new HighScoreStore(directory.resolve("wrap.txt")),snake,
                new Coin(new Point(5,5),Coin.Sprite.RED));
        session.effects().activate(WRAP,1); session.resume(); session.step('L');
        assertEquals(new Point(19,10),snake.getHead()); assertFalse(session.effects().active(WRAP));
        for(int i=0;i<20;i++) session.step('L');
        assertEquals(GameSession.State.RECOVERY,session.state()); assertEquals(2,game.hearts());
    }
    @Test void thirdCollisionEndsRunAndSavesRecordOnlyOnce() {
        // Disable random follow-up powers: a random blue pickup must not mask the intended wall collision.
        Game game=new Game(20,20,false,'N');Obstacle obstacles=new Obstacle(20,20);
        var session=new GameSession(game,obstacles,new HighScoreStore(directory.resolve("best.txt")),
                new Snake(20,20,false,obstacles),new Coin(new Point(9,10),Coin.Sprite.GOLD));
        session.resume(); assertTrue(session.step('L'));
        for(int life=0;life<3;life++) {
            session.effects().activate(RUSH,30);
            for(int step=0;step<21 && session.state()==GameSession.State.RUNNING;step++) session.step('L');
            assertEquals(2-life,session.game().hearts());
            assertTrue(session.effects().snapshot().isEmpty());
            if(life<2) { assertEquals(GameSession.State.RECOVERY,session.state()); session.resume(); }
        }
        assertEquals(GameSession.State.OVER,session.state());
        int points=session.game().getScorePoints();
        assertTrue(points>=15); assertTrue(session.newRecord());
        assertEquals(points,new HighScoreStore(directory.resolve("best.txt")).best());
        assertFalse(session.step('L')); assertEquals(0,session.game().hearts());
        session.reset(); assertEquals(GameSession.State.READY,session.state());
        assertEquals(3,session.game().hearts()); assertEquals(0,session.game().getScorePoints());
        assertEquals(points,session.best()); assertFalse(session.newRecord());
        assertTrue(session.obstacles().getObstacles().isEmpty());
    }
    @Test void silverOpensAHedgeGateAndItsTimerStopsWhilePaused() {
        var session=fixture(Coin.Sprite.SILVER,80,0);
        session.obstacles().installLayout(java.util.List.of(new Point(3,5),new Point(4,5),new Point(5,5),new Point(6,5)));
        session.resume();session.step('L');
        assertEquals(48,session.effects().steps(GAP));assertEquals(2,session.obstacles().gateCells().size());
        session.pause();int warning=session.obstacles().warningMoves();session.step('L');
        assertEquals(48,session.effects().steps(GAP));assertEquals(warning,session.obstacles().warningMoves());
    }
    @Test void resetSavesAnAbandonedRunAndClearsEffects() {
        var session=fixture(Coin.Sprite.BLUE,10,0);session.resume();session.step('L');
        session.reset();
        assertEquals(15,new HighScoreStore(directory.resolve("best.txt")).best());
        assertTrue(session.effects().snapshot().isEmpty());assertEquals(1,session.game().level());
    }
    @Test void copyingBodyCannotMutateCollisionPositions() {
        var session=fixture(Coin.Sprite.RED,0,0);var body=session.snake().getBody();
        body.get(0).setLocation(0,0);assertEquals(new Point(11,10),session.snake().getBody().get(0));
    }
    @Test void difficultyKeepsRisingWithoutSelectingAModeAndStaysCapped() {
        var session=fixture(Coin.Sprite.RED,0,0);
        assertEquals(8.5,session.speed());
        session.game().addPoints(40);assertEquals(9.5,session.speed());
        session.game().addPoints(40);assertEquals(10.5,session.speed());
        session.game().addPoints(80);assertEquals(11,session.speed());
        session.game().addPoints(100000);assertEquals(14,session.speed());
        session.effects().activate(RUSH,40);assertEquals(17,session.speed());
    }

    @Test void recoveryPrefersTheCentreAndWaitsWithoutSpendingAnotherLife() {
        Game game=new Game(20,20,true,'N');game.addPoints(25);
        Obstacle obstacles=new Obstacle(20,20);
        var session=new GameSession(game,obstacles,new HighScoreStore(directory.resolve("centre.txt")),
                new Snake(20,20,false,obstacles),new Coin(new Point(17,17),Coin.Sprite.RED));
        session.resume();
        for(int i=0;i<12 && session.state()==GameSession.State.RUNNING;i++)session.step('L');
        assertEquals(GameSession.State.RECOVERY,session.state());
        assertEquals(new Point(10,10),session.snake().getHead());
        assertEquals(2,session.game().hearts());
        int score=session.game().getScorePoints();
        for(int i=0;i<50;i++) assertFalse(session.step('L'));
        assertEquals(new Point(10,10),session.snake().getHead());
        assertEquals(2,session.game().hearts());assertEquals(score,session.game().getScorePoints());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(chars={'U','R','D','L'})
    void anyArrowCanResumeAFreshlyPositionedSnakeSafely(char direction) {
        var session=fixture(Coin.Sprite.RED,0,0);session.resume();
        for(int i=0;i<12 && session.state()==GameSession.State.RUNNING;i++)session.step('L');
        assertEquals(GameSession.State.RECOVERY,session.state());
        session.resume(direction);
        Point head=session.snake().getHead();
        session.step(direction);
        assertEquals(GameSession.State.RUNNING,session.state());
        assertFalse(session.snake().getDead());assertEquals(2,session.game().hearts());
        Point expected=new Point(head);
        switch(direction){case 'L'->expected.translate(-1,0);case 'R'->expected.translate(1,0);
            case 'U'->expected.translate(0,-1);case 'D'->expected.translate(0,1);}
        assertEquals(expected,session.snake().getHead());
    }

}
