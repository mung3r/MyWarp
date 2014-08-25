/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */
package me.taylorkelly.mywarp.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Commands that wish to display a paginated list of results can use this class
 * to do the actual pagination, giving a list of items, a page number, and basic
 * formatting information.
 * 
 * TODO check for upstream fixed in commandbook and include them
 * 
 * @param <T>
 *            the type that should be listed by this instance.
 */
public abstract class PaginatedResult<T> {
    protected static final int PER_PAGE = 8;

    private final String header;
    private final String note;

    /**
     * Initializes this object.
     * 
     * @param header
     *            the header that is send on top the formatted list.
     */
    public PaginatedResult(String header) {
        this(header, null);
    }

    /**
     * Initializes this object.
     * 
     * @param header
     *            the header that is send on top the formatted list
     * @param note
     *            the note will be displayed italic right underneath the header
     */
    public PaginatedResult(String header, @Nullable String note) {
        this.header = header;
        this.note = note;
    }

    /**
     * Displays the results to the given sender.
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
                    .getString("lister.unknown-page", sender, maxPages + 1));
        }

        sender.sendMessage(ChatColor.GOLD
                + FormattingUtils.center(" " + header
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
