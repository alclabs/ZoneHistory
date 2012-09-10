package com.controlj.addon.zonehistory.util;

import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.LocationType;
import com.controlj.green.addonsupport.access.UnresolvableException;
import org.jetbrains.annotations.NotNull;

/**
 * Static utilities to find nodes.
 */
public class LocationUtilities
{
    /**
     * Finds a parent equipment
     *
     * @param insideEq Location (must be at or inside of an equipment)
     * @return location of equipment
     */
    public static Location findMyEquipment(Location insideEq)
    {
        while (insideEq.getType() != LocationType.Equipment)
        {
            try
            {
                insideEq = insideEq.getParent();
            }
            catch (UnresolvableException e)
            {
                throw new RuntimeException("Thought this couldn't happen - can't find parent equipment", e);
            }
        }
        return insideEq;
    }

    public static String relativeDisplayPath(@NotNull Location ancestor, @NotNull Location descendant) throws UnresolvableException
    {
        StringBuilder result = new StringBuilder();
        result.append(descendant.getDisplayName());

        if (!ancestor.equals(descendant))
        {  // if a single node is selected, these may be the same.  Then just skip loop
            Location current = descendant.getParent();
            while (!current.equals(ancestor))
            {
                result.insert(0, " / ");
                result.insert(0, current.getDisplayName());
                current = current.getParent();
            }
        }

        return result.toString();
    }

    public static String createTransientLookupPathString(Location loc)
    {
        StringBuffer result = new StringBuffer();
        Location current = loc;
        while (true)
        {
            if (result.length() != 0)
            {   // there is other content, prepend delimeter
                result.insert(0, "/");
            }
            result.insert(0, current.getTransientLookupString());
            if (current.hasParent())
            {
                try
                {
                    current = current.getParent();
                }
                catch (UnresolvableException e)
                {
                    throw new RuntimeException("programming error - can't find parent when hasParent is true");
                } // shouldn't happen
            }
            else
            {
                break;
            }
        }

        return result.toString();
    }

}
