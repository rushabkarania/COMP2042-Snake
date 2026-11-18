package com.comp2042;

import java.awt.Point;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ObstacleSpawningTest {
    private final Point coin=new Point(17,17);
    private static void advance(Obstacle field,Snake snake,Point coin,int moves,int gap) {
        for(int i=0;i<moves;i++)field.advance(snake,coin,gap);
    }
    private static List<Point> row(int first,int last,int y) {
        var cells=new ArrayList<Point>();for(int x=first;x<=last;x++)cells.add(new Point(x,y));return cells;
    }
    private static void walkTo(Snake snake,Point target) {
        while(snake.getHead().x!=target.x)snake.move(snake.getHead().x<target.x?'R':'L',null);
        while(snake.getHead().y!=target.y)snake.move(snake.getHead().y<target.y?'D':'U',null);
        assertFalse(snake.getDead());
    }
    @Test void firstLayoutIsVisibleButNonCollidingForTheFullWarning() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);
        field.requestLayout(40,snake,coin);
        assertEquals(8,field.pendingCells().size());assertEquals(12,field.warningMoves());assertTrue(field.getObstacles().isEmpty());
        advance(field,snake,coin,11,0);assertEquals(1,field.warningMoves());assertTrue(field.getObstacles().isEmpty());
        field.advance(snake,coin,0);assertEquals(8,field.getObstacles().size());assertFalse(field.hasPendingLayout());
    }
    @Test void laterStagesKeepOldWallsUntilTheirReplacementIsWarned() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);
        field.requestLayout(40,snake,coin);advance(field,snake,coin,12,0);var old=new HashSet<>(field.getObstacles());
        field.requestLayout(80,snake,coin);assertTrue(field.hasPendingLayout());assertEquals(old,new HashSet<>(field.getObstacles()));
        advance(field,snake,coin,12,0);assertNotEquals(old,new HashSet<>(field.getObstacles()));assertEquals(14,field.getObstacles().size());
    }
    @Test void aSnakeInThePreviewDefersActivationUntilItMovesClear() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);
        field.requestLayout(40,snake,coin);Point target=field.pendingCells().get(0);
        walkTo(snake,target);advance(field,snake,coin,12,0);
        assertTrue(field.getObstacles().isEmpty());assertTrue(field.hasPendingLayout());assertEquals(0,field.warningMoves());
        walkTo(snake,new Point(10,10));field.advance(snake,coin,0);
        assertFalse(field.getObstacles().isEmpty());assertFalse(field.getObstacles().contains(snake.getHead()));
    }
    @Test void aReplacementCoinInThePreviewCannotBeCovered() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);
        field.requestLayout(40,snake,coin);Point blockedCoin=field.pendingCells().get(0);
        advance(field,snake,blockedCoin,12,0);assertTrue(field.getObstacles().isEmpty());
        field.advance(snake,coin,0);assertEquals(8,field.getObstacles().size());
    }
    @Test void aNewWallWithinThreeHeadStepsIsDeferred() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);
        field.requestLayout(40,snake,coin);Point p=field.pendingCells().get(0);
        walkTo(snake,new Point(p.x,p.y+2));advance(field,snake,coin,12,0);
        assertTrue(field.getObstacles().isEmpty());assertTrue(field.hasPendingLayout());
    }
    @Test void layoutsNeverPartitionTheGardenOrPlaceOnTheSnake() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);
        for(int score:new int[]{40,80,160,240,320,400,800,1600,10000}) {
            var proposed=HedgeLayouts.propose(20,20,score,snake,coin,List.of());
            assertFalse(proposed.isEmpty());assertTrue(HedgeLayouts.safeTransition(proposed,List.of(),snake,coin,20,20));
            assertTrue(proposed.size()<=22);
            for(Point p:proposed)assertTrue(p.x>=2&&p.x<=17&&p.y>=2&&p.y<=17);
        }
    }
    @Test void connectivityAndBodyRouteAreRevalidated() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);
        var partition=new ArrayList<Point>();for(int y=0;y<20;y++)partition.add(new Point(5,y));
        assertFalse(HedgeLayouts.safeTransition(partition,partition,snake,coin,20,20));
        var pocket=List.of(new Point(15,16),new Point(16,15),new Point(17,16),new Point(16,17));
        assertFalse(HedgeLayouts.safeTransition(pocket,List.of(),snake,coin,20,20));
        assertFalse(HedgeLayouts.safeTransition(List.of(snake.getHead()),List.of(),snake,coin,20,20));
        assertTrue(snake.move('L',new Point(9,10)));assertTrue(snake.move('U',new Point(9,9)));
        assertTrue(snake.move('R',new Point(10,9)));
        var enclosing=List.of(new Point(10,8),new Point(11,9));
        // These walls alone leave a connected garden, but together with the live body trap the head.
        assertFalse(HedgeLayouts.safeTransition(enclosing,enclosing,snake,coin,20,20));
    }
    @Test void tinyBoardsSkipLayoutsAndStayPlayable() {
        var field=new Obstacle(4,1);var snake=new Snake(4,1,false,field);
        field.requestLayout(1000,snake,new Point(1,0));advance(field,snake,new Point(1,0),30,0);
        assertFalse(field.hasPendingLayout());assertTrue(field.getObstacles().isEmpty());
    }
    @Test void repeatedRequestsDoNotResetTheSameWarning() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);
        field.requestLayout(40,snake,coin);field.advance(snake,coin,0);field.requestLayout(50,snake,coin);
        assertEquals(11,field.warningMoves());
    }
    @Test void silverOpensTwoAdjacentCellsAndPreservesTheRest() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);field.installLayout(row(3,8,5));
        field.advance(snake,coin,48);assertEquals(2,field.gateCells().size());assertEquals(4,field.getObstacles().size());
        Point a=field.gateCells().get(0),b=field.gateCells().get(1);assertEquals(1,a.distance(b));
        for(Point p:field.gateCells())assertFalse(field.getObstacles().contains(p));
    }
    @Test void expiredGapsWarnForEightMovesBeforeClosing() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);field.installLayout(row(3,8,5));
        field.advance(snake,coin,48);field.advance(snake,coin,0);assertEquals(8,field.gateWarningMoves());
        advance(field,snake,coin,7,0);assertEquals(1,field.gateWarningMoves());assertEquals(4,field.getObstacles().size());
        field.advance(snake,coin,0);assertTrue(field.gateCells().isEmpty());assertEquals(6,field.getObstacles().size());
    }
    @Test void aGateCannotCloseOnTheSnakeOrTheCoin() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);field.installLayout(row(3,8,5));field.advance(snake,coin,48);
        Point gate=field.gateCells().get(0);advance(field,snake,gate,9,0);assertFalse(field.gateCells().isEmpty());
        walkTo(snake,gate);field.advance(snake,coin,0);assertFalse(field.getObstacles().contains(gate));assertFalse(snake.getDead());
        // Leave vertically through the opening before moving back to the centre.
        snake.move('D',null);walkTo(snake,new Point(10,10));field.advance(snake,coin,0);assertTrue(field.gateCells().isEmpty());
    }
    @Test void silverRefreshCancelsAClosingWarningWithoutMovingTheGap() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);field.installLayout(row(3,8,5));field.advance(snake,coin,48);
        var gate=field.gateCells();field.advance(snake,coin,0);field.advance(snake,coin,48);
        assertEquals(-1,field.gateWarningMoves());assertEquals(gate,field.gateCells());
    }
    @Test void bankedSilverOpensAGapWhenTheFirstLayoutArrives() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);field.requestLayout(40,snake,coin);
        advance(field,snake,coin,12,36);assertFalse(field.hasPendingLayout());assertEquals(2,field.gateCells().size());assertEquals(6,field.getObstacles().size());
    }
    @Test void resetClearsBothKindsOfWarningAndAllCollisionCells() {
        var field=new Obstacle(20,20);var snake=new Snake(20,20,false,field);field.installLayout(row(3,8,5));field.advance(snake,coin,48);field.requestLayout(80,snake,coin);
        field.reset();assertTrue(field.getObstacles().isEmpty());assertTrue(field.pendingCells().isEmpty());assertTrue(field.gateCells().isEmpty());assertEquals("",field.warningText());
    }
}
