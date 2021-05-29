package com.m1zark.pokehunt;

import com.google.inject.Inject;
import com.m1zark.pokehunt.listeners.DayCareListener;
import com.m1zark.pokehunt.listeners.PixelmonListener;
import com.m1zark.pokehunt.listeners.JoinListener;
import com.m1zark.pokehunt.tasks.checkExpired;
import com.m1zark.pokehunt.commands.CommandManager;
import com.m1zark.pokehunt.config.*;
import com.m1zark.pokehunt.storage.DataSource;
import lombok.Getter;
import org.slf4j.Logger;
import java.nio.file.Path;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import com.pixelmonmod.pixelmon.Pixelmon;

@Getter
@Plugin(id = PHInfo.ID, version = PHInfo.VERSION, name = PHInfo.NAME, description = PHInfo.DESCRIPTION, authors = "m1zark")
public class PokeHunt {
    @Inject private Logger logger;
    private static PokeHunt instance;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    private BountiesConfig bountiesConfig;
    private HuntsConfig huntsConfig;
    private MessageConfig messageConfig;
    private BreedingConfig breedingConfig;
    private Config config;
    private DataSource sql;
    private boolean enabled = true;

    @Listener public void onInitialization(GameInitializationEvent event){
        instance = this;

        PHInfo.startup();
        this.enabled = PHInfo.dependencyCheck();

        if (this.enabled) {
            this.config = new Config();
            this.messageConfig = new MessageConfig();
            this.bountiesConfig = new BountiesConfig();
            this.huntsConfig = new HuntsConfig();
            this.breedingConfig = new BreedingConfig();

            new CommandManager().registerCommands(this);
            Pixelmon.EVENT_BUS.register(new PixelmonListener());
            Sponge.getEventManager().registerListeners(this, new JoinListener());
            if(Sponge.getPluginManager().isLoaded("daycare")) Sponge.getEventManager().registerListeners(this, new DayCareListener());

            this.sql = new DataSource("PH_BOUNTIES","PH_HUNTS","PH_PLAYERDATA","PH_LOGS");
            this.sql.createTables();

            getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.PREFIX, "Initialization complete!")));
        }
    }

    @Listener public void postGameStart(GameStartedServerEvent event){
        checkExpired.initialize();
    }

    @Listener public void onReload(GameReloadEvent e) {
        if (this.enabled) {
            this.config = new Config();
            this.messageConfig = new MessageConfig();
            this.bountiesConfig = new BountiesConfig();
            this.huntsConfig = new HuntsConfig();
            this.breedingConfig = new BreedingConfig();
            getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.PREFIX, "Configurations have been reloaded")));
        }
    }

    @Listener public void onServerStop(GameStoppingEvent e) {
        try {
            this.sql.shutdown();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static PokeHunt getInstance() {
        return instance;
    }

    public Logger getLogger(){
        return this.logger;
    }

    public Optional<ConsoleSource> getConsole() {
        return Optional.ofNullable(Sponge.isServerAvailable() ? Sponge.getServer().getConsole() : null);
    }
}
