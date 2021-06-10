package com.m1zark.pokehunt.util;

import com.m1zark.pokehunt.config.HuntsConfig;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo;
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.spawning.util.SetLoader;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumMega;
import com.pixelmonmod.pixelmon.enums.forms.EnumSpecial;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter
public class HuntsManager {
    private static final Random RANDOM = new Random();

    private String spawnRate;
    private String nature;
    private String gender;
    private String growth;
    private String name;
    private EnumSpecies pokemon;
    private int huntID;
    private int form;
    private int flavorText;

    private boolean isLegendary;
    private boolean isUltraBeast;
    private boolean isMega;
    private boolean isBoss;
    private boolean needsGender;
    private boolean needsNature;
    private boolean needsSize;
    private int amountNeeded;

    public HuntsManager(int id) {
        this.huntID = id;
        setDetails();

        this.spawnRate = HuntsConfig.getRarity(this.huntID);
        this.flavorText = HuntsConfig.getRandomText(this.huntID);

        if(this.isLegendary || this.isUltraBeast) {
            List<String> legends = new ArrayList<>();
            if(this.isLegendary) legends.addAll(EnumSpecies.legendaries);
            if(this.isUltraBeast) legends.addAll(EnumSpecies.ultrabeasts);

            this.pokemon = EnumSpecies.getFromNameAnyCase(legends.get(RANDOM.nextInt(legends.size())));

            while(HuntsConfig.getBlacklist().contains(this.pokemon.name)) {
                this.pokemon = EnumSpecies.getFromNameAnyCase(legends.get(RANDOM.nextInt(legends.size())));
            }
        } else if(this.isMega) {
            this.pokemon = EnumSpecies.randomPoke(false);
            while(!this.pokemon.hasMega() || HuntsConfig.getBlacklist().contains(this.pokemon.name)) {
                this.pokemon = EnumSpecies.randomPoke(false);
            }
        } else {
            this.pokemon = EnumSpecies.randomPoke(false);
            if(!this.isBoss) {
                while (checkRarity(this.pokemon.name) || HuntsConfig.getBlacklist().contains(this.pokemon.name)) {
                    this.pokemon = EnumSpecies.randomPoke(false);
                }
            }
        }

        this.form = this.isMega ? (this.pokemon.getPossibleForms(true).contains(EnumMega.MegaX) ? RandomHelper.getRandomNumberBetween(1, 2) : 1) : HuntsManager.getRandomForm(this.pokemon.name);
        this.name = this.isBoss ? "Boss" : this.pokemon.name();
        this.nature = (this.needsNature) ? EnumNature.getRandomNature().getLocalizedName() : null;
        this.growth = (this.needsSize) ? EnumGrowth.getRandomGrowth().getLocalizedName() : null;
        this.gender = (this.needsGender) ? this.setGender() : null;
    }

    private boolean checkRarity(String name) {
        String[] spawn = this.spawnRate.trim().split("\\s*-\\s*");
        int rarity = -1;

        for (Object set : SetLoader.getAllSets()) {
            for (SpawnInfo info : ((SpawnSet) set).spawnInfos) {
                if (!(info instanceof SpawnInfoPokemon) || !((SpawnInfoPokemon)info).getPokemonSpec().name.equalsIgnoreCase(name)) continue;
                rarity = (int) info.rarity;
            }
        }

        return !PokeUtils.between(rarity, Integer.parseInt(spawn[0]), Integer.parseInt(spawn[1])) || rarity <= 0;
    }

    private static int getRandomForm(String pokemon) {
        List<IEnumForm> forms = EnumSpecies.getFromNameAnyCase(pokemon).getPossibleForms(false);
        if (forms.size() > 0) {
            int random = new Random().nextInt(forms.size());
            IEnumForm form = forms.get(random);
            while (Arrays.toString(EnumSpecial.values()).contains(form.getLocalizedName()) || form.getForm() > 0) {
                random = new Random().nextInt(forms.size());
                form = forms.get(random);
            }
            return random;
        }
        return 0;
    }

    private String setGender() {
        int malePercent = (int) EnumSpecies.getFromNameAnyCase(this.name).getBaseStats().getMalePercent();
        return malePercent==0 ? "Female" : malePercent==100 ? "Male" : malePercent < 0 ? "Genderless" : (RandomHelper.getRandomNumberBetween(0, 1) == 0) ? "Female" : "Male";
    }

    private void setDetails() {
        this.isLegendary = HuntsConfig.getOptions(this.huntID, "Legendary");
        this.isUltraBeast = HuntsConfig.getOptions(this.huntID, "UltraBeasts");
        this.isMega = HuntsConfig.getOptions(this.huntID, "Mega");
        this.isBoss = HuntsConfig.getOptions(this.huntID, "Boss");

        this.needsGender = HuntsConfig.getOptions(this.huntID, "Gender");
        this.needsNature = HuntsConfig.getOptions(this.huntID, "Nature");
        this.needsSize = HuntsConfig.getOptions(this.huntID, "Size");

        this.amountNeeded = HuntsConfig.getAmountNeeded(this.huntID);
    }
}
