package com.intellisrc.deeplearningui.view.window.imp;

import com.intellisrc.deeplearningui.controllers.AbstractController;
import com.intellisrc.deeplearningui.view.AbstractWindow;

import java.util.ResourceBundle;

public class MainWindow extends AbstractWindow {

    public MainWindow(AbstractController controller, ResourceBundle bundle) {
        super(controller, bundle);
    }

    @Override
    protected String iconFileName() {
        return "ui.png";
    }

    @Override
    protected String fxmlFileName() {
        return "main.fxml";
    }

    @Override
    public String titleBundleKey() {
        return "main.title";
    }
}
