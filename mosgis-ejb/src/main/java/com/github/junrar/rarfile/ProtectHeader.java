package com.github.junrar.rarfile;

import com.github.junrar.io.Raw;

public class ProtectHeader extends BlockHeader {
		
	/**
	 * the header size
	 */
	public static final int protectHeaderSize = 8;
	
	private byte version;
	private short recSectors;
	private int totalBlocks;
	private byte mark;
	
	
	public ProtectHeader(BlockHeader bh, byte[] protectHeader){
		super(bh);
		
		int pos = 0;
		version |= protectHeader[pos]&0xff;
		
		recSectors = Raw.readShortLittleEndian(protectHeader, pos);
		pos += 2;
		totalBlocks = Raw.readIntLittleEndian(protectHeader, pos);
		pos += 4;
		mark |= protectHeader[pos]&0xff;
	}
	
	
	public byte getMark() {
		return mark;
	}
	
	public short getRecSectors() {
		return recSectors;
	}
	
	public int getTotalBlocks() {
		return totalBlocks;
	}
	
	public byte getVersion() {
		return version;
	}
}
