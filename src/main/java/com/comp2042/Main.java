package com.comp2042;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX application entry point that creates the single-window view manager.
 */
public class Main extends Application {
    /** Creates the JavaFX application instance. */
    public Main() { }


    private static ViewManager viewManager;

    /**
     * Creates the main window and displays the start panel.
     *
     * @param primaryStage primary stage supplied by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        viewManager = new ViewManager(primaryStage);
        viewManager.initializeMainMenu();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Returns the manager created during application startup.
     *
     * @return active view manager, or null before start has run
     */
    public static ViewManager getViewManager() {
        return viewManager;
    }
}
