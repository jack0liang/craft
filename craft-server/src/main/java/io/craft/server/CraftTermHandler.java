package io.craft.server;

import lombok.extern.slf4j.Slf4j;
import sun.misc.Signal;
import sun.misc.SignalHandler;

@Slf4j
public class CraftTermHandler implements SignalHandler {

    private final CraftServer server;

    public CraftTermHandler(CraftServer server) {
        this.server = server;
    }

    @Override
    public void handle(Signal signal) {
        try {
            server.close();
        } catch (Exception e) {
            logger.error("crfat server close error={}", e.getMessage(), e);
        }
    }
}
