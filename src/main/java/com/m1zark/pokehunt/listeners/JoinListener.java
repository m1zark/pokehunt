package com.m1zark.pokehunt.listeners;

import com.m1zark.pokehunt.PokeHunt;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class JoinListener {
    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        PokeHunt.getInstance().getSql().addPlayerData(player.getUniqueId());
    }
}
