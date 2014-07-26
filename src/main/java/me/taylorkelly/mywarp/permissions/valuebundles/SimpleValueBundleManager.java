package me.taylorkelly.mywarp.permissions.valuebundles;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.permissions.PermissionsManager;

import org.bukkit.command.CommandSender;

/**
 * A simple value-bundle-manager implementation: bundles apply only when a
 * command-sender has the corresponding permission.
 * 
 * @see ValueBundleManager
 * 
 * @param <T>
 *            the value-bundle implementation this manager should manage
 */
public class SimpleValueBundleManager<T extends ValueBundle> extends ValueBundleManager<T> {

    /**
     * Initializes this value-bundle manager with the given bundles.
     * 
     * @param manager
     *            the permissions-manager that stores this value-bundle-manager
     * @param bundles
     *            all non-default bundles that a player can optionally have
     * @param defaultBundle
     *            the default bundle that acts as a fallback whenever none of
     *            the more specific bundles applies
     */
    public SimpleValueBundleManager(PermissionsManager manager, Iterable<T> bundles, T defaultBundle) {
        super(manager, bundles, defaultBundle);
    }

    /**
     * Gets the bundle that applies for the given command-sender. If none of the
     * specific bundles matches, this method will return the defaultBundle.
     * 
     * @param sender
     *            the command-sender
     * @return the bundle that applies for the given sender
     */
    public T getBundle(CommandSender sender) {
        for (T bundle : bundles) {
            if (MyWarp.inst().getPermissionsManager().hasPermission(sender, bundle.getPermission())) {
                return bundle;
            }
        }
        return defaultBundle;
    }
}