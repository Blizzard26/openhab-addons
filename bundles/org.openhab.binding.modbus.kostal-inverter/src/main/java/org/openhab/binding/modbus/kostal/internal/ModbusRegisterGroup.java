package org.openhab.binding.modbus.kostal.internal;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.modbus.ModbusConstants;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

@NonNullByDefault
public class ModbusRegisterGroup {

    public interface ModbusRegister {

        int getRegisterNumber();

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

    public ModbusRegisterGroup(List<ModbusRegister> registers) {
        this.registers = new ArrayList<>(registers);
        init(registers);
    }

    public ModbusRegisterGroup(ModbusRegister... registers) {
        this(Arrays.asList(registers));
    }

    private void init(List<ModbusRegister> registers) {
        if (registers.isEmpty()) {
            throw new IllegalArgumentException("At least one register must be defined");
        }

        // Make sure registers are sorted by Register Number
        this.registers.sort(Comparator.comparingInt(ModbusRegister::getRegisterNumber));
        ModbusRegister first = this.registers.get(0);
        ModbusRegister last = this.registers.get(this.registers.size() - 1);

        startAddress = first.getRegisterNumber();
        registerCount = last.getRegisterNumber() - first.getRegisterNumber() + last.getRegisterCount();

        if (registerCount > ModbusConstants.MAX_REGISTERS_READ_COUNT) {
            throw new IllegalStateException("ModbusRegisterGroup must not contain more than 125 registers.");
        }
    }

    public List<ModbusRegister> getRegisters() {
        return registers;
    }

    public ModbusReadRequestBlueprint buildBluePrint(int slaveId, int maxTries) {
        return new ModbusReadRequestBlueprint(slaveId, functionCode, startAddress, registerCount, maxTries);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModbusRegisterGroup that)) {
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
