package io.github.railroad.ui.defaults;

import javafx.scene.layout.AnchorPane;

public class RRAnchorPane extends AnchorPane {
    public RRAnchorPane() {
        super();
        getStyleClass().addAll("Railroad", "Pane", "AnchorPane", "background-2");
    }
}