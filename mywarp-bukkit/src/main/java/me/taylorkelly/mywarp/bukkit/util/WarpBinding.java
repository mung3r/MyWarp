/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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

package me.taylorkelly.mywarp.bukkit.util;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.argument.ArgumentStack;
import com.sk89q.intake.parametric.binding.BindingBehavior;
import com.sk89q.intake.parametric.binding.BindingHelper;
import com.sk89q.intake.parametric.binding.BindingMatch;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Name.Condition;
import me.taylorkelly.mywarp.util.MatchList;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * A binding for {@link Warp}s.
 */
public class WarpBinding extends BindingHelper {

  private final WarpManager warpManager;

  /**
   * Creates an instance.
   *
   * @param warpManager the WarpManager this Binding will bind warps from
   */
  public WarpBinding(WarpManager warpManager) {
    this.warpManager = warpManager;
  }

  /**
   * Gets a Warp matching the query and the Condition modifier given by the command.
   *
   * @param context    the command's context
   * @param classifier the classifier
   * @return a matching Warp
   * @throws NoSuchWarpException if no Warp matching the query and Condition exists
   * @throws ParameterException  on a parameter error
   */
  @BindingMatch(classifier = Name.class, type = Warp.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
  public Warp getWarp(ArgumentStack context, Annotation classifier) throws NoSuchWarpException, ParameterException {
    Condition conditionValue = ((Name) classifier).value();

    CommandLocals locals = context.getContext().getLocals();
    Actor actor = locals.get(Actor.class);
    if (actor == null || !(conditionValue.clazz.isAssignableFrom(actor.getClass()))) {
      throw new IllegalArgumentException("This Binding must be used by " + conditionValue.clazz.getName() + "s.");
    }

    Predicate<Warp> predicate = null;
    switch (conditionValue) {
      case MODIFIABLE:
        predicate = WarpUtils.isModifiable(actor);
        break;
      case USABLE:
        predicate = WarpUtils.isUsable((LocalEntity) actor);
        break;
      case VIEWABLE:
        predicate = WarpUtils.isViewable(actor);
        break;
    }

    return getWarp(context.next(), predicate);
  }

  /**
   * Gets a Warp matching the given name and fulfilling the given Predicate.
   *
   * @param query     the query
   * @param predicate the Predicate
   * @return the matching Warp
   * @throws NoSuchWarpException if such a Warp does not exist
   */
  private Warp getWarp(String query, Predicate<Warp> predicate) throws NoSuchWarpException {
    MatchList matches = warpManager.getMatchingWarps(query, predicate);
    Optional<Warp> exactMatch = matches.getExactMatch();

    if (!exactMatch.isPresent()) {
      throw new NoSuchWarpException(query, matches);
    }
    return exactMatch.get();
  }

  /**
   * Indicates that a warp is parsed by name.
   */
  @java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
  @java.lang.annotation.Target(ElementType.PARAMETER)
  public @interface Name {

    /**
     * The condition the parsed warp must meat.
     */
    Condition value();

    /**
     * The condition a warp must meat.
     */
    enum Condition {
      /**
       * The Warp is viewable.
       *
       * @see Warp#isViewable(Actor)
       */
      VIEWABLE(Actor.class),
      /**
       * The Warp is usable.
       *
       * @see Warp#isUsable(LocalEntity)
       */
      USABLE(LocalEntity.class),
      /**
       * The Warp is modifiable.
       *
       * @see Warp#isModifiable(Actor)
       */
      MODIFIABLE(Actor.class);

      private final Class<?> clazz;

      /**
       * Creates an instance.
       *
       * @param clazz the class of the instance that corresponds with this Condition.
       */
      private Condition(Class<?> clazz) {
        this.clazz = clazz;
      }
    }

  }

  /**
   * Indicates that none of the Warps has a name that matches the given query.
   */
  public class NoSuchWarpException extends Exception {

    private static final long serialVersionUID = 484195949141477133L;

    private final String query;
    private final MatchList matches;

    /**
     * Creates an instance.
     *
     * @param query   the query
     * @param matches the possible matches of the query
     */
    public NoSuchWarpException(String query, MatchList matches) {
      this.query = query;
      this.matches = matches;
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public String getQuery() {
      return query;
    }

    /**
     * Gets the matches.
     *
     * @return the matches
     */
    public MatchList getMatches() {
      return matches;
    }
  }
}
