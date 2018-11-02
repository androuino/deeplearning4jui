package com.intellisrc.deeplearningui.view.window;

import com.intellisrc.deeplearningui.controllers.AbstractController;
import com.intellisrc.deeplearningui.controllers.impl.MainController;
import com.intellisrc.deeplearningui.handler.ViewHandler;
import com.intellisrc.deeplearningui.view.AbstractWindow;
import com.intellisrc.deeplearningui.view.window.imp.MainWindow;

import java.util.ResourceBundle;

public enum WindowFactory {
    MAIN {
        @Override
        public AbstractWindow createWindow(ViewHandler viewHandler, ResourceBundle bundle) {
            final AbstractController controller = new MainController(viewHandler);
            return new MainWindow(controller, bundle);
        }
    };

    public abstract AbstractWindow createWindow(ViewHandler viewHandler, ResourceBundle bundle);
}
