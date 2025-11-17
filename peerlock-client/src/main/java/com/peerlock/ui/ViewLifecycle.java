package com.peerlock.ui;

/**
 * Common lifecycle contract for all UI views (screens, dialogs, overlays).
 * 
 * Usage:
 *  - build():   create UI nodes, bind to view models, register listeners.
 *  - destroy(): undo everything from build(), release resources, unregister listeners.
 */
public interface ViewLifecycle {

    /**
     * Build the view.
     * <p>
     * Should:
     *  - Create and layout JavaFX nodes.
     *  - Initialize state.
     *  - Bind to view models / controllers.
     *  - Register event listeners.
     * </p>
     */
    void build();

    /**
     * Tear down the view.
     * <p>
     * Should:
     *  - Unbind all properties.
     *  - Unregister listeners and observers.
     *  - Stop timers / background tasks related to this view.
     * </p>
     */
    void destroy();
}
