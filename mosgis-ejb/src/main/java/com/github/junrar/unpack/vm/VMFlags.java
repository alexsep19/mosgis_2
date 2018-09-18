package com.github.junrar.unpack.vm;

public enum VMFlags {
	/**
	 * 
	 */
	VM_FC (1),
	/**
	 * 
	 */
	VM_FZ (2),
	/**
	 * 
	 */
	VM_FS (0x80000000);
	
	private int flag;
	
	private VMFlags(int flag){
		this.flag = flag;
	}
	
	/**
	 * Returns the VMFlags Type of the given int or null
	 * @param flag as int
	 * @return VMFlag of the int value
	 */
	public static VMFlags findFlag(int flag){
		if(VM_FC.equals(flag)){
			return VM_FC;
		}
		if(VM_FS.equals(flag)){
			return VM_FS;
		}
		if(VM_FZ.equals(flag)){
			return VM_FZ;
		}
		return null;
	}
	
	/**
	 * Returns true if the flag provided as int is equal to the enum
	 * @param flag
	 * @return returns true if the flag is equal to the enum
	 */
	public boolean equals(int flag){
		return this.flag == flag;
	}
	/**
	 * @return the flag as int
	 */
	public int getFlag() {
		return flag;
	}
	
}
