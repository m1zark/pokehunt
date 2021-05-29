package com.m1zark.pokehunt.config;

import com.m1zark.pokehunt.PHInfo;
import com.m1zark.pokehunt.PokeHunt;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MessageConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode main;

    public MessageConfig() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(PokeHunt.getInstance().getConfigDir() + "/messages.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(PokeHunt.getInstance().getConfigDir())) Files.createDirectory(PokeHunt.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode messages = main.getNode("Messages");

            messages.setComment("{player} - Player, {hunt} - Name of Hunt from config, {reward} - reward name, {count} - completions remaining, {rank} - name set in config, {type} - bounty/hunt");
            messages.getNode("serverAnnounce").getBoolean(true);
            messages.getNode("Completion","redeemPlayer").getString("&7[&dPok\u00E9Hunts&7] &7You completed a {hunt}!");
            messages.getNode("Completion","redeemBroadcast").getString("&7[&dPok\u00E9Hunts&7] &d{player} &7completed a {hunt}!");
            messages.getNode("Completion","itemsReceived").getString("&7[&dPok\u00E9Hunts&7] &7You received &a{reward}&7.");
            messages.getNode("Completion","shinyReward").getString("&7[&dPok\u00E9Hunts&7] &7Your new &a{pokemon} &7is now shiny!");
            messages.getNode("Rank","done").getString("&7[&dPok\u00E9Hunts&7] &7Congratulations! You completed enough {type} to receive the {rank} Rank!");
            messages.getNode("Rank","needMore").getString("&7[&dPok\u00E9Hunts&7] &7You need &a{count} &7more completions to receive the {rank} Rank.");
            messages.getNode("Rank","rankNames","bounty").getString("&6Pok\u00E9&3Ranger");
            messages.getNode("Rank","rankNames","hunt").getString("&6Pok\u00E9&3Hunter");
            messages.getNode("UI","info").getString("&7You can get the {rank} Rank &7after\n&7completing &b{count} &7level 4 or 5 {type}.");
            messages.getNode("UI","infoTitle").getString("&6Completion Rewards Info");
            messages.getNode("UI","guiTitle").getString("&c&lPok\u00E9&3&lHunts");
            messages.getNode("Misc","announceNewHunts").getString("&7[&dPok\u00E9Hunts&7] &cNew {type} have been added! Check them out with /ph.");
            messages.getNode("Misc","missingPermission").getString("&7[&dPok\u00E9Hunts&7] Sorry, but you seem to have limited access to this command.");

            messages.getNode("Events","announceNewEvent").getString("&7[&bMirage&6Events&7] &7A new {event} has been started. It expires in &d{time}&7.");
            messages.getNode("Events","announceEventEnd").getString("&7[&bMirage&6Events&7] &7The current {event} is now over. Thank you all who participated.");

            messages.getNode("Events","eventExpired").getString("&7[&bMirage&6Events&7] &7The {event} has already ended.");
            messages.getNode("Events","noEvent").getString("&7[&bMirage&6Events&7] &7There are no current {event} running right now.");
            messages.getNode("Events","maxWinners").getString("&7[&bMirage&6Events&7] &7The max number of winners for this {event} has already been achieved. Better luck next time.");

            messages.getNode("Events","Entry","newEntry").getString("&7[&bMirage&6Events&7] &7You successfully entered the {event}. Check out further details with &d/ph&7.");
            messages.getNode("Events","Entry","alreadyEntered").getString("&7[&bMirage&6Events&7] &7You already entered this competition. Check out the full details with &d/ph&7.");
            messages.getNode("Events","missingPermission").getString("&7[&bMirage&6Events&7] Sorry, but you don't seem to have access to this {event}.");

            messages.getNode("Events","Completion","rewardsReceived").getString("&7[&bMirage&6Events&7] &7You received &a{reward}&7.");
            messages.getNode("Events","Completion","shinyReward").getString("&7[&bMirage&6Events&7] &7Your &a{pokemon} &7is now shiny!");
            messages.getNode("Events","Completion","playerMessage").getString("&7[&bMirage&6Events&7] &7You have completed this {event}... Congratulation!");
            messages.getNode("Events","Completion","alreadyCompleted").getString("&7[&bMirage&6Events&7] &7You have already completed this {event}.");

            loader.save(main);
        } catch (IOException e) {
            PokeHunt.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.ERROR_PREFIX, "There was an issue loading the config...")));
            e.printStackTrace();
            return;
        }

        PokeHunt.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.PREFIX, "Loading configuration...")));
    }

    public static void saveConfig() {
        try {
            loader.save(main);
        } catch (IOException var1) {
            var1.printStackTrace();
        }
    }

    public void reload() {
        try {
            main = loader.load();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public static boolean getServerAnnounce() { return main.getNode("Messages","serverAnnounce").getBoolean(); }

    public static String getMessages(String value) { return main.getNode((Object[])value.split("\\.")).getString(); }
}
