package com.github.junrar.unpack.ppm;

public abstract class Pointer
{
	protected byte[] mem;
	protected int pos;
	
	/** 
	 * Initialize the object with the array (may be null)
	 * @param mem the byte array
	 */
	public Pointer(byte[] mem){
		this.mem = mem;
	}
	/**
	 * returns the position of this object in the byte[] 
	 * @return the address of this object
	 */
	public int getAddress(){
        assert (mem != null);
		return pos;
	}

	/**
	 * needs to set the fields of this object to the values in the byte[] 
	 * at the given position.
	 * be aware of the byte order
	 * @param pos the position this object should point to
	 * @return true if the address could be set
	 */
	public void setAddress(int pos) {
        assert (mem != null);
        assert (pos >= 0) && (pos < mem.length) : pos;
        this.pos = pos;
    }
}
