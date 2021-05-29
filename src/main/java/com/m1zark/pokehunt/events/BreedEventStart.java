package com.m1zark.pokehunt.events;

import lombok.NonNull;
import org.spongepowered.api.event.cause.Cause;

public class BreedEventStart
extends BaseEvent {
    public final String ends;
    @NonNull
    private final Cause cause;

    @Override
    public Cause getCause() {
        return this.cause;
    }

    public String getEnds() {
        return this.ends;
    }

    public BreedEventStart(String ends, @NonNull Cause cause) {
        if (cause == null) {
            throw new NullPointerException("cause is marked @NonNull but is null");
        }
        this.ends = ends;
        this.cause = cause;
    }
}

