package com.m1zark.pokehunt.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pokehunt.PokeHunt;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class Reload implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        PokeHunt.getInstance().getConfig().reload();
        PokeHunt.getInstance().getBountiesConfig().reload();
        PokeHunt.getInstance().getHuntsConfig().reload();
        PokeHunt.getInstance().getMessageConfig().reload();
        PokeHunt.getInstance().getBreedingConfig().reload();

        Chat.sendMessage(src, "&7PokeHunts config successfully reloaded.");

        return CommandResult.success();
    }
}