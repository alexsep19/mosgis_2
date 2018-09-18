package com.github.junrar.unpack.ppm;

import com.github.junrar.io.Raw;

public class RarNode extends Pointer{
	private int next; //rarnode pointer

	public static final int size = 4;
	
	public RarNode(byte[] mem){
		super(mem);
	}
	
	public int getNext() {
		if(mem!=null){
			next = Raw.readIntLittleEndian(mem,  pos);
		}
		return next;
	}

	public void setNext(RarNode next) {
		setNext(next.getAddress());
	}
	
	public void setNext(int next) {
		this.next = next;
		if(mem!=null){
			Raw.writeIntLittleEndian(mem, pos, next);
		}
	}

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("State[");
        buffer.append("\n  pos=");
        buffer.append(pos);
        buffer.append("\n  size=");
        buffer.append(size);
        buffer.append("\n  next=");
        buffer.append(getNext());
        buffer.append("\n]");
        return buffer.toString();
    }
}
