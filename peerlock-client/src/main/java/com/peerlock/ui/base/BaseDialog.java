package com.peerlock.ui.base;

import com.peerlock.ui.ViewLifecycle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Base class for modal dialogs (e.g. New Chat, Settings, About).
 */
public abstract class BaseDialog implements ViewLifecycle {

    protected final Stage stage;
    protected final BorderPane root = new BorderPane();

    private boolean built = false;

    protected BaseDialog(Stage owner) {
        this.stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setScene(new Scene(root));
    }

    @Override
    public final void build() {
        if (built) {
            return;
        }
        built = true;

        buildUI();
        bindViewModel();
        registerListeners();
        onAfterBuild();
    }

    @Override
    public final void destroy() {
        unregisterListeners();
        unbindViewModel();
        onDestroy();
    }

    /** Create and arrange JavaFX nodes, and put them into root. */
    protected abstract void buildUI();

    protected void bindViewModel() {
        // optional
    }

    protected void unbindViewModel() {
        // optional
    }

    protected void registerListeners() {
        // optional
    }

    protected void unregisterListeners() {
        // optional
    }

    protected void onAfterBuild() {
        // optional
    }

    protected void onDestroy() {
        // optional
    }

    public void showDialog() {
        build();
        stage.showAndWait();
    }

    public void closeDialog() {
        destroy();
        stage.close();
    }

    public Stage getStage() {
        return stage;
    }

    public Parent getRoot() {
        return root;
    }
}
