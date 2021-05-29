package com.m1zark.pokehunt.util;

import com.google.common.base.Strings;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.config.BountiesConfig;
import com.m1zark.pokehunt.config.HuntsConfig;
import com.pixelmonmod.pixelmon.client.gui.GuiResources;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumMega;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.*;

public class Pokemon {
    private int ID;
    private String type;
    private int dexNumber;
    private int form;
    private int flavorText;
    private Date timeRemaining;
    private String name;
    private String ability;
    private String nature;
    private String gender;
    private String growth;
    private String pokeball;

    private boolean needsGender;
    private boolean needsNature;
    private boolean needsSize;
    private boolean needsAbility;
    private boolean needsHA;
    private boolean needsBall;

    private int amountNeeded;
    private boolean isLegendary;
    private boolean isMega;
    private boolean isBoss;

    public Pokemon(int ID, String type){
        this.ID = ID;
        this.type = type;

        setDetails();

        HashMap<String, String> info;
        if(this.type.equals("bounties")) {
            info = PokeHunt.getInstance().getSql().loadBountiesData(this.ID);
        } else {
            info = PokeHunt.getInstance().getSql().loadHuntsData(this.ID);
        }

        this.timeRemaining = Date.from(Instant.parse(info.get("date")));
        this.name = info.get("name");

        if(this.type.equals("bounties")) {
            this.ability = info.get("ability");
            this.pokeball = info.get("pokeball");
        }

        this.nature = info.get("nature");
        this.gender = info.get("gender");
        this.growth = info.get("growth");

        this.flavorText = Integer.parseInt(info.get("lore"));

        this.dexNumber = !this.isBoss ? EnumSpecies.getPokedexNumber(this.name) : 132;
        this.form = Integer.parseInt(info.get("form"));
    }

    private static String updatePokemonName(String name){
        if(name.equalsIgnoreCase("MrMime")) return "Mr. Mime";
        else if(name.equalsIgnoreCase("MimeJr")) return "Mime Jr.";
        else if(name.equalsIgnoreCase("Nidoranfemale")) return "Nidoran&d\u2640&r";
        else if(name.equalsIgnoreCase("Nidoranmale")) return "Nidoran&b\u2642&r";
        else if(name.equalsIgnoreCase("Farfetchd")) return "Farfetch'd";
        else if(name.contains("Alolan")){
            return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(name.replaceAll("\\d+", "")), " ");
        }

        return name;
    }

    public boolean checkHasExpired() {
        return this.timeRemaining.before(Date.from(Instant.now()));
    }

    public Date getTimeRemaining() { return this.timeRemaining; }

    private void setDetails(){
        if(this.type.equals("bounties")) {
            this.needsAbility = BountiesConfig.getOptions(this.ID, "Ability");
            this.needsHA = BountiesConfig.getOptions(this.ID, "HA");
            this.needsGender = BountiesConfig.getOptions(this.ID, "Gender");
            this.needsNature = BountiesConfig.getOptions(this.ID, "Nature");
            this.needsBall = BountiesConfig.getOptions(this.ID, "Ball");
            this.needsSize = BountiesConfig.getOptions(this.ID, "Size");
        } else {
            this.isLegendary = HuntsConfig.getOptions(this.ID, "Legendary");
            this.isMega = HuntsConfig.getOptions(this.ID, "Mega");
            this.isBoss = HuntsConfig.getOptions(this.ID, "Boss");
            this.needsGender = HuntsConfig.getOptions(this.ID, "Gender");
            this.needsNature = HuntsConfig.getOptions(this.ID, "Nature");
            this.needsSize = HuntsConfig.getOptions(this.ID, "Size");
            this.amountNeeded = HuntsConfig.getAmountNeeded(this.ID);
        }
    }

    public ItemStack getSprite(Player player) {
        Optional<ItemType> sprite = Sponge.getRegistry().getType(ItemType.class, "pixelmon:pixelmon_sprite");
        ItemStack Item = ItemStack.builder().itemType(sprite.get()).build();

        ItemStack item = this.setPicture(Item);
        this.setItemData(item, player);
        return item;
    }

    private ItemStack setPicture(ItemStack item) {
        Gender gen = Gender.None;
        if(this.needsGender) gen = this.gender.equalsIgnoreCase("Male") ? Gender.Male : Gender.Female;
        String spriteData =  this.isMega || this.isBoss ? "pixelmon:" + GuiResources.getSpritePath(EnumSpecies.getFromDex(this.dexNumber), this.form, gen, "", false) : "pixelmon:" + GuiResources.getSpritePath(EnumSpecies.getFromDex(this.dexNumber), EnumSpecies.getFromDex(this.dexNumber).getPossibleForms(false).get(this.form).getForm(), gen, "", false);

        return ItemStack.builder().fromContainer(item.toContainer().set(DataQuery.of("UnsafeData","SpriteName"), spriteData)).build();
    }

    private void setItemData(ItemStack item, Player player) {
        Time time = new Time(this.timeRemaining.toInstant().toEpochMilli());
        String title = this.type.equals("bounties") ? BountiesConfig.getName(this.ID) : HuntsConfig.getName(this.ID);
        String flavorText = this.type.equals("bounties") ? BountiesConfig.getFlavorText(this.ID, this.flavorText) : HuntsConfig.getFlavorText(this.ID, this.flavorText);
        String pokemon = this.isLegendary ? "Legendary " : this.isMega ? "Mega " : "";

        item.offer(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&l&6" + title + " Details")));

        String megaForm = "";
        if (this.isMega && EnumSpecies.getFromNameAnyCase(this.name).getPossibleForms(true).contains(EnumMega.MegaX)) {
            megaForm = this.form == 1 ? "-X" : "-Y";
        }

        ArrayList<Text> itemLore = new ArrayList<>();
        if(!Strings.isNullOrEmpty(flavorText)) {
            String info = PokeUtils.insertLinebreaks(flavorText.replace("{pokemon}", this.name + megaForm), 40);
            String[] newInfo = info.split("\n");
            for (String s : newInfo) itemLore.add(Text.of(Chat.embedColours("&7" + s)));
            itemLore.add(Text.of(Chat.embedColours("")));
        }

        itemLore.add(Text.of(Chat.embedColours("&e&lPok\u00E9mon:&r " + (this.isBoss ? "Any Boss Pok\u00E9mon" : pokemon + updatePokemonName(this.name) + megaForm))));
        itemLore.add(Text.of(Chat.embedColours("&e&lTime Remaining:&r " + time.toString("%1$dd %2$dh %3$dm"))));
        if(this.type.equals("hunts")) itemLore.add(Text.of(Chat.embedColours("&e&lProgress:&r " + PokeHunt.getInstance().getSql().getPlayerHuntCount(player, this.ID))));
        itemLore.add(Text.of(Chat.embedColours("")));
        itemLore.add(Text.of(Chat.embedColours("&e&lRequirements")));
        if(this.type.equals("hunts")) itemLore.add(Text.of(Chat.embedColours("&f - &aNeed to Defeat: &f" + this.amountNeeded)));
        if(this.needsNature) itemLore.add(Text.of(Chat.embedColours("&f - &aNature: &f" + this.nature)));
        if(this.needsSize) itemLore.add(Text.of(Chat.embedColours("&f - &aSize: &f" + this.growth)));
        if(this.needsGender) itemLore.add(Text.of(Chat.embedColours("&f - &aGender: &f" + this.gender)));
        if(this.type.equals("bounties") && this.needsAbility || this.needsHA) itemLore.add(Text.of(Chat.embedColours("&f - &aAbility: &f" + this.ability)));
        if(this.type.equals("bounties") && this.needsBall) itemLore.add(Text.of(Chat.embedColours("&f - &aCapture Ball: &f" + this.pokeball)));
        itemLore.add(Text.of(Chat.embedColours("")));

        List<String> rewards = this.type.equals("bounties") ? BountiesConfig.getRewards(this.ID) : HuntsConfig.getRewards(this.ID);
        itemLore.add(Text.of(Chat.embedColours("&e&lRewards")));
        rewards.forEach(reward -> itemLore.add(Text.of(Chat.embedColours("&f - " + reward))));

        item.offer(Keys.ITEM_LORE, itemLore);
    }
}
