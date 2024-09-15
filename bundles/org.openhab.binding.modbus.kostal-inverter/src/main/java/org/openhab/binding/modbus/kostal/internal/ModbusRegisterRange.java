package org.openhab.binding.modbus.kostal.internal;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.modbus.ModbusConstants;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * Represents a range of modbus registers which can be read using one read request.
 *
 * <p>
 * It is possible to read up to 125 registers from modbus in one read request
 * as long as all addresses in the range are defined.
 * </p>
 *
 * @author Andreas Lanz - Initial contribution
 */
@NonNullByDefault
public class ModbusRegisterRange {

    public interface ModbusRegister {

        int getRegisterAddress();

        int getRegisterCount();

        ModbusConstants.ValueType getType();

        String getChannelGroup();

        String getChannelName();

        State createState(DecimalType decimalType);
    }

    private ModbusReadFunctionCode functionCode = ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS;

    private final List<ModbusRegister> registers;

    private int startAddress;
    private int registerCount;

    public ModbusRegisterRange(List<ModbusRegister> registers) {
        this.registers = new ArrayList<>(registers);
        init(registers);
    }

    public ModbusRegisterRange(ModbusRegister... registers) {
        this(Arrays.asList(registers));
    }

    private void init(List<ModbusRegister> registers) {
        if (registers.isEmpty()) {
            throw new IllegalArgumentException("At least one register must be defined");
        }

        // Make sure registers are sorted by Register Number
        this.registers.sort(Comparator.comparingInt(ModbusRegister::getRegisterAddress));
        ModbusRegister first = this.registers.get(0);
        ModbusRegister last = this.registers.get(this.registers.size() - 1);

        startAddress = first.getRegisterAddress();
        registerCount = last.getRegisterAddress() - first.getRegisterAddress() + last.getRegisterCount();

        if (registerCount > ModbusConstants.MAX_REGISTERS_READ_COUNT) {
            throw new IllegalArgumentException("ModbusRegisterGroup must not contain more than 125 registers.");
        }
    }

    public int getStartAddress() {
        return startAddress;
    }

    /**
     * Returns the {@link ModbusRegister} in this range.
     *
     * @return the registers in this range.
     */
    public List<ModbusRegister> getRegisters() {
        return Collections.unmodifiableList(registers);
    }

    /**
     * Builds a {@link ModbusReadRequestBlueprint} for this range of registers.
     * 
     * @param slaveId id the of the modbus slave to read from
     * @param maxTries maximum number of tries to read.
     * @return ModbusReadRequestBlueprint for this range of registers.
     */
    public ModbusReadRequestBlueprint buildReadRequestBluePrint(int slaveId, int maxTries) {
        return new ModbusReadRequestBlueprint(slaveId, functionCode, startAddress, registerCount, maxTries);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModbusRegisterRange that)) {
            return false;
        }
        return startAddress == that.startAddress && registerCount == that.registerCount
                && functionCode == that.functionCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionCode, startAddress, registerCount);
    }
}
