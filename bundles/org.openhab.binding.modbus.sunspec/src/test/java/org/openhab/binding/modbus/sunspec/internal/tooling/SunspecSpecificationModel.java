package org.openhab.binding.modbus.sunspec.internal.tooling;

import java.util.List;

public class SunspecSpecificationModel
{
    public static class SunspecSpecificationPoint {
        String desc;
        String label;
        String mandatory;
        String name;
        int size;
        String staticValue;
        String type;
        String value;
        String sf;
        String units;
    }

    public static class SunspecSpecificationGroup {
        String label;
        String name;
        List<SunspecSpecificationPoint> points;
        String type;
    }

    SunspecSpecificationGroup group;
    String id;
}
