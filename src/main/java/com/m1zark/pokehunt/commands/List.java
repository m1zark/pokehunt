package com.m1zark.pokehunt.commands;

import com.m1zark.pokehunt.gui.MainUI;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class List implements CommandExecutor {
    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        if (BattleRegistry.getBattle((EntityPlayerMP)src) != null) throw new CommandException(Text.of(TextColors.RED, "Cannot use Pok\u00E9Hunts while in battle!"));

        ((Player) src).openInventory((new MainUI((Player) src)).getInventory());

        return CommandResult.success();
    }
}
