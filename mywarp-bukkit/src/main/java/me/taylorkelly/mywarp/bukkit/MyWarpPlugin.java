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

package me.taylorkelly.mywarp.bukkit;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.sk89q.intake.CommandCallable;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.InvalidUsageException;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.fluent.CommandGraph;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.util.auth.AuthorizationException;
import com.sk89q.intake.util.i18n.ResourceProvider;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.InitializationException;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.Platform;
import me.taylorkelly.mywarp.bukkit.commands.ImportCommands;
import me.taylorkelly.mywarp.bukkit.commands.InformativeCommands;
import me.taylorkelly.mywarp.bukkit.commands.ManagementCommands;
import me.taylorkelly.mywarp.bukkit.commands.SocialCommands;
import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.bukkit.commands.UtilityCommands;
import me.taylorkelly.mywarp.bukkit.conversation.WarpAcceptancePromptFactory;
import me.taylorkelly.mywarp.bukkit.conversation.WelcomeEditorFactory;
import me.taylorkelly.mywarp.bukkit.economy.BukkitFeeProvider;
import me.taylorkelly.mywarp.bukkit.economy.VaultService;
import me.taylorkelly.mywarp.bukkit.limits.BukkitLimitProvider;
import me.taylorkelly.mywarp.bukkit.markers.DynmapMarkers;
import me.taylorkelly.mywarp.bukkit.timer.BukkitDurationProvider;
import me.taylorkelly.mywarp.bukkit.timer.BukkitTimerService;
import me.taylorkelly.mywarp.bukkit.util.ActorAuthorizer;
import me.taylorkelly.mywarp.bukkit.util.ActorBindung;
import me.taylorkelly.mywarp.bukkit.util.CommandResourceProvider;
import me.taylorkelly.mywarp.bukkit.util.EncodedControl;
import me.taylorkelly.mywarp.bukkit.util.ExceptionConverter;
import me.taylorkelly.mywarp.bukkit.util.FallbackDispatcher;
import me.taylorkelly.mywarp.bukkit.util.FileBinding;
import me.taylorkelly.mywarp.bukkit.util.IntakeResourceProvider;
import me.taylorkelly.mywarp.bukkit.util.PlayerBinding;
import me.taylorkelly.mywarp.bukkit.util.ProfileBinding;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding;
import me.taylorkelly.mywarp.bukkit.util.economy.EconomyInvokeHandler;
import me.taylorkelly.mywarp.bukkit.util.permissions.BukkitPermissionsRegistration;
import me.taylorkelly.mywarp.bukkit.util.permissions.group.GroupResolver;
import me.taylorkelly.mywarp.bukkit.util.permissions.group.GroupResolverManager;
import me.taylorkelly.mywarp.bukkit.util.profile.SquirrelIdProfileService;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;

import net.milkbowl.vault.economy.Economy;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.slf4j.Logger;

import java.io.File;
import java.util.ResourceBundle;

import javax.annotation.Nullable;

/**
 * The MyWarp plugin instance when running on Bukkit.
 */
public class MyWarpPlugin extends JavaPlugin implements Platform {

  private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);
  private static final Logger log = MyWarpLogger.getLogger(MyWarpPlugin.class);

  private final File bundleFolder = new File(getDataFolder(), "lang");
  private final ResourceBundle.Control control = new EncodedControl(Charsets.UTF_8);

  private GroupResolverManager groupResolverManager;
  private SquirrelIdProfileService profileService;
  private BukkitSettings settings;
  private BukkitAdapter adapter;
  private BukkitGame game;

  private MyWarp myWarp;

  private Dispatcher dispatcher;

  @Nullable
  private VaultService economyService;
  @Nullable
  private BukkitTimerService timerService;
  @Nullable
  private BukkitFeeProvider feeProvider;
  @Nullable
  private BukkitLimitProvider limitProvider;
  @Nullable
  private BukkitDurationProvider durationProvider;

  // -- JavaPlugin methods

  @Override
  public void onEnable() {

    // create the data-folder
    getDataFolder().mkdirs();

    profileService = new SquirrelIdProfileService(new File(getDataFolder(), "profiles.db"));
    groupResolverManager = new GroupResolverManager();
    adapter = new BukkitAdapter(this);

    // setup the configurations
    settings =
        new BukkitSettings(new File(getDataFolder(), "config.yml"),
                           YamlConfiguration.loadConfiguration(getTextResource("config.yml")), adapter);

    // setup the Game
    game = new BukkitGame(new BukkitExecutor(this), adapter);

    // try to setup the core
    try {
      myWarp = new MyWarp(this);
    } catch (InitializationException e) {
      log.error("A critical failure has been encountered and MyWarp is unable to continue. MyWarp will be disabled.",
                e);
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    // command registration
    ResourceProvider resourceProvider = new IntakeResourceProvider(control);
    ExceptionConverter exceptionConverter = new ExceptionConverter();
    PlayerBinding playerBinding = new PlayerBinding(game);
    WarpBinding warpBinding = new WarpBinding(myWarp.getWarpManager());

    ParametricBuilder builder = new ParametricBuilder(resourceProvider);
    builder.setAuthorizer(new ActorAuthorizer());
    builder.setExternalResourceProvider(new CommandResourceProvider(control));
    builder.addBinding(new ActorBindung());
    builder.addBinding(new FileBinding(getDataFolder()));
    builder.addBinding(playerBinding);
    builder.addBinding(new ProfileBinding(profileService));
    builder.addBinding(warpBinding);
    builder.addExceptionConverter(exceptionConverter);

    builder.addInvokeListener(new EconomyInvokeHandler(myWarp.getEconomyManager()));

    UsageCommands usageCommands = new UsageCommands(myWarp);

    //XXX this should be covered by unit tests
    CommandCallable fallback = Iterables.getOnlyElement(builder.build(usageCommands).values());

    // @formatter:off
    dispatcher = new CommandGraph(resourceProvider).builder(builder)
            .commands()
              .registerMethods(usageCommands)
              .group(new FallbackDispatcher(resourceProvider, fallback), "warp", "myWarp", "mw")
                .describeAs("warp-to.description")
                .registerMethods(new InformativeCommands(myWarp.getLimitManager(), settings, myWarp.getWarpManager()))
                .registerMethods(new ManagementCommands(myWarp, this, new WelcomeEditorFactory(this, adapter)))
                .registerMethods(new SocialCommands(game, myWarp.getLimitManager(), profileService,
                                                    new WarpAcceptancePromptFactory(this, adapter)))
                .registerMethods(new UtilityCommands(myWarp, this))
                .group("import", "migrate")
                  .describeAs("import.description")
                  .registerMethods(new ImportCommands(myWarp))
              .graph()
            .getDispatcher();
    // @formatter:on

    setupPlugin();
  }

  /**
   * Sets up the plugin. Calling this method will setup all functions that depend on configurations and are, by design,
   * optional.
   */
  private void setupPlugin() {
    profileService.registerEvents(this);

    if (settings.isWarpSignsEnabled()) {
      new WarpSignListener(adapter, myWarp.getWarpSignManager()).registerEvents(this);
    }

    if (settings.isDynmapEnabled()) {
      Plugin dynmap = getServer().getPluginManager().getPlugin("dynmap");
      if (dynmap != null && dynmap.isEnabled()) {
        new DynmapMarkers(this, (DynmapCommonAPI) dynmap, myWarp.getWarpManager());
      } else {
        log.error("Failed to hook into Dynmap. Disabling Dynmap support.");
      }
    }

    // register world access permissions
    for (World loadedWorld : Bukkit.getWorlds()) {
      Permission perm = new Permission("myWarp.warp.world." + loadedWorld.getName());
      perm.addParent("myWarp.warp.world.*", true);
      BukkitPermissionsRegistration.INSTANCE.register(perm);
    }
  }

  @Override
  public void onDisable() {
    HandlerList.unregisterAll(this);
    BukkitPermissionsRegistration.INSTANCE.unregisterAll();

    if (myWarp != null) {
      myWarp.unload();
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // create the CommandLocals
    CommandLocals locals = new CommandLocals();
    Actor actor = wrap(sender);
    locals.put(Actor.class, actor);

    // set the locale for this command session
    LocaleManager.setLocale(actor.getLocale());

    // No parent commands
    String[] parentCommands = new String[0];

    // create the command string
    StrBuilder builder = new StrBuilder();
    builder.append(label);
    for (String argument : args) {
      builder.appendSeparator(' ');
      builder.append(argument);
    }

    // call the command
    try {
      return dispatcher.call(builder.toString(), locals, parentCommands);
    } catch (IllegalStateException e) {
      log.error(String.format("The command '%s' could not be executed as the underling method could not be called.",
                              cmd.toString()), e);
      actor.sendError(MESSAGES.getString("exception.unknown"));
    } catch (InvocationCommandException e) {
      // An InvocationCommandException can only be thrown if a thrown
      // Exception is not covered by our ExceptionConverter and is
      // therefore unintended behavior.
      actor.sendError(MESSAGES.getString("exception.unknown"));
      log.error(String.format("The command '%s' could not be executed.", cmd), e);
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
      actor.sendError(error.toString());
    } catch (CommandException e) {
      actor.sendError(e.getMessage());
    } catch (AuthorizationException e) {
      actor.sendError(MESSAGES.getString("exception.insufficient-permission"));
    }
    return true;
  }

  // -- custom methods

  /**
   * Wraps a CommandSender to an Actor.
   *
   * @param sender the CommandSender
   * @return the Actor representing the given CommandSender
   */
  public Actor wrap(CommandSender sender) {
    if (sender instanceof Player) {
      return adapter.adapt((Player) sender);
    }
    return new BukkitActor(sender, settings.getLocalizationDefaultLocale());
  }

  /**
   * Gets the adapter.
   *
   * @return the adapter
   */
  public BukkitAdapter getAdapter() {
    return adapter;
  }

  /**
   * Gets the GroupResolver.
   *
   * @return the GroupResolver
   */
  public GroupResolver getGroupResolver() {
    return groupResolverManager;
  }

  /**
   * gets the Dispatcher.
   *
   * @return the Dispatcher
   */
  public Dispatcher getDispatcher() {
    return dispatcher;
  }
  // -- Platform methods

  @Override
  public void reload() {
    // cleanup old stuff
    HandlerList.unregisterAll(this);
    BukkitPermissionsRegistration.INSTANCE.unregisterAll();

    // load new stuff
    settings.reload();
    setupPlugin();
  }

  @Override
  public ResourceBundle.Control getResourceBundleControl() {
    return control;
  }

  @Override
  public Game getGame() {
    return game;
  }

  @Override
  public BukkitSettings getSettings() {
    return settings;
  }

  @Override
  public SquirrelIdProfileService getProfileService() {
    return profileService;
  }

  @Override
  public VaultService getEconomyService() {
    if (economyService == null) {
      try {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider == null) {
          log.error("Failed to hook into Vault (EconomyProvider is null). EconomySupport will not be available.");
        } else {
          economyService = new VaultService(economyProvider, adapter);
        }
      } catch (NoClassDefFoundError e) {
        log.error(
            "Failed to hook into Vault (EconomyProviderClass not available). EconomySupport will not be available.");
      }
      throw new UnsupportedOperationException();
    }

    return economyService;
  }

  @Override
  public BukkitTimerService getTimerService() {
    if (timerService == null) {
      timerService = new BukkitTimerService(this);
    }
    return timerService;
  }

  @Override
  public BukkitFeeProvider getFeeProvider() {
    if (feeProvider == null) {
      feeProvider =
          new BukkitFeeProvider(settings.getEconomyConfiguredFeeBundles(), settings.getEconomyDefaultFeeBundle());
    }
    return feeProvider;
  }

  @Override
  public BukkitLimitProvider getLimitProvider() {
    if (limitProvider == null) {
      limitProvider =
          new BukkitLimitProvider(settings.getLimitsConfiguredLimitBundles(), settings.getLimitsDefaultLimitBundle());
    }
    return limitProvider;
  }

  @Override
  public BukkitDurationProvider getDurationProvider() {
    if (durationProvider == null) {
      durationProvider =
          new BukkitDurationProvider(settings.getTimersConfiguredDurationBundles(),
                                     settings.getTimersDefaultDurationBundle());
    }
    return durationProvider;
  }

}
