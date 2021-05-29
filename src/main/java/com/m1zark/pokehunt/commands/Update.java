package com.m1zark.pokehunt.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.util.BountiesManager;
import com.m1zark.pokehunt.util.HuntsManager;
import com.m1zark.pokehunt.config.BountiesConfig;
import com.m1zark.pokehunt.config.HuntsConfig;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class Update implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Integer> id = args.getOne(Text.of("id"));
        Optional<String> type = args.getOne(Text.of("type"));

        type.ifPresent(t -> {
            if(id.isPresent()) {
                if(t.equals("bounty")) {
                    PokeHunt.getInstance().getSql().updateBounties(new BountiesManager(id.get()), false);
                    Chat.sendMessage(src, "&7Bounty " + id.get() + " has been reset.");
                } else {
                    PokeHunt.getInstance().getSql().updateHunts(new HuntsManager(id.get()), false);
                    Chat.sendMessage(src, "&7Hunt " + id.get() + " has been reset.");
                }
            } else {
                int i = 1;

                if(t.equals("bounty")) {
                    while (i <= BountiesConfig.getNumberBounties()) {
                        PokeHunt.getInstance().getSql().updateBounties(new BountiesManager(i), true);
                        i++;
                    }
                    Chat.sendMessage(src, "&7All bounties have been reset.");
                } else {
                    while (i <= HuntsConfig.getNumberHunts()) {
                        PokeHunt.getInstance().getSql().updateHunts(new HuntsManager(i), true);
                        i++;
                    }
                    Chat.sendMessage(src, "&7All hunts have been reset.");
                }
            }
        });

        return CommandResult.success();
    }
}
