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

package me.taylorkelly.mywarp.command;

import com.google.common.collect.Iterables;
import com.sk89q.intake.CommandCallable;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.CommandMapping;
import com.sk89q.intake.InvalidUsageException;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.fluent.CommandGraph;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.util.auth.AuthorizationException;
import com.sk89q.intake.util.i18n.ResourceProvider;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.Platform;
import me.taylorkelly.mywarp.command.definition.ImportCommands;
import me.taylorkelly.mywarp.command.definition.InformativeCommands;
import me.taylorkelly.mywarp.command.definition.ManagementCommands;
import me.taylorkelly.mywarp.command.definition.SocialCommands;
import me.taylorkelly.mywarp.command.definition.UsageCommands;
import me.taylorkelly.mywarp.command.definition.UtilityCommands;
import me.taylorkelly.mywarp.command.parametric.ActorAuthorizer;
import me.taylorkelly.mywarp.command.parametric.CommandResourceProvider;
import me.taylorkelly.mywarp.command.parametric.ExceptionConverter;
import me.taylorkelly.mywarp.command.parametric.FallbackDispatcher;
import me.taylorkelly.mywarp.command.parametric.IntakeResourceProvider;
import me.taylorkelly.mywarp.command.parametric.binding.ActorBindung;
import me.taylorkelly.mywarp.command.parametric.binding.ConnectionConfigurationBinding;
import me.taylorkelly.mywarp.command.parametric.binding.FileBinding;
import me.taylorkelly.mywarp.command.parametric.binding.PlayerBinding;
import me.taylorkelly.mywarp.command.parametric.binding.ProfileBinding;
import me.taylorkelly.mywarp.command.parametric.binding.WarpBinding;
import me.taylorkelly.mywarp.command.parametric.economy.EconomyInvokeHandler;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;

import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Handles MyWarp's commands.
 */
public class CommandHandler {

  public static final String RESOURCE_BUNDLE_NAME = "me.taylorkelly.mywarp.lang.Commands";

  private static final DynamicMessages msg = new DynamicMessages(RESOURCE_BUNDLE_NAME);
  private static final Logger log = MyWarpLogger.getLogger(CommandHandler.class);

  private final Dispatcher dispatcher;

  /**
   * Creates an instance. Commands will hook into the given MyWarp instance.
   *
   * @param myWarp the MyWarp instance
   */
  public CommandHandler(MyWarp myWarp) {

    Platform platform = myWarp.getPlatform();

    // command registration
    ResourceProvider resourceProvider = new IntakeResourceProvider(platform.getResourceBundleControl());
    ExceptionConverter exceptionConverter = new ExceptionConverter();
    PlayerBinding playerBinding = new PlayerBinding(platform.getGame());
    WarpBinding warpBinding = new WarpBinding(myWarp.getWarpManager(), myWarp.getAuthorizationService());

    ParametricBuilder builder = new ParametricBuilder(resourceProvider);
    builder.setAuthorizer(new ActorAuthorizer());
    builder.setExternalResourceProvider(new CommandResourceProvider(platform.getResourceBundleControl()));
    builder.addBinding(new ActorBindung());
    builder.addBinding(new ConnectionConfigurationBinding());
    builder.addBinding(new FileBinding(platform.getDataFolder()));
    builder.addBinding(playerBinding);
    builder.addBinding(new ProfileBinding(platform.getProfileService()));
    builder.addBinding(warpBinding);
    builder.addExceptionConverter(exceptionConverter);

    builder.addInvokeListener(new EconomyInvokeHandler(myWarp.getEconomyService()));

    UsageCommands usageCommands = new UsageCommands(myWarp);

    //XXX this should be covered by unit tests
    CommandCallable fallback = Iterables.getOnlyElement(builder.build(usageCommands).values());

    // @formatter:off
    dispatcher = new CommandGraph(resourceProvider).builder(builder)
            .commands()
              .registerMethods(usageCommands)
              .group(new FallbackDispatcher(resourceProvider, fallback), "warp", "myWarp", "mw")
                .describeAs("warp-to.description")
                .registerMethods(new InformativeCommands(myWarp.getLimitService(), platform.getSettings(),
                                                         myWarp.getWarpManager(),
                                                         myWarp.getAuthorizationService()))
                .registerMethods(new ManagementCommands(myWarp))
                .registerMethods(new SocialCommands(platform.getGame(),
                                                    myWarp.getLimitService(),
                                                    platform.getProfileService()))
                .registerMethods(new UtilityCommands(myWarp))
                .group("import", "migrate")
                  .describeAs("import.description")
                  .registerMethods(new ImportCommands(myWarp))
              .graph()
            .getDispatcher();
    // @formatter:on
  }

  /**
   * Executes the given {@code command} with the given Actor.
   *
   * @param command the full command string as given by the caller
   * @param caller  the calling Actor
   */
  public void callCommand(String command, Actor caller) {
    // create the CommandLocals
    CommandLocals locals = new CommandLocals();
    locals.put(Actor.class, caller);

    //call the command
    try {
      dispatcher.call(command, locals, new String[0]);

      //handle errors
    } catch (IllegalStateException e) {
      log.error(
          String.format("The command '%s' could not be executed as the underling method could not be called.", command),
          e);
      caller.sendError(msg.getString("exception.unknown"));
    } catch (InvocationCommandException e) {
      // An InvocationCommandException can only be thrown if a thrown
      // Exception is not covered by our ExceptionConverter and is
      // therefore unintended behavior.
      caller.sendError(msg.getString("exception.unknown"));
      log.error(String.format("The command '%s' could not be executed.", command), e);
    } catch (InvalidUsageException e) {
      StrBuilder error = new StrBuilder();
      error.append(e.getSimpleUsageString("/"));
      if (e.getMessage() != null) {
        error.appendNewLine();
        error.append(e.getMessage());
      }
      if (e.isFullHelpSuggested()) {
        error.appendNewLine();
        error.append(e.getCommand().getDescription().getHelp());
      }
      caller.sendError(error.toString());
    } catch (CommandException e) {
      caller.sendError(e.getMessage());
    } catch (AuthorizationException e) {
      caller.sendError(msg.getString("exception.insufficient-permission"));
    }
  }

  /**
   * Returns whether the given String is a sub command of the {@code warp} command.
   *
   * @param str the String
   * @return {@code true} if the String is a sub command of the warp command
   */
  public boolean isSubCommand(String str) {
    //XXX this should probably be covered by unit tests
    CommandMapping mapping = dispatcher.get("warp");
    if (mapping == null || !(mapping.getCallable() instanceof Dispatcher)) {
      return false;
    }
    Dispatcher dispatcher = (Dispatcher) mapping.getCallable();
    return dispatcher.contains(str);
  }

  /**
   * Gets a Set with all commands usable for the given Actor. <p>The commands are represented as Strings. Each command
   * is prefixed with {@code /}, aliases are seperated by {@code |}.</p>
   *
   * @param forWhom the Actor for whom the returned commands should be usable
   * @return all usable commands as strings
   */
  public Set<String> getUsableCommands(Actor forWhom) {
    Set<String> usableCommands = new TreeSet<String>();

    CommandLocals locals = new CommandLocals();
    locals.put(Actor.class, forWhom);
    flattenCommands(usableCommands, locals, "", dispatcher);

    return usableCommands;
  }

  /**
   * Adds a all commands from the given Dispatcher to the given Collection, transforming them into Strings that include
   * the full command string as the user would enter it. Commands that are not usable under the given CommandLocals are
   * excluded and the given prefix is added before all commands. <p>This algorithm actually calls every Command, it is
   * <b> not</b> lazy.</p>
   *
   * @param entries    the Collection the Commands are added to
   * @param locals     the CommandLocals
   * @param prefix     the prefix
   * @param dispatcher the Dispatcher to add
   */
  private void flattenCommands(Collection<String> entries, CommandLocals locals, String prefix, Dispatcher dispatcher) {
    for (CommandMapping rootCommand : dispatcher.getCommands()) {
      flattenCommands(entries, locals, prefix, rootCommand);
    }
  }

  /**
   * Adds a all commands from the given CommandMapping to the given Collection, transforming them into Strings that
   * include the full command string as the user would enter it. Commands that are not usable under the given
   * CommandLocals are excluded and the given prefix is added before all commands. <p>This algorithm actually calls
   * every Command, it is <b> not</b> lazy.</p>
   *
   * @param entries the Collection the Commands are added to
   * @param locals  the CommandLocals
   * @param prefix  the prefix
   * @param current the CommandMapping to add
   */
  private void flattenCommands(Collection<String> entries, CommandLocals locals, String prefix,
                               CommandMapping current) {
    CommandCallable currentCallable = current.getCallable();
    if (!currentCallable.testPermission(locals)) {
      return;
    }
    StrBuilder builder = new StrBuilder().append(prefix).append(prefix.isEmpty() ? '/' : ' ');

    //subcommands
    if (currentCallable instanceof Dispatcher) {
      builder.append(current.getPrimaryAlias());
      flattenCommands(entries, locals, builder.toString(), (Dispatcher) currentCallable);
    } else {
      // the end
      builder.appendWithSeparators(current.getAllAliases(), "|");
      builder.append(' ');
      builder.append(current.getDescription().getUsage());
      entries.add(builder.toString());
    }

  }
}
