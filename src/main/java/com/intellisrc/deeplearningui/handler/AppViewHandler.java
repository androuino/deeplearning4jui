package com.intellisrc.deeplearningui.handler;

import com.intellisrc.deeplearningui.controllers.impl.MainController;
import com.intellisrc.deeplearningui.view.AbstractWindow;
import com.intellisrc.deeplearningui.view.window.WindowFactory;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class AppViewHandler implements ViewHandler {
    private final Stage main;
    private final ResourceBundle bundle;
    private final Image icon;
    private String value;

    public AppViewHandler(Stage main, ResourceBundle bundle, Image icon) {
        this.main = main;
        this.bundle = bundle;
        this.icon = icon;
    }

    @Override
    public void launchMainScene() throws IOException {
        buildAndShowScene(main, WindowFactory.MAIN.createWindow(this, bundle));
    }

    @Override
    public void logsBus(String msg) {
        this.value = msg;
    }

    @Override
    public String getTransportedMsg() {
        return this.value;
    }

    private void buildAndShowScene(Stage stage, AbstractWindow window) throws IOException {
        try {
            stage.getIcons().add(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        stage.setTitle(bundle.getString(window.titleBundleKey()));
        stage.setResizable(window.resizable());
        stage.setScene(new Scene(window.root(), 951, 917));
        stage.getScene().getStylesheets().clear();
        stage.getScene().getStylesheets().add(getClass().getResource("/css/darktheme.css").toString());
        stage.show();
    }
}
