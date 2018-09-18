package com.github.junrar.unpack.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class VMPreparedProgram 
{
	private List<VMPreparedCommand> Cmd  = new ArrayList<VMPreparedCommand>();
	private List<VMPreparedCommand> AltCmd  =new ArrayList<VMPreparedCommand>();
	private int CmdCount;


	
	private Vector<Byte> GlobalData = new Vector<Byte>();
	private Vector<Byte> StaticData = new Vector<Byte>(); // static data contained in DB operators
	private int InitR[] = new int[7];

	private int FilteredDataOffset;
	private int FilteredDataSize;
	
	public VMPreparedProgram() 
	{
		AltCmd=null;
	}

	

	public List<VMPreparedCommand> getAltCmd() {
		return AltCmd;
	}



	public void setAltCmd(List<VMPreparedCommand> altCmd) {
		AltCmd = altCmd;
	}



	public List<VMPreparedCommand> getCmd() {
		return Cmd;
	}

	public void setCmd(List<VMPreparedCommand> cmd) {
		Cmd = cmd;
	}

	public int getCmdCount() {
		return CmdCount;
	}

	public void setCmdCount(int cmdCount) {
		CmdCount = cmdCount;
	}

	

	public int getFilteredDataOffset() {
		return FilteredDataOffset;
	}



	public void setFilteredDataOffset(int filteredDataOffset) {
		FilteredDataOffset = filteredDataOffset;
	}



	public int getFilteredDataSize() {
		return FilteredDataSize;
	}

	public void setFilteredDataSize(int filteredDataSize) {
		FilteredDataSize = filteredDataSize;
	}

	public Vector<Byte> getGlobalData() {
		return GlobalData;
	}

	public void setGlobalData(Vector<Byte> globalData) {
		GlobalData = globalData;
	}

	public int[] getInitR() {
		return InitR;
	}

	public void setInitR(int[] initR) {
		InitR = initR;
	}

	public Vector<Byte> getStaticData() {
		return StaticData;
	}

	public void setStaticData(Vector<Byte> staticData) {
		StaticData = staticData;
	}
	
	
}
