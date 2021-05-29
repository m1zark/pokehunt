package com.m1zark.pokehunt.util.logs;

import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Getter
public class Log {
    private String timeStamp;
    private String timeRemaining;
    private String player;
    private String huntType;
    private int ID;

    public Log(String player, int ID, String huntType, String timeStamp, String timeRemaining) {
        this.player = player;
        this.ID = ID;
        this.huntType = huntType;
        this.timeStamp = timeStamp;
        this.timeRemaining = timeRemaining;
    }
}
