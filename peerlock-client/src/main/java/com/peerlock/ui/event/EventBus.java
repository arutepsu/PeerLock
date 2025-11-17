package com.peerlock.ui.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javafx.application.Platform;

public class EventBus {

    // Map: event type -> listeners
    private final Map<Class<? extends UiEvent>, List<Consumer<UiEvent>>> listeners =
            new ConcurrentHashMap<>();

    /**
     * Subscribe to a specific event type.
     */
    public <T extends UiEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<UiEvent>> list =
                listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());

        @SuppressWarnings("unchecked")
        Consumer<UiEvent> casted = (Consumer<UiEvent>) listener;

        list.add(casted);
    }

    /**
     * Unsubscribe a listener from a specific event type.
     */
    public <T extends UiEvent> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<UiEvent>> list = listeners.get(eventType);
        if (list == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Consumer<UiEvent> casted = (Consumer<UiEvent>) listener;

        list.remove(casted);
        if (list.isEmpty()) {
            listeners.remove(eventType);
        }
    }

    /**
     * Publish an event to all listeners of its exact type.
     * Delivery is always done on the JavaFX Application Thread.
     */
    public void publish(UiEvent event) {
        Class<? extends UiEvent> eventType = event.getClass();
        List<Consumer<UiEvent>> list = listeners.get(eventType);
        if (list == null || list.isEmpty()) {
            return;
        }

        for (Consumer<UiEvent> consumer : list) {
            Platform.runLater(() -> consumer.accept(event));
        }
    }
}
