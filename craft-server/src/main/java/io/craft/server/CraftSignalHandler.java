package io.craft.server;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class CraftSignalHandler implements SignalHandler {

    private final CraftServer server;

    public CraftSignalHandler(CraftServer server) {
        this.server = server;
    }

    @Override
    public void handle(Signal signal) {
        System.out.println(signal);
        try {
            server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
