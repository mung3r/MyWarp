/*
 * Copyright (C) 2011 - 2016, MyWarp team and contributors
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

package me.taylorkelly.mywarp.bukkit.util.paginator;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.bukkit.util.FormattingUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginates results.
 *
 * @param <E> the type of elements that should be paginated
 */
public class StringPaginator<E> {

  private static final DynamicMessages MESSAGES = new DynamicMessages("me.taylorkelly.mywarp.lang.StringPaginator");


  private final String header;
  private final List<? extends E> elements;

  @SuppressWarnings("unchecked")
  private Function<E, String> mapping = (Function<E, String>) Functions.toStringFunction();
  private List<String> notes = new ArrayList<String>();
  private int entriesPerPage = 9;

  /**
   * Creates an instance.
   *
   * @param header   the header that will be displayed on top with page informations
   * @param elements the elements to paginate
   */
  private StringPaginator(String header, List<? extends E> elements) {
    this.header = header;
    this.elements = elements;
  }

  /**
   * Creates a Paginator of the given elements while using the given header.
   *
   * @param <E>      the type of elements that should be paginated
   * @param header   the header to display on top
   * @param elements the elements to display
   * @return a Paginator
   */
  public static <E> StringPaginator<E> of(String header, E... elements) {
    return new StringPaginator<E>(header, Lists.newArrayList(elements));
  }

  /**
   * Creates a Paginator of the given elements while using the given header.
   *
   * @param <E>      the type of elements that should be paginated
   * @param header   the header to display on top
   * @param elements the elements to display
   * @return a Paginator
   */
  public static <E> StringPaginator<E> of(String header, Iterable<? extends E> elements) {
    return new StringPaginator<E>(header, Lists.newArrayList(elements));
  }

  /**
   * Creates a Paginator of the given elements while using the given header.
   *
   * @param <E>      the type of elements that should be paginated
   * @param header   the header to display on top
   * @param elements the elements to display
   * @return a Paginator
   */
  public static <E> StringPaginator<E> of(String header, List<? extends E> elements) {
    return new StringPaginator<E>(header, elements);
  }

  /**
   * Adds a note-line. Notes will be displayed on each page, directly under the header but before the paginated content.
   * Do not add to many notes or the content itself becomes unreadable!
   *
   * @param note the note
   * @return this Paginator for chaining
   */
  public StringPaginator<E> withNote(String note) {
    notes.add(note);
    return this;
  }

  /**
   * Sets how many entries should be displayed on each page. The number must be higher than the amount of note lines.
   *
   * @param entriesPerPage the number of entries to display on each page
   * @return this Paginator for chaining
   */
  public StringPaginator<E> withEntrysPerPage(int entriesPerPage) {
    this.entriesPerPage = entriesPerPage;
    return this;
  }

  /**
   * Sets the mapping function that outputs the string that is shown in the final listing.
   *
   * @param mapping the mapping function
   * @return this Paginator for chaining
   */
  public StringPaginator<E> withMapping(Function<E, String> mapping) {
    this.mapping = mapping;
    return this;
  }

  /**
   * Paginates the elements.
   *
   * @return the paginated elements
   */
  public PaginatedResults paginate() {
    return new PaginatedResults(this);
  }

  /**
   * Results that are paginated.
   */
  public class PaginatedResults {

    private final String header;
    private final List<String> notes;
    private final List<List<String>> pages;

    /**
     * Creates an instance.
     *
     * @param paginator the paginator
     */
    private PaginatedResults(StringPaginator<E> paginator) {
      this.header = paginator.header;
      this.notes = paginator.notes;
      this.pages =
          Lists.partition(Lists.transform(paginator.elements, paginator.mapping),
                          paginator.entriesPerPage - notes.size());
    }

    /**
     * Gets the given page as List. Each entry is one single line.
     *
     * @param page the page number
     * @return the given page as List
     * @throws NoResultsException   if there are no results that could be paginated
     * @throws UnknownPageException if there are results that could be paginated, but no page with the given number
     *                              exists
     */
    public List<String> getPage(int page) throws NoResultsException, UnknownPageException {
      if (pages.isEmpty()) {
        throw new NoResultsException();
      }
      if (page < 1 || page > pages.size()) {
        throw new UnknownPageException(pages.size());
      }

      List<String> ret = new ArrayList<String>();
      ret.add(ChatColor.GOLD + toHeader(page, pages.size()));
      for (String note : notes) {
        ret.add(ChatColor.ITALIC + note);
      }
      ret.addAll(pages.get(page - 1));

      return ret;
    }

    /**
     * Gets the given page as String.
     *
     * @param page the page number
     * @return the given page as String
     * @throws NoResultsException   if there are no results that could be paginated
     * @throws UnknownPageException if there are results that could be paginated, but no page with the given number
     *                              exists
     */
    public String getPageAsString(int page) throws NoResultsException, UnknownPageException {
      return Joiner.on(System.getProperty("line.separator")).join(getPage(page));
    }

    /**
     * Displays the given page to the given Actor. This method will catch checked exceptions and send the appropriate
     * error message instead.
     *
     * @param actor the Actor
     * @param page  the page number
     */
    public void display(Actor actor, int page) {
      try {
        actor.sendMessage(getPageAsString(page));
      } catch (NoResultsException e) {
        actor.sendError(MESSAGES.getString("no-results"));
      } catch (UnknownPageException e) {
        actor.sendError(MESSAGES.getString("unknown-page", e.getHighestPage()));
      }
    }

    /**
     * Gets the header applicable for the given arguments.
     *
     * @param page     the page to display
     * @param maxPages the maximum number of pages
     * @return the full header
     */
    private String toHeader(int page, int maxPages) {
      return FormattingUtils
          .center(" " + header + " - " + MESSAGES.getString("page") + ' ' + page + '/' + maxPages + ' ', '-');
    }
  }

}
