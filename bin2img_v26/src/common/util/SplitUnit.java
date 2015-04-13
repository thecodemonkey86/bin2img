package common.util;

import util.Pair;

public class SplitUnit extends Pair<String, Integer>{

	public SplitUnit(String name, int value) {
		super(name, value);
	}

	@Override
	public String toString() {
		return value1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SplitUnit) {
			return ((SplitUnit)obj).value2.equals(value2);
		} else {
			return false;
		}
		
	}
}
