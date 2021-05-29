package com.m1zark.pokehunt.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.pokehunt.PHInfo;
import com.m1zark.pokehunt.PokeHunt;
import com.pixelmonmod.pixelmon.RandomHelper;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HuntsConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode main;

    public HuntsConfig() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(PokeHunt.getInstance().getConfigDir() + "/hunts.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(PokeHunt.getInstance().getConfigDir())) Files.createDirectory(PokeHunt.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode hunts = main.getNode("Hunts");
            CommentedConfigurationNode rank = main.getNode("Rank");
            CommentedConfigurationNode blacklist = main.getNode("Blacklist");

            hunts.setComment("All options default to false unless they are set in here.\nNote: Legendary and Mega options ignore the rarity check. Only have 1 set.\nNote: Boss option ignores everything basically.");

            hunts.getNode("1","huntName").getString("Level 1 Hunt");
            hunts.getNode("1","duration").getInt(1440);
            hunts.getNode("1","flavorText").getList(TypeToken.of(String.class), Lists.newArrayList("&7This is just a test...","&7This is another test..."));
            hunts.getNode("1","options","setMinPokemonRarity").getString("150-200");
            hunts.getNode("1","options","setAmountNeeded").getInt(10);
            hunts.getNode("1","options","Boss").getBoolean(false);
            hunts.getNode("1","options","Mega").getBoolean(false);
            hunts.getNode("1","options","Legendary").getBoolean(false);
            hunts.getNode("1","options","UltraBeasts").getBoolean(false);
            hunts.getNode("1","options","Nature").getBoolean(false);
            hunts.getNode("1","options","Size").getBoolean(false);
            hunts.getNode("1","options","Gender").getBoolean(false);
            hunts.getNode("1","rewards").getList(TypeToken.of(String.class), Lists.newArrayList());

            rank.getNode("totalCompletions").getInt(5);
            rank.getNode("hunts").getString("4,5");
            rank.getNode("command").getString("adminpay @p 100000");

            blacklist.getNode("Pokemon").getList(TypeToken.of(String.class), Lists.newArrayList("Omanyte","Kabuto","Aerodactyl","Lileep","Anorith","Cranidos","Shieldon","Tirtouga","Archen","Tyrunt","Amaura"));

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            PokeHunt.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.ERROR_PREFIX, "There was an issue loading the config...")));
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

    public void reload() {
        try {
            main = loader.load();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public static String getName(int id) { return main.getNode("Hunts", String.valueOf(id), "huntName").getString(); }

    public static int getRandomText(int id) {
        int random = 0;
        if(!main.getNode("Hunts", String.valueOf(id), "flavorText").isVirtual() && main.getNode("Hunts", String.valueOf(id), "flavorText").getChildrenList().size() > 0) {
            random = RandomHelper.getRandomNumberBetween(0, main.getNode("Hunts", String.valueOf(id), "flavorText").getChildrenList().size() - 1);
        }
        return random;
    }

    public static String getFlavorText(int id, int number) {
        String text = "";

        try {
            if(!main.getNode("Hunts", String.valueOf(id), "flavorText").isVirtual() && main.getNode("Hunts", String.valueOf(id), "flavorText").getList(TypeToken.of(String.class)).size() > 0) {
                text = main.getNode("Hunts", String.valueOf(id), "flavorText").getList(TypeToken.of(String.class)).get(number);
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        return text;
    }

    public static String getRarity(int id) { return main.getNode("Hunts", String.valueOf(id), "options", "setMinPokemonRarity").getString(); }

    public static int getAmountNeeded(int id) { return main.getNode("Hunts", String.valueOf(id), "options", "setAmountNeeded").getInt(); }

    public static boolean getOptions(int id, String value) {
        return !main.getNode("Hunts", String.valueOf(id), "options", value).isVirtual() &&
                main.getNode("Hunts", String.valueOf(id), "options", value).getBoolean();
    }

    public static long getTime(int id) {
        if(main.getNode("Hunts", String.valueOf(id), "duration").isVirtual()) return 1440 * 60;

        return main.getNode("Hunts", String.valueOf(id), "duration").getInt() * 60;
    }

    public static int getNumberHunts() { return main.getNode("Hunts").getChildrenMap().size(); }

    public static List<String> getRewards(int id) {
        List<String> rewards = new ArrayList<>();

        for (int i = 0; i < main.getNode("Hunts",String.valueOf(id),"rewards").getChildrenList().size(); i++) {
            CommentedConfigurationNode reward = main.getNode("Hunts",String.valueOf(id),"rewards").getChildrenList().get(i);
            rewards.add(reward.getNode("displayName").getString());
        }

        return rewards;
    }

    public static List<String> getCommandRewards(int id){
        List<String> rewards = new ArrayList<>();

        for (int i = 0; i < main.getNode("Hunts",String.valueOf(id),"rewards").getChildrenList().size(); i++) {
            CommentedConfigurationNode reward = main.getNode("Hunts",String.valueOf(id),"rewards").getChildrenList().get(i);
            String type = reward.getNode("type").getString();

            if(type.equals("command")) rewards.add(reward.getNode("command").getString() + "," + reward.getNode("displayName").getString());
        }

        return rewards;
    }

    public static List<String> getItemRewards(int id){
        List<String> rewards = new ArrayList<>();

        for (int i = 0; i < main.getNode("Hunts",String.valueOf(id),"rewards").getChildrenList().size(); i++) {
            CommentedConfigurationNode reward = main.getNode("Hunts",String.valueOf(id),"rewards").getChildrenList().get(i);
            String type = reward.getNode("type").getString();

            if(type.equals("item")) rewards.add(reward.getNode("id").getString() + "," + reward.getNode("count").getInt() + "," + reward.getNode("displayName").getString());
        }

        return rewards;
    }

    public static int getTotalCompletions(){
        return main.getNode("Rank","totalCompletions").getInt();
    }

    public static String getCommandCompletions() { return main.getNode("Rank","command").getString(); }

    public static String getHuntCompletions() { return main.getNode("Rank","hunts").getString(); }

    public static List<String> getBlacklist() {
        try {
            return main.getNode("Blacklist","Pokemon").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            return Lists.newArrayList();
        }
    }
}
