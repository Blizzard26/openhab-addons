package org.openhab.binding.modbus.sunspec.internal.dto;

import java.util.Optional;

/**
 * This class contains information parsed from the Battery Base Model,
 *
 * @see https://github.com/sunspec/models/blob/master/json/model_802.json
 *
 * @author Andreas Lanz - Initial Contribution
 */
public class BatteryModelBlock {
    public Integer ID;
    public Integer L;
    public Integer AHRtg;
    public Integer WHRtg;
    public Integer WChaRteMax;
    public Integer WDisChaRteMax;
    public Optional<Integer> DisChaRte;
    public Optional<Integer> SoCMax;
    public Optional<Integer> SoCMin;
    public Optional<Integer> SocRsvMax;
    public Optional<Integer> SoCRsvMin;
    public Integer SoC;
    public Optional<Integer> DoD;
    public Optional<Integer> SoH;
    public Optional<Long> NCyc;
    public Optional<Integer> ChaSt;
    public Integer LocRemCtl;
    public Optional<Integer> Hb;
    public Optional<Integer> CtrlHb;
    public Integer AlmRst;
    public Integer Typ;
    public Integer State;
    public Optional<Integer> StateVnd;
    public Optional<Long> WarrDt;
    public Long Evt1;
    public Long Evt2;
    public Long EvtVnd1;
    public Long EvtVnd2;
    public Integer V;
    public Optional<Integer> VMax;
    public Optional<Integer> VMin;
    public Optional<Integer> CellVMax;
    public Optional<Integer> CellVMaxStr;
    public Optional<Integer> CellVMaxMod;
    public Optional<Integer> CellVMin;
    public Optional<Integer> CellVMinStr;
    public Optional<Integer> CellVMinMod;
    public Optional<Integer> CellVAvg;
    public Short A;
    public Optional<Integer> AChaMax;
    public Optional<Integer> ADisChaMax;
    public Short W;
    public Optional<Integer> ReqInvState;
    public Optional<Short> ReqW;
    public Integer SetOp;
    public Integer SetInvState;
    public Short AHRtg_SF;
    public Short WHRtg_SF;
    public Short WChaDisChaMax_SF;
    public Optional<Short> DisChaRte_SF;
    public Short SoC_SF;
    public Optional<Short> DoD_SF;
    public Optional<Short> SoH_SF;
    public Short V_SF;
    public Short CellV_SF;
    public Short A_SF;
    public Short AMax_SF;
    public Optional<Short> W_SF;
}
