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
import org.openhab.binding.modbus.kostal.KostalInverterConfiguration;
import org.openhab.binding.modbus.kostal.internal.Endianness;
import org.openhab.binding.modbus.kostal.internal.KostalInverterConstants;
import org.openhab.binding.modbus.kostal.internal.ModbusRegisterRange;
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

    private Endianness endianness = Endianness.BIG_ENDIAN;
    private int pollInterval;
    private int maxTries;

    private final List<ModbusRegisterRange> modbusRequests = new ArrayList<>();
    private final Map<ChannelUID, ModbusRegisterRange> channelUidToRequest = new HashMap<>();

    private final Map<ModbusRegisterRange, PollTask> activeRequests = new HashMap<>();

    @Nullable
    private String productName = null;

    public KostalInverterHandler(Thing thing) {
        super(thing);
        this.modbusRequests.addAll(KostalInverterConstants.registerGroups);
    }

    private Endianness getEndianness() {
        return endianness;
    }

    @Override
    public void modbusInitialize() {
        // Read and validate configuration values
        final KostalInverterConfiguration config = getConfigAs(KostalInverterConfiguration.class);

        if (config.pollInterval <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid poll interval: " + config.pollInterval);
            return;
        }
        pollInterval = config.pollInterval;

        if (config.maxTries <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid number of max tries: " + config.maxTries);
            return;
        }
        maxTries = config.maxTries;

        // Kostal is by default little endian
        endianness = config.littleEndian ? Endianness.LITTLE_ENDIAN : Endianness.BIG_ENDIAN;

        this.updateStatus(ThingStatus.UNKNOWN);

        // Read inverter details to properties
        readInverterTypeAndInfo();

        // Initialize reading process
        for (ModbusRegisterRange request : modbusRequests) {
            channelUidToRequest.putAll(
                    request.getRegisters().stream().collect(Collectors.toMap(this::createChannelUid, r -> request)));

            updatePollTask(request);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        ModbusRegisterRange modbusRegisterRange = channelUidToRequest.get(channelUID);

        if (modbusRegisterRange == null) {
            this.logger.debug("Unknown Channel Linked: {}", channelUID);
        } else {
            if (!activeRequests.containsKey(modbusRegisterRange)) {
                // Register range of channel is not yet active. Activate reading process.
                updatePollTask(modbusRegisterRange);
            }
        }

        super.channelLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);

        ModbusRegisterRange modbusRegisterRange = channelUidToRequest.get(channelUID);

        if (modbusRegisterRange == null) {
            this.logger.debug("Unknown Channel Unlinked: {}", channelUID);
        } else {
            if (activeRequests.containsKey(modbusRegisterRange)) {
                // Check if reading for register range can be deactivated.
                updatePollTask(modbusRegisterRange);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        ModbusRegisterRange registerRange = this.channelUidToRequest.get(channelUID);

        if (registerRange == null) {
            logger.debug("Unknown Channel UID: {}", channelUID);
            return;
        }

        if (command instanceof RefreshType) {
            if (this.activeRequests.containsKey(registerRange)) {
                submitOneTimePoll(registerRange.buildReadRequestBluePrint(getSlaveId(), maxTries),
                        result -> this.readSuccessful(registerRange, result), this::readError);
            }
        }
    }

    private void updatePollTask(ModbusRegisterRange request) {
        // Any linked channels for this register group?
        boolean hasActiveChannel = request.getRegisters().stream().map(this::createChannelUid).anyMatch(this::isLinked);
        if (hasActiveChannel) {
            // Create poll task - if there is not yet any.
            activeRequests.computeIfAbsent(request,
                    registerRange -> registerRegularPoll(
                            registerRange.buildReadRequestBluePrint(getSlaveId(), maxTries), pollInterval, 0,
                            result -> this.readSuccessful(registerRange, result), this::readError));
        } else {
            // No more linked channels. Remove active PollTask.
            PollTask pollTask = activeRequests.remove(request);
            if (pollTask != null) {
                unregisterRegularPoll(pollTask);
            }
        }
    }

    private void readInverterTypeAndInfo() {
        // 768 | Productname (e.g. PLENTICORE plus) | - | String | 32 | RO | 0x03
        // 800 | Power class (e.g. 10) | - | String | 32 | RO | 0x03
        String productName = editProperties().get("productName");
        if (productName == null || productName.isEmpty()) {
            submitOneTimePoll(
                    new ModbusReadRequestBlueprint(getSlaveId(), ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, //
                            768, 64, 1), //
                    this::readInverterType, this::readError);
        }

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

    private void readInverterType(AsyncModbusReadResult result) {
        if (result.getRegisters().isEmpty()) {
            logger.debug("Request for holding registers did not return any registers.");
            return;
        }
        ModbusRegisterArray registers = result.getRegisters().get();
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        productName = ModbusBitUtilities.extractStringFromRegisters(registers, 0, 32, StandardCharsets.US_ASCII);
        String productClass = ModbusBitUtilities.extractStringFromRegisters(registers, 32, 32,
                StandardCharsets.US_ASCII);

        Map<String, String> properties = editProperties();
        properties.put("productName", Objects.requireNonNull(productName));
        properties.put("productClass", productClass);
        updateProperties(properties);

        logger.debug("Detected Kostal Inverter: {} {}", productName, productClass);
    }

    private void readInverterInfo(AsyncModbusReadResult result) {
        if (result.getRegisters().isEmpty()) {
            logger.debug("Request for holding registers did not return any registers.");
            return;
        }
        ModbusRegisterArray registers = result.getRegisters().get();
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        int index = 0;
        // 5 | MODBUS Byte Order | - | U16 | 1 | R/W | 0x03/0x06
        int byteOrder = ModbusBitUtilities.extractUInt8(registers.getBytes(), index);
        index += 1;
        // 6 | Inverter article number | - | String | 8 | RO | 0x03
        String articleNumber = ModbusBitUtilities.extractStringFromRegisters(registers, index, 16,
                StandardCharsets.US_ASCII);
        index += 8;
        // 14 | Inverter serial number | - | String | 8 | RO | 0x03
        String serialNumber = ModbusBitUtilities.extractStringFromRegisters(registers, index, 16,
                StandardCharsets.US_ASCII);
        index += 8;
        // 22-30 | ??
        // 30 | Number of bidirectional converter | - | U16 | 1 | RO | 0x03
        // 32 | Number of AC phases | - | U16 | 1 | RO | 0x03
        // 34 | Number of PV strings | - | U16 | 1 | RO | 0x03
        index += 14;
        // 36 | Hardware-Version | - | U16 | 2 | RO | 0x03
        int hardwareVersionRaw = ModbusBitUtilities.extractUInt16(registers.getBytes(), index);
        // Format is hex with inverted byte order (1537 -> 0x0601 -> 0106)
        String hardwareVersion = String.format("%02X%02X", hardwareVersionRaw & 0xFF, hardwareVersionRaw >> 8 & 0xFF);
        index += 2;
        // 38 | Software-Version Maincontroller (MC) | - | String | 8 | RO | 0x03
        String softwareVersionMainController = ModbusBitUtilities.extractStringFromRegisters(registers, index, 8,
                StandardCharsets.US_ASCII);
        index += 8;
        // 46 | Software-Version IO-Controller (IOC) | - | String | 8 | RO | 0x03
        String softwareVersionIoController = ModbusBitUtilities.extractStringFromRegisters(registers, index, 8,
                StandardCharsets.US_ASCII);
        // index += 8;
        // 54 | Power-ID | - | U32 | 2 | RO | 0x03

        if (byteOrder == 0 && endianness != Endianness.LITTLE_ENDIAN
                || byteOrder == 1 && endianness != Endianness.BIG_ENDIAN) {
            logger.warn("Endianness might be miss-configured. Inverter reports {}", //
                    byteOrder == 0 ? "little endian" : "big endian");
        }

        Map<String, String> properties = editProperties();
        properties.put("articleNumber", articleNumber);
        properties.put("serialNumber", serialNumber);
        properties.put("hardwareVersion", hardwareVersion);
        properties.put("softwareVersionMainController", softwareVersionMainController);
        properties.put("softwareVersionIoController", softwareVersionIoController);
        updateProperties(properties);
    }

    private ModbusConstants.ValueType mapValueType(ModbusConstants.ValueType type, Endianness endianness) {
        // Modbus default is Big Endian
        if (Endianness.BIG_ENDIAN.equals(endianness)) {
            return type;
        }

        // For little endian swap byte order of any 4 or 8 byte types (a single register contains 2 bytes and is not
        // swapped).
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

    private void readSuccessful(ModbusRegisterRange request, AsyncModbusReadResult result) {
        if (result.getRegisters().isEmpty()) {
            logger.debug("Request for holding registers did not return any registers.");
            return;
        }

        ModbusRegisterArray registers = result.getRegisters().get();
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        int firstRegister = request.getStartAddress();

        for (ModbusRegisterRange.ModbusRegister channel : request.getRegisters()) {
            int offset = channel.getRegisterAddress() - firstRegister;

            ModbusConstants.ValueType type = mapValueType(channel.getType(), getEndianness());
            ModbusBitUtilities.extractStateFromRegisters(registers, offset, type).map(channel::createState)
                    .ifPresent(v -> updateState(createChannelUid(channel), v));
        }
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

    private ChannelUID createChannelUid(ModbusRegisterRange.ModbusRegister register) {
        return new ChannelUID( //
                thing.getUID(), //
                register.getChannelGroup(), //
                register.getChannelName() //
        );
    }
}
