package com.controlj.addon.zonehistory.util;

import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.green.addonsupport.access.AspectAcceptor;
import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import com.controlj.green.addonsupport.access.aspect.SetPoint;
import org.jetbrains.annotations.NotNull;

public class EnabledColorTrendWithSetpointAcceptor implements AspectAcceptor<EquipmentColorTrendSource>
{
    @Override
    public boolean accept(@NotNull EquipmentColorTrendSource equipmentColorTrendSource)
    {
        boolean result = equipmentColorTrendSource.isEnabled();
        if (result)
        {
            Location equipment = LocationUtilities.findMyEquipment(equipmentColorTrendSource.getLocation());
            result = equipment.hasAspect(SetPoint.class);
        }
        return result;
    }
}


