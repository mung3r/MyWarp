package me.taylorkelly.mywarp.utils;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Commands that wish to display a paginated list of results can use this class
 * to do the actual pagination, giving a list of items, a page number, and basic
 * formatting information.
 */
public abstract class PaginatedResult<T> {
    private final String header;

    protected static final int PER_PAGE = 8;

    public PaginatedResult(String header) {
        this.header = header;
    }

    public void display(CommandSender sender, Collection<? extends T> results,
            int page) throws CommandException {
        display(sender, new ArrayList<T>(results), page);
    }

    public void display(CommandSender sender, List<? extends T> results,
            int page) throws CommandException {
        if (results.size() == 0) {
            throw new CommandException(LanguageManager.getString("lister.noResults"));
        }
        --page;

        int maxPages = results.size() / PER_PAGE;
        if (page < 0 || page > maxPages) {
            throw new CommandException(LanguageManager.getEffectiveString("lister.unknownPage", "%pages%", Integer.toString(maxPages -1)));
        }

        sender.sendMessage(ChatColor.GOLD + MinecraftFontWidthCalculator.centralize(" " + header + LanguageManager.getColorlessString("lister.page") +" " + (page + 1)
                + "/" + (maxPages + 1) + " ", '-'));
        for (int i = PER_PAGE * page; i < PER_PAGE * page + PER_PAGE
                && i < results.size(); i++) {
            sender.sendMessage(format(results.get(i), sender));
        }
    }

    public abstract String format(T entry, CommandSender sender);

}
