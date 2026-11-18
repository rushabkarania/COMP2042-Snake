package com.comp2042;

import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/** Uses one window for the start panel and gameplay, preserving the saved record between runs. */
public class ViewManager {
    private final Stage stage;
    private GameController controller;
    public ViewManager(Stage stage) { this.stage=stage; }
    public void initializeMainMenu() {
        GameSession session=new GameSession(new Game(20,20,true,'N'));
        GameView view=new GameView(session);
        controller=new GameController(session,view,stage,this::showMainMenu);
        Scene scene=new Scene(view.root(),640,Math.min(724,javafx.stage.Screen.getPrimary().getVisualBounds().getHeight()-64));
        scene.getStylesheets().add(Main.class.getResource("/resources/views/garden.css").toExternalForm());
        scene.addEventFilter(KeyEvent.KEY_PRESSED,controller::handle);
        stage.setScene(scene);stage.setMinWidth(520);stage.setMinHeight(620);
        stage.setTitle("Snake");stage.getIcons().setAll(GardenAssets.logo(16),GardenAssets.logo(32),GardenAssets.logo(64));
        stage.setFullScreenExitHint("");stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
        stage.show();view.root().requestFocus();
    }
    public Stage getPrimaryStage() { return stage; }
    public Stage getGameStage() { return stage; }
    public void showMainMenu() { controller.showStart(); }
}
