/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.kostal.internal;

import java.math.BigDecimal;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants for converting values.
 *
 * @author Andreas Lanz - Initial contribution
 */
@NonNullByDefault
final class ConversionConstants {

    private ConversionConstants() {
    }

    /**
     * Value conversion from Celsius to Kelvin.
     */
    static final Function<BigDecimal, BigDecimal> CELSIUS_TO_KELVIN = (BigDecimal celsius) -> celsius
            .add(new BigDecimal("273.15"));

    static final Function<BigDecimal, String> MAP_INVERTER_STATES = (BigDecimal state) -> switch (state.intValue()) {
        case 0:
            yield "Off";
        case 1:
            yield "Init";
        case 2:
            yield "IsoMeas";
        case 3:
            yield "GridCheck";
        case 4:
            yield "StartUp";
        case 5:
            yield "-";
        case 6:
            yield "FeedIn";
        case 7:
            yield "Throttled";
        case 8:
            yield "ExtSwitchOff";
        case 9:
            yield "Update";
        case 10:
            yield "Standby";
        case 11:
            yield "GridSync";
        case 12:
            yield "GridPreCheck";
        case 13:
            yield "GridSwitchOff";
        case 14:
            yield "Overheating";
        case 15:
            yield "Shutdown";
        case 16:
            yield "ImproperDcVoltage";
        case 17:
            yield "ESB";
        case 18:
            yield "Unknown";
        default:
            yield "Undefined";
    };

    static final Function<BigDecimal, String> MAP_STATE_OF_ENERGY_MANAGER = (
            BigDecimal state) -> switch (state.intValue()) {
                case 0x00:
                    yield "Idle";
                case 0x01:
                    yield "n/a";
                case 0x02:
                    yield "Emergency Battery Charge";
                case 0x04:
                    yield "n/a";
                case 0x08:
                    yield "Winter Mode Step 1";
                case 0x10:
                    yield "Winter Mode Step 2";
                default:
                    yield "Unknown";
            };
}
