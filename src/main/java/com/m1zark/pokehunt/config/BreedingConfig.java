package com.m1zark.pokehunt.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.m1zark.pokehunt.PHInfo;
import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.util.BreedEvent.PlayerEntry;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.loader.HeaderMode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BreedingConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode main;
    private static Gson gson = new Gson();

    public BreedingConfig() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path dir = Paths.get(PokeHunt.getInstance().getConfigDir() + "/events");
        Path configFile = Paths.get(dir + "/breeding_comp.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).setHeaderMode(HeaderMode.NONE).build();

        try {
            if (!Files.exists(dir)) Files.createDirectory(dir);
            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode settings = main.getNode("Settings");
            settings.getNode("enableEvent").getBoolean(false);
            settings.getNode("eventEndDate").getLong();
            settings.getNode("maxWinners").getInt(5);
            settings.getNode("takePokemon").getBoolean(true);
            settings.getNode("info").getString("&7Think your a top breeder on the server? Enter the breeding competition and test your skills! Enter using &d/bc enter&7!");
            settings.getNode("rewards").getList(TypeToken.of(String.class), Lists.newArrayList());

            settings.getNode("Options","ability").getBoolean(true);
            settings.getNode("Options","nature").getBoolean(true);
            settings.getNode("Options","size").getBoolean(true);
            settings.getNode("Options","eggmove").getBoolean(true);
            settings.getNode("Options","gender").getBoolean(true);
            settings.getNode("Options","pokeball").getBoolean(false);
            settings.getNode("Options","hiddenpower").getBoolean(true);

            CommentedConfigurationNode player = main.getNode("Data");

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            PokeHunt.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.ERROR_PREFIX, "There was an issue loading the config (Breeding Event)")));
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            loader.save(main);
        } catch (IOException var1) {
            var1.printStackTrace();
        }
    }

    public static void reload() {
        try {
            main = loader.load();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public static boolean getOptions(String option) { return main.getNode("Settings","Options",option).getBoolean(); }

    public static int getMaxWinners() { return main.getNode("Settings","maxWinners").getInt(); }

    public static String getInfoMessage() { return main.getNode("Settings","info").getString(); }

    public static boolean doRemovePokemon() { return main.getNode("Settings","takePokemon").getBoolean(); }

    public static boolean getCompEnabled() {
        return main.getNode("Settings","enableEvent").getBoolean();
    }





    public static void setCompEnabled(boolean enabled) {
        main.getNode("Settings","enableEvent").setValue(enabled);
        saveConfig();
    }


    public static long getEndDate() { return main.getNode("Settings","eventEndDate").getLong(); }

    public static void setEndDate(long time) {
        main.getNode("Settings","eventEndDate").setValue(time);
        saveConfig();
    }


    public static void clearData() {
        main.getNode("Data","players").setValue(null);
        main.getNode("Data","winners").setValue(null);
        saveConfig();
    }

    public static PlayerEntry getPlayerData(UUID uuid) {
        if(main.getNode("Data","players",uuid.toString()).isVirtual()) return null;

        return gson.fromJson(main.getNode("Data","players",uuid.toString()).getString(), PlayerEntry.class);
    }

    public static boolean savePlayerData(PlayerEntry entry) {
        if(main.getNode("Data","players",entry.getPlayer().toString()).isVirtual()) {
            main.getNode("Data","players",entry.getPlayer().toString()).setValue(gson.toJson(entry));

            saveConfig();
            return true;
        }

        return false;
    }

    public static List<String> getCompWinners() {
        try {
            return main.getNode("Data","winners").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            return Lists.newArrayList();
        }
    }

    public static void saveWinner(List list) {
        main.getNode("Data","winners").setValue(list);
        saveConfig();
    }





    public static List<String> getRewards() {
        List<String> rewards = new ArrayList<>();

        for (int i = 0; i < main.getNode("Settings","rewards").getChildrenList().size(); i++) {
            CommentedConfigurationNode reward = main.getNode("Settings","rewards").getChildrenList().get(i);
            rewards.add(reward.getNode("displayName").getString());
        }

        return rewards;
    }

    public static List<String> getCommandRewards(){
        List<String> rewards = new ArrayList<>();

        for (int i = 0; i < main.getNode("Settings","rewards").getChildrenList().size(); i++) {
            CommentedConfigurationNode reward = main.getNode("Settings","rewards").getChildrenList().get(i);
            String type = reward.getNode("type").getString();

            if(type.equals("command")) rewards.add(reward.getNode("command").getString() + "," + reward.getNode("displayName").getString());
        }

        return rewards;
    }

    public static List<String> getItemRewards(){
        List<String> rewards = new ArrayList<>();

        for (int i = 0; i < main.getNode("Settings","rewards").getChildrenList().size(); i++) {
            CommentedConfigurationNode reward = main.getNode("Settings","rewards").getChildrenList().get(i);
            String type = reward.getNode("type").getString();

            if(type.equals("item")) rewards.add(reward.getNode("id").getString() + "," + reward.getNode("count").getInt() + "," + reward.getNode("displayName").getString());
        }

        return rewards;
    }

    public static Boolean getShinyRewards(){
        for (int i = 0; i < main.getNode("Settings","rewards").getChildrenList().size(); i++) {
            CommentedConfigurationNode reward = main.getNode("Settings","rewards").getChildrenList().get(i);
            String type = reward.getNode("type").getString();

            if(type.equals("shiny")) return true;
        }

        return false;
    }
}
