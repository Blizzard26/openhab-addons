package org.openhab.binding.modbus.kostal.internal;

import static org.openhab.binding.modbus.kostal.internal.ConversionConstants.*;
import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.*;
import static org.openhab.core.library.unit.Units.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import javax.measure.Unit;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * This class contains constants for Kostal Modbus Binding. In particular the Register Configuration for reading from
 * modbus in groups.
 *
 * @author Andreas Lanz - Initial contribution
 */
public class KostalInverterConstants {

    public static final List<ModbusRegisterRange> registerGroups = List.of( //
                                                                            // Registers 56-57
            new ModbusRegisterRange(
                    // Addr | Description | Unit | Format | N¹ | Access | Function Code
                    // 2 | MODBUS Enable | - | Bool | 1 | R/W | 0x03/0x06
                    // 4 | MODBUS Unit-ID | - | U16 | 1 | R/W | 0x03/0x06
                    // 5 | MODBUS Byte Order Note7 | - | U16 | 1 | R/W | 0x03/0x06
                    // 6 | Inverter article number | - | String | 8 | RO | 0x03
                    // 14 | Inverter serial number | - | String | 8 | RO | 0x03
                    // 30 | Number of bidirectional converter | - | U16 | 1 | RO | 0x03
                    // 32 | Number of AC phases | - | U16 | 1 | RO | 0x03
                    // 34 | Number of PV strings | - | U16 | 1 | RO | 0x03
                    // 36 | Hardware-Version | - | U16 | 2 | RO | 0x03
                    // 38 | Software-Version Maincontroller (MC) | - | String | 8 | RO | 0x03
                    // 46 | Software-Version IO-Controller (IOC) | - | String | 8 | RO | 0x03
                    // 54 | Power-ID | - | U32 | 2 | RO | 0x03
                    // 56 | Inverter state2 | - | U32 | 2 | RO | 0x03
                    new KostalModbusRegister(56, UINT32, stringType(MAP_INVERTER_STATES), //
                            "inverter-state", "device-information"),
                    new KostalModbusRegister(56, UINT32, DecimalType::new, //
                            "inverter-state-raw", "device-information") //
            ),
            // Registers 98-125
            new ModbusRegisterRange(
                    // 98 | Temperature of controller PCB | °C | Float | 2 | RO | 0x03
                    // 100 | Total DC power | W | Float | 2 | RO | 0x03
                    new KostalModbusRegister(100, FLOAT32, quantityType(WATT), //
                            "total-dc-power", "consumption"),
                    // 104 | State of energy manager | - | U32 | 2 | RO | 0x03
                    new KostalModbusRegister(104, UINT32, stringType(MAP_STATE_OF_ENERGY_MANAGER), //
                            "state-of-energy-manager", "device-information"),
                    new KostalModbusRegister(104, UINT32, DecimalType::new, //
                            "state-of-energy-manager-raw", "device-information"),
                    // 106 | Home own consumption from battery8 | W | Float | 2 | RO | 0x03
                    new KostalModbusRegister(106, FLOAT32, quantityType(WATT), //
                            "home-own-consumption-from-battery", "consumption"),
                    // 108 | Home own consumption from grid8 | W | Float | 2 | RO | 0x03
                    new KostalModbusRegister(108, FLOAT32, quantityType(WATT), //
                            "home-own-consumption-from-grid", "consumption"),
                    // 110 | Total home consumption Battery8 | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(110, FLOAT32, quantityType(WATT_HOUR), //
                            "total-home-consumption-battery", "consumption"),
                    // 112 | Total home consumption Grid8 | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(112, FLOAT32, quantityType(WATT_HOUR), //
                            "total-home-consumption-grid", "consumption"),
                    // 114 | Total home consumption PV8 | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(114, FLOAT32, quantityType(WATT_HOUR), //
                            "total-home-consumption-pv", "consumption"),
                    // 116 | Home own consumption from PV8 | W | Float | 2 | RO | 0x03
                    new KostalModbusRegister(116, FLOAT32, quantityType(WATT), //
                            "home-own-consumption-pv", "consumption"),
                    // 118 | Total home consumption8 | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(118, FLOAT32, quantityType(WATT_HOUR), //
                            "total-home-consumption", "consumption"),
                    // 120 | Isolation resistance | Ohm | Float | 2 | RO | 0x03
                    // 122 | Power limit from EVU | % | Float | 2 | RO | 0x03
                    // 124 | Total home consumption rate | % | Float | 2 | RO | 0x03
                    new KostalModbusRegister(124, FLOAT32, quantityType(PERCENT), //
                            "total-home-consumption-rate", "consumption") //
            ),
            // 144 | Worktime | s | Float | 2 | RO | 0x03
            // 150 | Actual cos φ | - | Float | 2 | RO | 0x03
            // 152 | Grid frequency | Hz | Float | 2 | RO | 0x03
            // 154 | Current Phase 1 | A | Float | 2 | RO | 0x03
            // 156 | Active power Phase 1 | W | Float | 2 | RO | 0x03
            // 158 | Voltage Phase 1 | V | Float | 2 | RO | 0x03
            // 160 | Current Phase 2 | A | Float | 2 | RO | 0x03
            // 162 | Active power Phase 2 | W | Float | 2 | RO | 0x03
            // 164 | Voltage Phase 2 | V | Float | 2 | RO | 0x03
            // 166 | Current Phase 3 | A | Float | 2 | RO | 0x03
            // 168 | Active power Phase 3 | W | Float | 2 | RO | 0x03
            // 170 | Voltage Phase 3 | V | Float | 2 | RO | 0x03
            new ModbusRegisterRange(
                    // 172 | Total AC active power | W | Float | 2 | RO | 0x03
                    new KostalModbusRegister(172, FLOAT32, quantityType(WATT), //
                            "total-ac-active-power", "grid-information"),
                    // 174 | Total AC reactive power | Var | Float | 2 | RO | 0x03
                    new KostalModbusRegister(174, FLOAT32, quantityType(VAR), //
                            "total-ac-reactive-power", "grid-information"),
                    // 178 | Total AC apparent power | VA | Float | 2 | RO | 0x03
                    new KostalModbusRegister(178, FLOAT32, quantityType(VOLT_AMPERE), //
                            "total-ac-apparent-power", "grid-information")),
            // Registers 190 - 217
            new ModbusRegisterRange(
                    // 190 | Battery charge current | A | Float | 2 | RO | 0x03
                    new KostalModbusRegister(190, FLOAT32, quantityType(AMPERE), //
                            "battery-charge-current", "battery-information"),
                    // 194 | Number of battery cycles | - | Float | 2 | RO | 0x03
                    new KostalModbusRegister(194, FLOAT32, DecimalType::new, //
                            "number-of-battery-cycles", "battery-information"),
                    // 200 | Actual battery charge (-) / discharge (+) current | A | Float | 2 | RO | 0x03
                    new KostalModbusRegister(200, FLOAT32, quantityType(AMPERE), //
                            "actual-battery-charge-discharge-current", "battery-information"),
                    // 202 | PSSB fuse state5 | - | Float | 2 | RO | 0x03
                    // 208 | Battery ready flag | - | Float | 2 | RO | 0x03
                    // 210 | Act. state of charge | % | Float | 2 | RO | 0x03
                    new KostalModbusRegister(210, FLOAT32, quantityType(PERCENT), //
                            "actual-state-of-charge", "battery-information"),
                    // 214 | Battery temperature | °C | Float | 2 | RO | 0x03
                    new KostalModbusRegister(214, FLOAT32, CELSIUS_TO_KELVIN.andThen(quantityType(KELVIN)), //
                            "battery-temperature", "battery-information"),
                    // 216 | Battery voltage | V | Float | 2 | RO | 0x03
                    new KostalModbusRegister(216, FLOAT32, quantityType(VOLT), //
                            "battery-voltage", "battery-information") //
            ),
            // 218 | Cos φ (powermeter) | - | Float | 2 | RO | 0x03
            // 220 | Frequency (powermeter) | Hz | Float | 2 | RO | 0x03
            // 222 | Current phase 1 (powermeter) | A | Float | 2 | RO | 0x03
            // 224 | Active power phase 1 (powermeter) | W | Float | 2 | RO | 0x03
            // 226 | Reactive power phase 1 (powermeter) | Var | Float | 2 | RO | 0x03
            // 228 | Apparent power phase 1 (powermeter) | VA | Float | 2 | RO | 0x03
            // 230 | Voltage phase 1 (powermeter) | V | Float | 2 | RO | 0x03
            // 232 | Current phase 2 (powermeter) | A | Float | 2 | RO | 0x03
            // 234 | Active power phase 2 (powermeter) | W | Float | 2 | RO | 0x03
            // 236 | Reactive power phase 2 (powermeter) | Var | Float | 2 | RO | 0x03
            // 238 | Apparent power phase 2 (powermeter) | VA | Float | 2 | RO | 0x03
            // 240 | Voltage phase 2 (powermeter) | V | Float | 2 | RO | 0x03
            // 242 | Current phase 3 (powermeter) | A | Float | 2 | RO | 0x03
            // 244 | Active power phase 3 (powermeter) | W | Float | 2 | RO | 0x03
            // 246 | Reactive power phase 3 (powermeter) | Var | Float | 2 | RO | 0x03
            // 248 | Apparent power phase 3 (powermeter) | VA | Float | 2 | RO | 0x03
            // 250 | Voltage phase 3 (powermeter) | V | Float | 2 | RO | 0x03
            // 252 | Total active power (powermeter) Sensor position 1 (home consumption): (+) House consumption, (-)
            // generation
            // Sensor position 2 (grid connection): (+) Power supply, (-) feed-in | W | Float | 2 | RO | 0x03
            // 254 | Total reactive power (powermeter) Sensor position 2 (grid connection): (+) Power supply, (-)
            // feed-in Sensor
            // position 1 (home consumption): (+) House consumption, (-) generation | Var | Float | 2 | RO | 0x03
            // 256 | Total apparent power (powermeter) Sensor position 2 (grid connection): (+) Power supply, (-)
            // feed-in Sensor
            // position 1 (home consumption): (+) House consumption, (-) generation | VA | Float | 2 | RO | 0x03
            // 258 | Current DC1 | A | Float | 2 | RO | 0x03
            // 260 | Power DC1 | W | Float | 2 | RO | 0x03
            // 266 | Voltage DC1 | V | Float | 2 | RO | 0x03
            // 268 | Current DC2 | A | Float | 2 | RO | 0x03
            // 270 | Power DC2 | W | Float | 2 | RO | 0x03
            // 276 | Voltage DC2 | V | Float | 2 | RO | 0x03
            // 278 | Current DC3 | A | Float | 2 | RO | 0x03
            // 280 | Power DC3 | W | Float | 2 | RO | 0x03
            // 286 | Voltage DC3 | V | Float | 2 | RO | 0x03

            // Registers 320-328
            new ModbusRegisterRange(
                    // 320 | Total yield | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(320, FLOAT32, quantityType(WATT_HOUR), //
                            "total-yield", "statistics"), //
                    // 322 | Daily yield | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(322, FLOAT32, quantityType(WATT_HOUR), //
                            "daily-yield", "statistics"), //
                    // 324 | Yearly yield | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(324, FLOAT32, quantityType(WATT_HOUR), //
                            "yearly-yield", "statistics"), //
                    // 326 | Monthly yield | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(326, FLOAT32, quantityType(WATT_HOUR), //
                            "monthly-yield", "statistics") //
            ),
            // Network
            // 384 | Inverter network name | - | String | 32 | RO | 0x03
            // 416 | IP enable | - | U16 | 1 | RO | 0x03
            // 418 | Manual IP / Auto-IP | - | U16 | 1 | RO | 0x03
            // 420 | IP-address | - | String | 8 | RO | 0x03
            // 428 | IP-subnetmask | - | String | 8 | RO | 0x03
            // 436 | IP-gateway | - | String | 8 | RO | 0x03
            // 444 | IP-auto-DNS | - | U16 | 1 | RO | 0x03
            // 446 | IP-DNS1 | - | String | 8 | RO | 0x03
            // 454 | IP-DNS2 | - | String | 8 | RO | 0x03

            // Registers 512-529
            new ModbusRegisterRange(
                    // Battery Info
                    // 512 | Battery gross capacity | Ah | U32 | 2 | RO | 0x03
                    new KostalModbusRegister(512, UINT32, quantityType(AMPERE_HOUR), //
                            "battery-gross-capacity", "battery-information"),
                    // 514 | Battery actual SOC | % | U16 | 1 | RO | 0x03
                    new KostalModbusRegister(514, UINT16, quantityType(PERCENT), //
                            "battery-actual-soc", "battery-information"),
                    // 517 | Battery Manufacturer | - | String | 8 | RO | 0x03
                    // 525 | Battery Model ID | - | U32 | 2 | RO | 0x03
                    // 527 | Battery Serial Number | - | U32 | 2 | RO | 0x03
                    // 529 | Work Capacity | Wh | U32 | 2 | RO | 0x03
                    new KostalModbusRegister(529, UINT32, quantityType(WATT_HOUR), //
                            "work-capacity", "battery-information")//
            ),
            // Inverter Info
            // 531 | Inverter Max Power | W | U16 | 1 | RO | 0x03
            // 532 | Inverter Max Power Scale Factor | - | - | 1 | RO | 0x03
            // 533 | Active Power Setpoint | % | U16 | 1 | RW | 0x03/0x06
            // 535 | Inverter Manufacturer | - | String | 16 | RO | 0x03
            // 559 | Inverter Serial Number | - | String | 16 | RO | 0x03
            // 575 | Inverter Generation Power (actual) | W | S16 | 1 | RO | 0x03
            // 576 | Power Scale Factor | - | - | 1 | RO | 0x03
            // 577 | Generation Energy | Wh | U32 | 2 | RO | 0x03
            // 579 | Energy Scale Factor | - | - | 1 | RO | 0x03
            // 582 | Actual battery charge/discharge power | W | S16 | 1 | RO | 0x03
            // 583 | Reactive Power Setpoint | % | S16 | 1 | RW | 0x03/0x06
            // 585 | Delta-cos φ Setpoint | - | S16 | 1 | RW | 0x03/0x06
            // 586 | Battery Firmware | - | U32 | 2 | RO | 0x03
            // 588 | Battery Type6 | - | U16 | 1 | RO | 0x03

            // I/O-Board
            // 608 | I/O-Board, Switched Output 1 | - | U16 | 1 | RW | 0x03/0x06
            // 609 | I/O-Board, Switched Output 2 | - | U16 | 1 | RW | 0x03/0x06
            // 610 | I/O-Board, Switched Output 3 | - | U16 | 1 | RW | 0x03/0x06
            // 611 | I/O-Board, Switched Output 4 | - | U16 | 1 | RW | 0x03/0x06

            // Inverter Type
            // 768 | Productname (e.g. PLENTICORE plus) | - | String | 32 | RO | 0x03
            // 800 | Power class (e.g. 10) | - | String | 32 | RO | 0x03

            new ModbusRegisterRange(
                    // External Battery Management 1
                    // 1024 | Battery charge power (AC) setpoint Note1,6 | W | S16 | 1 | RO | 0x06
                    // 1025 | Power Scale Factor Note2, 6 | - | S16 | 1 | RO | 0x03
                    // 1026 | Battery charge power (AC) setpoint, absolute Note1,6 | W | Float | 2 | RW | 0x03/0x10
                    // 1028 | Battery charge current (DC) setpoint, relative Note 1,3 | % | Float | 2 | RW | 0x03/0x10
                    // 1030 | Battery charge power (AC) setpoint, relative Note 1,3,6 | % | Float | 2 | RW | 0x03/0x10
                    // 1032 | Battery charge current (DC) setpoint, absolute Note 1 | A | Float | 2 | RW | 0x03/0x10
                    // 1034 | Battery charge power (DC) setpoint, absolute Note 1 | W | Float | 2 | RW | 0x03/0x10
                    // 1036 | Battery charge power (DC) setpoint, relative Note 1,3 | % | Float | 2 | RW | 0x03/0x10
                    // 1038 | Battery max. charge power limit, absolute | W | Float | 2 | RW | 0x03/0x10
                    new KostalModbusRegister(1038, FLOAT32, quantityType(WATT), //
                            "battery-max-charge-power-limit", "battery-information"), //
                    // 1040 | Battery max. discharge power limit, absolute | W | Float | 2 | RW | 0x03/0x10
                    new KostalModbusRegister(1040, FLOAT32, quantityType(WATT), //
                            "battery-max-discharge-power-limit", "battery-information"),
                    // 1042 | Minimum SOC | % | Float | 2 | RW | 0x03/0x10
                    new KostalModbusRegister(1042, FLOAT32, quantityType(PERCENT), //
                            "minimum-soc", "battery-information"),
                    // 1044 | Maximum SOC | % | Float | 2 | RW | 0x03/0x10
                    new KostalModbusRegister(1044, FLOAT32, quantityType(PERCENT), //
                            "maximum-soc", "battery-information"),

                    // Energy Flow
                    // 1046 | Total DC charge energy (DC-side to battery) | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1046, FLOAT32, quantityType(WATT_HOUR), //
                            "total-dc-to-battery", "battery-information"),
                    // 1048 | Total DC discharge energy (DC-side from battery) | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1048, FLOAT32, quantityType(WATT_HOUR), //
                            "total-dc-from-battery", "battery-information"),
                    // 1050 | Total AC charge energy (AC-side to battery) | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1050, FLOAT32, quantityType(WATT_HOUR), //
                            "total-ac-to-battery", "battery-information"),
                    // 1052 | Total AC discharge energy (battery to grid) | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1052, FLOAT32, quantityType(WATT_HOUR), //
                            "total-battery-to-grid", "battery-information"),
                    // 1054 | Total AC charge energy (grid to battery) | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1054, FLOAT32, quantityType(WATT_HOUR), //
                            "total-grid-to-battery", "battery-information"),
                    // 1056 | Total DC PV energy (sum of all PV inputs) | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1056, FLOAT32, quantityType(WATT_HOUR), //
                            "total-dc-pv-energy", "string-information"),
                    // 1058 | Total DC energy from PV1 | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1058, FLOAT32, quantityType(WATT_HOUR), //
                            "total-dc-energy-pv1", "string-information"),
                    // 1060 | Total DC energy from PV2 | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1060, FLOAT32, quantityType(WATT_HOUR), //
                            "total-dc-energy-pv2", "string-information"),
                    // 1062 | Total DC energy from PV3 | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1062, FLOAT32, quantityType(WATT_HOUR), //
                            "total-dc-energy-pv3", "string-information"),
                    // 1064 | Total energy AC-side to grid | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1064, FLOAT32, quantityType(WATT_HOUR), //
                            "total-ac-to-grid", "consumption"),
                    // 1066 | Total DC power (sum of all PV inputs) | W | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1066, FLOAT32, quantityType(WATT), //
                            "total-dc-power-pv", "string-information"),

                    // External Battery Management 2
                    // 1068 | Battery work capacity | Wh | Float | 2 | RO | 0x03
                    new KostalModbusRegister(1068, FLOAT32, quantityType(WATT_HOUR), //
                            "battery-work-capacity", "battery-information")
            // 1070 | Battery serial number | - | U32 | 2 | RO | 0x03
            // 1072 | Reserved | - | - | 2 | RO | 0x03
            // 1074 | Reserved | - | - | 2 | RO | 0x03
            // 1076 | Maximum charge power limit (read-out from battery) | W | Float | 2 | RO | 0x03
            // 1078 | Maximum discharge power limit (read-out from battery) | W | Float | 2 | RO | 0x03
            // 1080 | Battery management mode Note4 | - | U8 | 1 | RO | 0x03
            // 1081 | reserved | - | - | 1 | RO | 0x03
            // 1082 | Installed sensor type Note 5 | - | U8 | 1 | RO | 0x03
            ));

    /**
     * Creates a Function that creates {@link QuantityType} states with the given {@link Unit}.
     *
     * @param unit {@link Unit} to be used for the value.
     * @return Function for value creation.
     */
    private static Function<BigDecimal, State> quantityType(Unit<?> unit) {
        return (BigDecimal value) -> new QuantityType<>(value, unit);
    }

    /**
     * Creates a Function that creates {@link StringType} states with a function mapping BigDecimal to String.
     *
     * @param toString function for mapping register value to string.
     * @return Function for value creation.
     */
    private static Function<BigDecimal, State> stringType(Function<BigDecimal, String> toString) {
        return (BigDecimal value) -> new StringType(toString.apply(value));
    }
}
