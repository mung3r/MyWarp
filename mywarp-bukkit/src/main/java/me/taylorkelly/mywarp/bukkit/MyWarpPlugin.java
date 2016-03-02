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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Predicates;
import com.google.common.eventbus.Subscribe;

import me.taylorkelly.mywarp.InitializationException;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.settings.BukkitSettings;
import me.taylorkelly.mywarp.bukkit.util.conversation.AcceptancePromptFactory;
import me.taylorkelly.mywarp.bukkit.util.conversation.WelcomeEditorFactory;
import me.taylorkelly.mywarp.bukkit.util.permission.BukkitPermissionsRegistration;
import me.taylorkelly.mywarp.bukkit.util.permission.group.GroupResolver;
import me.taylorkelly.mywarp.bukkit.util.permission.group.GroupResolverFactory;
import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.event.PostInitializationEvent;
import me.taylorkelly.mywarp.platform.event.ReloadEvent;
import me.taylorkelly.mywarp.platform.event.WarpsLoadedEvent;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.FolderSourcedControl;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.warp.Warp;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

/**
 * The MyWarp plugin singleton when running on Bukkit.
 *
 * <p>This class is loaded by Bukkit when the plugin is initialized.</p>
 */
public final class MyWarpPlugin extends JavaPlugin {

  public static final String CONVERSATION_RESOURCE_BUNDLE_NAME = "me.taylorkelly.mywarp.lang.Conversations";
  public static final int CONVERSATION_TIMEOUT = 30;

  private static final Logger log = MyWarpLogger.getLogger(MyWarpPlugin.class);

  private final ResourceBundle.Control control = new FolderSourcedControl(new File(getDataFolder(), "lang"));
  private final Set<Closeable> closeables = Collections.newSetFromMap(new WeakHashMap<Closeable, Boolean>());

  private BukkitPlatform platform;
  private MyWarp myWarp;
  private GroupResolver groupResolver;
  private AcceptancePromptFactory acceptancePromptFactory;
  private WelcomeEditorFactory welcomeEditorFactory;

  @Nullable
  private DynmapMarker marker;

  @Override
  public void onEnable() {

    // initialize platform
    DynamicMessages.setControl(control);
    platform = new BukkitPlatform(this, YamlConfiguration.loadConfiguration(this.getTextResource("config.yml")));

    // setup the core
    try {
      myWarp = new MyWarp(platform);
    } catch (InitializationException e) {
      log.error("A critical failure has been encountered and MyWarp is unable to continue. MyWarp will be disabled.",
                e);
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    // register internal event listener
    myWarp.getEventBus().register(this);

    // further platform-specific objects
    groupResolver = GroupResolverFactory.createResolver();
    acceptancePromptFactory =
        new AcceptancePromptFactory(createConversationFactory(), myWarp.getAuthorizationResolver(), platform.getGame(),
                                    this);
    welcomeEditorFactory = new WelcomeEditorFactory(createConversationFactory());
  }

  @Override
  public void onDisable() {
    unregisterPermsAndListeners();

    //close any registered Closables
    for (Closeable closeable : closeables) {
      try {
        closeable.close();
      } catch (IOException e) {
        log.warn("Failed to close " + closeable.getClass().getCanonicalName(), e);
      }
    }
  }

  /**
   * Called when MyWarp's core is reloaded.
   *
   * @param event the event
   * @deprecated This method should only be called by the {@link com.google.common.eventbus.EventBus} and will be
   * privatized when support for legacy Guava versions is removed.
   */
  @Deprecated
  @Subscribe
  public void onCoreReload(ReloadEvent event) {
    // cleanup old stuff
    unregisterPermsAndListeners();
    platform.resetCapabilities();
    if (marker != null) {
      marker.clear();
    }

    // load new stuff
    getSettings().reload();
  }

  /**
   * Called after MyWarp's core is initialized.
   *
   * @param event the event
   * @deprecated This method should only be called by the {@link com.google.common.eventbus.EventBus} and will be
   * privatized when support for legacy Guava versions is removed.
   */
  @Deprecated
  @Subscribe
  public void onCoreInitialized(PostInitializationEvent event) {

    //register profile service listener
    getProfileCache().registerEvents(this);

    //register warp sign listener
    if (getSettings().isWarpSignsEnabled()) {
      new WarpSignListener(this, myWarp.createWarpSignHandler()).registerEvents(this);
    }

    // register world access permissions
    for (World loadedWorld : Bukkit.getWorlds()) {
      Permission perm = new Permission("mywarp.world-access." + loadedWorld.getName());
      perm.addParent("mywarp.world-access.*", true);
      BukkitPermissionsRegistration.INSTANCE.register(perm);
    }
  }

  /**
   * Called after MyWarp's core has successfully loaded Warps from database.
   *
   * @param event the event
   * @deprecated This method should only be called by the {@link com.google.common.eventbus.EventBus} and will be
   * privatized when support for legacy Guava versions is removed.
   */
  @Deprecated
  @Subscribe
  public void onWarpsLoadedEvent(WarpsLoadedEvent event) {
    if (getSettings().isDynmapEnabled()) {
      Plugin dynmap = getServer().getPluginManager().getPlugin("dynmap");
      if (dynmap != null && dynmap.isEnabled() && dynmap instanceof DynmapCommonAPI) {
        marker =
            new DynmapMarker((DynmapCommonAPI) dynmap, this, getSettings(), WarpUtils.isType(Warp.Type.PUBLIC),
                             platform.getGame());
        marker.addMarker(myWarp.getWarpManager().filter(Predicates.<Warp>alwaysTrue()));
        myWarp.getEventBus().register(marker);
      } else {
        log.error("Failed to hook into Dynmap. Disabling Dynmap support.");
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

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    Actor actor = wrap(sender);

    // set the locale for this command session
    LocaleManager.setLocale(actor.getLocale());

    // create the command string
    StrBuilder builder = new StrBuilder();
    builder.append(alias);
    for (String argument : args) {
      builder.appendSeparator(' ');
      builder.append(argument);
    }
    return myWarp.getCommandHandler().getSuggestions(builder.toString(), actor);
  }

  /**
   * Creates a new Actor instance by wrapping the given Bukkit {@code commandSender}.
   *
   * @param commandSender the CommandSender to wrap
   * @return a new Actor referencing the {@code commandSender}
   */
  public Actor wrap(CommandSender commandSender) {
    if (commandSender instanceof Player) {
      return wrap((Player) commandSender);
    }
    return new BukkitActor(commandSender, getSettings());
  }

  /**
   * Creates a new LocalPlayer instance by wrapping the given Bukkit {@code player}.
   *
   * @param player the Player to wrap
   * @return a new LocalPlayer referencing the {@code player}
   */
  public LocalPlayer wrap(Player player) {
    return new BukkitPlayer(player, getAcceptancePromptFactory(), getWelcomeEditorFactory(), getGroupResolver(),
                            getProfileCache(), getSettings());
  }

  /**
   * Gets the GroupResolver that resolve's a player's group.
   *
   * @return the configured GroupResolver
   */
  protected GroupResolver getGroupResolver() {
    checkState(groupResolver != null, "'groupResolver' is not yet initialized");
    return groupResolver;
  }

  /**
   * Gets the conversation factory for welcome editor conversations.
   *
   * @return the configured welcome editor factory
   */
  protected WelcomeEditorFactory getWelcomeEditorFactory() {
    checkState(welcomeEditorFactory != null, "'welcomeEditorFactory' is not yet initialized");
    return welcomeEditorFactory;
  }

  /**
   * Gets the conversation factory for warp acceptance conversations.
   *
   * @return the configured acceptance prompt factory
   */
  protected AcceptancePromptFactory getAcceptancePromptFactory() {
    checkState(acceptancePromptFactory != null, "'acceptancePromptFactory' is not yet initialized");
    return acceptancePromptFactory;
  }

  /**
   * Gets the BukkitSettings instance that provides access to MyWarp's settings.
   *
   * @return the configured settings
   */
  protected BukkitSettings getSettings() {
    checkState(platform != null, "'platform' is not yet initialized");
    return platform.getSettings();
  }

  /**
   * Gets the ProfileCache that stored Profiles for known players.
   *
   * @return the configured ProfileCache
   */
  protected SquirrelIdProfileCache getProfileCache() {
    checkState(platform != null, "'platform' is not yet initialized");
    return platform.getProfileCache();
  }

  /**
   * Registers the given {@code closable} for closure when the plugin is disabled.
   *
   * <p>Registered Closables will be stored within a {@link java.lang.ref.WeakReference}. If MyWarp is disabled by
   * Bukkit and the reference is still valid, {@link Closeable#close()} is invoked.</p>
   *
   * @param closeable the Closable to register
   */
  protected void registerClosable(Closeable closeable) {
    closeables.add(closeable);
  }

  private ConversationFactory createConversationFactory() {
    return new ConversationFactory(this).withModality(true).withTimeout(CONVERSATION_TIMEOUT);
  }

  private void unregisterPermsAndListeners() {
    HandlerList.unregisterAll(this);
    BukkitPermissionsRegistration.INSTANCE.unregisterAll();
  }

}
