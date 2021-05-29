package com.m1zark.pokehunt.events;

import com.m1zark.pokehunt.events.BaseEvent;
import lombok.NonNull;
import org.spongepowered.api.event.cause.Cause;

public class BreedEventEnd
extends BaseEvent {
    @NonNull
    private final Cause cause;

    @Override
    public Cause getCause() {
        return this.cause;
    }

    public BreedEventEnd(@NonNull Cause cause) {
        if (cause == null) {
            throw new NullPointerException("cause is marked @NonNull but is null");
        }
        this.cause = cause;
    }
}

