package com.m1zark.pokehunt.util.BreedEvent;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.EnumType;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import lombok.Getter;

import java.util.*;

@Getter
public class BreedManager {
    private UUID player;
    private Pokemon pokemon;
    private static final Random RANDOM = new Random();
    private String hiddenpower;

    public BreedManager(UUID uuid) {
        this.player = uuid;

        pokemon = Pixelmon.pokemonFactory.create(EnumSpecies.randomPoke(false).getBaseSpecies());
        pokemon.setCaughtBall(getRandomPokeBall());
        pokemon.setGrowth(EnumGrowth.getRandomGrowth());
        pokemon.setNature(EnumNature.getRandomNature());
        pokemon.setAbility(getRandomAbility());
        pokemon.getMoveset().set(0, getRandomMove());
        pokemon.setGender(setGender());
        hiddenpower = getRandomHiddenPower();
    }

    private String getRandomHiddenPower() {
        String hiddenpower = EnumType.getAllTypes().get(RANDOM.nextInt(18)).getName();
        while(hiddenpower.equalsIgnoreCase("Mystery") || hiddenpower.equalsIgnoreCase("Normal")) {
            hiddenpower = EnumType.getAllTypes().get(RANDOM.nextInt(18)).getName();
        }

        return hiddenpower;
    }

    private Gender setGender() {
        int malePerecent = (int) this.pokemon.getBaseStats().getMalePercent();

        return malePerecent==0 ? Gender.Female : malePerecent==100 ? Gender.Male : malePerecent < 0 ? Gender.None : (RANDOM.nextInt(1) == 0) ? Gender.Female : Gender.Male;
    }

    private String getRandomAbility() {
        String[] Abilities = this.pokemon.getBaseStats().getAbilitiesArray();

        int slot = RANDOM.nextInt(1);
        while(Abilities[slot] == null) slot = RANDOM.nextInt(1);

        return Abilities[slot];
    }

    private Attack getRandomMove() {
        List<Attack> eggMoves = this.pokemon.getBaseStats().getEggMoves();
        return new Attack(eggMoves.get(RANDOM.nextInt(eggMoves.size()-1)).getActualMove());
    }

    private EnumPokeballs getRandomPokeBall(){
        ArrayList<EnumPokeballs> pokeball = new ArrayList<>();

        for(int i = 0; i <= EnumPokeballs.values().length;){
            pokeball.add(EnumPokeballs.getFromIndex(i));
            i++;
        }

        pokeball.removeIf(name -> name.equals(EnumPokeballs.ParkBall));
        pokeball.removeIf(name -> name.equals(EnumPokeballs.CherishBall));
        pokeball.removeIf(name -> name.equals(EnumPokeballs.GSBall));
        pokeball.removeIf(name -> name.equals(EnumPokeballs.BeastBall));
        pokeball.removeIf(name -> name.equals(EnumPokeballs.MasterBall));

        return pokeball.get(RANDOM.nextInt(pokeball.size()));
    }
}
