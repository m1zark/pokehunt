package com.m1zark.pokehunt.listeners;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.util.PokeUtils;
import com.m1zark.pokehunt.config.*;
import com.m1zark.pokehunt.util.logs.Log;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
import com.pixelmonmod.pixelmon.api.events.BreedEvent;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumMega;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PixelmonListener {
    @SubscribeEvent
    public void onPokemonCapture(CaptureEvent.SuccessfulCapture event) {
        Player player = (Player) event.player;
        Pokemon pokemon = event.getPokemon().getPokemonData();

        if(player.hasPermission("pokehunt.player.base")) {
            if (Config.getEnabled("enableBounties")) {
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    for (int i = 1; i <= BountiesConfig.getNumberBounties(); i++) {
                        if (!PokeHunt.getInstance().getSql().checkCompleted(player.getUniqueId(), i, "bounty")) {
                            HashMap<String, String> info = PokeHunt.getInstance().getSql().loadBountiesData(i);

                            if (this.checkBountyPokemon(info, pokemon) && Date.from(Instant.parse(info.get("date"))).after(Date.from(Instant.now()))) {
                                this.addLog(player, i, "Bounty", Date.from(Instant.parse(info.get("date"))));

                                Chat.sendMessage(player, MessageConfig.getMessages("Messages.Completion.redeemPlayer").replace("{hunt}", BountiesConfig.getName(i)));

                                if (MessageConfig.getServerAnnounce()) {
                                    Chat.sendBroadcastMessage(player, MessageConfig.getMessages("Messages.Completion.redeemBroadcast").replace("{player}", player.getName()).replace("{hunt}", BountiesConfig.getName(i)));
                                }

                                PokeHunt.getInstance().getSql().updatePlayerData(player, i, "bounty");
                                PokeUtils.processBountyRewards(i, player, pokemon);
                                PokeUtils.processBountyCompletionRewards(i, player);
                            }
                        }
                    }
                }).delay(2, TimeUnit.SECONDS).submit(PokeHunt.getInstance());
            }
        } else {
            Chat.sendMessage(player, MessageConfig.getMessages("Messages.Misc.missingPermission"));
        }
    }

    private boolean checkBountyPokemon(HashMap<String, String> info, Pokemon pokemon) {
        String gender = (pokemon.getBaseStats().getMalePercent() == -1) ? "Genderless" : pokemon.getGender().name();

        if (this.checkExpired(Date.from(Instant.parse(info.get("date"))))) return false;
        if (!info.get("name").equalsIgnoreCase(pokemon.getSpecies().name())) return false;

        if (pokemon.getSpecies().getPossibleForms(false).get(Integer.parseInt(info.get("form"))).getForm() != pokemon.getForm()) {
            return false;
        }

        if (info.get("ability") != null)
            if (!info.get("ability").equalsIgnoreCase(pokemon.getAbility().getName())) return false;
        if (info.get("nature") != null)
            if (!info.get("nature").equalsIgnoreCase(pokemon.getNature().toString())) return false;
        if (info.get("gender") != null)
            if (!info.get("gender").equalsIgnoreCase(gender)) return false;
        if (info.get("growth") != null)
            if (!info.get("growth").equalsIgnoreCase(pokemon.getGrowth().name())) return false;
        if (info.get("pokeball") != null)
            if (!info.get("pokeball").equalsIgnoreCase(pokemon.getCaughtBall().name())) return false;

        return true;
    }


    @SubscribeEvent
    public void onBattleEnd(BeatWildPixelmonEvent event) {
        Player player = (Player) event.player;

        if(player.hasPermission("pokehunt.player.base")) {
            for (PixelmonWrapper wrapper : event.wpp.allPokemon) {
                EntityPixelmon pokemon = wrapper.entity;

                if (Config.getEnabled("enableHunts")) {
                    if (pokemon.getPokemonData().getOwnerPlayer() == null || pokemon.getPokemonData().getOriginalTrainer() == null) {
                        for (int i = 1; i <= HuntsConfig.getNumberHunts(); i++) {
                            if (!PokeHunt.getInstance().getSql().checkCompleted(player.getUniqueId(), i, "hunt")) {
                                HashMap<String, String> info = PokeHunt.getInstance().getSql().loadHuntsData(i);

                                if (this.checkHuntPokemon(info, i, pokemon) && Date.from(Instant.parse(info.get("date"))).after(Date.from(Instant.now()))) {
                                    PokeHunt.getInstance().getSql().updatePlayerData(player, i, "hunt");

                                    if (PokeHunt.getInstance().getSql().getPlayerHuntCount(player, i) == HuntsConfig.getAmountNeeded(i)) {
                                        Chat.sendMessage(player, MessageConfig.getMessages("Messages.Completion.redeemPlayer").replace("{hunt}", HuntsConfig.getName(i)));

                                        if (MessageConfig.getServerAnnounce()) {
                                            Chat.sendBroadcastMessage(player, MessageConfig.getMessages("Messages.Completion.redeemBroadcast").replace("{player}", player.getName()).replace("{hunt}", HuntsConfig.getName(i)));
                                        }

                                        this.addLog(player, i, "Hunt", Date.from(Instant.parse(info.get("date"))));
                                        PokeUtils.processHuntRewards(i, player);
                                        PokeUtils.processHuntCompletionRewards(i, player);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Chat.sendMessage(player, MessageConfig.getMessages("Messages.Misc.missingPermission"));
        }
    }

    private boolean checkHuntPokemon(HashMap<String, String> info, int id, EntityPixelmon pokemon) {
        String gender = (pokemon.getBaseStats().getMalePercent() == -1) ? "Genderless" : pokemon.getPokemonData().getGender().name();

        if (this.checkExpired(Date.from(Instant.parse(info.get("date"))))) return false;

        if(!HuntsConfig.getOptions(id, "Boss")) if(!info.get("name").equalsIgnoreCase(pokemon.getSpecies().name())) return false;

        if (info.get("nature") != null)
            if (!info.get("nature").equalsIgnoreCase(pokemon.getPokemonData().getNature().name())) return false;
        if (info.get("gender") != null)
            if (!info.get("gender").equalsIgnoreCase(gender)) return false;
        if (info.get("growth") != null)
            if (!info.get("growth").equalsIgnoreCase(pokemon.getPokemonData().getGrowth().name())) return false;

        if(HuntsConfig.getOptions(id, "Legendary")) {
            if (!EnumSpecies.legendaries.contains(pokemon.getSpecies().name()) || pokemon.getFormEnum() == EnumMega.Mega || pokemon.isBossPokemon())
                return false;
        }

        if(HuntsConfig.getOptions(id, "UltraBeasts")) {
            if (!EnumSpecies.ultrabeasts.contains(pokemon.getSpecies().name()) || pokemon.getFormEnum() == EnumMega.Mega || pokemon.isBossPokemon())
                return false;
        }

        if(HuntsConfig.getOptions(id, "Mega")) {
            if(pokemon.getFormEnum() != EnumMega.Mega) return false;
            if(Integer.parseInt(info.get("form")) != pokemon.getPokemonData().getForm()) {
                return false;
            }
        }

        if(HuntsConfig.getOptions(id, "Boss")) {
            return pokemon.isBossPokemon() && pokemon.getFormEnum() != EnumMega.Mega;
        }

        return true;
    }



    private boolean checkExpired(Date date) {
        Time time = new Time(date.toInstant().toEpochMilli());
        return time.toString("").equals("Expired");
    }

    private void addLog(Player player, int id, String type, Date date) {
        SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yyyy @ h:mm a z");
        ft.setTimeZone(TimeZone.getTimeZone("EST"));

        Time time;
        if(type.equals("Bounty")) {
            time = new Time(Instant.now().toEpochMilli() - date.toInstant().minusSeconds(BountiesConfig.getTime(id)).toEpochMilli());
        } else {
            time = new Time(Instant.now().toEpochMilli() - date.toInstant().minusSeconds(HuntsConfig.getTime(id)).toEpochMilli());
        }

        PokeHunt.getInstance().getSql().addLog(new Log(player.getName(), id, type, ft.format(new Date()), time.toString("%1$dd %2$dh %3$dm")));
    }

    @SubscribeEvent
    public void onEggCollect(BreedEvent.CollectEgg event) {
        if(BreedingConfig.getCompEnabled()) {
            event.getEgg().getPersistentData().setString("Breeder", event.owner.toString());
            event.getEgg().getPersistentData().setBoolean("WasEgg", true);
        }
    }
}


