package com.m1zark.pokehunt.util;

import com.m1zark.pokehunt.config.BountiesConfig;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo;
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.spawning.util.SetLoader;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumNoForm;
import com.pixelmonmod.pixelmon.enums.forms.EnumSpecial;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import lombok.Getter;

import java.util.*;

@Getter
public class BountiesManager {
    private String spawnRate;
    private String ability;
    private String nature;
    private String gender;
    private String growth;
    private String name;
    private String pokeball;
    private int bountyID;
    private int form;
    private int flavorText;

    private boolean needsAbility;
    private boolean needsHA;
    private boolean needsGender;
    private boolean needsNature;
    private boolean needsBall;
    private boolean needsSize;

    public BountiesManager(int id){
        this.bountyID = id;

        setDetails();
        this.spawnRate = BountiesConfig.getRarity(this.bountyID);
        this.flavorText = BountiesConfig.getRandomText(this.bountyID);
        this.name = EnumSpecies.randomPoke(false).name;

        while(checkPokemon(this.name) || BountiesConfig.getBlacklist().contains(this.name)) {
            this.name = EnumSpecies.randomPoke(false).name;
        }

        if(!this.needsAbility && !this.needsHA) {
            this.ability = null;
        }else{
            if(this.needsAbility && !this.needsHA) {
                this.ability = getRandomAbility(this.name);
            } else {
                this.ability = getHiddenAbility(this.name);
            }
        }

        this.form = BountiesManager.getRandomForm(this.name);
        this.nature = (this.needsNature) ? EnumNature.getRandomNature().getLocalizedName() : null;
        this.gender = (this.needsGender) ? this.setGender() : null;
        this.growth = (this.needsSize) ? EnumGrowth.getRandomGrowth().getLocalizedName(): null;
        this.pokeball = (this.needsBall) ? getRandomPokeBall() : null;
    }

    private String setGender() {
        int malePercent = (int) EnumSpecies.getFromNameAnyCase(this.name).getBaseStats().getMalePercent();
        return malePercent==0 ? "Female" : malePercent==100 ? "Male" : malePercent < 0 ? "Genderless" : (RandomHelper.getRandomNumberBetween(0, 1) == 0) ? "Female" : "Male";
    }

    private static String getRandomAbility(String pokemon) {
        String[] Abilities = EnumSpecies.getFromNameAnyCase(pokemon).getBaseStats().getAbilitiesArray();

        int slot = RandomHelper.getRandomNumberBetween(0, 1);
        while(Abilities[slot] == null) {
            slot = RandomHelper.getRandomNumberBetween(0, 1);
        }

        return Abilities[slot];
    }

    private static String getHiddenAbility(String pokemon){
        String[] abilities = EnumSpecies.getFromNameAnyCase(pokemon).getBaseStats().getAbilitiesArray();
        while(abilities[2] == null) abilities = EnumSpecies.getFromNameAnyCase(pokemon).getBaseStats().getAbilitiesArray();

        return abilities[2];
    }

    private static String getRandomPokeBall(){
        ArrayList<String> pokeball = new ArrayList<>();

        for(int i = 0; i <= EnumPokeballs.class.getEnumConstants().length;){
            pokeball.add(EnumPokeballs.getFromIndex(i).toString());
            i++;
        }

        pokeball.removeIf(name -> name.equals("MasterBall"));
        pokeball.removeIf(name -> name.equals("ParkBall"));
        pokeball.removeIf(name -> name.equals("CherishBall"));
        pokeball.removeIf(name -> name.equals("GSBall"));
        pokeball.removeIf(name -> name.equals("BeastBall"));

        return pokeball.get(new Random().nextInt(pokeball.size()));
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

    private boolean checkPokemon(String name){
        String[] abilities = EnumSpecies.getFromNameAnyCase(name).getBaseStats().getAbilitiesArray();
        String[] spawn = this.spawnRate.trim().split("\\s*-\\s*");
        int rarity = -1;

        for (Object set : SetLoader.getAllSets()) {
            for (SpawnInfo info : ((SpawnSet) set).spawnInfos) {
                if (!(info instanceof SpawnInfoPokemon) || !((SpawnInfoPokemon)info).getPokemonSpec().name.equalsIgnoreCase(name)) continue;
                rarity = (int) info.rarity;
            }
        }

        if(!PokeUtils.between(rarity, Integer.parseInt(spawn[0]), Integer.parseInt(spawn[1])) || rarity <= 0) return true;

        if(this.needsHA) return abilities[2] == null;

        return false;
    }

    private void setDetails(){
        this.needsAbility = BountiesConfig.getOptions(this.bountyID, "Ability");
        this.needsHA = BountiesConfig.getOptions(this.bountyID, "HA");
        this.needsGender = BountiesConfig.getOptions(this.bountyID, "Gender");
        this.needsNature = BountiesConfig.getOptions(this.bountyID, "Nature");
        this.needsBall = BountiesConfig.getOptions(this.bountyID, "Ball");
        this.needsSize = BountiesConfig.getOptions(this.bountyID, "Size");
    }
}
