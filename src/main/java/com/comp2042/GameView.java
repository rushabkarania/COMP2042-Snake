package com.comp2042;

import java.awt.Point;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

/** Board-first arcade view: one HUD row and a compact start/pause/game-over panel. */
public class GameView {
    private final GameSession session;
    private final Game game;
    private final BorderPane root = new BorderPane();
    private final Canvas gridCanvas = new Canvas(560,560);
    private final GraphicsContext gc = gridCanvas.getGraphicsContext2D();
    private final StackPane arena = new StackPane();
    private final StackPane board = new StackPane();
    private final StackPane scrim = new StackPane();
    private final VBox card = new VBox(10);
    private final VBox directionPrompt = new VBox(9);
    private final Label scoreLabel = label("0","hud-score");
    private final Label bestLabel = label("0","hud-score");
    private final Label levelLabel = label("Level 1","level-label");
    private final Label effectLabel = label("","effect-label");
    private final HBox hearts = new HBox(3);
    private final Button pause = iconButton("pause","Pause (P)");
    private final Button mute = iconButton("sound","Mute (M)");
    private final Button full = iconButton("fullscreen","Fullscreen (F)");
    private final Button close = iconButton("close","Close game");
    private Runnable resumeAction=()->{};
    private Runnable restartAction=()->{};
    private GameSession.State shownState;
    private boolean help;
    private boolean waitingForArrow;
    private List<Point> previousBody=List.of();
    private Point previousHead=new Point();
    private double progress=1;
    private javafx.geometry.Point2D renderedHead;
    /**
     * Returns the head position from the latest rendered path for visual checks.
     * @return last rendered head in cell coordinates
     */
    javafx.geometry.Point2D renderedHead() { return renderedHead; }

    /**
     * Builds the resizable board, HUD and start/help overlays for a session.
     *
     * @param session session whose state is displayed
     */
    public GameView(GameSession session) {
        this.session=session; game=session.game();
        root.getStyleClass().add("game-root"); root.setId("game-root"); root.setFocusTraversable(true);
        root.setTop(makeHeader());
        board.setId("game-board"); board.setMinSize(0,0);
        board.getStyleClass().add("board-frame");
        scrim.setId("state-overlay"); scrim.getStyleClass().add("board-scrim");
        scrim.setManaged(false); scrim.setMinSize(0,0);
        card.setAlignment(Pos.CENTER); card.setMinWidth(280); card.setPrefWidth(280); card.setMaxWidth(280);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.layoutBoundsProperty().addListener((value,before,after)->fitCard());
        scrim.getChildren().add(card);
        makeDirectionPrompt();
        board.getChildren().addAll(gridCanvas,directionPrompt,scrim);
        StackPane.setAlignment(directionPrompt,Pos.TOP_CENTER);
        StackPane.setMargin(directionPrompt,new Insets(36,0,0,0));
        arena.getChildren().add(board); arena.setPadding(new Insets(20,20,8,20)); arena.setMinSize(0,0);
        root.setCenter(arena);
        Region space=new Region(); HBox.setHgrow(space,Priority.ALWAYS);
        HBox footer=new HBox(12,levelLabel,space,effectLabel);
        effectLabel.setMaxWidth(Double.MAX_VALUE);effectLabel.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(effectLabel,Priority.ALWAYS);
        footer.getStyleClass().add("status-bar"); footer.setAlignment(Pos.CENTER_LEFT);
        root.setBottom(footer);
        arena.widthProperty().addListener((value,before,after)->resizeBoard());
        arena.heightProperty().addListener((value,before,after)->resizeBoard());
        beginStep(session.snake()); refresh();
    }

    /**
     * Exposes the top-level view for attachment to a scene.
     *
     * @return root layout node
     */
    public BorderPane root() { return root; }
    /**
     * Hides the start card and shows the arrow prompt without starting movement.
     */
    void waitForArrow() { waitingForArrow=true;help=false;updateCard(); }
    /**
     * Restores the start card and closes help.
     */
    void showStartScreen() { waitingForArrow=false;help=false;updateCard(); }

    private void makeDirectionPrompt() {
        directionPrompt.setId("direction-prompt");directionPrompt.getStyleClass().add("direction-prompt");
        directionPrompt.setAlignment(Pos.CENTER);directionPrompt.setMaxSize(Region.USE_PREF_SIZE,Region.USE_PREF_SIZE);
        directionPrompt.setMouseTransparent(true);
        GridPane keys=new GridPane();keys.setHgap(4);keys.setVgap(4);keys.setAlignment(Pos.CENTER);
        String[] arrows={"↑","←","↓","→"};int[][] positions={{1,0},{0,1},{1,1},{2,1}};
        for(int i=0;i<arrows.length;i++) {
            Label key=label(arrows[i],"direction-key");key.setAlignment(Pos.CENTER);
            keys.add(key,positions[i][0],positions[i][1]);
        }
        Label message=label("Press an arrow key","direction-message");message.setId("direction-message");
        directionPrompt.getChildren().addAll(keys,message);
    }
    /**
     * Snapshots positions before a logical move for rendering interpolation.
     *
     * @param snake snake whose current positions are copied
     */
    void beginStep(Snake snake) { previousHead=snake.getHead(); previousBody=List.copyOf(snake.getBody()); }
    /**
     * Sets the movement interpolation fraction.
     *
     * @param fraction progress from 0 at the previous positions to 1 at the current positions
     */
    void setProgress(double fraction) { progress=fraction; }
    private double cell() { return gridCanvas.getWidth()/game.getWidth(); }

    /**
     * Binds view controls to controller callbacks.
     *
     * @param resume play or resume callback
     * @param togglePause pause/resume toggle callback
     * @param restart new-run callback
     * @param menu close-button callback supplied by the controller
     * @param toggleMute sound toggle callback
     * @param fullscreen fullscreen toggle callback
     */
    void bindActions(Runnable resume,Runnable togglePause,Runnable restart,Runnable menu,Runnable toggleMute,Runnable fullscreen) {
        resumeAction=resume; restartAction=restart;
        pause.setOnAction(event->togglePause.run()); mute.setOnAction(event->toggleMute.run());
        full.setOnAction(event->fullscreen.run()); close.setOnAction(event->menu.run());
    }
    /**
     * Updates the sound icon, tooltip and accessibility text.
     *
     * @param muted whether sound is currently muted
     */
    void setMuted(boolean muted) {
        mute.setGraphic(UiIcons.icon(muted?"muted":"sound"));
        mute.setAccessibleText(muted?"Sound off. Unmute (M)":"Sound on. Mute (M)");
        mute.setTooltip(new Tooltip(mute.getAccessibleText()));
    }

    private HBox makeHeader() {
        scoreLabel.setId("score-value");bestLabel.setId("best-value");hearts.setId("lives-icons");mute.setId("sound-toggle");
        ImageView points=GardenAssets.icon("points",27); points.setId("score-icon");
        points.setAccessibleText("Total points from all pickups");
        HBox score=new HBox(7,points,scoreLabel);
        HBox best=new HBox(7,GardenAssets.icon("trophy",27),bestLabel);
        score.setAlignment(Pos.CENTER_LEFT);best.setAlignment(Pos.CENTER_LEFT);hearts.setAlignment(Pos.CENTER_LEFT);
        Tooltip.install(score,new Tooltip("Score"));Tooltip.install(best,new Tooltip("Personal best"));
        Region space=new Region();HBox.setHgrow(space,Priority.ALWAYS);
        HBox actions=new HBox(2,pause,full,mute,close);actions.setAlignment(Pos.CENTER_RIGHT);
        HBox header=new HBox(19,score,best,hearts,space,actions);header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("hud");return header;
    }

    private static ImageView coinIcon(CoinSequence.Type type,int size) {
        ImageView icon=new ImageView(GardenAssets.coinThumbnail(type));
        icon.setFitWidth(size);icon.setFitHeight(size);icon.setPreserveRatio(true);return icon;
    }
    private static Button iconButton(String name,String help) {
        Button button=button("","icon-button");button.setGraphic(UiIcons.icon(name));
        button.setAccessibleText(help);button.setTooltip(new Tooltip(help));return button;
    }
    private void resizeBoard() {
        double side=Math.floor(Math.min(arena.getWidth()-40,arena.getHeight()-28));
        if(side<64)return;
        board.setMaxSize(side,side);board.setPrefSize(side,side);
        gridCanvas.setWidth(side);gridCanvas.setHeight(side);
        scrim.resizeRelocate(0,0,side,side);fitCard();render();
    }
    private void fitCard() {
        double h=card.getLayoutBounds().getHeight(),w=card.getLayoutBounds().getWidth();
        if(h<=0||w<=0)return;
        double scale=Math.min(1,Math.min((gridCanvas.getHeight()-20)/h,(gridCanvas.getWidth()-20)/w));
        card.setScaleX(scale);card.setScaleY(scale);
    }

    /**
     * Updates the HUD and repaints the board.
     */
    void refresh() { updateHud(); render(); }

    /**
     * Refreshes scores, lives, effect timers, warnings and state-dependent controls.
     */
    void updateHud() {
        scoreLabel.setText(Integer.toString(game.getScorePoints()));bestLabel.setText(Integer.toString(session.best()));
        if(!Integer.valueOf(game.hearts()).equals(hearts.getUserData())) {
            hearts.setUserData(game.hearts());hearts.getChildren().clear();
            for(int i=0;i<3;i++)hearts.getChildren().add(GardenAssets.icon(i<game.hearts()?"heart":"heart-empty",19));
        }
        hearts.setAccessibleText(game.hearts()+" lives remaining");
        levelLabel.setText("Level "+game.level());
        String next=game.level()==1?"Hedges at 40 points":game.level()==2?"New hedge layouts at 80 points":"Layouts change every 80 points";
        Tooltip.install(levelLabel,new Tooltip(next));
        var effects=new java.util.ArrayList<String>();
        for(var effect:PowerUpState.Effect.values())if(session.effects().active(effect))
            effects.add(switch(effect) {case WRAP->"Edge crossing";case RUSH->"Speed boost";case GAP->"Open passage";}
                    +" "+session.effects().steps(effect)+" steps");
        String warning=session.obstacles().warningText();
        if(!warning.isEmpty())effects.add(0,warning);
        effectLabel.setText(String.join("   ·   ",effects));
        effectLabel.setAccessibleText("Active effects, remaining grid steps: "+effectLabel.getText());
        effectLabel.setTooltip(new Tooltip("One step = one square moved. Countdown stops while waiting or paused.\n"+effectLabel.getText()));
        pause.setGraphic(UiIcons.icon(session.state()==GameSession.State.PAUSED?"play":"pause"));
        pause.setDisable(session.state()!=GameSession.State.RUNNING&&session.state()!=GameSession.State.PAUSED);
        if(shownState!=session.state()) { shownState=session.state();help=false;waitingForArrow=false;updateCard(); }
    }

    private void updateCard() {
        boolean ready=session.state()==GameSession.State.READY&&waitingForArrow;
        boolean recovering=session.state()==GameSession.State.RECOVERY;
        directionPrompt.setVisible(ready||recovering);
        directionPrompt.setAccessibleText(recovering?"Press an arrow key to continue":"Press an arrow key to start");
        ((Label)directionPrompt.lookup("#direction-message")).setText(recovering?"Arrow key to continue":"Press an arrow key");
        boolean visible=session.state()!=GameSession.State.RUNNING && !recovering && !ready;
        scrim.setVisible(visible);
        if(!visible)return;
        card.getChildren().clear();var state=session.state();
        VBox panel=new VBox(14);panel.setAlignment(Pos.TOP_CENTER);panel.getStyleClass().add("score-panel");
        javafx.scene.shape.Rectangle clip=new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(panel.widthProperty());clip.heightProperty().bind(panel.heightProperty());
        clip.setArcWidth(14);clip.setArcHeight(14);panel.setClip(clip);
        String title=help?"How to play":switch(state) {case READY->"Snake";case PAUSED->"Paused";case RECOVERY->"Life lost";default->"Game over";};
        Label heading=label(title,"panel-title");heading.setId("state-title");panel.getChildren().add(heading);
        if(help) {
            String[] descriptions={"+5 points · grow","+15 points · grow","Cross board edges · 48 steps","Move faster · 40 steps","Hedge passage · 48 steps","Shorten tail by 3 squares"};
            String[] details={"Earn 5 points and grow by one square.","Earn 15 points and grow by one square.",
                "Earn 5 points and grow. Cross a board edge to appear on the opposite side for 48 steps.",
                "Earn 5 points and grow. Temporarily move faster for 40 steps.",
                "Earn 5 points and grow. Open two neighbouring hedge squares for 48 steps.",
                "Earn 5 points and remove up to three tail squares, keeping the minimum snake length."};
            VBox guide=new VBox(9);guide.setPadding(new Insets(0,20,14,20));
            var types=CoinSequence.Type.values();
            for(int i=0;i<types.length;i++) {
                HBox row=new HBox(12,coinIcon(types[i],23),label(descriptions[i],"help-text"));row.setAlignment(Pos.CENTER_LEFT);
                row.setAccessibleText(details[i]);Tooltip.install(row,new Tooltip(details[i]));guide.getChildren().add(row);
            }
            Label controls=label("1 step = move one square\nTimers stop while paused or waiting\n\nArrow keys to start and steer\nP pause · R restart · M mute","help-controls");controls.setAlignment(Pos.CENTER);
            panel.getChildren().addAll(guide,controls);
        } else {
            HBox results=new HBox(58,stat(false,game.getScorePoints()),stat(true,session.best()));results.setAlignment(Pos.CENTER);
            panel.getChildren().add(results);
            if(state==GameSession.State.OVER&&session.newRecord())panel.getChildren().add(label("New best!","panel-subtitle"));
            if(state==GameSession.State.OVER&&!session.savingAvailable())panel.getChildren().add(label("Best saved for this session only","panel-subtitle"));
            Canvas illustration=SnakeRenderer.menuIllustration(session.snake());
            Region grass=new Region();grass.getStyleClass().add("panel-grass");grass.setId("menu-grass");grass.setMinHeight(70);grass.setPrefHeight(70);
            VBox art=new VBox(0,illustration,grass);art.setAlignment(Pos.CENTER);panel.getChildren().add(art);
        }
        card.getChildren().add(panel);
        Button primary=button(help?"Back":switch(state) {case READY->"Play";case PAUSED->"Resume";case RECOVERY->"Continue";default->"Play again";},"primary-button");
        primary.setId("primary-action");primary.setMaxWidth(Double.MAX_VALUE);
        primary.setOnAction(event->{if(help){help=false;updateCard();return;}if(state==GameSession.State.OVER)restartAction.run();resumeAction.run();});
        card.getChildren().add(primary);
        if(!help) {
            Button info=button("How to play","secondary-button");info.setMaxWidth(Double.MAX_VALUE);
            info.setOnAction(event->{help=true;updateCard();});card.getChildren().add(info);
        }
    }
    private VBox stat(boolean trophy,int value) {
        ImageView icon=GardenAssets.icon(trophy?"trophy":"points",42);
        VBox stat=new VBox(6,icon,label(Integer.toString(value),"result-value"));stat.setAlignment(Pos.CENTER);return stat;
    }
    private static Label label(String text,String style) {Label label=new Label(text);label.getStyleClass().add(style);return label;}
    private static Button button(String text,String style) {Button button=new Button(text);button.getStyleClass().add(style);button.setFocusTraversable(false);return button;}

    /**
     * Repaints the board, obstacles, pickup and interpolated snake at the current canvas size.
     */
    void render() {
        double cell=cell();gc.setImageSmoothing(true);
        gc.setFill(new ImagePattern(GardenAssets.image("board-tile"),0,0,2*cell,2*cell,false));gc.fillRect(0,0,gridCanvas.getWidth(),gridCanvas.getHeight());
        Coin coin=session.coin();var p=coin.getPos();double size=cell*.83;
        gc.drawImage(coin.getImage(),p.x*cell+(cell-size)/2,p.y*cell+(cell-size)/2,size,size);
        Obstacle hedges=session.obstacles();
        for(Point hedge:hedges.getObstacles())drawHedge(hedge,cell,1);
        for(Point hedge:hedges.pendingCells()) {
            if(hedges.getObstacles().contains(hedge))continue;
            drawHedge(hedge,cell,.30);
            gc.setStroke(Color.web("#fff2a0"));gc.setLineWidth(1.6);gc.setLineDashes(3,3);
            gc.strokeRect(hedge.x*cell+2,hedge.y*cell+2,cell-4,cell-4);gc.setLineDashes();
        }
        for(Point gap:hedges.gateCells()) {
            gc.setStroke(Color.web(hedges.gateWarningMoves()<0?"#e6ffff":"#ffda72"));
            gc.setLineWidth(2);gc.setLineDashes(3,3);
            gc.strokeRect(gap.x*cell+3,gap.y*cell+3,cell-6,cell-6);gc.setLineDashes();
        }
        drawSnake(session.snake());
        if(session.effects().active(PowerUpState.Effect.WRAP)) {
            gc.setStroke(Color.web("#53a9cf"));gc.setLineWidth(3);gc.strokeRect(1.5,1.5,gridCanvas.getWidth()-3,gridCanvas.getHeight()-3);
        }
    }

    private void drawHedge(Point p,double cell,double opacity) {
        gc.save();gc.setGlobalAlpha(opacity);
        gc.drawImage(GardenAssets.image("hedge"),p.x*cell,p.y*cell,cell,cell);gc.restore();
    }

    /** Draws the gameplay snake through the same renderer used by the menu illustration. */
    private void drawSnake(Snake snake) {
        Point head=snake.getHead();
        List<Point> body=SnakeSpriteSelector.drawingBody(snake.getBody(),head,snake.getDead());
        List<javafx.geometry.Point2D> path=bodyPath(body,head,previousBody,previousHead,
                game.getWidth(),game.getHeight(),progress);
        renderedHead=path.get(path.size()-1);
        SnakeRenderer.paint(gc,snake,path,game.getWidth(),game.getHeight(),cell());
    }

    /**
     * Builds an interpolated tail-to-head path while keeping occupied bends fixed.
     *
     * @param body current body positions ordered tail first
     * @param head current head cell
     * @param oldBody previous body positions ordered tail first
     * @param oldHead previous head cell
     * @param width board width in cells
     * @param height board height in cells
     * @param fraction movement progress from 0 to 1
     * @return new path in cell coordinates ending at the interpolated head
     */
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

    /**
     * Interpolates a one-cell coordinate change, taking the short route across a wrapped boundary.
     *
     * @param from previous cell coordinate
     * @param to current cell coordinate
     * @param size board dimension on this axis
     * @param progress movement progress; values at least 1 return the destination
     * @return interpolated coordinate, which may briefly lie outside the board at a wrap
     */
    static double interpolate(int from, int to, int size, double progress) {
        if (progress >= 1) return to;
        int delta = to - from;
        if (delta > 1) delta -= size;
        if (delta < -1) delta += size;
        return from + delta * progress;
    }

}
