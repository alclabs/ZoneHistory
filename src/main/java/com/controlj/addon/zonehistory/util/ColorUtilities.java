package com.controlj.addon.zonehistory.util;

import com.controlj.green.addonsupport.access.EquipmentColor;

public abstract class ColorUtilities
{
    public static boolean isActiveCooling(EquipmentColor color)
    {
        return color == EquipmentColor.COOLING_ALARM || color == EquipmentColor.MAXIMUM_COOLING || color == EquipmentColor.MODERATE_COOLING;
    }

    public static boolean isActiveHeating(EquipmentColor color)
    {
        return color == EquipmentColor.HEATING_ALARM || color == EquipmentColor.MAXIMUM_HEATING || color == EquipmentColor.MODERATE_HEATING;
    }

    public static boolean isOperational(EquipmentColor color)
    {
         return color != EquipmentColor.CORAL && color != EquipmentColor.DOWNLOAD_REQUIRED &&
                color != EquipmentColor.HARDWARE_COMM_ERROR && color != EquipmentColor.SOFTWARE_COMM_ERROR &&
                color != EquipmentColor.UNKNOWN && color != EquipmentColor.UNOCCUPIED;
    }
}
