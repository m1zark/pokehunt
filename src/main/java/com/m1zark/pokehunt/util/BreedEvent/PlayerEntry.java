package com.m1zark.pokehunt.util.BreedEvent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PlayerEntry {
    private final UUID player;
    private final String pokemon_name;
    private final String pokemon_gender;
    private final String pokemon_nature;
    private final String pokemon_growth;
    private final String pokemon_pokeball;
    private final String pokemon_ability;
    private final String pokemon_eggMove;
    private final String pokemon_hiddenpower;
}
