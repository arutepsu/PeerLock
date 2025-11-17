package com.peerlock.ui.base;

import com.peerlock.ui.ViewLifecycle;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Base class for all main application screens that live inside the primary Stage.
 */
public abstract class BaseScreen implements ViewLifecycle {

    private boolean built = false;

    /** Root node for this screen. This will be set as Scene root. */
    protected final BorderPane root = new BorderPane();

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

    /** Bind JavaFX properties to your view model / controller. */
    protected void bindViewModel() {
        // optional
    }

    /** Reverse of bindViewModel, unbind properties to prevent leaks. */
    protected void unbindViewModel() {
        // optional
    }

    /** Add event handlers, listeners, subscriptions. */
    protected void registerListeners() {
        // optional
    }

    /** Remove listeners and subscriptions. */
    protected void unregisterListeners() {
        // optional
    }

    /** Optional: run logic after everything has been built. */
    protected void onAfterBuild() {
        // optional
    }

    /** Optional: extra cleanup, closing resources, etc. */
    protected void onDestroy() {
        // optional
    }

    /** Node to set as Scene root. */
    public Parent getRoot() {
        return root;
    }
}
