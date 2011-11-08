package com.controlj.addon.zonehistory;

import com.controlj.green.addonsupport.access.AspectAcceptor;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import com.controlj.green.addonsupport.access.aspect.SetPoint;
import org.jetbrains.annotations.NotNull;

public class ColorAndSetPointAcceptor implements AspectAcceptor<EquipmentColorTrendSource>
{
    @Override
    public boolean accept(@NotNull EquipmentColorTrendSource eqColorTrendSource)
    {
        return eqColorTrendSource.isEnabled() && eqColorTrendSource.getLocation().hasAspect(SetPoint.class);
    }
}


