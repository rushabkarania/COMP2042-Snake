package com.comp2042;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/** Draws the original head, patterned body and tapered tail for both gameplay and menu art. */
final class SnakeRenderer {
    static void paint(GraphicsContext gc,Snake snake,List<Point2D> path,int width,int height,double cell) {
        paint(gc,snake,path,width,height,cell,snake.getSnakeDir(),0);
    }
    private static void paint(GraphicsContext gc,Snake snake,List<Point2D> path,int width,int height,double cell,int rotation,double headOffsetY) {
        gc.save();gc.scale(cell,cell);gc.setImageSmoothing(false);gc.setFill(Color.GREEN);
        SnakeBodyPainter.paint(new SnakeBodyPainter.Surface() {
            public void joint(double x,double y,double radius) { gc.fillOval(x-radius,y-radius,2*radius,2*radius); }
            public void strip(double x,double y,double angle,double length,double offset,boolean tail) {
                Image image=(tail?snake.getImageViewTail():snake.getImageViewBody()).getImage();
                double h=image.getHeight()*(tail?.95:1);
                gc.save();gc.translate(x,y);gc.rotate(angle);
                gc.drawImage(image,0,Math.max(0,1-offset-length)*h,image.getWidth(),length*h,-.5,-length,1,length);
                gc.restore();
            }
        },path,width,height);
        gc.restore();
        Point2D tip=path.get(path.size()-1);
        Point2D head=new Point2D(tip.getX(),tip.getY()+headOffsetY);
        sprite(gc,snake.getImageViewHead().getImage(),rotation,head.getX(),head.getY(),cell);
        if(head.getX()<0)sprite(gc,snake.getImageViewHead().getImage(),rotation,head.getX()+width,head.getY(),cell);
        if(head.getX()>width-1)sprite(gc,snake.getImageViewHead().getImage(),rotation,head.getX()-width,head.getY(),cell);
        if(head.getY()<0)sprite(gc,snake.getImageViewHead().getImage(),rotation,head.getX(),head.getY()+height,cell);
        if(head.getY()>height-1)sprite(gc,snake.getImageViewHead().getImage(),rotation,head.getX(),head.getY()-height,cell);
    }
    private static void sprite(GraphicsContext gc,Image image,int rotation,double x,double y,double cell) {
        gc.save();gc.translate((x+.5)*cell,(y+.5)*cell);gc.rotate(rotation);
        gc.drawImage(image,-cell/2,-cell/2,cell,cell);gc.restore();
    }
    /** Large side-on snake entering from off-canvas, with its body resting above the grass. */
    static Canvas menuIllustration(Snake snake) {
        Canvas canvas=new Canvas(280,112);canvas.setId("menu-snake-sprites");
        canvas.setAccessibleText("Gameplay snake entering from the left, facing right above the grass");
        List<Point2D> path=new ArrayList<>();
        // The tapered tail stays outside the panel, so the visible body enters through the left edge.
        double bodyEdge=rightEdge(snake.getImageViewBody().getImage());
        double headEdge=rightEdge(snake.getImageViewHead().getImage());
        double y=112.0/56-bodyEdge-.5;
        for(int x=-3;x<=2;x++)path.add(new Point2D(x,y));
        path.add(new Point2D(2.75,y));
        paint(canvas.getGraphicsContext2D(),snake,path,10,4,56,90,bodyEdge-headEdge);
        return canvas;
    }
    /** Transparent padding differs between sprites; align the visible lower edges, not the bitmaps. */
    private static double rightEdge(Image image) {
        var pixels=image.getPixelReader();
        for(int x=(int)image.getWidth()-1;x>=0;x--)for(int y=0;y<(int)image.getHeight();y++)
            if((pixels.getArgb(x,y)>>>24)>0)return (x+1)/image.getWidth()-.5;
        return .5;
    }
    private SnakeRenderer() { }
}
