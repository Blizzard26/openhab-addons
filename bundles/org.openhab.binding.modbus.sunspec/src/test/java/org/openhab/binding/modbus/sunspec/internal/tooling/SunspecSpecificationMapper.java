package org.openhab.binding.modbus.sunspec.internal.tooling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openhab.core.library.unit.Units;

import java.io.*;
import java.util.Locale;

public class SunspecSpecificationMapper
{


    private final String specificationFile;

    public static void main(String[] args) throws IOException
    {
        StringWriter out = new StringWriter();
        BufferedWriter writer = new BufferedWriter(out);
        SunspecSpecificationMapper mapper = new SunspecSpecificationMapper("/model_802.json");
        mapper.generateModel(writer);
        writer.close();
        System.out.println(out);
    }

    public SunspecSpecificationMapper(String specificationFile)
    {
        this.specificationFile = specificationFile;
    }

    public void generateModel(BufferedWriter writer) throws IOException
    {
        SunspecSpecificationModel sunspecSpecification = readSpecificationFile(specificationFile);

        writer.write("================================");
        writer.newLine();
        writeModelFile(sunspecSpecification, writer);
        writer.write("================================");
        writer.newLine();
        writeParser(sunspecSpecification, writer);
        writer.write("================================");
        writer.newLine();
        writeHandler(sunspecSpecification, writer);
        writer.write("================================");
        writer.newLine();
        writer.flush();
    }

    private void writeHandler(SunspecSpecificationModel sunspecSpecification, BufferedWriter writer)
    {
        PrintWriter printWriter = new PrintWriter(writer);
        for (SunspecSpecificationModel.SunspecSpecificationPoint point : sunspecSpecification.group.points)
        {
            if (!"sunssf".equals(point.type))
            {
                printWriter.printf("updateState(channelUID(CHANNEL_GROUP, \"%s\"), %s);", channelName(point),
                                   getValueMapper(point));
                printWriter.println();
            }
        }
    }

    private String channelName(SunspecSpecificationModel.SunspecSpecificationPoint point)
    {
        return point.label != null ? point.label.toLowerCase(Locale.ROOT).replace(" ", "-") : point.name;
    }

    private String getValueMapper(SunspecSpecificationModel.SunspecSpecificationPoint point)
    {
        StringBuilder builder = new StringBuilder();
        if (point.sf != null)
        {
            builder.append(String.format("getScaled(block.%s, block.%s, %s)", point.name, point.sf,
                                        mapUnit(point.units)));
        }
        else
        {
            if (point.mandatory == null)
            {
                builder.append(String.format("block.%s.isEmpty() ? UnDefType.UNDEF : ", point.name));
                if (point.units != null)
                {
                    builder.append(String.format("new QuantityType(block.%s.get() + \" \" + %s)", point.name,
                                                 mapUnit(point.units)));
                }
                else
                {
                    builder.append(String.format("new DecimalType(block.%s.get())", point.name));
                }
            }
            else {
                if (point.units != null)
                {
                    builder.append(String.format("new QuantityType(block.%s + \" \" + %s)", point.name,
                                                 mapUnit(point.units)));
                }
                else
                {
                    builder.append(String.format("new DecimalType(block.%s)", point.name));
                }
            }

        }
        return builder.toString();
    }

    private String mapUnit(String units)
    {
        return switch (units)
        {
            case "V": yield "Units.VOLT";
            case "A": yield "Units.AMPERE";
            case "W": yield "Units.WATT";
            case "Ah": yield "Units.AMPERE_HOUR";
            case "Wh": yield "Units.WATT_HOUR";
            case "%": yield "Units.PERCENT";

            default:
                if (units.startsWith("%"))
                    yield "Units.PERCENT";
                yield "...";
        };

    }

    private void writeParser(SunspecSpecificationModel sunspecSpecification, BufferedWriter writer)
    {
        String modelName = getModelClassName(sunspecSpecification);
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.printf("public class %s extends AbstractBaseParser implements " +
                                   "SunspecParser<%s> {", getParserClassName(sunspecSpecification), modelName);
        printWriter.println();
        printWriter.println();
        printWriter.println("\t@Override");

        printWriter.printf("\tpublic %s parse(@NonNull ModbusRegisterArray raw) {", modelName);
        printWriter.println();
        printWriter.printf("\t\t%1$s block = new %1$s();", modelName);
        printWriter.println();
        printWriter.println();
        int index = 0;
        for (SunspecSpecificationModel.SunspecSpecificationPoint point : sunspecSpecification.group.points)
        {
            printWriter.printf("\t\tblock.%s = %s;", mapVariableName(point), String.format(mapExtractionMethod(point,
                                                                                                          index),
                                                                                           index));
            printWriter.println();
            index += point.size;

        }
        printWriter.println();
        printWriter.println("\t\treturn block;");
        printWriter.println("\t}");
        printWriter.println("}");
        printWriter.flush();
    }

    private String getParserClassName(SunspecSpecificationModel sunspecSpecification)
    {
        return className(sunspecSpecification.group.name, "Model", "Parser");
    }

    private String mapExtractionMethod(SunspecSpecificationModel.SunspecSpecificationPoint point, int index)
    {
        return switch (point.type)
        {
            case "int16":
                if ("M".equals(point.mandatory))
                    yield "extractInt16(raw, %d, (short)0)";
                else
                    yield "extractOptionalInt16(raw, %d)";
            case "sunssf":
                if ("M".equals(point.mandatory))
                    yield "extractSunSSF(raw, %d)";
                else
                    yield "extractOptionalSunSSF(raw, %d)";
            case "uint16":
            case "enum16":
                if ("M".equals(point.mandatory))
                    yield "extractUInt16(raw, %d, 0)";
                else
                    yield "extractOptionalUInt16(raw, %d)";
            case "uint32":
            case "bitfield32":
                if ("M".equals(point.mandatory))
                    yield "extractUInt32(raw, %d, 0)";
                else
                    yield "extractOptionalUInt32(raw, %d)";
            default:
                System.out.println("unknown type: "+ point.type);
                yield "...";
        };
    }

    private void writeModelFile(SunspecSpecificationModel sunspecSpecification, BufferedWriter writer)
    {
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.printf("public class %s {", getModelClassName(sunspecSpecification));
        printWriter.println();
        sunspecSpecification.group.points.forEach(point -> {
            printWriter.printf("\tpublic %s %s;", mapVariableType(point), mapVariableName(point));
            printWriter.println();
        });
        printWriter.println("}");
        printWriter.flush();
    }

    private String getModelClassName(SunspecSpecificationModel sunspecSpecification)
    {
        return className(sunspecSpecification.group.name, "Model", "Block");
    }

    private String mapVariableName(SunspecSpecificationModel.SunspecSpecificationPoint point)
    {
        return point.name;
    }

    private String mapVariableType(SunspecSpecificationModel.SunspecSpecificationPoint point)
    {
        return switch (point.type)
        {
            case "int16":
            case "sunssf":
                if ("M".equals(point.mandatory))
                    yield "Short";
                else
                    yield "Optional<Short>";
            case "uint16":
            case "enum16":
                if ("M".equals(point.mandatory))
                    yield "Integer";
                else
                    yield "Optional<Integer>";
            case "uint32":
            case "bitfield32":
                if ("M".equals(point.mandatory))
                    yield "Long";
                else
                    yield "Optional<Long>";
            default:
                System.out.println("unknown type: "+ point.type);
                yield "...";
        };
    }

    private String className(String... parts)
    {
        StringBuilder builder = new StringBuilder();
        for (String part : parts)
        {
            builder.append(part.substring(0,1).toUpperCase(Locale.ROOT));
            builder.append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return builder.toString();
    }


    private SunspecSpecificationModel readSpecificationFile(String specificationFile) throws IOException
    {
        try (InputStream resource = this.getClass().getResourceAsStream(specificationFile))
        {
            if (resource == null)
            {
                throw new IllegalStateException("Resource "+specificationFile+ " not found");
            }
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            return gson.fromJson(new InputStreamReader(resource), SunspecSpecificationModel.class);
        }
    }
}
