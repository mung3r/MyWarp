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

import com.sk89q.intake.CommandException;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.fluent.CommandGraph;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.util.auth.AuthorizationException;

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
import me.taylorkelly.mywarp.bukkit.permissions.BukkitPermissionsRegistration;
import me.taylorkelly.mywarp.bukkit.permissions.GroupResolver;
import me.taylorkelly.mywarp.bukkit.permissions.GroupResolverManager;
import me.taylorkelly.mywarp.bukkit.profile.SquirrelIdProfileService;
import me.taylorkelly.mywarp.bukkit.timer.BukkitDurationProvider;
import me.taylorkelly.mywarp.bukkit.timer.BukkitTimerService;
import me.taylorkelly.mywarp.bukkit.util.ActorAuthorizer;
import me.taylorkelly.mywarp.bukkit.util.ActorBindung;
import me.taylorkelly.mywarp.bukkit.util.ExceptionConverter;
import me.taylorkelly.mywarp.bukkit.util.I18nInvokeHandler;
import me.taylorkelly.mywarp.bukkit.util.PlayerBinding;
import me.taylorkelly.mywarp.bukkit.util.ProfileBinding;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding;
import me.taylorkelly.mywarp.bukkit.util.WarpDispatcher;
import me.taylorkelly.mywarp.bukkit.util.economy.EconomyInvokeHandler;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;

import net.milkbowl.vault.economy.Economy;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;

import java.io.File;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.annotation.Nullable;

/**
 * The MyWarp plugin instance when running on Bukkit.
 */
public class MyWarpPlugin extends JavaPlugin implements Platform {

  private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

  private final File bundleFolder = new File(getDataFolder(), "lang");
  private final ResourceBundle.Control control = new ResourceBundle.Control() {
  }; // new FolderSourcedControl(bundleFolder);

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

    groupResolverManager = new GroupResolverManager();
    profileService = new SquirrelIdProfileService();

    // setup the configurations
    settings =
        new BukkitSettings(new File(getDataFolder(), "config.yml"),
                           YamlConfiguration.loadConfiguration(getTextResource("config.yml")), adapter); // NON-NLS

    adapter = new BukkitAdapter(groupResolverManager, profileService, settings);

    // setup the Game
    game = new BukkitGame(new BukkitExecutor(this), adapter);

    // try to setup the core
    try {
      myWarp = new MyWarp(this);
    } catch (InitializationException e) {
      getLogger().log(Level.SEVERE,
                      "A critical failure has been encountered and MyWarp is unable to continue. MyWarp will be "
                      + "disabled.", e);
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    WelcomeEditorFactory welcomeEditorFactory = new WelcomeEditorFactory(this, adapter);
    WarpAcceptancePromptFactory warpAcceptancePromptFactory = new WarpAcceptancePromptFactory(this, adapter);

    // command registration
    ExceptionConverter exceptionConverter = new ExceptionConverter();
    PlayerBinding playerBinding = new PlayerBinding(game);
    WarpBinding warpBinding = new WarpBinding(myWarp.getWarpManager());

    ParametricBuilder builder = new ParametricBuilder();
    builder.setAuthorizer(new ActorAuthorizer());
    builder.addBinding(new ActorBindung());
    builder.addBinding(playerBinding);
    builder.addBinding(new ProfileBinding(profileService));
    builder.addBinding(warpBinding);
    builder.addExceptionConverter(exceptionConverter);

    builder.addInvokeListener(new EconomyInvokeHandler(myWarp.getEconomyManager()));
    builder.addInvokeListener(new I18nInvokeHandler());

    UsageCommands usageCommands = new UsageCommands(myWarp);

    // @formatter:off
    dispatcher = new CommandGraph().builder(builder)
            .commands()
              .registerMethods(usageCommands)
              .group(new WarpDispatcher(exceptionConverter, playerBinding, warpBinding, usageCommands), "warp",
                     "myWarp", "mw") //NON-NLS NON-NLS
                .describeAs("warp-to.description")
                .registerMethods(new InformativeCommands(myWarp.getLimitManager(), settings, myWarp.getWarpManager()))
                .registerMethods(new ManagementCommands(myWarp, welcomeEditorFactory))
                .registerMethods(new SocialCommands(game, myWarp.getLimitManager(), profileService,
                                                    warpAcceptancePromptFactory))
                .registerMethods(new UtilityCommands(myWarp))
                .group("import", "migrate") //NON-NLS NON-NLS
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
      Plugin dynmap = getServer().getPluginManager().getPlugin("dynmap"); // NON-NLS
      if (dynmap != null && dynmap.isEnabled()) {
        new DynmapMarkers(this, (DynmapCommonAPI) dynmap, myWarp.getWarpManager());
      } else {
        getLogger().severe("Failed to hook into Dynmap. Disabling Dynmap support."); // NON-NLS
      }
    }

    // register world access permissions
    for (World loadedWorld : Bukkit.getWorlds()) {
      Permission perm = new Permission("myWarp.warp.world." + loadedWorld.getName()); // NON-NLS
      perm.addParent("myWarp.warp.world.*", true); // NON-NLS
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
  public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
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
      getLogger().log(Level.SEVERE, String
          .format("The command '%s' could not be executed as the underling method could not be called.",
                  cmd.toString()), e);
      actor.sendError(MESSAGES.getString("exception.unknown"));
    } catch (InvocationCommandException e) {
      // An InvocationCommandException can only be thrown if a thrown
      // Exception is not covered by our ExceptionConverter and is
      // therefore unintended behavior.
      actor.sendError(MESSAGES.getString("exception.unknown"));
      getLogger().log(Level.SEVERE, String.format("The command '%s' could not be executed.", cmd), e);
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
        RegisteredServiceProvider<Economy>
            economyProvider =
            Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider == null) {
          getLogger().severe(
              "Failed to hook into Vault (EconomyProvider is null). EconomySupport will not be avilable."); // NON-NLS
        } else {
          economyService = new VaultService(economyProvider, adapter);
        }
      } catch (NoClassDefFoundError e) {
        getLogger().severe(
            "Failed to hook into Vault (EconomyProviderClass not available). EconomySupport will not be avilable.");
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
