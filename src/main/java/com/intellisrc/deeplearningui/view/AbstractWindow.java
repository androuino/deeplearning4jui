package com.intellisrc.deeplearningui.view;

import com.intellisrc.deeplearningui.controllers.AbstractController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractWindow {
    private final AbstractController controller;
    private final ResourceBundle bundle;

    public AbstractWindow(AbstractController controller, ResourceBundle bundle) {
        this.controller = controller;
        this.bundle = bundle;
    }

    public Parent root() throws IOException {
        FXMLLoader loader = new FXMLLoader(url(), bundle);
        loader.setController(controller);
        return loader.load();
    }

    private URL url() throws IOException {
        return new File(System.getProperty("user.dir")
                + File.separator
                + "src"
                + File.separator
                + "main"
                + File.separator
                + "resources"
                + File.separator
                + "fxml"
                + File.separator
                + fxmlFileName()).toURI().toURL();
    }

    String iconFilePath() {
        return System.getProperty("user.dir")
                + File.separator
                + "src"
                + File.separator
                + "main"
                + File.separator
                + "resources"
                + File.separator
                + "icons"
                + File.separator
                + iconFileName();
    }

    public boolean resizable() {
        return true;
    }

    protected abstract String iconFileName();
    protected abstract String fxmlFileName();
    public abstract String titleBundleKey();
}
