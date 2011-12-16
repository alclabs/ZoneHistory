package com.controlj.addon.zonehistory.util;

import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.LocationType;
import com.controlj.green.addonsupport.access.UnresolvableException;

/**
 * Static utilities to find nodes.
 */
public class FindNodes
{
    /**
     * Finds a parent equipment
     * @param insideEq Location (must be at or inside of an equipment)
     * @return location of equipment
     */
    public static Location findMyEquipment(Location insideEq) {
       while (insideEq.getType() != LocationType.Equipment) {
           try {
               insideEq = insideEq.getParent();
           } catch (UnresolvableException e) {
               throw new RuntimeException("Thought this couldn't happen - can't find parent equipment", e);
           }
       }
       return insideEq;
   }

}
