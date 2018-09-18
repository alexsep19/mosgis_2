package com.github.junrar.unpack.ppm;

import com.github.junrar.io.Raw;

public class FreqData extends Pointer{

	public static final int size = 6; 

//    struct FreqData
//    {
//        ushort SummFreq;
//        STATE _PACK_ATTR * Stats;
//    };

	public FreqData(byte[]mem){
		super(mem);
	}

    public FreqData init(byte[] mem) {
        this.mem = mem;
        pos = 0;
        return this;
    }

    public int getSummFreq() {
		return Raw.readShortLittleEndian(mem,  pos)&0xffff;
	}

	public void setSummFreq(int summFreq) {
        Raw.writeShortLittleEndian(mem, pos, (short)summFreq);
	}

    public void incSummFreq(int dSummFreq) {
        Raw.incShortLittleEndian(mem, pos, dSummFreq);
    }

    public int getStats() {
        return Raw.readIntLittleEndian(mem,  pos+2);
	}

	public void setStats(State state) {
		setStats(state.getAddress());
	}

    public void setStats(int state) {
        Raw.writeIntLittleEndian(mem, pos+2, state);
	}

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("FreqData[");
        buffer.append("\n  pos=");
        buffer.append(pos);
        buffer.append("\n  size=");
        buffer.append(size);
        buffer.append("\n  summFreq=");
        buffer.append(getSummFreq());
        buffer.append("\n  stats=");
        buffer.append(getStats());
        buffer.append("\n]");
        return buffer.toString();
    }
	
}
