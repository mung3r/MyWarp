package me.taylorkelly.mywarp.permissions.valuebundles;

/**
 * An abstract implementation of a value-bundle-
 */
public abstract class AbstractValueBundle implements ValueBundle {

    private final String identifier;

    /**
     * Initializes this bundle with the given identifier. The identifier should
     * be unique.
     * 
     * @param identifier
     *            the identifier
     */
    public AbstractValueBundle(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public int compareTo(ValueBundle that) {
        return this.getIdentifier().compareTo(that.getIdentifier());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * me.taylorkelly.mywarp.permissions.valuebundles.ValueBundle#getIdentifier
     * ()
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the base-permission of this bundle. The base-permission is identical
     * for a set of bundles that set the same value-types. It is suffixed by a
     * bundles identifier to get the full permission that represents an
     * individual bundle.
     * 
     * @return the base-permission
     */
    protected abstract String getBasePermission();

    /*
     * (non-Javadoc)
     * 
     * @see
     * me.taylorkelly.mywarp.permissions.valuebundles.ValueBundle#getPermission
     * ()
     */
    @Override
    public String getPermission() {
        return getBasePermission() + "." + identifier;
    }

}
