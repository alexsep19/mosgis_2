package com.github.junrar.unpack.vm;

public class VMPreparedOperand {
	private VMOpType Type;
	private int Data;
	private int Base;
	private int offset;
	
	
	public int getBase() {
		return Base;
	}
	public void setBase(int base) {
		Base = base;
	}
	public int getData() {
		return Data;
	}
	public void setData(int data) {
		Data = data;
	}
	public VMOpType getType() {
		return Type;
	}
	public void setType(VMOpType type) {
		Type = type;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	 
}
