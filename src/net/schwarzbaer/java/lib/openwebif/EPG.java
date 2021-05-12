package net.schwarzbaer.java.lib.openwebif;

import java.util.Vector;
import java.util.function.Consumer;

public class EPG {

	private final Tools tools;
	
	public EPG(Tools tools) {
		this.tools = tools;
	}
	
	public interface Tools {
		String getTimeStr(long millis);
	}
	
	public void getEPGforService(String baseURL, StationID stationID, Long beginTime_UnixTS, Long endTime_UnixTS, Consumer<String> setIndeterminateProgressTask) {
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

	private void addAll(Vector<EPGevent> events) {
		// TODO: store events
	}
	
}
