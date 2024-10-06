/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.kostal.internal;

import java.math.BigDecimal;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * The {@link KostalModbusRegister} is responsible for defining Modbus registers and their units.
 *
 * @author Andreas Lanz - Initial contribution
 */
@NonNullByDefault
public class KostalModbusRegister implements ModbusRegisterRange.ModbusRegister {

    private final int registerAddress;
    private final ValueType type;

    private final Function<BigDecimal, State> stateFactory;
    private final String channelGroup;
    private final String channelName;

    KostalModbusRegister(int registerAddress, ValueType type, Function<BigDecimal, State> stateFactory,
            String channelName, String channelGroup) {
        this.registerAddress = registerAddress;
        this.type = type;
        this.stateFactory = stateFactory;
        this.channelName = channelName;
        this.channelGroup = channelGroup;
    }

    /**
     * Returns the modbus register number.
     *
     * @return modbus register number.
     */
    @Override
    public int getRegisterAddress() {
        return registerAddress;
    }

    /**
     * Returns the {@link ValueType} for the channel.
     * 
     * @return {@link ValueType} for the channel.
     */
    public ValueType getType() {
        return type;
    }

    /**
     * Returns the count of registers read to return the value of this register.
     * 
     * @return register count.
     */
    @Override
    public int getRegisterCount() {
        return this.type.getBits() / 16;
    }

    /**
     * Returns the channel group.
     * 
     * @return channel group id.
     */
    public String getChannelGroup() {
        return channelGroup;
    }

    /**
     * Returns the channel name.
     * 
     * @return the channel name.
     */
    public String getChannelName() {
        return this.channelName;
    }

    /**
     * Creates the {@link State} for the given register value.
     *
     * @param registerValue the value for the channel.
     * @return {@link State] for the given value.
     */
    @NonNull
    public State createState(DecimalType registerValue) {
        final BigDecimal value = registerValue.toBigDecimal();

        return this.stateFactory.apply(value);
    }
}
