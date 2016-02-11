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

package me.taylorkelly.mywarp.command.parametric.binding;

import com.google.common.base.Optional;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.argument.ArgumentStack;
import com.sk89q.intake.parametric.binding.BindingBehavior;
import com.sk89q.intake.parametric.binding.BindingHelper;
import com.sk89q.intake.parametric.binding.BindingMatch;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalPlayer;
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

  private final Game game;

  /**
   * Creates an instance.
   *
   * @param game the Game this Binding will bind players from
   */
  public PlayerBinding(Game game) {
    this.game = game;
  }

  /**
   * Gets a player matching the name given by the command.
   *
   * @param context the command's context
   * @return a matching player
   * @throws NoSuchPlayerException if no matching player was found
   * @throws ParameterException    on a parameter error
   */
  @BindingMatch(type = LocalPlayer.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
  public LocalPlayer getPlayer(ArgumentStack context) throws NoSuchPlayerException, ParameterException {
    return getPlayerFromGame(context.next());
  }

  /**
   * Gets the player who called the command.
   *
   * @param context    the command's context
   * @param classifier the classifier
   * @return the player
   * @throws IllegalCommandSenderException if the command was not called by a player
   */
  @BindingMatch(classifier = Sender.class, type = LocalPlayer.class, behavior = BindingBehavior.PROVIDES)
  public LocalPlayer getSendingPlayer(ArgumentStack context, Annotation classifier)
      throws IllegalCommandSenderException {
    return getPlayerFromLocals(context.getContext().getLocals());
  }

  /**
   * Gets the player who used the command with the given CommandLocals. <p> It is expected that the sender of the
   * command is an {@link Actor} and stored in the given CommandLocals under the actor class. </p>
   *
   * @param locals the CommandLocals
   * @return the player
   * @throws IllegalArgumentException      if CommandLocals does not contain a mapping for the {@link Actor} class
   * @throws IllegalCommandSenderException if the mapping for the {@link Actor} class is not a player
   */
  private LocalPlayer getPlayerFromLocals(CommandLocals locals) throws IllegalCommandSenderException {
    Actor actor = locals.get(Actor.class);
    if (actor == null) {
      throw new IllegalArgumentException(
          "No Actor available. Either this command was not used by one or he is missing from the CommandLocales.");
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
  protected LocalPlayer getPlayerFromGame(String query) throws NoSuchPlayerException {
    Optional<LocalPlayer> optional = game.getPlayer(query);

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
