package org.openhab.binding.modbus.sunspec.internal.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.modbus.sunspec.internal.dto.BatteryModelBlock;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

import javax.validation.constraints.NotNull;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecConstants.BATTERY_BASE_MODEL;

public class BatteryModelParser extends AbstractBaseParser implements SunspecParser<BatteryModelBlock> {

    @Override
    public BatteryModelBlock parse(@NonNull ModbusRegisterArray raw) {
        BatteryModelBlock block = new BatteryModelBlock();

        block.ID = extractUInt16(raw, 0, 0);
        block.L = extractUInt16(raw, 1, 0);
        block.AHRtg = extractUInt16(raw, 2, 0);
        block.WHRtg = extractUInt16(raw, 3, 0);
        block.WChaRteMax = extractUInt16(raw, 4, 0);
        block.WDisChaRteMax = extractUInt16(raw, 5, 0);
        block.DisChaRte = extractOptionalUInt16(raw, 6);
        block.SoCMax = extractOptionalUInt16(raw, 7);
        block.SoCMin = extractOptionalUInt16(raw, 8);
        block.SocRsvMax = extractOptionalUInt16(raw, 9);
        block.SoCRsvMin = extractOptionalUInt16(raw, 10);
        block.SoC = extractUInt16(raw, 11, 0);
        block.DoD = extractOptionalUInt16(raw, 12);
        block.SoH = extractOptionalUInt16(raw, 13);
        block.NCyc = extractOptionalUInt32(raw, 14);
        block.ChaSt = extractOptionalUInt16(raw, 16);
        block.LocRemCtl = extractUInt16(raw, 17, 0);
        block.Hb = extractOptionalUInt16(raw, 18);
        block.CtrlHb = extractOptionalUInt16(raw, 19);
        block.AlmRst = extractUInt16(raw, 20, 0);
        block.Typ = extractUInt16(raw, 21, 0);
        block.State = extractUInt16(raw, 22, 0);
        block.StateVnd = extractOptionalUInt16(raw, 23);
        block.WarrDt = extractOptionalUInt32(raw, 24);
        block.Evt1 = extractUInt32(raw, 26, 0);
        block.Evt2 = extractUInt32(raw, 28, 0);
        block.EvtVnd1 = extractUInt32(raw, 30, 0);
        block.EvtVnd2 = extractUInt32(raw, 32, 0);
        block.V = extractUInt16(raw, 34, 0);
        block.VMax = extractOptionalUInt16(raw, 35);
        block.VMin = extractOptionalUInt16(raw, 36);
        block.CellVMax = extractOptionalUInt16(raw, 37);
        block.CellVMaxStr = extractOptionalUInt16(raw, 38);
        block.CellVMaxMod = extractOptionalUInt16(raw, 39);
        block.CellVMin = extractOptionalUInt16(raw, 40);
        block.CellVMinStr = extractOptionalUInt16(raw, 41);
        block.CellVMinMod = extractOptionalUInt16(raw, 42);
        block.CellVAvg = extractOptionalUInt16(raw, 43);
        block.A = extractInt16(raw, 44, (short)0);
        block.AChaMax = extractOptionalUInt16(raw, 45);
        block.ADisChaMax = extractOptionalUInt16(raw, 46);
        block.W = extractInt16(raw, 47, (short)0);
        block.ReqInvState = extractOptionalUInt16(raw, 48);
        block.ReqW = extractOptionalInt16(raw, 49);
        block.SetOp = extractUInt16(raw, 50, 0);
        block.SetInvState = extractUInt16(raw, 51, 0);
        block.AHRtg_SF = extractSunSSF(raw, 52);
        block.WHRtg_SF = extractSunSSF(raw, 53);
        block.WChaDisChaMax_SF = extractSunSSF(raw, 54);
        block.DisChaRte_SF = extractOptionalSunSSF(raw, 55);
        block.SoC_SF = extractSunSSF(raw, 56);
        block.DoD_SF = extractOptionalSunSSF(raw, 57);
        block.SoH_SF = extractOptionalSunSSF(raw, 58);
        block.V_SF = extractSunSSF(raw, 59);
        block.CellV_SF = extractSunSSF(raw, 60);
        block.A_SF = extractSunSSF(raw, 61);
        block.AMax_SF = extractSunSSF(raw, 62);
        block.W_SF = extractOptionalSunSSF(raw, 63);

        return block;
    }
}
