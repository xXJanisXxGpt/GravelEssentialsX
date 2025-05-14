package com.xXJanisXx.gravelEssentialsX.models;

import java.util.UUID;

public class TPARequest {
    private final UUID senderUUID;
    private final UUID targetUUID;
    private final long timestamp;

    public TPARequest(UUID senderUUID, UUID targetUUID) {
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public long getTimestamp() {
        return timestamp;
    }
}