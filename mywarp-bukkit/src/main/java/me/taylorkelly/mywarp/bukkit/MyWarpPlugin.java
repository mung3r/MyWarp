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

package me.taylorkelly.mywarp.bukkit;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.InitializationException;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.Platform;
import me.taylorkelly.mywarp.bukkit.conversation.AcceptancePromptFactory;
import me.taylorkelly.mywarp.bukkit.conversation.WelcomeEditorFactory;
import me.taylorkelly.mywarp.bukkit.economy.BukkitFeeProvider;
import me.taylorkelly.mywarp.bukkit.economy.VaultProvider;
import me.taylorkelly.mywarp.bukkit.limits.BukkitLimitProvider;
import me.taylorkelly.mywarp.bukkit.markers.DynmapMarkers;
import me.taylorkelly.mywarp.bukkit.timer.BukkitDurationProvider;
import me.taylorkelly.mywarp.bukkit.timer.BukkitTimerService;
import me.taylorkelly.mywarp.bukkit.util.permissions.BukkitPermissionsRegistration;
import me.taylorkelly.mywarp.bukkit.util.permissions.group.GroupResolver;
import me.taylorkelly.mywarp.bukkit.util.permissions.group.GroupResolverManager;
import me.taylorkelly.mywarp.bukkit.util.profile.SquirrelIdProfileService;
import me.taylorkelly.mywarp.sign.WarpSignManager;
import me.taylorkelly.mywarp.storage.ConnectionConfiguration;
import me.taylorkelly.mywarp.storage.RelationalDataService;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.i18n.FolderSourcedControl;
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

/**
 * The MyWarp plugin instance when running on Bukkit.
 */
public final class MyWarpPlugin extends JavaPlugin implements Platform {

  public static final String CONVERSATIONS_RESOURCE_BUNDLE_NAME = "me.taylorkelly.mywarp.lang.Conversations";

  private static final Logger log = MyWarpLogger.getLogger(MyWarpPlugin.class);

  private final File bundleFolder = new File(getDataFolder(), "lang");
  private final ResourceBundle.Control control = new FolderSourcedControl(bundleFolder);
  private final Set<Closeable> closeables = Collections.newSetFromMap(new WeakHashMap<Closeable, Boolean>());

  private SingleConnectionDataService dataService;
  private GroupResolverManager groupResolverManager;
  private SquirrelIdProfileService profileService;
  private BukkitSettings settings;
  private BukkitAdapter adapter;
  private BukkitGame game;
  private AcceptancePromptFactory acceptancePromptFactory;
  private WelcomeEditorFactory welcomeEditorFactory;

  private MyWarp myWarp;

  @Nullable
  private VaultProvider economyService;
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
    //TODO fail for SQLite driver 3.7.x...
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

    acceptancePromptFactory = new AcceptancePromptFactory(this, myWarp.getAuthorizationService(), game);
    welcomeEditorFactory = new WelcomeEditorFactory(this);

    setupPlugin();
  }

  /**
   * Sets up the plugin. Calling this method will setup all functions that depend on configurations and are, by design,
   * optional.
   */
  private void setupPlugin() {
    profileService.registerEvents(this);

    if (settings.isWarpSignsEnabled()) {
      new WarpSignListener(adapter, new WarpSignManager(settings.getWarpSignsIdentifiers(), myWarp))
          .registerEvents(this);
    }

    if (settings.isDynmapEnabled()) {
      Plugin dynmap = getServer().getPluginManager().getPlugin("dynmap");
      if (dynmap != null && dynmap.isEnabled()) {
        new DynmapMarkers(this, (DynmapCommonAPI) dynmap, myWarp.getWarpManager(), myWarp.getEventBus(), game);
      } else {
        log.error("Failed to hook into Dynmap. Disabling Dynmap support.");
      }
    }

    // register world access permissions
    for (World loadedWorld : Bukkit.getWorlds()) {
      Permission perm = new Permission("mywarp.world-access." + loadedWorld.getName());
      perm.addParent("mywarp.world-access.*", true);
      BukkitPermissionsRegistration.INSTANCE.register(perm);
    }
  }

  @Override
  public void onDisable() {
    HandlerList.unregisterAll(this);
    BukkitPermissionsRegistration.INSTANCE.unregisterAll();

    for (Closeable closeable : closeables) {
      try {
        closeable.close();
      } catch (IOException e) {
        log.warn("Failed to close " + closeable.getClass().getCanonicalName(), e);
      }
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    Actor actor = wrap(sender);

    // set the locale for this command session
    LocaleManager.setLocale(actor.getLocale());

    // create the command string
    StrBuilder builder = new StrBuilder();
    builder.append(label);
    for (String argument : args) {
      builder.appendSeparator(' ');
      builder.append(argument);
    }

    myWarp.getCommandHandler().callCommand(builder.toString(), actor);
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

  public WelcomeEditorFactory getWelcomeEditorFactory() {
    return welcomeEditorFactory;
  }

  public AcceptancePromptFactory getAcceptancePromptFactory() {
    return acceptancePromptFactory;
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
  public RelationalDataService createDataService(ConnectionConfiguration configuration) {
    RelationalDataService ret = new SingleConnectionDataService(configuration);

    //add weak reference so it can be closed on shutdown if not done by the caller
    closeables.add(ret);

    return ret;
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
  public VaultProvider getEconomyService() {
    if (economyService == null) {
      try {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
          economyService = new VaultProvider(economyProvider, adapter);
        } else {
          log.error("Failed to hook into Vault (EconomyProvider is null). Economy support will not be available.");
          throw new UnsupportedOperationException();
        }
      } catch (NoClassDefFoundError e) {
        log.error(
            "Failed to hook into Vault (EconomyProviderClass not available). Economy support will not be available.");
        throw new UnsupportedOperationException();
      }
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
