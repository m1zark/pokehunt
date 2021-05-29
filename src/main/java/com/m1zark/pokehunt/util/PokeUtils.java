package com.m1zark.pokehunt.util;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.pokehunt.PHInfo;
import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.config.BountiesConfig;
import com.m1zark.pokehunt.config.BreedingConfig;
import com.m1zark.pokehunt.config.HuntsConfig;
import com.m1zark.pokehunt.config.MessageConfig;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class PokeUtils {
    public static boolean between(int i, int minValueInclusive, int maxValueInclusive) {
        return (i >= minValueInclusive && i <= maxValueInclusive);
    }

    public static String insertLinebreaks(String s, int charsPerLine) {
        char[] chars = s.toCharArray();
        int lastLinebreak = 0;
        boolean wantLinebreak = false;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            if (wantLinebreak && chars[i] == ' ') {
                sb.append('\n');
                lastLinebreak = i;
                wantLinebreak = false;
            } else {
                sb.append(chars[i]);
            }
            if (i - lastLinebreak + 1 == charsPerLine)
                wantLinebreak = true;
        }
        return sb.toString();
    }

    public static String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    @Nullable
    public static PlayerPartyStorage getPlayerStorage(EntityPlayerMP player) {
        return Pixelmon.storageManager.getParty(player);
    }



    public static void processBountyRewards(int id, Player player, Pokemon pokemon) {
        List<String> commands = BountiesConfig.getCommandRewards(id);
        commands.forEach(command->{
            String[] reward = command.trim().split("\\s*,\\s*");
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdParser(player, reward[0]));
            Chat.sendMessage(player, MessageConfig.getMessages("Messages.Completion.itemsReceived").replace("{reward}", reward[1]));
        });

        List<String> items = BountiesConfig.getItemRewards(id);
        items.forEach(item->{
            String[] reward = item.trim().split("\\s*,\\s*");
            ItemStack itemStack = parseItem(reward[0]);

            if(itemStack != null) {
                if (!Inventories.giveItem(player, itemStack, Integer.parseInt(reward[1]))) {
                    Chat.sendMessage(Sponge.getServer().getConsole(), "&cCouldn't give item(s) to " + player.getName() + " because of a full inventory and/or enderchest.");
                } else {
                    Chat.sendMessage(player, MessageConfig.getMessages("Messages.Completion.itemsReceived").replace("{reward}", reward[2]));
                }
            }
        });

        if(BountiesConfig.getShinyRewards(id)) {
            pokemon.setShiny(true);
            Chat.sendMessage(player, MessageConfig.getMessages("Messages.Completion.shinyReward").replace("{pokemon}", pokemon.getDisplayName()));
        }
    }

    public static void processBountyCompletionRewards(int id, Player player) {
        int completions = PokeHunt.getInstance().getSql().getPlayerCompletions(player,"bounty");
        if (completions == BountiesConfig.getTotalCompletions()) {
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdParser(player, BountiesConfig.getCommandCompletions()));
            Chat.sendMessage(player, MessageConfig.getMessages("Messages.Rank.done").replace("{rank}", MessageConfig.getMessages("Messages.Rank.rankNames.bounty")).replace("{type}", "bounties"));
        } else if (completions < BountiesConfig.getTotalCompletions()) {
            for(String searching : BountiesConfig.getHuntCompletions().trim().split(",")) {
                if (String.valueOf(id).equals(searching)) {
                    int total = BountiesConfig.getTotalCompletions() - completions;
                    Chat.sendMessage(player, MessageConfig.getMessages("Messages.Rank.needMore").replace("{count}", String.valueOf(total)).replace("{rank}", MessageConfig.getMessages("Messages.Rank.rankNames.bounty")));
                }
            }
        }
    }


    public static void processHuntRewards(int id, Player player) {
        List<String> commands = HuntsConfig.getCommandRewards(id);
        commands.forEach(command->{
            String[] reward = command.trim().split("\\s*,\\s*");
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdParser(player, reward[0]));
            Chat.sendMessage(player, MessageConfig.getMessages("Messages.Completion.itemsReceived").replace("{reward}", reward[1]));
        });

        List<String> items = HuntsConfig.getItemRewards(id);
        items.forEach(item->{
            String[] reward = item.trim().split("\\s*,\\s*");
            ItemStack itemStack = parseItem(reward[0]);

            if(itemStack != null) {
                if (!Inventories.giveItem(player, itemStack, Integer.parseInt(reward[1]))) {
                    Chat.sendMessage(Sponge.getServer().getConsole(), "&cCouldn't give item(s) to " + player.getName() + " because of a full inventory and/or enderchest.");
                } else {
                    Chat.sendMessage(player, MessageConfig.getMessages("Messages.Completion.itemsReceived").replace("{reward}", reward[2]));
                }
            }
        });
    }

    public static void processHuntCompletionRewards(int id, Player player) {
        int completions = PokeHunt.getInstance().getSql().getPlayerCompletions(player,"hunt");
        if (completions == HuntsConfig.getTotalCompletions()) {
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdParser(player, HuntsConfig.getCommandCompletions()));
            Chat.sendMessage(player, MessageConfig.getMessages("Messages.Rank.done").replace("{rank}", MessageConfig.getMessages("Messages.Rank.rankNames.hunt")).replace("{type}", "hunts"));
        } else if (completions < HuntsConfig.getTotalCompletions()) {
            for(String searching : HuntsConfig.getHuntCompletions().trim().split(",")) {
                if (String.valueOf(id).equals(searching)) {
                    int total = HuntsConfig.getTotalCompletions() - completions;
                    Chat.sendMessage(player, MessageConfig.getMessages("Messages.Rank.needMore").replace("{count}", String.valueOf(total)).replace("{rank}", MessageConfig.getMessages("Messages.Rank.rankNames.hunt")));
                }
            }
        }
    }



    public static void processBreedingCompRewards(Player player, Pokemon pokemon) {
        List<String> commands = BreedingConfig.getCommandRewards();
        commands.forEach(command->{
            String[] reward = command.trim().split("\\s*,\\s*");
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdParser(player, reward[0]));
            Chat.sendMessage(player, MessageConfig.getMessages("Messages.Events.Completion.rewardsReceived").replace("{reward}", reward[1]));
        });

        List<String> items = BreedingConfig.getItemRewards();
        items.forEach(item->{
            String[] reward = item.trim().split("\\s*,\\s*");
            ItemStack itemStack = parseItem(reward[0]);

            if(itemStack != null) {
                if (!Inventories.giveItem(player, itemStack, Integer.parseInt(reward[1]))) {
                    Chat.sendMessage(Sponge.getServer().getConsole(), "&cCouldn't give item(s) to " + player.getName() + " because of a full inventory and/or enderchest.");
                } else {
                    Chat.sendMessage(player, MessageConfig.getMessages("Messages.Events.Completion.rewardsReceived").replace("{reward}", reward[2]));
                }
            }
        });

        if(BreedingConfig.getShinyRewards() && !BreedingConfig.doRemovePokemon()) {
            pokemon.setShiny(true);
            //pokemon.update(EnumUpdateType.Stats, EnumUpdateType.Nickname, EnumUpdateType.Ability, EnumUpdateType.HeldItem, EnumUpdateType.HP, EnumUpdateType.Moveset, EnumUpdateType.Friendship);
            //pokemon.getStorage().ifPresent(PlayerStorage::sendUpdatedList);
            Chat.sendMessage(player, MessageConfig.getMessages("Messages.Events.Completion.shinyReward").replace("{pokemon}", pokemon.getSpecies().name()));
        }
    }



    private static ItemStack parseItem(String name) {
        String[] parts = name.split(":");
        int damage = 0;
        Optional<ItemType> itemType;

        switch (parts.length) {
            case 1:
                itemType = Sponge.getGame().getRegistry().getType(ItemType.class, name);
                break;
            case 2:
                itemType = Sponge.getGame().getRegistry().getType(ItemType.class, name);
                if (!itemType.isPresent()) {
                    itemType = Sponge.getRegistry().getType(ItemType.class, parts[0]);
                    try {
                        damage = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException exc) {
                        PokeHunt.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.ERROR_PREFIX, "Metadata parsing failed!")));
                    }
                }
                break;
            default:
                PokeHunt.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.ERROR_PREFIX, "There was an issue with parsing an item: ", name)));
                return null;
        }

        if (itemType.isPresent()) {
            ItemStack stack = ItemStack.of(itemType.get(), 1);
            DataContainer container = stack.toContainer().set(DataQuery.of("UnsafeDamage"), damage);

            stack = ItemStack.builder().fromContainer(container).build();
            return stack;
        } else {
            PokeHunt.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.ERROR_PREFIX, "There was an issue with parsing an item: ", name)));
            return null;
        }
    }

    private static String cmdParser(Player player, String command) {
        StringBuilder cmd = new StringBuilder();
        String[] parts = command.split(" ");
        for (String part : parts) {
            if (part.contains("@p")) part = part.replace(part, player.getName());
            cmd.append(" ").append(part);
        }
        return cmd.substring(1);
    }
}