package com.github.junrar.rarfile;

import java.util.logging.Logger;

import com.github.junrar.io.Raw;


public class SubBlockHeader 
extends BlockHeader 
{
	private Logger logger = Logger.getLogger(SubBlockHeader.class.getName());
	
	public static final short SubBlockHeaderSize = 3;
	
	private short subType;
	private byte level;
	
	public SubBlockHeader(SubBlockHeader sb)
	{
		super(sb);
		subType = sb.getSubType().getSubblocktype();
		level = sb.getLevel();
	}
	
	public SubBlockHeader(BlockHeader bh, byte[] subblock)
	{
		super(bh);
		int position = 0;
		subType = Raw.readShortLittleEndian(subblock, position);
		position +=2;
		level |= subblock[position]&0xff;
	}

	/**
	 * @return
	 */
	public byte getLevel() {
		return level;
	}

	/**
	 * @return
	 */
	public SubBlockHeaderType getSubType() {
		return SubBlockHeaderType.findSubblockHeaderType(subType);
	}

	public void print()
	{
		super.print();
		logger.info("subtype: "+getSubType());
		logger.info("level: "+level);
	}
}
