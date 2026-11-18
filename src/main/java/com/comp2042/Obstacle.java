package com.comp2042;

import java.awt.Point;
import java.util.*;
import javafx.scene.paint.Color;

/** Warned, stationary hedge layouts with safe replacement and temporary two-cell gates. */
public class Obstacle {
    static final int LAYOUT_WARNING_MOVES=12;
    static final int GATE_WARNING_MOVES=8;
    private final int width,height;
    private final ArrayList<Point> obstacles=new ArrayList<>();
    private List<Point> fullLayout=List.of(),pending=List.of(),gate=List.of();
    private int warningMoves,gateWarning=-1,requestedRevision=-1,completedRevision=-1,retryMoves;

    public Obstacle(int width,int height) { this.width=width;this.height=height; }
    public Color getColor() { return Color.web("#28713d"); }
    /** Live collision cells, excluding the temporarily opened gate. */
    public ArrayList<Point> getObstacles() { return obstacles; }
    public List<Point> pendingCells() { return pending.stream().map(Point::new).toList(); }
    public List<Point> gateCells() { return gate.stream().map(Point::new).toList(); }
    public int warningMoves() { return warningMoves; }
    public int gateWarningMoves() { return gateWarning; }
    public boolean hasPendingLayout() { return !pending.isEmpty(); }

    /** Requests a new pattern at 40/80 points and each additional 80 points. */
    void requestLayout(int score,Snake snake,Point coin) {
        int revision=HedgeLayouts.revision(score);
        if(revision<0)return;
        if(revision==requestedRevision&&(!pending.isEmpty()||retryMoves>0||completedRevision==revision))return;
        if(revision!=requestedRevision) { pending=List.of();retryMoves=0; }
        requestedRevision=revision;
        List<Point> proposal=HedgeLayouts.propose(width,height,score,snake,coin,obstacles);
        if(proposal.isEmpty()) { retryMoves=12;return; }
        requestedRevision=revision;pending=proposal;warningMoves=LAYOUT_WARNING_MOVES;retryMoves=0;
    }

    /** Called only after a successful movement; paused/recovery states cannot consume warnings. */
    void advance(Snake snake,Point coin,int gapMoves) {
        if(retryMoves>0)retryMoves--;
        if(!pending.isEmpty()) {
            if(warningMoves>0)warningMoves--;
            if(warningMoves==0&&HedgeLayouts.safeTransition(pending,obstacles,snake,coin,width,height)) {
                installLayout(pending);pending=List.of();completedRevision=requestedRevision;
            }
        }
        if(gapMoves>0) {
            gateWarning=-1;
            if(gate.isEmpty())openGate(snake);
        } else if(!gate.isEmpty()) {
            if(gateWarning<0)gateWarning=GATE_WARNING_MOVES;
            else if(gateWarning>0)gateWarning--;
            if(gateWarning==0&&HedgeLayouts.safeTransition(fullLayout,obstacles,snake,coin,width,height)) {
                obstacles.clear();fullLayout.forEach(p->obstacles.add(new Point(p)));gate=List.of();gateWarning=-1;
            }
        }
    }

    /** Installs a scripted or validated pattern and discards a gate from the previous layout. */
    void installLayout(Collection<Point> cells) {
        fullLayout=cells.stream().map(Point::new).distinct().toList();
        obstacles.clear();fullLayout.forEach(p->obstacles.add(new Point(p)));
        gate=List.of();gateWarning=-1;
    }

    private void openGate(Snake snake) {
        var candidates=new ArrayList<List<Point>>();
        Set<Point> cells=new HashSet<>(obstacles);
        for(Point first:obstacles)for(Point second:List.of(new Point(first.x+1,first.y),new Point(first.x,first.y+1))) {
            if(cells.contains(second))candidates.add(List.of(new Point(first),second));
        }
        Point head=snake.getHead();
        candidates.sort(Comparator.comparingDouble(pair->pair.stream().mapToDouble(p->p.distanceSq(head)).sum()));
        // Prefer a genuine opening through a run, leaving hedge cells on both sides when possible.
        List<Point> chosen=null;
        for(List<Point> pair:candidates) {
            Point a=pair.get(0),b=pair.get(1);int dx=b.x-a.x,dy=b.y-a.y;
            if(cells.contains(new Point(a.x-dx,a.y-dy))&&cells.contains(new Point(b.x+dx,b.y+dy))) {chosen=pair;break;}
        }
        if(chosen==null&&!candidates.isEmpty())chosen=candidates.get(0);
        if(chosen!=null) { gate=chosen;obstacles.removeAll(gate); }
    }

    public String warningText() {
        var messages=new ArrayList<String>();
        if(!pending.isEmpty())messages.add(warningMoves>0?"Hedges change in "+warningMoves+" steps":"Hedges waiting for clear space");
        if(!gate.isEmpty()&&gateWarning>=0)messages.add(gateWarning>0?"Gap closes in "+gateWarning+" steps":"Gap waiting for clear space");
        return String.join(" · ",messages);
    }
    public void reset() {
        obstacles.clear();fullLayout=List.of();pending=List.of();gate=List.of();
        warningMoves=0;gateWarning=-1;requestedRevision=-1;completedRevision=-1;retryMoves=0;
    }
}
