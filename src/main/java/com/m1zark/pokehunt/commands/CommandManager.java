package com.m1zark.pokehunt.commands;

import com.m1zark.pokehunt.PHInfo;
import com.m1zark.pokehunt.PokeHunt;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.HashMap;

public class CommandManager {
    public void registerCommands(PokeHunt plugin) {
        Sponge.getCommandManager().register(plugin, viewHunts, "pokehunts", "ph", "hunt", "hunts", "pokehunt");
        Sponge.getCommandManager().register(plugin, breedEvent, "breedcomp", "bcomp");

        PokeHunt.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PHInfo.PREFIX, "Registering commands...")));
    }

    CommandSpec reload = CommandSpec.builder()
            .permission("pokehunt.admin.reload")
            .description(Text.of("Reload the pokehunts config file"))
            .executor(new Reload())
            .build();

    CommandSpec log = CommandSpec.builder()
            .permission("pokehunt.admin.logs")
            .description(Text.of("Display PokeHunt capture logs"))
            .arguments(
                    GenericArguments.choices(Text.of("type"),new HashMap<String, String>(){{put("capture","Bounty");put("defeat","Hunt");put("delete","delete");}})
            )
            .executor(new Logs())
            .build();

    CommandSpec update = CommandSpec.builder()
            .permission("pokehunt.admin.update")
            .description(Text.of("Updates database with new hunts"))
            .arguments(
                    GenericArguments.choices(Text.of("type"),new HashMap<String, String>(){{put("capture","bounty");put("defeat","hunt");}}),
                    GenericArguments.optional(GenericArguments.integer(Text.of("id")))
            )
            .executor(new Update())
            .build();

    CommandSpec breedEventAdmin = CommandSpec.builder()
            .permission("pokehunt.admin.event")
            .arguments(
                    GenericArguments.choices(Text.of("type"),new HashMap<String, String>(){{put("start","start");put("stop","stop");}}),
                    GenericArguments.optionalWeak(GenericArguments.integer(Text.of("time")))
            )
            .executor(new BreedEvent.Event())
            .build();

    CommandSpec breedEvent = CommandSpec.builder()
            .permission("pokehunt.player.breeding")
            .child(breedEventAdmin, "event")
            .arguments(
                    GenericArguments.choices(Text.of("type"),new HashMap<String, String>(){{put("enter","enter");put("submit","submit");}}),
                    GenericArguments.optionalWeak(GenericArguments.integer(Text.of("slot")))
            )
            .executor(new BreedEvent())
            .build();

    CommandSpec migrate = CommandSpec.builder()
            .permission("pokehunt.admin.migrate")
            .description(Text.of(""))
            .executor(new Migrate())
            .build();

    CommandSpec viewHunts = CommandSpec.builder()
            .permission("pokehunt.player.base")
            .description(Text.of("Displays current hunts."))
            .child(reload, "reload")
            .child(update, "update")
            .child(log, "logs")
            .child(migrate, "migrate")
            .executor(new List())
            .build();
}
