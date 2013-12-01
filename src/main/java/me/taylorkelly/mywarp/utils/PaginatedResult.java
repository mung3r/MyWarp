package me.taylorkelly.mywarp.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Commands that wish to display a paginated list of results can use this class
 * to do the actual pagination, giving a list of items, a page number, and basic
 * formatting information.
 */
public abstract class PaginatedResult<T> {
    protected static final int PER_PAGE = 8;

    private final String header;
    private final String note;

    /**
     * Initializes this object
     * 
     * @param header
     *            the header that is send ontop the formatted list
     */
    public PaginatedResult(String header) {
        this(header, null);
    }

    /**
     * Initializes this object
     * 
     * @param header
     *            the header that is send ontop the formatted list
     * @param note
     *            the note will be displayed italic right underneath the header
     */
    public PaginatedResult(String header, String note) {
        this.header = header;
        this.note = note;
    }

    /**
     * Displays the results to the given sender by calling
     * {@link #display(CommandSender, Collection, int)}
     * 
     * @param sender
     *            the command sender
     * @param results
     *            results to display
     * @param page
     *            the page to display
     * @throws CommandException
     *             if the page does not exist or the given list is empty
     */
    public void display(CommandSender sender, Collection<? extends T> results, int page)
            throws CommandException {
        display(sender, new ArrayList<T>(results), page);
    }

    /**
     * Displays the results to the sender. Individual results are parsed via
     * {@link #format(Object, CommandSender)};
     * 
     * @param sender
     *            the command sender
     * @param results
     *            results to display
     * @param page
     *            the page to display
     * @throws CommandException
     *             if the page does not exist or the given list is empty
     */
    public void display(CommandSender sender, List<? extends T> results, int page) throws CommandException {
        if (results.size() == 0) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("lister.no-results", sender));
        }
        --page;

        int resultsPerPage = note != null ? PER_PAGE - 1 : PER_PAGE;

        int maxPages = results.size() / resultsPerPage;
        if (page < 0 || page > maxPages) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("lister.unknown-page", sender, maxPages - 1));
        }

        sender.sendMessage(ChatColor.GOLD
                + MinecraftFontWidthCalculator.centralize(" " + header
                        + MyWarp.inst().getLocalizationManager().getColorlessString("lister.page", sender)
                        + " " + (page + 1) + "/" + (maxPages + 1) + " ", '-'));
        if (note != null) {
            sender.sendMessage(ChatColor.ITALIC + note);
        }
        for (int i = resultsPerPage * page; i < resultsPerPage * page + resultsPerPage && i < results.size(); i++) {
            sender.sendMessage(format(results.get(i), sender));
        }
    }

    /**
     * This method is used to format entries of the given objects that are send
     * to the given sender. usage of {@link StringBuilder} is advised.
     * 
     * @param entry
     *            the entry to format
     * @param sender
     *            the command sender who will receive the results later
     * @return a formated entry
     */
    public abstract String format(T entry, CommandSender sender);

}
