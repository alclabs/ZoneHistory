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
        Location current = descendant.getParent();
        result.append(descendant.getDisplayName());

        if (!ancestor.equals(descendant))
        {  // if a single node is selected, these may be the same.  Then just skip loop
            while (!current.equals(ancestor))
            {
                result.insert(0, " / ");
                result.insert(0, current.getDisplayName());
                current = current.getParent();
            }
        }
        return result.toString();
    }

}
