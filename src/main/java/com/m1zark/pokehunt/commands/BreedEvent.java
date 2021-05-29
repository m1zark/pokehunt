package com.m1zark.pokehunt.commands;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.M1utilities;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Discord.Message;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pokehunt.events.BreedEventStart;
import com.m1zark.pokehunt.util.BreedEvent.BreedManager;
import com.m1zark.pokehunt.util.BreedEvent.PlayerEntry;
import com.m1zark.pokehunt.util.PokeUtils;
import com.m1zark.pokehunt.config.BreedingConfig;
import com.m1zark.pokehunt.config.Config;
import com.m1zark.pokehunt.config.MessageConfig;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BreedEvent implements CommandExecutor {
    private static String event = "breeding competition";
    private static Map<String, String> winners = new ConcurrentHashMap<>();

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = (Player) src;
        Optional<String> type = args.getOne(Text.of("type"));
        Optional<Integer> slot = args.getOne(Text.of("slot"));

        if(!BreedingConfig.getCompEnabled()) throw new CommandException(Chat.embedColours(MessageConfig.getMessages("Messages.Events.noEvent").replace("{event}",event)));

        if(!player.hasPermission("pokehunt.player.breeding")) throw new CommandException(Chat.embedColours(MessageConfig.getMessages("Messages.Events.missingPermission").replace("{event}",event)));

        Time time = new Time(BreedingConfig.getEndDate());
        if(time.toString().equalsIgnoreCase("Expired")) throw new CommandException(Chat.embedColours(MessageConfig.getMessages("Messages.Events.eventExpired").replace("{event}",event)));

        BreedingConfig.getCompWinners().forEach(uuid -> {
            String[] parts = uuid.split(":");
            winners.put(parts[0], parts[1]);
        });

        if (type.isPresent()) {
            if (type.get().equalsIgnoreCase("enter")) {
                BreedManager manager = new BreedManager(player.getUniqueId());
                Pokemon pokemon = manager.getPokemon();
                PlayerEntry data = new PlayerEntry(player.getUniqueId(), pokemon.getSpecies().name(), pokemon.getGender().name(), pokemon.getNature().getLocalizedName(), pokemon.getGrowth().getLocalizedName(), pokemon.getCaughtBall().name(), pokemon.getAbility().getName(), pokemon.getMoveset().get(0).getActualMove().getAttackName(), manager.getHiddenpower());

                if (BreedingConfig.savePlayerData(data)) {
                    Chat.sendMessage(player, MessageConfig.getMessages("Messages.Events.Entry.newEntry").replace("{event}", event));
                } else {
                    Chat.sendMessage(player, MessageConfig.getMessages("Messages.Events.Entry.alreadyEntered").replace("{event}", event));
                }
            } else if (type.get().equalsIgnoreCase("submit")) {
                if (slot.isPresent()) {
                    if (slot.get() < 1 || slot.get() > 6) throw new CommandException(Chat.embedColours("&cSlot number must be between 1 and 6!"));
                } else {
                    throw new CommandException(Chat.embedColours("&cYou must include a slot number!"));
                }

                PlayerPartyStorage storage = PokeUtils.getPlayerStorage((EntityPlayerMP) player);
                if (storage == null) throw new CommandException(Chat.embedColours("&cThere was an error getting your player storage... try again."));

                Pokemon pokemon = storage.get(slot.get() - 1);
                if (pokemon == null) throw new CommandException(Chat.embedColours("&cUnable to find a Pok\u00E9mon in the specified slot."));

                PlayerEntry playerEntry = BreedingConfig.getPlayerData(player.getUniqueId());
                if (playerEntry == null) throw new CommandException(Chat.embedColours("&7You have not entered this " + event + ". Use &d/bcomp enter &7to enter."));

                if (!pokemon.getOriginalTrainer().equalsIgnoreCase(player.getName()) || !pokemon.getOwnerPlayer().equals((EntityPlayerMP) player)) throw new CommandException(Chat.embedColours("&cYou are not the OT for this Pok\u00E9mon!!"));

                if (!pokemon.getPersistentData().getBoolean("WasEgg")) throw new CommandException(Chat.embedColours("&7This " + pokemon.getDisplayName() + " doesn't seem to have been hatched from an egg."));

                if(pokemon.getPersistentData().hasKey("Breeder")) {
                    if (!pokemon.getPersistentData().getString("Breeder").equals(playerEntry.getPlayer().toString())) throw new CommandException(Chat.embedColours("&7This " + pokemon.getDisplayName() + " doesn't seem to have been breed by you."));
                }

                if (!checkPokemon(playerEntry, pokemon)) throw new CommandException(Chat.embedColours("&7This is not the required Pok\u00E9mon."));

                if (BreedingConfig.getCompWinners().size() < BreedingConfig.getMaxWinners()) {
                    if (!winners.containsKey(player.getUniqueId().toString())) {
                        Chat.sendMessage(player, MessageConfig.getMessages("Messages.Events.Completion.playerMessage").replace("{event}", event));

                        PokeUtils.processBreedingCompRewards(player, pokemon);
                        if (BreedingConfig.doRemovePokemon()) {
                            storage.retrieveAll();
                            storage.set(slot.get() - 1, null);
                        }

                        List<String> winners = new ArrayList<>(Lists.newArrayList(BreedingConfig.getCompWinners()));
                        winners.add(player.getUniqueId().toString() + ":" + player.getName());
                        BreedingConfig.saveWinner(winners);
                    } else {
                        Chat.sendMessage(player, MessageConfig.getMessages("Messages.Events.Completion.alreadyCompleted").replace("{event}", event));
                    }
                } else {
                    Chat.sendMessage(player, MessageConfig.getMessages("Messages.Events.maxWinners").replace("{event}", event));
                }
            }
        }

        return CommandResult.success();
    }

    private boolean checkPokemon(PlayerEntry playerEntry, Pokemon entityPixelmon) {
        if(!entityPixelmon.getDisplayName().equalsIgnoreCase(playerEntry.getPokemon_name())) return false;

        if(BreedingConfig.getOptions("gender")) {
            if (!entityPixelmon.getGender().name().equalsIgnoreCase(playerEntry.getPokemon_gender())) return false;
        }
        if(BreedingConfig.getOptions("ability")) {
            if (!entityPixelmon.getAbility().getName().equalsIgnoreCase(playerEntry.getPokemon_ability())) return false;
        }
        if(BreedingConfig.getOptions("nature")) {
            if (!entityPixelmon.getNature().getLocalizedName().equalsIgnoreCase(playerEntry.getPokemon_nature())) return false;
        }
        if(BreedingConfig.getOptions("size")) {
            if (!entityPixelmon.getGrowth().getLocalizedName().equalsIgnoreCase(playerEntry.getPokemon_growth())) return false;
        }
        if(BreedingConfig.getOptions("pokeball")) {
            if (!entityPixelmon.getCaughtBall().name().equalsIgnoreCase(playerEntry.getPokemon_pokeball())) return false;
        }
        if(BreedingConfig.getOptions("hiddenpower")) {
            if (!HiddenPower.getHiddenPowerType(entityPixelmon.getIVs()).getLocalizedName().equalsIgnoreCase(playerEntry.getPokemon_hiddenpower())) return false;
        }

        if(BreedingConfig.getOptions("eggmove")) {
            boolean move = false;
            for (Attack attack : entityPixelmon.getMoveset()) {
                if (attack != null && attack.getActualMove().getAttackName().equalsIgnoreCase(playerEntry.getPokemon_eggMove()))
                    move = true;
            }

            return move;
        }

        return true;
    }

    public static class Event implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Optional<String> type = args.getOne(Text.of("type"));
            Optional<Integer> time = args.getOne(Text.of("time"));

            type.ifPresent(t -> {
                int length = time.orElse(1) * 86400;

                if(t.equalsIgnoreCase("start")) {
                    if(!BreedingConfig.getCompEnabled()) {
                        BreedingConfig.setCompEnabled(true);
                        BreedingConfig.setEndDate(Instant.now().plusSeconds(length).toEpochMilli());
                        BreedingConfig.reload();

                        Time expires = new Time(BreedingConfig.getEndDate());
                        Chat.sendServerWideMessage(MessageConfig.getMessages("Messages.Events.announceNewEvent").replace("{event}",event).replace("{time}",expires.toString("%1$dd %2$dh %3$dm")));

                        BreedEventStart eventStart = new BreedEventStart(expires.toString("%1$dd %2$dh %3$dm"), Cause.builder().append(this).build(EventContext.builder().build()));
                        Sponge.getEventManager().post(eventStart);

                        if(Config.getEnabled("enableDiscordNotifications")) {
                            M1utilities.getInstance().getDiscordNotifier().ifPresent(notifier -> {
                                Message message = notifier.forgeMessage(Config.discordOption("breeding-comp-start",event,expires.toString("%1$dd %2$dh %3$dm")));
                                notifier.sendMessage(message);
                            });
                        }
                    }
                } else if(t.equalsIgnoreCase("stop")) {
                    if(BreedingConfig.getCompEnabled()) {
                        BreedingConfig.setCompEnabled(false);
                        BreedingConfig.setEndDate(0);
                        BreedingConfig.clearData();
                        Chat.sendServerWideMessage(MessageConfig.getMessages("Messages.Events.announceEventEnd").replace("{event}",event));

                        if(Config.getEnabled("enableDiscordNotifications")) {
                            M1utilities.getInstance().getDiscordNotifier().ifPresent(notifier -> {
                                Message message = notifier.forgeMessage(Config.discordOption("breeding-comp-end",event,""));
                                notifier.sendMessage(message);
                            });
                        }
                    }
                }
            });

            return CommandResult.success();
        }
    }
}
