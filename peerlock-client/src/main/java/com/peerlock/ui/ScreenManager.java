package com.peerlock.ui;

import com.peerlock.ui.base.BaseScreen;
import javafx.scene.Scene;

/**
 * Handles switching between screens within a single JavaFX Scene.
 */
public class ScreenManager {

    private final Scene scene;
    private BaseScreen currentScreen;

    public ScreenManager(Scene scene) {
        this.scene = scene;
    }

    public void show(BaseScreen screen) {
        if (currentScreen != null) {
            currentScreen.destroy();
        }

        currentScreen = screen;
        currentScreen.build();
        scene.setRoot(currentScreen.getRoot());
    }

    public BaseScreen getCurrentScreen() {
        return currentScreen;
    }
}
