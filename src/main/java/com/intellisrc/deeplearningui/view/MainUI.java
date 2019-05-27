package com.intellisrc.deeplearningui.view;

import com.intellisrc.deeplearningui.handler.AppViewHandler;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainUI extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        File file = new File(System.getProperty("user.dir"));
        URL urls = file.toURI().toURL();
        ClassLoader loader = new URLClassLoader(new URL[]{urls});
        ResourceBundle bundle = ResourceBundle.getBundle("bundle" + File.separator + "configSrc", Locale.getDefault(), loader);
        Image icon = new Image(new File(file.getPath() + File.separator + "resources" + File.separator + "close.png").toURI().toString());
        new AppViewHandler(stage, bundle, icon).launchMainScene();
    }
}
