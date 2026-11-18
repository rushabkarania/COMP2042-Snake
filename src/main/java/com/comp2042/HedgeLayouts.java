package com.comp2042;

import java.awt.Point;
import java.util.*;

/** Designs bounded hedge patterns and checks both board connectivity and the current route to food. */
final class HedgeLayouts {
    static int revision(int score) { return score < 40 ? -1 : score < 80 ? 0 : 1 + (score - 80) / 80; }

    /** Tries rotated versions of increasingly structured patterns, never forcing an unsafe layout. */
    static List<Point> propose(int width, int height, int score, Snake snake, Point coin, Collection<Point> current) {
        if (width < 12 || height < 12 || score < 40) return List.of();
        int revision = revision(score);
        int pattern = Math.min(3, revision);
        for (int attempt = 0; attempt < 4; attempt++) {
            Set<Point> cells = new LinkedHashSet<>();
            for (Point original : template(pattern)) {
                int x=original.x, y=original.y;
                for (int turn=0; turn<(revision+attempt)%4; turn++) { int next=19-y; y=x; x=next; }
                cells.add(new Point(2+(int)Math.round((x-2)*(width-5)/15.0),
                        2+(int)Math.round((y-2)*(height-5)/15.0)));
            }
            var layout = new ArrayList<>(cells);
            if (!new HashSet<>(current).equals(cells) && safeTransition(layout,current,snake,coin,width,height)) return layout;
        }
        return List.of();
    }

    private static List<Point> template(int pattern) {
        var cells=new ArrayList<Point>();
        switch (pattern) {
            case 0 -> { line(cells,3,5,6,5); line(cells,13,14,16,14); }
            case 1 -> {
                line(cells,3,5,6,5);line(cells,3,6,3,8);
                line(cells,13,14,16,14);line(cells,16,11,16,13);
            }
            case 2 -> {
                line(cells,3,5,8,5);line(cells,11,14,16,14);
                line(cells,13,5,13,8);line(cells,6,11,6,14);
            }
            default -> {
                line(cells,5,3,5,7);line(cells,5,11,5,16);
                line(cells,14,3,14,8);line(cells,14,12,14,16);
            }
        }
        return cells;
    }
    private static void line(List<Point> cells,int x1,int y1,int x2,int y2) {
        int dx=Integer.compare(x2,x1),dy=Integer.compare(y2,y1);
        for(int x=x1,y=y1;;x+=dx,y+=dy) { cells.add(new Point(x,y));if(x==x2&&y==y2)break; }
    }

    /** New walls cannot occupy living objects or appear within three steps of the head. */
    static boolean safeTransition(Collection<Point> proposed,Collection<Point> current,Snake snake,Point coin,int width,int height) {
        Set<Point> walls=new HashSet<>(proposed),old=new HashSet<>(current);
        Set<Point> occupied=new HashSet<>(snake.getBody());occupied.add(snake.getHead());
        Point head=snake.getHead();
        for(Point cell:walls) {
            if(cell.x<0||cell.y<0||cell.x>=width||cell.y>=height||occupied.contains(cell)||cell.equals(coin))return false;
            if(!old.contains(cell)&&Math.abs(cell.x-head.x)+Math.abs(cell.y-head.y)<=3)return false;
        }
        // No permanent pockets: every non-hedge cell must belong to one connected garden.
        Set<Point> open=flood(head,walls,width,height);
        if(open.size()!=width*height-walls.size())return false;
        // Also preserve an immediate route to the current pickup around the current body.
        walls.addAll(snake.getBody());
        return coin==null||coin.equals(head)||flood(head,walls,width,height).contains(coin);
    }

    private static Set<Point> flood(Point start,Set<Point> blocked,int width,int height) {
        var visited=new HashSet<Point>();var queue=new ArrayDeque<Point>();
        if(blocked.contains(start))return visited;
        visited.add(new Point(start));queue.add(new Point(start));
        while(!queue.isEmpty()) {
            Point p=queue.remove();
            for(Point next:List.of(new Point(p.x-1,p.y),new Point(p.x+1,p.y),new Point(p.x,p.y-1),new Point(p.x,p.y+1))) {
                if(next.x>=0&&next.y>=0&&next.x<width&&next.y<height&&!blocked.contains(next)&&visited.add(next))queue.add(next);
            }
        }
        return visited;
    }
    private HedgeLayouts() { }
}
