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
package org.openhab.binding.modbus.kostal.handler;

import static org.openhab.core.io.transport.modbus.ModbusConstants.ValueType.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.BaseModbusThingHandler;
import org.openhab.binding.modbus.kostal.internal.Endianness;
import org.openhab.binding.modbus.kostal.internal.KostalInverterConfiguration;
import org.openhab.binding.modbus.kostal.internal.KostalInverterConstants;
import org.openhab.binding.modbus.kostal.internal.ModbusRegisterGroup;
import org.openhab.core.io.transport.modbus.*;
import org.openhab.core.io.transport.modbus.exception.ModbusSlaveErrorResponseException;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KostalInverterHandler} is responsible for reading the modbus values of the
 * kostal inverter.
 *
 * @author Andreas Lanz - Initial contribution
 */
@NonNullByDefault
public class KostalInverterHandler extends BaseModbusThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KostalInverterHandler.class);

    private static final int TRIES = 1;

    private Endianness endianness = Endianness.BIG_ENDIAN;
    private int pollInterval;
    private int maxTries = TRIES;

    private final Map<ModbusRegisterGroup, PollTask> activeRequests = new HashMap<>();
    private final Map<ChannelUID, ModbusRegisterGroup> channelUidToRequest = new HashMap<>();

    private final List<ModbusRegisterGroup> modbusRequests = new ArrayList<>();

    @Nullable
    private String productName = null;
    @Nullable
    private String productClass = null;

    public KostalInverterHandler(Thing thing) {
        super(thing);
        this.modbusRequests.addAll(KostalInverterConstants.registerGroups);
    }

    private Endianness getEndianness() {
        return endianness;
    }

    @Override
    public void modbusInitialize() {
        final KostalInverterConfiguration config = getConfigAs(KostalInverterConfiguration.class);

        if (config.pollInterval <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid poll interval: " + config.pollInterval);
            return;
        }
        pollInterval = config.pollInterval;

        // Kostal is by default little endian
        endianness = config.littleEndian ? Endianness.LITTLE_ENDIAN : Endianness.BIG_ENDIAN;

        this.updateStatus(ThingStatus.UNKNOWN);

        readInverterType();
        readInverterInfo();

        for (ModbusRegisterGroup request : modbusRequests) {
            channelUidToRequest.putAll(
                    request.getRegisters().stream().collect(Collectors.toMap(this::createChannelUid, r -> request)));

            updatePollTask(request);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        ModbusRegisterGroup modbusRegisterGroup = channelUidToRequest.get(channelUID);

        if (modbusRegisterGroup == null) {
            this.logger.debug("Unknown Channel Linked: {}", channelUID);
        } else {
            if (!activeRequests.containsKey(modbusRegisterGroup)) {
                updatePollTask(modbusRegisterGroup);
            }
        }

        super.channelLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);

        ModbusRegisterGroup modbusRegisterGroup = channelUidToRequest.get(channelUID);

        if (modbusRegisterGroup == null) {
            this.logger.debug("Unknown Channel Unlinked: {}", channelUID);
        } else {
            if (activeRequests.containsKey(modbusRegisterGroup)) {
                updatePollTask(modbusRegisterGroup);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType && !this.activeRequests.isEmpty()) {
            ModbusRegisterGroup registerGroup = this.channelUidToRequest.get(channelUID);

            if (registerGroup == null) {
                logger.debug("Unknown Channel UID: {}", channelUID);
                return;
            }

            if (this.activeRequests.containsKey(registerGroup)) {
                submitOneTimePoll(registerGroup.buildBluePrint(getSlaveId(), TRIES),
                        (AsyncModbusReadResult result) -> this.readSuccessful(registerGroup, result),
                        (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> readError(registerGroup, error));
            }
        }
    }

    private void updatePollTask(ModbusRegisterGroup request) {
        // Any linked channels for this register group?
        boolean hasActiveChannel = request.getRegisters().stream().map(this::createChannelUid).anyMatch(this::isLinked);
        if (hasActiveChannel) {
            // Creating poll task if there is not yet any.
            activeRequests.computeIfAbsent(request, (r) -> registerRegularPoll(r.buildBluePrint(getSlaveId(), maxTries),
                    pollInterval, 0, result -> this.readSuccessful(r, result), error -> readError(r, error)));
        } else {
            // No more linked channels. Remove active PollTask.
            PollTask pollTask = activeRequests.remove(request);
            if (pollTask != null) {
                unregisterRegularPoll(pollTask);
            }
        }
    }

    private void readInverterType() {
        // 768 | Productname (e.g. PLENTICORE plus) | - | String | 32 | RO | 0x03
        // 800 | Power class (e.g. 10) | - | String | 32 | RO | 0x03
        String productName = editProperties().get("productName");
        if (productName == null || productName.isEmpty()) {
            submitOneTimePoll(
                    new ModbusReadRequestBlueprint(getSlaveId(), ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, //
                            768, 64, 1), //
                    this::readInverterType, this::readError);
        }
    }

    private void readInverterType(AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            productName = ModbusBitUtilities.extractStringFromRegisters(registers, 0, 32, StandardCharsets.US_ASCII);
            productClass = ModbusBitUtilities.extractStringFromRegisters(registers, 32, 32, StandardCharsets.US_ASCII);
            Map<String, String> properties = editProperties();
            properties.put("productName", Objects.requireNonNull(productName));
            properties.put("productClass", Objects.requireNonNull(productClass));
            updateProperties(properties);

            logger.debug("Detected Kostal Inverter: {} {}", productName, productClass);
        });
    }

    private void readInverterInfo() {
        // 5 | MODBUS Byte Order | - | U16 | 1 | R/W | 0x03/0x06
        // 6 | Inverter article number | - | String | 8 | RO | 0x03
        // 14 | Inverter serial number | - | String | 8 | RO | 0x03
        // 30 | Number of bidirectional converter | - | U16 | 1 | RO | 0x03
        // 32 | Number of AC phases | - | U16 | 1 | RO | 0x03
        // 34 | Number of PV strings | - | U16 | 1 | RO | 0x03
        // 36 | Hardware-Version | - | U16 | 2 | RO | 0x03
        // 38 | Software-Version Maincontroller (MC) | - | String | 8 | RO | 0x03
        // 46 | Software-Version IO-Controller (IOC) | - | String | 8 | RO | 0x03
        // 54 | Power-ID | - | U32 | 2 | RO | 0x03
        String serialNumber = editProperties().get("serialNumber");
        if (serialNumber == null || serialNumber.isEmpty()) {
            submitOneTimePoll(
                    new ModbusReadRequestBlueprint(getSlaveId(), ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, //
                            5, 51, 1), //
                    this::readInverterInfo, this::readError);
        }
    }

    private void readInverterInfo(AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            int index = 0;
            // 5 | MODBUS Byte Order | - | U16 | 1 | R/W | 0x03/0x06
            int byteOrder = ModbusBitUtilities.extractUInt8(registers.getBytes(), index);
            index += 1;
            // 6 | Inverter article number | - | String | 8 | RO | 0x03
            String articleNumber = ModbusBitUtilities.extractStringFromRegisters(registers, index, 8,
                    StandardCharsets.US_ASCII);
            index += 8;
            // 14 | Inverter serial number | - | String | 8 | RO | 0x03
            String serialNumber = ModbusBitUtilities.extractStringFromRegisters(registers, index, 8,
                    StandardCharsets.US_ASCII);
            index += 8;
            // 30 | Number of bidirectional converter | - | U16 | 1 | RO | 0x03
            // 32 | Number of AC phases | - | U16 | 1 | RO | 0x03
            // 34 | Number of PV strings | - | U16 | 1 | RO | 0x03
            index += 3;
            // 36 | Hardware-Version | - | U16 | 2 | RO | 0x03
            int hardwareVersion = ModbusBitUtilities.extractUInt16(registers.getBytes(), index);
            index += 2;
            // 38 | Software-Version Maincontroller (MC) | - | String | 8 | RO | 0x03
            String softwareVersionMainController = ModbusBitUtilities.extractStringFromRegisters(registers, index, 8,
                    StandardCharsets.US_ASCII);
            index += 8;
            // 46 | Software-Version IO-Controller (IOC) | - | String | 8 | RO | 0x03
            String softwareVersionIoController = ModbusBitUtilities.extractStringFromRegisters(registers, index, 8,
                    StandardCharsets.US_ASCII);
            index += 8;
            // 54 | Power-ID | - | U32 | 2 | RO | 0x03

            if (byteOrder == 0 && endianness != Endianness.LITTLE_ENDIAN
                    || byteOrder == 1 && endianness != Endianness.BIG_ENDIAN) {
                logger.warn("Endianness might be miss-configured. Inverter reports {}", //
                        byteOrder == 0 ? "little endian" : "big endian");
            }

            Map<String, String> properties = editProperties();
            properties.put("articleNumber", articleNumber);
            properties.put("serialNumber", serialNumber);
            properties.put("hardwareVersion", "" + hardwareVersion);
            properties.put("softwareVersionMainController", softwareVersionMainController);
            properties.put("softwareVersionIoController", softwareVersionIoController);
            updateProperties(properties);

        });
    }

    private ModbusConstants.ValueType mapValueType(ModbusConstants.ValueType type, Endianness endianness) {
        // Modbus default is Big Endian
        if (Endianness.BIG_ENDIAN.equals(endianness)) {
            return type;
        }

        // For little endian swap byte order
        return switch (type) {
            case INT32:
                yield INT32_SWAP;
            case UINT32:
                yield UINT32_SWAP;
            case FLOAT32:
                yield FLOAT32_SWAP;
            case INT64:
                yield INT64_SWAP;
            case UINT64:
                yield UINT64_SWAP;
            default:
                yield type;
        };
    }

    private void readSuccessful(ModbusRegisterGroup request, AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            int firstRegister = request.getRegisters().get(0).getRegisterNumber();

            for (ModbusRegisterGroup.ModbusRegister channel : request.getRegisters()) {
                int index = channel.getRegisterNumber() - firstRegister;

                ModbusConstants.ValueType type = mapValueType(channel.getType(), getEndianness());
                ModbusBitUtilities.extractStateFromRegisters(registers, index, type).map(channel::createState)
                        .ifPresent(v -> updateState(createChannelUid(channel), v));
            }
        });
    }

    private void readError(AsyncModbusFailure<ModbusReadRequestBlueprint> error) {
        this.logger.warn("Failed to get modbus data", error.getCause());

        if (error.getCause() instanceof ModbusSlaveErrorResponseException cause) {
            if (cause.getExceptionCode() == ModbusSlaveErrorResponseException.ILLEGAL_DATA_ACCESS) {
                logger.warn("IllegalDataAccess error. This indicates that this inverter is not properly supported, "
                        + "yet.");
            }
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Failed to retrieve data: " + error.getCause().getMessage());
    }

    private void readError(ModbusRegisterGroup request, AsyncModbusFailure<ModbusReadRequestBlueprint> error) {
        readError(error);
    }

    private ChannelUID createChannelUid(ModbusRegisterGroup.ModbusRegister register) {
        return new ChannelUID( //
                thing.getUID(), //
                register.getChannelGroup(), //
                register.getChannelName() //
        );
    }
}
