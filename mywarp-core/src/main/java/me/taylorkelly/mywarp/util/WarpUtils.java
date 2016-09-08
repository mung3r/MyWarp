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

package me.taylorkelly.mywarp.util;

import com.google.common.base.Predicate;

import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.warp.Warp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods to work with warps.
 */
public final class WarpUtils {

  public static final int MAX_NAME_LENGTH = 32;
  private static final Pattern TOKEN_PATTERN = Pattern.compile("%(.+?)%");

  /**
   * Block initialization of this class.
   */
  private WarpUtils() {
  }

  /**
   * Gets the average number of visits per day, from the point the given {@code Warp} was created until this method is
   * called.
   *
   * @param warp the warp
   * @return the average number of visits per day
   */
  public static double visitsPerDay(Warp warp) {
    // this method might not be 100% exact (considering leap seconds), but
    // within the current Java API there are no alternatives
    long
            daysSinceCreation =
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - warp.getCreationDate().getTime());
    if (daysSinceCreation <= 0) {
      daysSinceCreation = 1;
    }
    return warp.getVisits() / daysSinceCreation;
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the warp being tested is created by player identified by the
   * given profile.
   *
   * @param uniqueId the Profile
   * @return a predicate that checks if the given warp is created by the given player
   * @see Warp#isCreator(java.util.UUID)
   */
  public static Predicate<Warp> isCreator(final UUID uniqueId) {
    return new Predicate<Warp>() {

      @Override
      public boolean apply(Warp warp) {
        return warp.isCreator(uniqueId);
      }

    };
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the warp being tested is of the given type.
   *
   * @param type the type
   * @return a predicate that checks if the given warp is of the given type
   * @see Warp#isType(Warp.Type)
   */
  public static Predicate<Warp> isType(final Warp.Type type) {
    return new Predicate<Warp>() {

      @Override
      public boolean apply(Warp warp) {
        return warp.isType(type);
      }

    };
  }

  /**
   * Replaces all tokens in the given {@code string} with the values applicable for the given {@code warp}.
   *
   * <p>The following tokens are available: <table> <tr> <th>Token</th> <th>Replacement</th> </tr> <tr>
   * <td>%creator%</td> <td>warp's creator</td> </tr> <tr> <td>%loc%</td> <td>warp's location</td> </tr> <tr>
   * <td>%visits%</td> <td>the warp's visits</td> </tr> <tr> <td>%warp%</td> <td>the warp's name</td> </tr> </table>
   * </p>
   *
   * @param string the string that contains the tokens
   * @param warp   the {@code Warp} whose values should be used as replacement
   * @return the string with replaced tokens
   */
  public static String replaceTokens(String string, Warp warp) {
    return replace(string, warpTokens(warp, new HashMap<String, String>()));
  }

  /**
   * Replaces all tokens in the given {@code string} with the values applicable for the given {@code warp} and the given
   * {@code actor}.
   *
   * <p>The following tokens are available: <table> <tr> <th>Token</th> <th>Replacement</th> </tr> <tr>
   * <td>%creator%</td> <td>warp's creator</td> </tr> <tr> <td>%loc%</td> <td>warp's location</td> </tr> <tr>
   * <td>%visits%</td> <td>the warp's visits</td> </tr> <tr> <td>%warp%</td> <td>the warp's name</td> </tr> <tr>
   * <td>%actor%</td> <td>the actor's name</td> </tr> </table> </p>
   *
   * @param string the string that contains the tokens
   * @param warp   the {@code Warp} whose values should be used as replacement
   * @param actor  the {@code Actor} whose values should be used as replacement
   * @return the string with replaced tokens
   */
  public static String replaceTokens(String string, Warp warp, Actor actor) {
    return replace(string, actorTokens(actor, (warpTokens(warp, new HashMap<String, String>()))));
  }

  /**
   * Adds all tokens that involve an Actor to the given map, using the given {@code actor} to create replacements.
   *
   * @param actor     the Actor whose values should be used as replacement
   * @param variables the {@code Map} tokens and variables are added to
   * @return the{@code Map} with added tokens and variables
   */
  private static Map<String, String> actorTokens(Actor actor, Map<String, String> variables) {
    variables.put("player", actor.getName());
    return variables;
  }

  /**
   * Adds all tokens that involve a warp to the given map, using the given {@code warp} to create replacements.
   *
   * @param warp      the warp whose values should be used as replacement
   * @param variables the {@code Map} tokens and variables are added to
   * @return the{@code Map} with added tokens and variables
   */
  private static Map<String, String> warpTokens(Warp warp, Map<String, String> variables) {
    variables.put("creator", warp.getCreator().toString()); //TODO not good
    variables.put("loc", "(" + warp.getPosition().getFloorX() + ", " + warp.getPosition().getFloorY() + ", " + warp
            .getPosition().getFloorZ() + ")");
    variables.put("visits", Integer.toString(warp.getVisits()));
    variables.put("warp", warp.getName());
    return variables;
  }

  /**
   * Replaces tokens found in the given {@code template} with the strings mapped under the token in the given {@code
   * Map}.
   *
   * <p>Tokens are strings enclosed by {@code %}.</p>
   *
   * @param template  the template String
   * @param variables the {@code Map} that stores tokens and their replacements
   * @return a string with replaced tokens
   */
  private static String replace(String template, Map<String, String> variables) {
    Matcher matcher = TOKEN_PATTERN.matcher(template);
    StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      if (variables.containsKey(matcher.group(1))) {
        String replacement = variables.get(matcher.group(1));
        // quote to work properly with $ and {,} signs
        matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "null");
      }
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

}
