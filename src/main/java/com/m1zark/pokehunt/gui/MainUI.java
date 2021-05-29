package com.m1zark.pokehunt.gui;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.GUI.Icon;
import com.m1zark.m1utilities.api.GUI.InventoryManager;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.util.BreedEvent.PlayerEntry;
import com.m1zark.pokehunt.util.PokeUtils;
import com.m1zark.pokehunt.util.Pokemon;
import com.m1zark.pokehunt.config.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MainUI extends InventoryManager {
    private Player player;
    private static Map<String, String> winners = new ConcurrentHashMap<>();

    public MainUI(Player player) {
        super(player, 5, Text.of(Chat.embedColours(MessageConfig.getMessages("Messages.UI.guiTitle"))));
        this.player = player;

        BreedingConfig.getCompWinners().forEach(uuid -> {
            String[] parts = uuid.split(":");
            winners.put(parts[0], parts[1]);
        });

        this.setupDesign();
    }

    private void setupDesign() {
        for(int y = 0, x = 0, index = 0; y < 5 && index < 45; ++index) {
            if (x == 9) {
                x = 0;
                ++y;
            }

            this.addIcon(this.borderIcon(x + (9 * y), index % 2 == 0 ? DyeColors.CYAN : DyeColors.RED, ""));
            ++x;
        }

        for(int i = 1, s = 10; i <= BountiesConfig.getNumberBounties(); i++) {
            Pokemon pokemon = new Pokemon(i, "bounties");

            if(PokeHunt.getInstance().getSql().checkCompleted(this.player.getUniqueId(), i, "bounty")) {
                this.addIcon(this.completedIcon(s,"bounty",pokemon.getTimeRemaining()));
            } else if (!Config.getEnabled("enableBounties")) {
                this.addIcon(this.enabledIcon(s));
            } else if (pokemon.checkHasExpired()) {
                this.addIcon(this.expiredIcon(s,"bounty"));
            } else {
                this.addIcon(new Icon(s, pokemon.getSprite(this.player)));
            }

            s++;
        }

        this.addIcon(this.infoIcon());

        for(int i = 1, s = 28; i <= HuntsConfig.getNumberHunts(); i++) {
            Pokemon pokemon = new Pokemon(i, "hunts");

            if(PokeHunt.getInstance().getSql().checkCompleted(this.player.getUniqueId(), i, "hunt")) {
                this.addIcon(this.completedIcon(s,"hunt",pokemon.getTimeRemaining()));
            } else if (!Config.getEnabled("enableHunts")) {
                this.addIcon(this.enabledIcon(s));
            } else if (pokemon.checkHasExpired()) {
                this.addIcon(this.expiredIcon(s,"hunt"));
            } else {
                this.addIcon(new Icon(s, pokemon.getSprite(this.player)));
            }

            s++;
        }

        this.addIcon(this.eventInfoIcon());
    }

    private Icon borderIcon(int slot, DyeColor color, String name) {
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours(name)))
                .add(Keys.DYE_COLOR, color)
                .build());
    }

    private Icon completedIcon(int slot, String name, Date expires) {
        Time time = new Time(expires.toInstant().toEpochMilli());

        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&7You have completed this " + name + " already.")));
        itemLore.add(Text.of(Chat.embedColours("&7New " + name + " in &d" + time.toString("%1$dd %2$dh %3$dm") + "&7.")));

        GameProfile profile = GameProfile.of(UUID.randomUUID());
        profile.addProperty(ProfileProperty.of("textures", ("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNlNjg3NjhmNGZhYjgxYzk0ZGY3MzVlMjA1YzNiNDVlYzQ1YTY3YjU1OGYzODg0NDc5YTYyZGQzZjRiZGJmOCJ9fX0=")));
        ItemStack skull = ItemStack.builder()
                .itemType(ItemTypes.SKULL)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&c" + PokeUtils.capitalize(name) + " Completed!")))
                .add(Keys.ITEM_LORE, itemLore)
                .add(Keys.SKULL_TYPE, SkullTypes.PLAYER)
                .add(Keys.REPRESENTED_PLAYER, profile).build();

        return new Icon(slot, skull);
    }

    private Icon enabledIcon(int slot) {
        ItemStack itemStack = ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:pixelmon_sprite").orElse(ItemTypes.BARRIER))
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&cCurrently Disabled!")))
                .build();

        return new Icon(slot, ItemStack.builder().fromContainer(itemStack.toContainer().set(DataQuery.of("UnsafeData","SpriteName"), "pixelmon:sprites/eggs/egg1")).build());
    }

    private Icon expiredIcon(int slot, String name) {
        ItemStack itemStack = ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:pixelmon_sprite").orElse(ItemTypes.BARRIER))
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GRAY, PokeUtils.capitalize(name) + " Expired!"))
                .build();

        return new Icon(slot, ItemStack.builder().fromContainer(itemStack.toContainer().set(DataQuery.of("UnsafeData","SpriteName"), "pixelmon:sprites/eggs/egg1")).build());
    }

    private Icon infoIcon() {
        ArrayList<Text> itemLore = new ArrayList<>();
        int bounty_completions = PokeHunt.getInstance().getSql().getPlayerCompletions(this.player,"bounty");
        int hunt_completions = PokeHunt.getInstance().getSql().getPlayerCompletions(this.player,"hunt");

        Optional<ItemType> sprite = Sponge.getRegistry().getType(ItemType.class, "pixelmon:pokemail_bubble");
        ItemStack Item = ItemStack.builder().itemType(sprite.get()).add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours(MessageConfig.getMessages("Messages.UI.infoTitle")))).build();

        String bountyInfo = MessageConfig.getMessages("Messages.UI.info").replace("{count}", String.valueOf(BountiesConfig.getTotalCompletions())).replace("{rank}", MessageConfig.getMessages("Messages.Rank.rankNames.bounty")).replace("{type}", "bounties");
        String huntInfo = MessageConfig.getMessages("Messages.UI.info").replace("{count}", String.valueOf(HuntsConfig.getTotalCompletions())).replace("{rank}", MessageConfig.getMessages("Messages.Rank.rankNames.hunt")).replace("{type}", "hunts");

        String[] newInfo = bountyInfo.split("\n");
        for(String s:newInfo) itemLore.add(Text.of(Chat.embedColours(s)));
        itemLore.add(Text.of(Chat.embedColours("&7Completions: " + (bounty_completions >= BountiesConfig.getTotalCompletions() ? "&cYou have already received the rank." : "&b" + bounty_completions))));

        itemLore.add(Text.of(""));

        newInfo = huntInfo.split("\n");
        for(String s:newInfo) itemLore.add(Text.of(Chat.embedColours(s)));
        itemLore.add(Text.of(Chat.embedColours("&7Completions: " + (hunt_completions >= HuntsConfig.getTotalCompletions() ? "&cYou have already received the rank." : "&b" + hunt_completions))));

        Item.offer(Keys.ITEM_LORE, itemLore);
        return new Icon(16, Item);
    }

    private Icon eventInfoIcon() {
        ArrayList<Text> itemLore = new ArrayList<>();

        if(BreedingConfig.getCompEnabled()) {
            Time breedingTime = new Time(BreedingConfig.getEndDate());
            if(!breedingTime.toString("%1$dd %2$dh %3$dm").equalsIgnoreCase("Expired")) {
                itemLore.add(Text.of(Chat.embedColours("&e&lBreeding Competition")));
                itemLore.add(Text.of(Chat.embedColours("&e&lTime Remaining: &f" + breedingTime.toString("%1$dd %2$dh %3$dm"))));
                itemLore.add(Text.of(Chat.embedColours("&e&lRemaining Winners: &f" + (BreedingConfig.getMaxWinners() - BreedingConfig.getCompWinners().size()))));
                itemLore.add(Text.of(Chat.embedColours("&e&lRewards")));
                BreedingConfig.getRewards().forEach(reward -> itemLore.add(Text.of(Chat.embedColours("&f - " + reward))));
                itemLore.add(Text.of(Chat.embedColours("")));

                String[] newInfo = PokeUtils.insertLinebreaks(BreedingConfig.getInfoMessage(), 40).split("\n");
                for (String s : newInfo) itemLore.add(Text.of(Chat.embedColours("&7" + s)));

                PlayerEntry playerEntry = BreedingConfig.getPlayerData(this.player.getUniqueId());
                if (playerEntry != null) {
                    itemLore.add(Text.of(Chat.embedColours("")));

                    if (!winners.containsKey(player.getUniqueId().toString())) {
                        itemLore.add(Text.of(Chat.embedColours("&ePok\u00E9mon: &f" + playerEntry.getPokemon_name())));
                        if (BreedingConfig.getOptions("gender")) itemLore.add(Text.of(Chat.embedColours("&eGender: &f" + playerEntry.getPokemon_gender())));
                        if (BreedingConfig.getOptions("ability")) itemLore.add(Text.of(Chat.embedColours("&eAbility: &f" + playerEntry.getPokemon_ability())));
                        if (BreedingConfig.getOptions("nature")) itemLore.add(Text.of(Chat.embedColours("&eNature: &f" + playerEntry.getPokemon_nature())));
                        if (BreedingConfig.getOptions("size")) itemLore.add(Text.of(Chat.embedColours("&eSize: &f" + playerEntry.getPokemon_growth())));
                        if (BreedingConfig.getOptions("pokeball")) itemLore.add(Text.of(Chat.embedColours("&ePok\u00E9ball: &f" + playerEntry.getPokemon_pokeball())));
                        if (BreedingConfig.getOptions("eggmove")) itemLore.add(Text.of(Chat.embedColours("&eRequired Move: &f" + playerEntry.getPokemon_eggMove())));
                        if (BreedingConfig.getOptions("hiddenpower")) itemLore.add(Text.of(Chat.embedColours("&eHidden Power: &f" + playerEntry.getPokemon_hiddenpower())));
                    } else {
                        itemLore.add(Text.of(Chat.embedColours("&bYou have already completed this breeding competition.")));
                    }
                } else {
                    itemLore.add(Text.of(Chat.embedColours("")));
                    itemLore.add(Text.of(Chat.embedColours("&7You are not currently entered in this competition.")));
                }
            } else {
                itemLore.add(Text.of(Chat.embedColours("")));
                itemLore.add(Text.of(Chat.embedColours("&7This breeding competition has ended.")));
            }
        } else {
            itemLore.add(Text.of(Chat.embedColours("")));
            itemLore.add(Text.of(Chat.embedColours("&eThere are no events currently active.")));
        }

        ItemStack Item = ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, BreedingConfig.getCompEnabled() ? "pixelmon:pixelmon_sprite" : "pixelmon:rare_candy").get())
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&6Ongoing PokeHunts Event")))
                .add(Keys.ITEM_LORE, itemLore)
                .build();

        return new Icon(34, ItemStack.builder().fromContainer(Item.toContainer().set(DataQuery.of("UnsafeData","SpriteName"),"pixelmon:sprites/eggs/egg1")).build());
    }
}
