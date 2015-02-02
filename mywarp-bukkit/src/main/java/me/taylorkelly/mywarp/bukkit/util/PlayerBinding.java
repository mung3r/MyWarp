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
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.argument.ArgumentStack;
import com.sk89q.intake.parametric.binding.BindingBehavior;
import com.sk89q.intake.parametric.binding.BindingHelper;
import com.sk89q.intake.parametric.binding.BindingMatch;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.util.profile.Profile;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A binding for {@link LocalPlayer}s.
 */
public class PlayerBinding extends BindingHelper {

  /**
   * Gets a player matching the name given by the command.
   *
   * @param context   the command's context
   * @param modifiers the command's modifiers
   * @return a matching player
   * @throws NoSuchPlayerException         if no matching player was found
   * @throws ParameterException            on a parameter error
   * @throws IllegalCommandSenderException if the binding has the {@link Sender} annotation, but the Actor who used the
   *                                       command is not a player instance
   */
  @BindingMatch(type = LocalPlayer.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1, provideModifiers =
      true)
  public LocalPlayer getString(ArgumentStack context, Annotation[] modifiers)
      throws NoSuchPlayerException, ParameterException, IllegalCommandSenderException {

    for (Annotation modifier : modifiers) {
      if (modifier instanceof Sender) {
        return getPlayer(context.getContext().getLocals());
      }
    }

    return getPlayer(context.next());
  }

  /**
   * Gets the player who used the command with the given CommandLocals. <p> It is expected that the sender of the
   * command is an {@link Actor} and stored in the given CommandLocals under the actor class. </p>
   *
   * @param locals the CommandLocals
   * @return the player
   * @throws ParameterException            if CommandLocals does not contain a mapping for the {@link Actor} class
   * @throws IllegalCommandSenderException if the mapping for the {@link Actor} class is not a player
   */
  protected LocalPlayer getPlayer(CommandLocals locals) throws ParameterException, IllegalCommandSenderException {
    Actor actor = locals.get(Actor.class);
    if (actor == null) {
      throw new ParameterException(
          "No Actor avilable. Either this command was not used by one or he is missing from the CommandLocales.");
    }
    if (actor instanceof LocalPlayer) {
      return (LocalPlayer) actor;
    }
    throw new IllegalCommandSenderException(actor);
  }

  /**
   * Gets an online player with a name matching the given query.
   *
   * @param query the query
   * @return a player with a matching name
   * @throws NoSuchPlayerException if no matching player could be found
   */
  protected LocalPlayer getPlayer(String query) throws NoSuchPlayerException {
    Optional<LocalPlayer> optional = MyWarp.getInstance().getOnlinePlayer(query);

    if (!optional.isPresent()) {
      throw new NoSuchPlayerException(query);
    }
    return optional.get();
  }

  /**
   * Indicates that the LocalPlayer binding should be parsed from the Actor who used the command instead of resolving it
   * from a command argument.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface Sender {

  }

  /**
   * Thrown when the player should be parsed from the Actor who called the command, but his Actor is not a player
   * instance.
   */
  public static class IllegalCommandSenderException extends Exception {

    private static final long serialVersionUID = -4275786561766713473L;

    private final Actor actor;

    /**
     * Creates an instance.
     *
     * @param actor the Actor who called the command
     */
    public IllegalCommandSenderException(Actor actor) {
      this.actor = actor;
    }

    /**
     * Gets the actor who called the command.
     *
     * @return the actor
     */
    public Actor getActor() {
      return actor;
    }
  }

  /**
   * Thrown when none of the players who are online has a name that matches a given query.
   */
  public static class NoSuchPlayerException extends Exception {

    private static final long serialVersionUID = -6717353157449643977L;

    private final String query;

    /**
     * Creates an instance.
     *
     * @param profile the profile of the Player
     */
    public NoSuchPlayerException(Profile profile) {
      this(profile.getName().isPresent() ? profile.getName().get() : profile.getUniqueId().toString());
    }

    /**
     * Creates an instance.
     *
     * @param query the query
     */
    public NoSuchPlayerException(String query) {
      this.query = query;
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public String getQuery() {
      return query;
    }
  }

}
