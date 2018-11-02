package com.intellisrc.deeplearningui.handler;

import java.io.IOException;

public interface ViewHandler {
    void launchMainScene() throws IOException;
    void logsBus(String msg);
    String getTransportedMsg();
}
