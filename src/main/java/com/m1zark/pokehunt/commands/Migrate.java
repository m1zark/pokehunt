package com.m1zark.pokehunt.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pokehunt.PokeHunt;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class Migrate implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        com.m1zark.pokehunt.storage.Migrate.migrate();

        Chat.sendMessage(src, "&7All player data has been migrated to H2.");

        return CommandResult.success();
    }
}
