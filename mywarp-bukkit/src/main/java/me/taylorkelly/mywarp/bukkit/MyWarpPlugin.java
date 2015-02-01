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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.MyWarpException;
import me.taylorkelly.mywarp.bukkit.commands.ImportCommands;
import me.taylorkelly.mywarp.bukkit.commands.InformativeCommands;
import me.taylorkelly.mywarp.bukkit.commands.ManagementCommands;
import me.taylorkelly.mywarp.bukkit.commands.SocialCommands;
import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.bukkit.commands.UtilityCommands;
import me.taylorkelly.mywarp.bukkit.conversation.WarpAcceptancePromptFactory;
import me.taylorkelly.mywarp.bukkit.conversation.WelcomeEditorFactory;
import me.taylorkelly.mywarp.bukkit.markers.DynmapMarkers;
import me.taylorkelly.mywarp.bukkit.permissions.BukkitPermissionsRegistration;
import me.taylorkelly.mywarp.bukkit.permissions.GroupResolver;
import me.taylorkelly.mywarp.bukkit.permissions.GroupResolverManager;
import me.taylorkelly.mywarp.bukkit.profile.SquirrelIdProfileService;
import me.taylorkelly.mywarp.bukkit.util.ActorAuthorizer;
import me.taylorkelly.mywarp.bukkit.util.ActorBindung;
import me.taylorkelly.mywarp.bukkit.util.ExceptionConverter;
import me.taylorkelly.mywarp.bukkit.util.I18nInvokeHandler;
import me.taylorkelly.mywarp.bukkit.util.PlayerBinding;
import me.taylorkelly.mywarp.bukkit.util.ProfileBinding;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding;
import me.taylorkelly.mywarp.bukkit.util.WarpDispatcher;
import me.taylorkelly.mywarp.bukkit.util.economy.EconomyInvokeHandler;
import me.taylorkelly.mywarp.util.PropertiesUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.util.profile.ProfileService;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;

import com.sk89q.intake.CommandException;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.fluent.CommandGraph;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.util.auth.AuthorizationException;

/**
 * The MyWarp plugin instance when running on Bukkit.
 */
public class MyWarpPlugin extends JavaPlugin {

    private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

    private final File bundleFolder = new File(getDataFolder(), "lang");
    private final ResourceBundle.Control control = new ResourceBundle.Control() {

    }; // new FolderSourcedControl(bundleFolder);

    private MyWarp mywarp;
    private Dispatcher dispatcher;
    private BukkitAdapter adapter;
    private BukkitSettings settings;
    private GroupResolverManager groupResolverManager;
    private SquirrelIdProfileService profileService;
    private WelcomeEditorFactory welcomeEditorFactory;
    private WarpAcceptancePromptFactory warpAcceptancePromptFactory;

    @Override
    public void onEnable() {

        // // create bundle path
        // if (!bundleFolder.exists()) {
        // bundleFolder.mkdirs();
        // }
        // // create bundles
        // // XXX finalize bundle names, edit POM accordingly, add Locales
        // for (String baseName :
        // Arrays.asList("me.taylorkelly.mywarp.lang.Commands",
        // "me.taylorkelly.mywarp.lang.Conversations",
        // "me.taylorkelly.mywarp.lang.DynmapMarkers",
        // "me.taylorkelly.mywarp.lang.Economy",
        // "me.taylorkelly.mywarp.lang.StringPaginator",
        // "me.taylorkelly.mywarp.lang.Timers",
        // "me.taylorkelly.mywarp.lang.Warp",
        // "me.taylorkelly.mywarp.lang.WarpSignManager")) {
        // try {
        // createBundle(baseName);
        // } catch (IOException e) {
        // getLogger().log(
        // Level.SEVERE,
        // "Failed to create default resource bundle '" + baseName
        // + "', using build in defaults.", e);
        // }
        // }

        groupResolverManager = new GroupResolverManager();
        profileService = new SquirrelIdProfileService();

        adapter = new BukkitAdapter(profileService, groupResolverManager);

        // setup the configurations
        settings = new BukkitSettings(new File(getDataFolder(), "config.yml"),
                YamlConfiguration.loadConfiguration(getTextResource("config.yml")), adapter); // NON-NLS

        try {
            mywarp = new MyWarp(new BukkitPlatform(this));
        } catch (MyWarpException e) {
            getLogger()
                    .log(Level.SEVERE,
                            "A critical failure has been encountered and MyWarp is unable to continue. MyWarp will be disabled.", // NON-NLS
                            e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        welcomeEditorFactory = new WelcomeEditorFactory(this, adapter);
        warpAcceptancePromptFactory = new WarpAcceptancePromptFactory(this, adapter);

        // command registration
        ExceptionConverter exceptionConverter = new ExceptionConverter();
        PlayerBinding playerBinding = new PlayerBinding();
        WarpBinding warpBinding = new WarpBinding();
        UsageCommands usageCommands = new UsageCommands();

        ParametricBuilder builder = new ParametricBuilder();
        builder.setAuthorizer(new ActorAuthorizer());
        builder.addBinding(new ActorBindung());
        builder.addBinding(playerBinding);
        builder.addBinding(new ProfileBinding());
        builder.addBinding(warpBinding);
        builder.addExceptionConverter(exceptionConverter);

        builder.addInvokeListener(new EconomyInvokeHandler());
        builder.addInvokeListener(new I18nInvokeHandler());

        // @formatter:off
        dispatcher = new CommandGraph().builder(builder)
                .commands()
                .registerMethods(usageCommands)
                .group(new WarpDispatcher(exceptionConverter, playerBinding, warpBinding, usageCommands), "warp", "mywarp", "mw") //NON-NLS NON-NLS NON-NLS
                .describeAs("warp-to.description")
                    .registerMethods(new InformativeCommands())
                    .registerMethods(new ManagementCommands(welcomeEditorFactory))
                    .registerMethods(new SocialCommands(warpAcceptancePromptFactory))
                    .registerMethods(new UtilityCommands())
                        .group("import", "migrate") //NON-NLS NON-NLS
                        .describeAs("import.description")
                        .registerMethods(new ImportCommands())
                        .graph()
                .getDispatcher();
        // @formatter:on

        setupPlugin();
    }

    /**
     * Creates or updates the ResourceBundle with the given name in the
     * configured bundleFolder.
     * 
     * @param baseName
     *            the name of the ResourceBundle
     * @throws IOException
     *             if a read/write error occurs
     */
    private void createBundle(String baseName) throws IOException {
        for (String localePrefix : Arrays.asList("")) {
            baseName = baseName + localePrefix + ".properties"; // NON-NLS

            File bundle = new File(bundleFolder, baseName);
            if (!bundle.exists()) {
                bundle.createNewFile();
            }
            Properties defaults = new Properties();
            Reader reader = getTextResource("lang/" + baseName); // NON-NLS
            try {
                defaults.load(reader);
                reader.close();
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            PropertiesUtils.copyMissing(bundle, defaults);
        }
    }

    /**
     * Sets up the plugin. Calling this method will setup all functions that
     * depend on configurations and are, by design, optional.
     */
    private void setupPlugin() {
        profileService.registerEvents(this);

        if (settings.isWarpSignsEnabled()) {
            new WarpSignManager(settings.getWarpSignsIdentifiers(), MyWarp.getInstance().getWarpManager(),
                    adapter).registerEvents(this);
        }

        if (settings.isDynmapEnabled()) {
            Plugin dynmap = getServer().getPluginManager().getPlugin("dynmap"); // NON-NLS
            if (dynmap != null && dynmap.isEnabled()) {
                new DynmapMarkers(this, (DynmapCommonAPI) dynmap, MyWarp.getInstance().getWarpManager());
            } else {
                getLogger().severe("Failed to hook into Dynmap. Disabling Dynmap support."); // NON-NLS
            }
        }

        // register world access permissions
        for (World loadedWorld : Bukkit.getWorlds()) {
            Permission perm = new Permission("mywarp.warp.world." + loadedWorld.getName()); // NON-NLS
            perm.addParent("mywarp.warp.world.*", true); // NON-NLS
            BukkitPermissionsRegistration.INSTANCE.register(perm);
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        BukkitPermissionsRegistration.INSTANCE.unregisterAll();

        if (mywarp != null) {
            mywarp.unload();
        }
    }

    /**
     * Reloads all functions that are specific for Bukkit.
     */
    protected void reload() {
        // cleanup old stuff
        HandlerList.unregisterAll(this);
        BukkitPermissionsRegistration.INSTANCE.unregisterAll();

        // load new stuff
        settings.reload();
        setupPlugin();
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
            getLogger()
                    .log(Level.SEVERE,
                            String.format(
                                    "The command '%s' could not be executed as the underling method could not be called.", // NON-NLS
                                    cmd.toString()), e);
            actor.sendError(MESSAGES.getString("exception.unknown"));
        } catch (InvocationCommandException e) {
            // An InvocationCommandException can only be thrown if a thrown
            // Exception is not covered by our ExceptionConverter and is
            // therefore unintended behavior.
            actor.sendError(MESSAGES.getString("exception.unknown"));
            getLogger().log(Level.SEVERE, String.format("The command '%s' could not be executed.", cmd), e); // NON-NLS
        } catch (CommandException e) {
            actor.sendError(e.getMessage());
            e.printStackTrace(); // DEBUG CommandException stacktrace
        } catch (AuthorizationException e) {
            actor.sendError(MESSAGES.getString("exception.insufficient-permission"));
        }
        return true;
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
     * Gets the welcomeEditorFactory.
     *
     * @return the welcomeEditorFactory
     */
    public WelcomeEditorFactory getWelcomeEditorFactory() {
        return welcomeEditorFactory;
    }

    /**
     * Gets the warpAcceptancePromptFactory.
     *
     * @return the warpAcceptancePromptFactory
     */
    public WarpAcceptancePromptFactory getWarpAcceptancePromptFactory() {
        return warpAcceptancePromptFactory;
    }

    /**
     * Gets the control.
     *
     * @return the control
     */
    public ResourceBundle.Control getResourceBundleControl() {
        return control;
    }

    /**
     * Gets the settings.
     *
     * @return the settings
     */
    public BukkitSettings getSettings() {
        return settings;
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
     * Gets the profileService.
     *
     * @return the profileService
     */
    public ProfileService getProfileService() {
        return profileService;
    }

    /**
     * Wraps a CommandSender to an Actor.
     * 
     * @param sender
     *            the CommandSender
     * @return the Actor representing the given CommandSender
     */
    public Actor wrap(CommandSender sender) {
        if (sender instanceof Player) {
            return adapter.adapt((Player) sender);
        }
        return new BukkitActor(sender);
    }

}
