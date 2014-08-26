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
import java.util.List;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import com.google.common.collect.Lists;

/**
 * Paginates results.
 * 
 * @param <E>
 *            the type of elements that should be paginated
 */
public class StringPaginator<E> {

    private static final int PER_PAGE_DEFAULT = 9;

    private final String header;
    private final List<? extends E> elements;

    // E must be an object!
    @SuppressWarnings("unchecked")
    private Function<E, String> mapping = (Function<E, String>) Functions.toStringFunction();
    private List<String> notes = new ArrayList<String>();
    private int entriesPerPage = PER_PAGE_DEFAULT;

    /**
     * Creates a Paginator of the given elements while using the given header.
     * 
     * @param <E>
     *            the type of elements that should be paginated
     * @param elements
     *            the elemnts to display
     * @param header
     *            the header to display on top
     * @return a Paginator
     */
    public static <E> StringPaginator<E> of(Iterable<? extends E> elements, String header) {
        return new StringPaginator<E>(Lists.newArrayList(elements), header);
    }

    /**
     * Creates a Paginator of the given elements while using the given header.
     * 
     * @param <E>
     *            the type of elements that should be paginated
     * @param elements
     *            the elemnts to display
     * @param header
     *            the header to display on top
     * @return a Paginator
     */
    public static <E> StringPaginator<E> of(List<? extends E> elements, String header) {
        return new StringPaginator<E>(elements, header);
    }

    /**
     * @param header
     *            the header that will be displayed on top with page
     *            informations
     * @param elements
     *            the elements to paginate
     */
    private StringPaginator(List<? extends E> elements, String header) {
        this.header = header;
        this.elements = elements;
    }

    /**
     * Adds a note-line. Notes will be displayed on each page, directly under
     * the header but before the paginated content. Do not add to many notes or
     * the content itself becomes unreadable!
     * 
     * @param note
     *            the note
     * @return this Paginator for chaining
     */
    public StringPaginator<E> withNote(String note) {
        notes.add(note);
        return this;
    }

    /**
     * Sets how many entries should be displayed on each page. The number must
     * be higher than the amount of note lines.
     * 
     * @param entriesPerPage
     *            the number of entries to display on each page
     * @return this Paginator for chaining
     */
    public StringPaginator<E> withEntrysPerPage(int entriesPerPage) {
        this.entriesPerPage = entriesPerPage;
        return this;
    }

    /**
     * Sets the mapping function that outputs the string that is shown in the
     * final listing.
     * 
     * @param mapping
     *            the mapping function
     * @return this Paginator for chaining
     */
    public StringPaginator<E> withMapping(Function<E, String> mapping) {
        this.mapping = mapping;
        return this;
    }

    /**
     * Displays the given page to the given command-sender.
     * 
     * @param sender
     *            the command-sender
     * @param page
     *            the page
     * @throws CommandException
     *             if the page does not exist or an negative page number is
     *             given
     */
    public void displayPage(CommandSender sender, int page) throws CommandException {
        // fail fast if there are no elements
        if (elements.isEmpty()) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("lister.no-results", sender));
        }
        int effectiveEntriesPerPage = entriesPerPage - notes.size();
        // computed lazy as needed, so there is no performance problem
        List<List<String>> pages = Lists.partition(Lists.transform(elements, mapping),
                effectiveEntriesPerPage);
        if (page < 1 || page > pages.size()) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("lister.unknown-page", sender, pages.size()));
        }
        // send the header
        sender.sendMessage(ChatColor.GOLD + FormattingUtils.center(toHeader(sender, page, pages.size()), '-'));
        page--;
        // send notes
        for (String note : notes) {
            sender.sendMessage(ChatColor.ITALIC + note);
        }
        // send results
        for (String str : pages.get(page)) {
            sender.sendMessage(str);
        }
    }

    /**
     * Gets the header applicable for the given arguments.
     * 
     * @param sender
     *            the sender
     * @param page
     *            the page to display
     * @param maxPages
     *            the maximum number of pages
     * @return the full header
     */
    private String toHeader(CommandSender sender, int page, int maxPages) {
        StringBuilder ret = new StringBuilder();
        ret.append(' ');
        ret.append(header);
        ret.append(", ");
        ret.append(MyWarp.inst().getLocalizationManager().getColorlessString("lister.page", sender));
        ret.append(' ');
        ret.append(page);
        ret.append('/');
        ret.append(maxPages);
        ret.append(' ');
        return ret.toString();
    }

}
