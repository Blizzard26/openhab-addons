package org.openhab.binding.modbus.sunspec.internal.handler;

import org.openhab.binding.modbus.sunspec.internal.dto.BatteryModelBlock;
import org.openhab.binding.modbus.sunspec.internal.dto.InverterModelBlock;
import org.openhab.binding.modbus.sunspec.internal.parser.BatteryModelParser;
import org.openhab.binding.modbus.sunspec.internal.parser.InverterModelParser;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatteryHandler extends AbstractSunSpecHandler
{
    private static final String CHANNEL_GROUP = "test";
    /**
     * Parser used to convert incoming raw messages into model blocks
     */
    private final BatteryModelParser parser = new BatteryModelParser();

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(BatteryHandler.class);


    /**
     * Instances of this handler should get a reference to the modbus manager
     *
     * @param thing the thing to handle
     */
    public BatteryHandler(Thing thing)
    {
        super(thing);
    }

    @Override
    protected void handlePolledData(ModbusRegisterArray registers)
    {
        logger.trace("Model block received, size: {}", registers.size());

        BatteryModelBlock block = parser.parse(registers);

        updateState(channelUID(CHANNEL_GROUP, "ID"), new DecimalType(block.ID));
        updateState(channelUID(CHANNEL_GROUP, "L"), new DecimalType(block.L));
        updateState(channelUID(CHANNEL_GROUP, "AHRtg"), getScaled(block.AHRtg, block.AHRtg_SF, Units.AMPERE_HOUR));
        updateState(channelUID(CHANNEL_GROUP, "WHRtg"), getScaled(block.WHRtg, block.WHRtg_SF, Units.WATT_HOUR));
        updateState(channelUID(CHANNEL_GROUP, "WChaRteMax"), getScaled(block.WChaRteMax, block.WChaDisChaMax_SF, Units.WATT));
        updateState(channelUID(CHANNEL_GROUP, "WDisChaRteMax"), getScaled(block.WDisChaRteMax, block.WChaDisChaMax_SF, Units.WATT));
        updateState(channelUID(CHANNEL_GROUP, "DisChaRte"), getScaled(block.DisChaRte, block.DisChaRte_SF, Units.PERCENT));
        updateState(channelUID(CHANNEL_GROUP, "SoCMax"), getScaled(block.SoCMax, block.SoC_SF, Units.PERCENT));
        updateState(channelUID(CHANNEL_GROUP, "SoCMin"), getScaled(block.SoCMin, block.SoC_SF, Units.PERCENT));
        updateState(channelUID(CHANNEL_GROUP, "SocRsvMax"), getScaled(block.SocRsvMax, block.SoC_SF, Units.PERCENT));
        updateState(channelUID(CHANNEL_GROUP, "SoCRsvMin"), getScaled(block.SoCRsvMin, block.SoC_SF, Units.PERCENT));
        updateState(channelUID(CHANNEL_GROUP, "SoC"), getScaled(block.SoC, block.SoC_SF, Units.PERCENT));
        updateState(channelUID(CHANNEL_GROUP, "DoD"), getScaled(block.DoD, block.DoD_SF, Units.PERCENT));
        updateState(channelUID(CHANNEL_GROUP, "SoH"), getScaled(block.SoH, block.SoH_SF, Units.PERCENT));
        updateState(channelUID(CHANNEL_GROUP, "NCyc"), block.NCyc.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.NCyc.get()));
        updateState(channelUID(CHANNEL_GROUP, "ChaSt"), block.ChaSt.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.ChaSt.get()));
        updateState(channelUID(CHANNEL_GROUP, "LocRemCtl"), new DecimalType(block.LocRemCtl));
        updateState(channelUID(CHANNEL_GROUP, "Hb"), block.Hb.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.Hb.get()));
        updateState(channelUID(CHANNEL_GROUP, "CtrlHb"), block.CtrlHb.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.CtrlHb.get()));
        updateState(channelUID(CHANNEL_GROUP, "AlmRst"), new DecimalType(block.AlmRst));
        updateState(channelUID(CHANNEL_GROUP, "Typ"), new DecimalType(block.Typ));
        updateState(channelUID(CHANNEL_GROUP, "State"), new DecimalType(block.State));
        updateState(channelUID(CHANNEL_GROUP, "StateVnd"), block.StateVnd.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.StateVnd.get()));
        updateState(channelUID(CHANNEL_GROUP, "WarrDt"), block.WarrDt.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.WarrDt.get()));
        updateState(channelUID(CHANNEL_GROUP, "Evt1"), new DecimalType(block.Evt1));
        updateState(channelUID(CHANNEL_GROUP, "Evt2"), new DecimalType(block.Evt2));
        updateState(channelUID(CHANNEL_GROUP, "EvtVnd1"), new DecimalType(block.EvtVnd1));
        updateState(channelUID(CHANNEL_GROUP, "EvtVnd2"), new DecimalType(block.EvtVnd2));
        updateState(channelUID(CHANNEL_GROUP, "V"), getScaled(block.V, block.V_SF, Units.VOLT));
        updateState(channelUID(CHANNEL_GROUP, "VMax"), getScaled(block.VMax, block.V_SF, Units.VOLT));
        updateState(channelUID(CHANNEL_GROUP, "VMin"), getScaled(block.VMin, block.V_SF, Units.VOLT));
        updateState(channelUID(CHANNEL_GROUP, "CellVMax"), getScaled(block.CellVMax, block.CellV_SF, Units.VOLT));
        updateState(channelUID(CHANNEL_GROUP, "CellVMaxStr"), block.CellVMaxStr.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.CellVMaxStr.get()));
        updateState(channelUID(CHANNEL_GROUP, "CellVMaxMod"), block.CellVMaxMod.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.CellVMaxMod.get()));
        updateState(channelUID(CHANNEL_GROUP, "CellVMin"), getScaled(block.CellVMin, block.CellV_SF, Units.VOLT));
        updateState(channelUID(CHANNEL_GROUP, "CellVMinStr"), block.CellVMinStr.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.CellVMinStr.get()));
        updateState(channelUID(CHANNEL_GROUP, "CellVMinMod"), block.CellVMinMod.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.CellVMinMod.get()));
        updateState(channelUID(CHANNEL_GROUP, "CellVAvg"), getScaled(block.CellVAvg, block.CellV_SF, Units.VOLT));
        updateState(channelUID(CHANNEL_GROUP, "A"), getScaled(block.A, block.A_SF, Units.AMPERE));
        updateState(channelUID(CHANNEL_GROUP, "AChaMax"), getScaled(block.AChaMax, block.AMax_SF, Units.AMPERE));
        updateState(channelUID(CHANNEL_GROUP, "ADisChaMax"), getScaled(block.ADisChaMax, block.AMax_SF, Units.AMPERE));
        updateState(channelUID(CHANNEL_GROUP, "W"), getScaled(block.W, block.W_SF.orElse((short)0), Units.WATT));
        updateState(channelUID(CHANNEL_GROUP, "ReqInvState"), block.ReqInvState.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.ReqInvState.get()));
        updateState(channelUID(CHANNEL_GROUP, "ReqW"), getScaled(block.ReqW, block.W_SF, Units.WATT));
        updateState(channelUID(CHANNEL_GROUP, "SetOp"), new DecimalType(block.SetOp));
        updateState(channelUID(CHANNEL_GROUP, "SetInvState"), new DecimalType(block.SetInvState));
        updateState(channelUID(CHANNEL_GROUP, "AHRtg_SF"), new DecimalType(block.AHRtg_SF));
        updateState(channelUID(CHANNEL_GROUP, "WHRtg_SF"), new DecimalType(block.WHRtg_SF));
        updateState(channelUID(CHANNEL_GROUP, "WChaDisChaMax_SF"), new DecimalType(block.WChaDisChaMax_SF));
        updateState(channelUID(CHANNEL_GROUP, "DisChaRte_SF"), block.DisChaRte_SF.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.DisChaRte_SF.get()));
        updateState(channelUID(CHANNEL_GROUP, "SoC_SF"), new DecimalType(block.SoC_SF));
        updateState(channelUID(CHANNEL_GROUP, "DoD_SF"), block.DoD_SF.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.DoD_SF.get()));
        updateState(channelUID(CHANNEL_GROUP, "SoH_SF"), block.SoH_SF.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.SoH_SF.get()));
        updateState(channelUID(CHANNEL_GROUP, "V_SF"), new DecimalType(block.V_SF));
        updateState(channelUID(CHANNEL_GROUP, "CellV_SF"), new DecimalType(block.CellV_SF));
        updateState(channelUID(CHANNEL_GROUP, "A_SF"), new DecimalType(block.A_SF));
        updateState(channelUID(CHANNEL_GROUP, "AMax_SF"), new DecimalType(block.AMax_SF));
        updateState(channelUID(CHANNEL_GROUP, "W_SF"), block.W_SF.isEmpty() ? UnDefType.UNDEF : new DecimalType(block.W_SF.get()));

        resetCommunicationError();
    }
}
