package com.townssiege.models;

import java.util.UUID;

public class Territory {
    private UUID id;

    public Territory() {
        this.id = UUID.randomUUID();
    }

    public Territory(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
