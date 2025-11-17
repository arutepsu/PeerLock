package com.peerlock.ui.event;

import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventBus {

    private final Map<Class<? extends UiEvent>, List<Consumer<? extends UiEvent>>> listeners =
            new ConcurrentHashMap<>();

    public <T extends UiEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                 .add(listener);
    }

    public <T extends UiEvent> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<? extends UiEvent>> list = listeners.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    public void publish(UiEvent event) {
        Class<? extends UiEvent> eventType = event.getClass();
        List<Consumer<? extends UiEvent>> list = listeners.get(eventType);
        if (list == null) {
            return;
        }

        // Make a snapshot to avoid ConcurrentModification
        List<Consumer<? extends UiEvent>> snapshot = new ArrayList<>(list);

        for (Consumer<? extends UiEvent> rawConsumer : snapshot) {
            @SuppressWarnings("unchecked")
            Consumer<UiEvent> consumer = (Consumer<UiEvent>) rawConsumer;

            // Always route to JavaFX thread for UI safety
            Platform.runLater(() -> consumer.accept(event));
        }
    }
}
