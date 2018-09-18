package com.github.junrar.unpack.vm;

public enum VMStandardFilters {
	VMSF_NONE ((int)0),
	VMSF_E8 ((int)1),
	VMSF_E8E9 ((int)2),
	VMSF_ITANIUM( (int)3), 
	VMSF_RGB ((int)4), 
	VMSF_AUDIO ((int)5), 
	VMSF_DELTA ((int)6),
	VMSF_UPCASE ((int)7);
	
	private int filter;
	
	private VMStandardFilters(int filter){
		this.filter=filter;
	}

	public int getFilter() {
		return filter;
	}
	
	public boolean equals(int filter){
		return this.filter == filter;
	}
	
	public static VMStandardFilters findFilter(int filter){
		if (VMSF_NONE.equals(filter)) {
			return VMSF_NONE;
		}		 
		
		if (VMSF_E8.equals(filter)) {
			return VMSF_E8;
		}		 
		
		if (VMSF_E8E9.equals(filter)) {
			return VMSF_E8E9;
		}		 
		if (VMSF_ITANIUM.equals(filter)) {
			return VMSF_ITANIUM;
		}		 
		
		if (VMSF_RGB.equals(filter)) {
			return VMSF_RGB;
		}		  
		
		if (VMSF_AUDIO.equals(filter)) {
			return VMSF_AUDIO;
		}		 
		if (VMSF_DELTA.equals(filter)) {
			return VMSF_DELTA;
		}		
		return null;
	}
	
}
