/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.udig.style.advanced.utils;

import eu.udig.style.advanced.internal.Messages;

/**
 * Enumeration of possible vendor options.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public enum VendorOptions {
    VENDOROPTION_MAXDISPLACEMENT("maxDisplacement", Messages.VendorOptions_1), // //$NON-NLS-1$
    VENDOROPTION_AUTOWRAP("autoWrap", Messages.VendorOptions_3), // //$NON-NLS-1$
    VENDOROPTION_SPACEAROUND("spaceAround", Messages.VendorOptions_5), // //$NON-NLS-1$
    VENDOROPTION_REPEAT("repeat", Messages.VendorOptions_7), // //$NON-NLS-1$
    VENDOROPTION_MAXANGLEDELTA("maxAngleDelta", Messages.VendorOptions_9), // //$NON-NLS-1$
    VENDOROPTION_FOLLOWLINE("followLine", Messages.VendorOptions_11), // //$NON-NLS-1$
    VENDOROPTION_OTHER("other", Messages.VendorOptions_13); //$NON-NLS-1$

    private String defString = null;
    private String guiString = null;
    VendorOptions( String defString, String guiString ) {
        this.defString = defString;
        this.guiString = guiString;
    }

    /**
     * Return the vendoroption based on the definition string.
     * 
     * @param defString the option definition string.
     * @return the {@link VendorOptions} or null.
     */
    public static VendorOptions toVendorOption( String defString ) {
        VendorOptions[] values = values();
        for( VendorOptions vendorOptions : values ) {
            if (defString.equals(vendorOptions.toString())) {
                return vendorOptions;
            }
        }
        return VendorOptions.VENDOROPTION_OTHER;
    }

    /**
     * Return the vendoroption based on the gui string.
     * 
     * @param guiString the option gui string.
     * @return the {@link VendorOptions} or null.
     */
    public static VendorOptions guiStringToVendorOption( String guiString ) {
        VendorOptions[] values = values();
        for( VendorOptions vendorOptions : values ) {
            if (guiString.equals(vendorOptions.toGuiString())) {
                return vendorOptions;
            }
        }
        return null;
    }

    public String toString() {
        return defString;
    }

    public String toGuiString() {
        return guiString;
    }
}