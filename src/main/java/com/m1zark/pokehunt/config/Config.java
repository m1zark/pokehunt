package com.m1zark.pokehunt.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.m1utilities.api.Discord.DiscordOption;
import com.m1zark.m1utilities.api.Discord.Field;
import com.m1zark.pokehunt.PHInfo;
import com.m1zark.pokehunt.PokeHunt;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Config {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode main;

    public static String storageType;
    public static String mysqlURL;
    public static String mysqlUsername;
    public static String mysqlPassword;

    public Config() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(PokeHunt.getInstance().getConfigDir() + "/settings.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(PokeHunt.getInstance().getConfigDir())) Files.createDirectory(PokeHunt.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode storage = main.getNode("Storage");
            CommentedConfigurationNode general = main.getNode("General");
            CommentedConfigurationNode discord = main.getNode("Discord");

            general.getNode("enableBounties").getBoolean(true);
            general.getNode("enableHunts").getBoolean(true);
            general.getNode("enableDiscordNotifications").getBoolean(true);

            storageType = storage.getNode("storageType").setComment("Types: h2, mysql").getString("h2");
            mysqlURL = storage.getNode("MYSQL","URL").getString("[host]:[port]/[database]");
            mysqlUsername = storage.getNode("MYSQL","Username").getString("");
            mysqlPassword = storage.getNode("MYSQL","Password").getString("");

            discord.getNode("webhook-url").getList(TypeToken.of(String.class));
            discord.getNode("notifications","breeding-comp-start","color").getString("#AA00AA");
            discord.getNode("notifications","breeding-comp-end","color").getString("#AA00AA");

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

    public static boolean getEnabled(String node) {
        return main.getNode("General", node).getBoolean();
    }

    public static DiscordOption discordOption(String id, String event, String time) {
        String username = main.getNode("Discord","notifications",id,"username").isVirtual() ? null : main.getNode("Discord","notifications",id,"username").getString();
        String avatar = main.getNode("Discord","notifications",id,"avatar").isVirtual() ? null : main.getNode("Discord","notifications",id,"avatar").getString();
        String content = main.getNode("Discord","notifications",id,"content").isVirtual() ? null : main.getNode("Discord","notifications",id,"content").getString().replace("{event}",event).replace("{time}",time);
        String title = main.getNode("Discord","notifications",id,"title").isVirtual() ? null : main.getNode("Discord","notifications",id,"title").getString().replace("{event}",event).replace("{time}",time);
        String description = main.getNode("Discord","notifications",id,"description").isVirtual() ? null : main.getNode("Discord","notifications",id,"description").getString().replace("{event}",event).replace("{time}",time);
        String thumbnail = main.getNode("Discord","notifications",id,"thumbnail").isVirtual() ? null : main.getNode("Discord","notifications",id,"thumbnail").getString();
        String image = main.getNode("Discord","notifications",id,"image").isVirtual() ? null : main.getNode("Discord","notifications",id,"image").getString();
        boolean timestamp = !main.getNode("Discord","notifications",id,"timestamp").isVirtual() && main.getNode("Discord","notifications",id,"timestamp").getBoolean();

        List<Field> fields = Lists.newArrayList();
        if(!main.getNode("Discord","notifications",id,"fields").isVirtual()) {
            for (int i = 0; i < main.getNode("Discord","notifications",id,"fields").getChildrenList().size(); i++) {
                CommentedConfigurationNode field = main.getNode("Discord","notifications",id,"fields").getChildrenList().get(i);

                fields.add(new Field(
                        field.getNode("name").isVirtual() ? null : field.getNode("name").getString().replace("{event}",event).replace("{time}",time),
                        field.getNode("value").isVirtual() ? null : field.getNode("value").getString().replace("{event}",event).replace("{time}",time),
                        !field.getNode("inline").isVirtual() && field.getNode("inline").getBoolean()
                ));
            }
        }

        Map<String,String> footer = new HashMap<>();
        if(!main.getNode("Discord","notifications",id,"footer").isVirtual()) {
            footer.put("text", main.getNode("Discord","notifications",id,"footer","text").getString());
            footer.put("icon", main.getNode("Discord","notifications",id,"footer","icon").getString());
        }

        try {
            return DiscordOption.builder()
                    .webhookChannels(main.getNode("Discord","webhook-url").getList(TypeToken.of(String.class)))
                    .username(username)
                    .avatar_url(avatar)
                    .content(content)
                    .color(Color.decode(main.getNode("Discord","notifications",id,"color").getString()))
                    .title(title)
                    .description(description)
                    .fields(fields)
                    .thumbnail(thumbnail)
                    .image(image)
                    .footer(footer)
                    .timestamp(timestamp)
                    .build();
        }catch (ObjectMappingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
