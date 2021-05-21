package net.schwarzbaer.java.lib.openwebif;

import java.util.function.Supplier;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;

public class Service {
	
	public String label;
	public final StationID stationID;

	protected Service() {
		label = null;
		stationID = new StationID();
	}
	
	public boolean isMarker() {
		return stationID.isMarker();
	}

	public static Service parseServiceStr(String serviceStr) {
		return parseServiceStr(serviceStr, Service::new);
	}
	public static <T extends Service> T parseServiceStr(String serviceStr, Supplier<T> createService) {
		T service = createService.get();
		String[] strs = serviceStr.split(":",12);
		for (int i=0; i<strs.length; i++) {
			if (i<10)
				try { service.stationID.numbers[i] = Integer.parseInt(strs[i], 16); }
				catch (NumberFormatException e) { System.err.printf("Parse Error @ [%d] in Service \"%s\": Can't parse hex integer.%n", i, serviceStr); return null; }
			else if (i==10) {
				if (!strs[i].isEmpty()) {
					System.err.printf("Parse Error @ [%d] in Service \"%s\": 11th Value is not empty.%n", i, serviceStr);
					return null;
				}
			} else if (i==11) {
				service.label = JSON_Data.decodeUnicode(strs[i]);
			}
		}
		return service;
	}

	@Override
	public String toString() {
		String str = stationID.toIDStr()+':';
		if (label!=null) str += ':'+label;
		return str;
	}
}