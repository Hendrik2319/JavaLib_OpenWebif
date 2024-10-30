package net.schwarzbaer.java.lib.openwebif;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;
import java.util.function.Consumer;

public class EPG {

	private final Tools tools;
	//private final EventList events;
	private final HashMap<String,StationEPG> stationEPGs;
	
	public EPG(Tools tools) {
		this.tools = tools;
		//events = new EventList();
		stationEPGs = new HashMap<>();
	}
	
	public interface Tools {
		String getTimeStr(long millis);
	}
	
	public synchronized void showStatus(PrintStream out)
	{
		int eventCount = 0;
		for (StationEPG stationEPG : stationEPGs.values())
			eventCount += stationEPG.events.size();
		out.printf("EPG:%n");
		out.printf("    stations: %d%n", stationEPGs.size());
		out.printf("    events  : %d%n", eventCount);
	}

	public boolean isEmpty() {
		return stationEPGs.isEmpty();
	}

	public Vector<EPGevent> readEPGNowForBouquet(String baseURL, Bouquet bouquet, Consumer<String> setIndeterminateProgressTask) {
		return readEPGSimpleForBouquet(baseURL, bouquet, API.API_EPGNOW, "readEPGNowForBouquet", setIndeterminateProgressTask);
	}

	public Vector<EPGevent> readEPGNowNextForBouquet(String baseURL, Bouquet bouquet, Consumer<String> setIndeterminateProgressTask) {
		return readEPGSimpleForBouquet(baseURL, bouquet, API.API_EPGNOWNEXT, "readEPGNowNextForBouquet", setIndeterminateProgressTask);
	}

	private Vector<EPGevent> readEPGSimpleForBouquet(String baseURL, Bouquet bouquet, String apiCommandPath, String commandLabel, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		String url = String.format("%s%s?bRef=%s", baseURL, apiCommandPath, OpenWebifTools.encodeForURL(bouquet.servicereference));
		
		String baseURL_ = baseURL;
		Vector<EPGevent> events = OpenWebifTools.getContentAndParseJSON(
				url,
				err->{
					err.printf("   %s(baseURL, bouquet)%n", commandLabel);
					err.printf("      baseURL  : \"%s\"%n", baseURL_);
					err.printf("      bouquet  : %s%n", bouquet.servicereference);
					err.printf("      -> url   : \"%s\"%n", url);
				},
				result -> {
					OpenWebifTools.EPGeventListResult parseResult = new OpenWebifTools.EPGeventListResult(result, "BouquetEvents");
					if (!parseResult.result) return null;
					return parseResult.events;
				},
				setIndeterminateProgressTask
		);
		
		addAll(events);
		
		return events;
	}

	public Vector<EPGevent> readEPGforBouquet(String baseURL, Bouquet bouquet, Long beginTime_UnixTS, Long endTime_Minutes, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		String beginTimeStr = beginTime_UnixTS ==null ? "" : "&time=" +beginTime_UnixTS.toString();
		String   endTimeStr =   endTime_Minutes==null ? "" : "&endTime="+endTime_Minutes.toString();
		String url = String.format("%s%s?bRef=%s%s%s", baseURL, API.API_EPGMULTI, OpenWebifTools.encodeForURL(bouquet.servicereference), beginTimeStr, endTimeStr);
		
		String baseURL_ = baseURL;
		Vector<EPGevent> events = OpenWebifTools.getContentAndParseJSON(
				url,
				err->{
					err.printf("   readEPGforBouquet(baseURL, bouquet, [beginTime], [endTime])%n");
					err.printf("      baseURL  : \"%s\"%n", baseURL_);
					err.printf("      bouquet  : %s%n", bouquet.servicereference);
					if (beginTime_UnixTS !=null) err.printf("      beginTime: %d, %s%n"    , beginTime_UnixTS , tools.getTimeStr(beginTime_UnixTS*1000));
					if (  endTime_Minutes!=null) err.printf("      endTime  : %d minutes%n",   endTime_Minutes);
					err.printf("      -> url   : \"%s\"%n", url);
				},
				result -> {
					OpenWebifTools.EPGeventListResult parseResult = new OpenWebifTools.EPGeventListResult(result, "BouquetEvents");
					if (!parseResult.result) return null;
					return parseResult.events;
				},
				setIndeterminateProgressTask
		);
		
		addAll(events);
		
		return events;
	}

	public Vector<EPGevent> readEPGforService(String baseURL, StationID stationID, Long beginTime_UnixTS, Long endTime_UnixTS, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		String beginTimeStr = beginTime_UnixTS==null ? "" : "&time=" +beginTime_UnixTS.toString();
		String   endTimeStr =   endTime_UnixTS==null ? "" : "&endTime="+endTime_UnixTS.toString();
		String url = String.format("%s%s?sRef=%s%s%s", baseURL, API.API_EPGSERVICE, stationID.toIDStr(true), beginTimeStr, endTimeStr);
		
		String baseURL_ = baseURL;
		Vector<EPGevent> events = OpenWebifTools.getContentAndParseJSON(
				url,
				err->{
					err.printf("   getEPGforService(baseURL, stationID, [beginTime], [endTime])%n");
					err.printf("      baseURL  : \"%s\"%n", baseURL_);
					err.printf("      stationID: %s%n", stationID.toIDStr(true));
					if (beginTime_UnixTS!=null) err.printf("      beginTime: %d, %s%n", beginTime_UnixTS, tools.getTimeStr(beginTime_UnixTS*1000));
					if (  endTime_UnixTS!=null) err.printf("      endTime  : %d, %s%n",   endTime_UnixTS, tools.getTimeStr(  endTime_UnixTS*1000));
					err.printf("      -> url   : \"%s\"%n", url);
				},
				result -> {
					OpenWebifTools.EPGeventListResult parseResult = new OpenWebifTools.EPGeventListResult(result);
					if (!parseResult.result) return null;
					return parseResult.events;
				},
				setIndeterminateProgressTask
		);
		
		addAll(stationID, events);
		
		return events;
	}
	
	public Vector<EPGevent> getEvents(StationID stationID, long beginTime_UnixTS, Long endTime_UnixTS, boolean sorted) {
		StationEPG stationEPG = getStationEPG(stationID.toIDStr(true), false);
		if (stationEPG==null) return new Vector<>();
		return stationEPG.getEvents(beginTime_UnixTS, endTime_UnixTS, sorted);
	}

	private void addAll(StationID stationID, Vector<EPGevent> events) {
		StationEPG stationEPG = getStationEPG(stationID.toIDStr(true), true);
		stationEPG.add(events);
	}

	private void addAll(Vector<EPGevent> events) {
		for (EPGevent event:events) {
			if (event.id==null) continue;
			if (event.sref==null) continue;
			
			StationEPG stationEPG = getStationEPG(event.sref, true);
			stationEPG.add(event);
		}
	}

	private synchronized StationEPG getStationEPG(String sref, boolean addIfNotExists) {
		StationEPG stationEPG = stationEPGs.get(sref);
		if (stationEPG==null && addIfNotExists) stationEPGs.put(sref,stationEPG = new StationEPG(sref));
		return stationEPG;
	}
	
	//private static class EventList {
	//	private final HashMap<Long,EPGevent> events = new HashMap<>();
	//
	//	synchronized void put(Long id, EPGevent event) {
	//		events.put(id, event);
	//	}
	//
	//	synchronized EPGevent get(long id) {
	//		return events.get(id);
	//	}
	//}
	
	public static class StationEPG {

		private final String sref;
		private final HashMap<Long,EPGevent> events;

		public StationEPG(String sref) {
			this.sref = sref;
			events = new HashMap<>();
		}

		public synchronized Vector<EPGevent> getEvents(long beginTime_UnixTS, Long endTime_UnixTS, boolean sorted) {
			Vector<EPGevent> result = new Vector<>();
			for (EPGevent event:events.values()) {
				if (event.begin_timestamp==null) continue;
				long eventBegin = event.begin_timestamp;
				long eventDuration = event.duration_sec==null ? 0 : event.duration_sec;
				if ( eventBegin+eventDuration < beginTime_UnixTS || (endTime_UnixTS!=null && endTime_UnixTS.longValue() < eventBegin) )
					continue;
				
				result.add(event);
			}
			
			if (sorted)
				result.sort(Comparator.comparingLong(event->event.begin_timestamp));
			
			return result;
		}

		public synchronized void add(Vector<EPGevent> events) {
			for (EPGevent oldEvent:this.events.values())
				oldEvent.isUpToDate = false;
			
			for (EPGevent event:events)
				add(event);
		}

		public synchronized void add(EPGevent event)
		{
			if (!sref.equals(event.sref)) {
				System.out.printf("Found Wrong EPGevent: Station.SRef=\"%s\", EPGevent.SRef=\"%s\"%n", sref, event.sref);
				return;
			}
			if (event.id==null) {
				System.out.printf("Found Wrong EPGevent: EPGevent.ID = <null>%n");
				return;
			}
			this.events.put(event.id, new EPGevent(event));
		}
		
	}
}
