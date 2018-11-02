package com.intellisrc.deeplearningui.controllers

import com.intellisrc.deeplearningui.handler.ViewHandler
import javafx.fxml.Initializable

abstract class AbstractController implements Initializable {
    protected final ViewHandler viewHandler;

    AbstractController(ViewHandler viewhandler) {
        this.viewHandler = viewHandler;
    }

    @Override
    abstract void initialize(URL url, ResourceBundle resourceBundle)
}
