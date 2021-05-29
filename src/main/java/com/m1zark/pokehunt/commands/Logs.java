package com.m1zark.pokehunt.commands;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pokehunt.PokeHunt;
import com.m1zark.pokehunt.util.logs.Log;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Logs implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<String> type = args.getOne(Text.of("type"));

        if(type.isPresent()) {
            if(type.get().equals("delete")) {
                PokeHunt.getInstance().getSql().deleteLogs();
            } else {
                List<Log> logs = PokeHunt.getInstance().getSql().getLogs(type.get());

                List<Text> texts = Lists.newArrayList();
                logs.forEach(log -> {
                    Text text = Text.of(Chat.embedColours("&b" + log.getPlayer() + "&7 - &eLevel " + log.getID() + " " + log.getHuntType() + " &7completed on &a" + log.getTimeStamp() + " &7(&a" + log.getTimeRemaining() + " Expired &7)"));
                    texts.add(text);
                });

                if (logs.isEmpty()) {
                    Chat.sendMessage(src, "&7There are no logs available at this time.");
                } else {
                    PaginationList.builder().contents(texts).title(Text.of("PokeHunt Logs")).build().sendTo(src);
                }
            }
        }

        return CommandResult.success();
    }
}
