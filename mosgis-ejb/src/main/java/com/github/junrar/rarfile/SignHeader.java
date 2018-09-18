package com.github.junrar.rarfile;

import com.github.junrar.io.Raw;

public class SignHeader extends BaseBlock {
	
	public static final short signHeaderSize = 8;
	
	private int creationTime=0;
	private short arcNameSize=0;
	private short userNameSize=0;
	
	
	public SignHeader(BaseBlock bb, byte[] signHeader){
		super(bb);
		
		int pos = 0;
		creationTime = Raw.readIntLittleEndian(signHeader, pos);
		pos +=4;
		arcNameSize = Raw.readShortLittleEndian(signHeader, pos);
		pos+=2;
		userNameSize = Raw.readShortLittleEndian(signHeader, pos);
	}
	
	public short getArcNameSize() {
		return arcNameSize;
	}
	
	public int getCreationTime() {
		return creationTime;
	}
	
	public short getUserNameSize() {
		return userNameSize;
	}
}
