package net.schwarzbaer.java.lib.openwebif;

import java.util.Arrays;
import java.util.Objects;

public class StationID implements Comparable<StationID> {
	
	// 1:0:1:sid:tsid:onid:namespace:0:0:0:
	final Integer[] numbers;
	StationID() {
		numbers = new Integer[10];
	}
	
	public boolean isMarker() {
		return numbers[1]==0x64;
	}
	
	public static boolean isMarker(String sref) {
		return sref!=null && sref.startsWith("1:64:");
	}
	
	public boolean isSameTransponderAs(StationID other) {
		return isSameTransponder(this, other);
	}
	
	public static boolean isSameTransponder(StationID id1, StationID id2) {
		if (id1 == null || id2 == null) return false;
		return Objects.equals(
				getTransponderID(id1),
				getTransponderID(id2)
		);
	}

	public static Integer getTransponderID(StationID id)
	{
		return id==null ? null : id.getNumber(4);
	}

	public Integer getNumber(int index) {
		if (0 <= index && index < numbers.length)
			return numbers[index];
		return null;
	}
	
	@Override public int compareTo(StationID other) {
		if (other==null) return -1;
		Integer thisN,otherN;
		for (int i=0; i<numbers.length; i++) {
			thisN = this.numbers[i];
			otherN = other.numbers[i];
			if (!equals(thisN, otherN)) {
				if (thisN==null) return +1;
				if (otherN==null) return -1;
				return thisN.intValue() - otherN.intValue();
			}
		}
		return 0;
	}

	public String toIDStr(boolean addColon) {
		return toIDStr()+(addColon ? ":" : "");
	}
	public String toIDStr() {
		return toJoinedHexStrings(":");
	}

	public String toPiconImageFileName() {
		return toJoinedHexStrings("_")+".png";
	}

	public String toJoinedHexStrings(String delimiter) {
		return toJoinedStrings(delimiter, "%X");
	}

	public String toJoinedStrings(String delimiter, String numberFormat) {
		return String.join(delimiter, getNumbersAsHexStringIterable(numberFormat));
	}

	private Iterable<String> getNumbersAsHexStringIterable(String numberFormat) {
		return ()->Arrays.stream(numbers).map(i->String.format(numberFormat,i)).iterator();
	}

	public static StationID parseIDStr(String idStr) {
		return parse(idStr.split(":",-1), "ID String", idStr);
	}
	
	public static StationID parsePiconImageFileName(String filename) {
		// 1_0_1_17_1_85_FFFF0000_0_0_0.png
		String idStr = filename;
		int pos = idStr.indexOf('.');
		if (pos>=0) idStr = idStr.substring(0,pos);
		if (idStr.length()>1) {
			String endStr = idStr.substring(idStr.length()-1);
			if (endStr.equals("_")) idStr = idStr.substring(0,idStr.length()-1);
		}
		String[] numberStrs = idStr.split("_",-1);
		return parse(numberStrs, "File Name", filename);
	}

	private static StationID parse(String[] numberStrs, String sourceType, String sourceStr) {
		StationID stationID = new StationID();
		for (int i=0; i<stationID.numbers.length; i++) {
			if (i<numberStrs.length)
				try { stationID.numbers[i] = Integer.parseUnsignedInt(numberStrs[i], 16); }
				catch (NumberFormatException e) {
					System.err.printf("Parse Error at position %d (%s) in %s \"%s\": Can't parse hex integer.%n", i, numberStrs[i], sourceType, sourceStr);
					// e.printStackTrace();
					return null;
				}
			else
				stationID.numbers[i] = null;
		}
		return stationID;
	}

	@Override public String toString() {
		return toIDStr();
	}
	
	@Override public int hashCode() {
		int value = 0;
		for (Integer x:numbers) value = value^x;
		return value;
	}
	
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof StationID)) return false;
		StationID other = (StationID)obj;
		for (int i=0; i<numbers.length; i++)
			if (!equals(this.numbers[i], other.numbers[i]))
				return false;
		return true;
	}
	
	private boolean equals(Integer i1, Integer i2) {
		if (i1==null) return (i2==null);
		if (i2==null) return false;
		return (i1.intValue()==i2.intValue());
	}
}