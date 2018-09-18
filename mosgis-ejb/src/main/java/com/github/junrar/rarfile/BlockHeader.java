package com.github.junrar.rarfile;

import java.util.logging.Logger;

import com.github.junrar.io.Raw;

public class BlockHeader extends BaseBlock{
	public static final short blockHeaderSize = 4;
	
	private Logger logger = Logger.getLogger(BlockHeader.class.getName());
	
	private int dataSize;
	private int packSize;
    
    public BlockHeader(){
    	
    }
    
    public BlockHeader(BlockHeader bh){
    	super(bh);
    	this.packSize = bh.getDataSize();
    	this.dataSize = packSize;
    	this.positionInFile = bh.getPositionInFile();
    }
    
    public BlockHeader(BaseBlock bb, byte[] blockHeader) 
    {
    	super(bb);
    	
    	this.packSize = Raw.readIntLittleEndian(blockHeader, 0);
    	this.dataSize  = this.packSize;
    }
    
	public int getDataSize() {
		return dataSize;
	}
	
	public int getPackSize() {
		return packSize;
	}
    
    public void print(){
    	super.print();
    	String s = "DataSize: "+getDataSize()+" packSize: "+getPackSize();
    	logger.info(s);
    }
}
