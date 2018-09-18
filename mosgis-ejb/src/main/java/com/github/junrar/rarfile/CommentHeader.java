package com.github.junrar.rarfile;

import com.github.junrar.io.Raw;

public class CommentHeader extends BaseBlock {
	
	public static final short commentHeaderSize = 6;
	
	private short unpSize;
	private byte unpVersion;
	private byte unpMethod;
	private short commCRC;
	
	
	public CommentHeader(BaseBlock bb, byte[] commentHeader){
		super(bb);
		
		int pos =0;
		unpSize = Raw.readShortLittleEndian(commentHeader, pos);
		pos += 2;
		unpVersion |= commentHeader[pos]&0xff;
		pos++;
		
		unpMethod |= commentHeader[pos]&0xff;
		pos++;
		commCRC =Raw.readShortLittleEndian(commentHeader, pos);
		
	}
	
	public short getCommCRC() {
		return commCRC;
	}
	
	public byte getUnpMethod() {
		return unpMethod;
	}
	
	public short getUnpSize() {
		return unpSize;
	}
	
	public byte getUnpVersion() {
		return unpVersion;
	}
}
