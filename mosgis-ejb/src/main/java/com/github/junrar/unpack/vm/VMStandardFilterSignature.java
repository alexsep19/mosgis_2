package com.github.junrar.unpack.vm;

public class VMStandardFilterSignature {
	private int length;

	private int CRC;

	private VMStandardFilters type;

	public VMStandardFilterSignature(int length, int crc, VMStandardFilters type) {
		super();
		this.length = length;
		CRC = crc;
		this.type = type;
	}

	public int getCRC() {
		return CRC;
	}

	public void setCRC(int crc) {
		CRC = crc;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public VMStandardFilters getType() {
		return type;
	}

	public void setType(VMStandardFilters type) {
		this.type = type;
	}

}
