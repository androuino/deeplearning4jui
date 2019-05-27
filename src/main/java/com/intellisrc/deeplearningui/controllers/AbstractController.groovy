package com.intellisrc.deeplearningui.controllers

import com.intellisrc.deeplearningui.handler.ViewHandler
import groovy.transform.CompileStatic
import javafx.fxml.Initializable

@CompileStatic
abstract class AbstractController implements Initializable {
    protected final ViewHandler viewHandler

    AbstractController(ViewHandler viewhandler) {
        this.viewHandler = viewHandler
    }

    @Override
    abstract void initialize(URL url, ResourceBundle resourceBundle)
}
