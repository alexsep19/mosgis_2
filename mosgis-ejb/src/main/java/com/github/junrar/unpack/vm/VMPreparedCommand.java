package com.github.junrar.unpack.vm;

public class VMPreparedCommand {
	private VMCommands OpCode;
	private boolean ByteMode;
	private VMPreparedOperand Op1 = new VMPreparedOperand();
	private VMPreparedOperand Op2 = new VMPreparedOperand();
	
	public boolean isByteMode() {
		return ByteMode;
	}
	public void setByteMode(boolean byteMode) {
		ByteMode = byteMode;
	}
	public VMPreparedOperand getOp1() {
		return Op1;
	}
	public void setOp1(VMPreparedOperand op1) {
		Op1 = op1;
	}
	public VMPreparedOperand getOp2() {
		return Op2;
	}
	public void setOp2(VMPreparedOperand op2) {
		Op2 = op2;
	}
	public VMCommands getOpCode() {
		return OpCode;
	}
	public void setOpCode(VMCommands opCode) {
		OpCode = opCode;
	}
	 
}
