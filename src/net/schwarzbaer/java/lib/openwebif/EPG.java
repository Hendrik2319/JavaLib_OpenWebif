package net.schwarzbaer.java.lib.openwebif;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.function.Consumer;

public class EPG {

	private final Tools tools;
	private final HashMap<Long,EPGevent> events;
	private final HashMap<String,StationEPG> stationEPGs;
	
	public EPG(Tools tools) {
		this.tools = tools;
		events = new HashMap<>();
		stationEPGs = new HashMap<>();
	}
	
	public interface Tools {
		String getTimeStr(long millis);
	}
	
	public void readEPGforService(String baseURL, StationID stationID, Long beginTime_UnixTS, Long endTime_UnixTS, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return;
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		String beginTimeStr = beginTime_UnixTS==null ? "" : "&time=" +beginTime_UnixTS.toString();
		String   endTimeStr =   endTime_UnixTS==null ? "" : "&endTime="+endTime_UnixTS.toString();
		String url = String.format("%s/api/epgservice?sRef=%s%s%s", baseURL, stationID.toIDStr(true), beginTimeStr, endTimeStr);
		
		String baseURL_ = baseURL;
		Vector<EPGevent> events = OpenWebifTools.getContentAndParseIt(url, err->{
			err.printf("   getEPGforService(baseURL, stationID, [beginTime], [endTime])%n");
			err.printf("      baseURL  : \"%s\"%n", baseURL_);
			err.printf("      stationID: %s%n", stationID.toIDStr(true));
			if (beginTime_UnixTS!=null) err.printf("      beginTime: %d, %s%n", beginTime_UnixTS, tools.getTimeStr(beginTime_UnixTS*1000));
			if (  endTime_UnixTS!=null) err.printf("      endTime  : %d, %s%n",   endTime_UnixTS, tools.getTimeStr(  endTime_UnixTS*1000));
		}, result -> {
			OpenWebifTools.EPGeventListResult parseResult = new OpenWebifTools.EPGeventListResult(result);
			if (!parseResult.result) return null;
			return parseResult.events;
		}, setIndeterminateProgressTask);
		
		addAll(events);
	}
	
	public Vector<EPGevent> getEvents(StationID stationID, long beginTime_UnixTS, long endTime_UnixTS, boolean sorted) {
		StationEPG stationEPG = stationEPGs.get(stationID.toIDStr(true));
		if (stationEPG==null) return new Vector<>();
		return stationEPG.getEvents(events, beginTime_UnixTS, endTime_UnixTS, sorted);
	}

	private void addAll(Vector<EPGevent> events) {
		for (EPGevent event:events) {
			if (event.id==null) continue;
			
			this.events.put(event.id, event);
			
			if (event.sref==null) continue;
			
			StationEPG stationEPG = stationEPGs.get(event.sref);
			if (stationEPG==null) stationEPGs.put(event.sref,stationEPG = new StationEPG(event.sref));
			stationEPG.add(event.id);
		}
	}
	
	public static class StationEPG {

		private final String sref;
		private final HashSet<Long> eventIDs;

		public StationEPG(String sref) {
			this.sref = sref;
			eventIDs = new HashSet<>();
		}

		public Vector<EPGevent> getEvents(HashMap<Long,EPGevent> events, long beginTime_UnixTS, long endTime_UnixTS, boolean sorted) {
			Vector<EPGevent> result = new Vector<>();
			for (long eventID:eventIDs) {
				EPGevent event = events.get(eventID);
				if (!sref.equals(event.sref)) continue;
				
				long eventBegin = event.begin_timestamp;
				long eventDuration = event.duration_sec;
				if ( eventBegin+eventDuration < beginTime_UnixTS || endTime_UnixTS < eventBegin )
					continue;
				
				result.add(event);
			}
			
			if (sorted)
				result.sort(Comparator.comparingLong(event->event.begin_timestamp));
			
			return result;
		}

		public void add(long eventID) {
			eventIDs.add(eventID);
		}
		
	}
}
