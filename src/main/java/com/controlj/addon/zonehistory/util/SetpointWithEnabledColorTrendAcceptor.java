package com.controlj.addon.zonehistory.util;

import com.controlj.green.addonsupport.access.AspectAcceptor;
import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import com.controlj.green.addonsupport.access.aspect.SetPoint;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SetpointWithEnabledColorTrendAcceptor implements AspectAcceptor<SetPoint>
{
    @Override
    public boolean accept(@NotNull SetPoint setPoint)
    {
        Location location = setPoint.getLocation();
        Collection<EquipmentColorTrendSource> sources = location.find(EquipmentColorTrendSource.class, new AspectAcceptor<EquipmentColorTrendSource>() {
            @Override
            public boolean accept(@NotNull EquipmentColorTrendSource equipmentColorTrendSource) {
                return equipmentColorTrendSource.isEnabled();
            }
        });
        return !sources.isEmpty();
    }
}


