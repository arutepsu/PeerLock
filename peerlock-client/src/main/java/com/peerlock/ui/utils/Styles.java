package com.peerlock.ui.utils;

import java.net.URL;
import java.util.Arrays;

import javafx.scene.Parent;

public final class Styles {

    private static final String BASE = "/stylesheets/base.css";
    private static final String LOGIN = "/stylesheets/login.css";
    private static final String MAIN = "/stylesheets/main.css";

    private Styles() {}

    public static void applyBase(Parent root) {
        addStyles(root, BASE);
    }

    public static void applyLogin(Parent root) {
        addStyles(root, BASE, LOGIN);
    }

    public static void applyMain(Parent root) {
        addStyles(root, BASE, MAIN);
    }

    private static void addStyles(Parent root, String... paths) {
        Arrays.stream(paths)
                .distinct()
                .forEach(path -> {
                    URL url = Styles.class.getResource(path);
                    if (url != null) {
                        String css = url.toExternalForm();
                        if (!root.getStylesheets().contains(css)) {
                            root.getStylesheets().add(css);
                        }
                    } else {
                        System.err.println("Stylesheet not found on classpath: " + path);
                    }
                });
    }
}
