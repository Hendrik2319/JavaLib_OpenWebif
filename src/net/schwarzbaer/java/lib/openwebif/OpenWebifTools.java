package net.schwarzbaer.java.lib.openwebif;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Vector;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Array;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Value;
import net.schwarzbaer.java.lib.jsonparser.JSON_Parser;
import net.schwarzbaer.java.lib.jsonparser.JSON_Parser.ParseException;

public class OpenWebifTools {

	static class NV extends JSON_Data.NamedValueExtra.Dummy{}

	static class V extends JSON_Data.ValueExtra.Dummy{}

	public static BufferedImage getPicon(String baseURL, StationID stationID) {
		if (baseURL==null || stationID==null) return null;
		baseURL = removeAllTrailingSlashes(baseURL);
		
		// http://192.168.2.75/picon/1_0_19_2B66_3F3_1_C00000_0_0_0.png
		String urlStr = String.format("%s/picon/%s", baseURL, stationID.toPiconImageFileName());
		URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException ex) {
			System.err.printf("MalformedURLException in URL(\"%s\"): %s%n", urlStr, ex.getMessage());
			return null;
		}
		
		try {
			return ImageIO.read(url);
		} catch (IOException ex) {
			String msg = ex.getMessage();
			if (!msg.equals("Can't get input stream from URL!"))
				System.err.printf("IOException while ImageIO.read(\"%s\"): %s%n", urlStr, msg);
			return null;
		}
	}
	
	public static ResponseMessage zapToStation(String baseURL, StationID stationID) {
		String url = getStationZapURL(baseURL, stationID);
		return getContentForStationAndParseIt(url, "zapToStation", baseURL, stationID, ResponseMessage::new, null);
	}
	
	public static class ResponseMessage {
		public final String message;
		public final boolean result;
		ResponseMessage(JSON_Data.Value<NV,V> response) throws TraverseException {
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(response, "Response");
			message = JSON_Data.getStringValue(object, "message", "Response");
			result  = JSON_Data.getBoolValue  (object, "result" , "Response");
		}
		public void printTo(PrintStream out) {
			out.println("Response:");
			out.printf ("   Result: %s%n", result);
			out.printf ("   Message: \"%s\"%n", message);
		}
	}
	
	public static String getStationZapURL(String baseURL, StationID stationID) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://et7x00/api/zap?sRef=1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s/api/zap?sRef=%s", baseURL, stationID.toIDStr(true));
	}
	
	public static String getStationStreamURL(String baseURL, StationID stationID) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://192.168.2.75:8001/1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s:8001/%s", baseURL, stationID.toIDStr(true));
	}

	public static String getMovieURL(String baseURL, MovieList.Movie movie) {
		baseURL = removeAllTrailingSlashes(baseURL);
		String movieURL = null;
		try { movieURL = URLEncoder.encode(movie.filename, "UTF-8");
		} catch (UnsupportedEncodingException e) { System.err.printf("Exception while creating movie URL: [UnsupportedEncodingException] %s%n", e.getMessage()); }
		
		return String.format("%s/file?file=%s", baseURL, movieURL);
	}
	
	public static String getIsServicePlayableURL(String baseURL, StationID stationID) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://et7x00/api/serviceplayable?sRef=1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s/api/serviceplayable?sRef=%s", baseURL, stationID.toIDStr(true));
	}
	
	public static Boolean getIsServicePlayable(String baseURL, StationID stationID, Consumer<String> setIndeterminateProgressTask) {
		String url = getIsServicePlayableURL(baseURL, stationID);
		return getContentForStationAndParseIt(url, "getIsServicePlayable", baseURL, stationID, result->{
			ServicePlayableResult parseResult = new ServicePlayableResult(result);
			if (!parseResult.result) return null;
			if (!parseResult.sRef.equals(stationID.toIDStr(true))) return null;
			return parseResult.isplayable;
		}, setIndeterminateProgressTask);
	}
	
	public static class ServicePlayableResult {
	
		public final boolean result;
		public final String sRef;
		public final boolean isplayable;
	
		public ServicePlayableResult(Value<NV, V> value) throws TraverseException { this(value,null); }
		public ServicePlayableResult(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "ServicePlayableResult";
			
			JSON_Object<NV, V> service;
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			result  = JSON_Data.getBoolValue  (object, "result" , debugOutputPrefixStr);
			service = JSON_Data.getObjectValue(object, "service", debugOutputPrefixStr);
			
			debugOutputPrefixStr += ".service";
			sRef       = decodeUnicode( JSON_Data.getStringValue(service, "servicereference", debugOutputPrefixStr) );
			isplayable =                JSON_Data.getBoolValue  (service, "isplayable"      , debugOutputPrefixStr);
		}
		
	}

	public static String getCurrentEPGeventURL(String baseURL, StationID stationID) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://et7x00/api/epgservicenow?sRef=1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s/api/epgservicenow?sRef=%s", baseURL, stationID.toIDStr(true));
	}

	public static Vector<EPGevent> getCurrentEPGevent(String baseURL, StationID stationID, Consumer<String> setIndeterminateProgressTask) {
		String url = getCurrentEPGeventURL(baseURL, stationID);
		return getContentForStationAndParseIt(url, "getCurrentEPGevent", baseURL, stationID, result->{
			CurrentEPGeventResult parseResult = new CurrentEPGeventResult(result);
			if (!parseResult.result) return null;
			return parseResult.events;
		}, setIndeterminateProgressTask);
	}
	
	private static class CurrentEPGeventResult {
	
		public final boolean result;
		public final Vector<EPGevent> events;
	
		public CurrentEPGeventResult(Value<NV, V> value) throws TraverseException { this(value,null); }
		public CurrentEPGeventResult(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "CurrentEPGevent";
			
			//OptionalValues<NV, V> optVal = new JSON_Helper.OptionalValues<NV,V>();
			//optVal.scan(value, "CurrentEPGevent");
			//optVal.show(System.err);
			//result = false;
			//events = null;
			
			JSON_Array<NV, V> eventsRaw;
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			result    = JSON_Data.getBoolValue (object, "result", debugOutputPrefixStr);
			eventsRaw = JSON_Data.getArrayValue(object, "events", debugOutputPrefixStr);
			
			events = new Vector<EPGevent>();
			for (int i=0; i<eventsRaw.size(); i++)
				events.add(EPGevent.parse(eventsRaw.get(i), debugOutputPrefixStr+"["+i+"]"));
		}
	}

	public static CurrentStation getCurrentStation(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		baseURL = removeAllTrailingSlashes(baseURL);
		String url = baseURL+"/api/getcurrent";
		
		return getContentAndParseIt(url, err->{
			err.printf("   getCurrentStation(url)%n");
			err.printf("      url: \"%s\"%n", url);
		}, CurrentStation::new, setIndeterminateProgressTask);
	}
	
	public static class CurrentStation {

		public final StationInfo stationInfo;
		public final EPGevent currentEPGevent;
		public final EPGevent nextEPGevent;
		
		public CurrentStation(Value<NV, V> value) throws TraverseException { this(value,null); }
		public CurrentStation(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "CurrentStation";
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			stationInfo     = new StationInfo(JSON_Data.getObjectValue(object,"info", debugOutputPrefixStr),debugOutputPrefixStr+".info");
			currentEPGevent = new EPGevent   (JSON_Data.getObjectValue(object,"now" , debugOutputPrefixStr),debugOutputPrefixStr+".now" );
			nextEPGevent    = new EPGevent   (JSON_Data.getObjectValue(object,"next", debugOutputPrefixStr),debugOutputPrefixStr+".next");
		}
	}
	
	public static class StationInfo {
		public final String bouquetName;
		public final String bouquetRef;
		public final String serviceName;
		public final String serviceRef;
		public final String provider;
		public final long width;
		public final long height;
		public final long aspect;
		public final boolean isWideScreen;
		public final long onid;
		public final long txtpid;
		public final long pmtpid;
		public final long tsid;
		public final long pcrpid;
		public final long sid;
		public final long namespace;
		public final long apid;
		public final long vpid;
		public final boolean result;
		public final StationID stationID;
		
		public StationInfo(JSON_Object<NV, V> object, String debugOutputPrefixStr) throws TraverseException {
			bouquetName  = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "bqname"      , debugOutputPrefixStr) ); // "bqname"      : "Sender (DVB-S2)",
			bouquetRef   = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "bqref"       , debugOutputPrefixStr) ); // "bqref"       : "1:7:1:0:0:0:0:0:0:0:FROM BOUQUET %22userbouquet.sender_dvb_s2.tv%22 ORDER BY bouquet",
			serviceName  = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "name"        , debugOutputPrefixStr) ); // "name"        : "WELT",
			serviceRef   = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "ref"         , debugOutputPrefixStr) ); // "ref"         : "1:0:1:445F:453:1:C00000:0:0:0:",
			provider     = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "provider"    , debugOutputPrefixStr) ); // "provider"    : "Sonstige Astra",
			width        =                               JSON_Data.getIntegerValue(object, "width"       , debugOutputPrefixStr);   // "width"       : 720,
			height       =                               JSON_Data.getIntegerValue(object, "height"      , debugOutputPrefixStr);   // "height"      : 576,
			aspect       =                               JSON_Data.getIntegerValue(object, "aspect"      , debugOutputPrefixStr);   // "aspect"      : 3,
			isWideScreen =                               JSON_Data.getBoolValue   (object, "iswidescreen", debugOutputPrefixStr);   // "iswidescreen": true
			onid         =                               JSON_Data.getIntegerValue(object, "onid"        , debugOutputPrefixStr);   // "onid"        : 1,
			txtpid       =                               JSON_Data.getIntegerValue(object, "txtpid"      , debugOutputPrefixStr);   // "txtpid"      : 35,
			pmtpid       =                               JSON_Data.getIntegerValue(object, "pmtpid"      , debugOutputPrefixStr);   // "pmtpid"      : 99,
			tsid         =                               JSON_Data.getIntegerValue(object, "tsid"        , debugOutputPrefixStr);   // "tsid"        : 1107,
			pcrpid       =                               JSON_Data.getIntegerValue(object, "pcrpid"      , debugOutputPrefixStr);   // "pcrpid"      : 1023,
			sid          =                               JSON_Data.getIntegerValue(object, "sid"         , debugOutputPrefixStr);   // "sid"         : 17503,
			namespace    =                               JSON_Data.getIntegerValue(object, "namespace"   , debugOutputPrefixStr);   // "namespace"   : 12582912,
			apid         =                               JSON_Data.getIntegerValue(object, "apid"        , debugOutputPrefixStr);   // "apid"        : 1024,
			vpid         =                               JSON_Data.getIntegerValue(object, "vpid"        , debugOutputPrefixStr);   // "vpid"        : 1023,
			result       =                               JSON_Data.getBoolValue   (object, "result"      , debugOutputPrefixStr);   // "result"      : true,
			
			stationID = serviceRef==null ? null : StationID.parseIDStr( removeTrailingStr(serviceRef, ":") );
		}
	}

	public interface BouquetReadInterface {
		void setIndeterminateProgressTask(String taskTitle);
		void addBouquet(Bouquet Bouquet);
		void addStationName(StationID stationID, String name);
	}
	
	public static class BouquetData {
		public final Vector<Bouquet> bouquets = new Vector<>();;
		public final HashMap<String,String> names = new HashMap<>();
	}

	public static BouquetData readBouquets(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		BouquetData bouquetData = new BouquetData();
		readBouquets(baseURL, new BouquetReadInterface() {
			@Override public void setIndeterminateProgressTask(String taskTitle) {
				setIndeterminateProgressTask.accept(taskTitle);
			}
			@Override public void addStationName(StationID stationID, String name) {
				if (stationID!=null && name!=null)
					bouquetData.names.put(stationID.toIDStr(), name);
			}
			@Override public void addBouquet(Bouquet bouquet) {
				bouquetData.bouquets.add(bouquet);
			}
		});
		return bouquetData;
	}
	
	public static void readBouquets(String baseURL, BouquetReadInterface localInterface) {
		if (baseURL==null) return;
		baseURL = removeAllTrailingSlashes(baseURL);
		String url = baseURL+"/api/getallservices";
		
		getContentAndParseIt(url, err->{
			err.printf("   readBouquets(url)%n");
			err.printf("      url: \"%s\"%n", url);
		}, parseResult -> {
			JSON_Object<NV,V> object = JSON_Data.getObjectValue(parseResult);
			if (object==null) return null;
			
			Boolean result = JSON_Data.getBoolValue(object.getValue("result"));
			if (result==null || result!=true) return null;
			
			JSON_Array<NV,V> services = JSON_Data.getArrayValue(object.getValue("services"));
			if (services==null) return null;
			
			for (JSON_Data.Value<NV,V> service:services) {
				Bouquet bouquet = Bouquet.parse(JSON_Data.getObjectValue(service),localInterface);
				if (bouquet!=null) localInterface.addBouquet(bouquet);
			}
			
			return null;
		}, localInterface::setIndeterminateProgressTask);
		
	}
	
	public interface MovieListReadInterface {
		void setIndeterminateProgressTask(String taskTitle);
	}

	public static MovieList readMovieList(String baseURL, String dir, MovieListReadInterface movieListReadInterface) {
		movieListReadInterface.setIndeterminateProgressTask("Build URL");
		String urlStr = String.format("%s/api/movielist", baseURL);
		String dir_ = dir;
		if (dir_!=null) {
			try { dir_ = URLEncoder.encode(dir_, "UTF-8");
			} catch (UnsupportedEncodingException e) { System.err.printf("Exception while converting directory name: [UnsupportedEncodingException] %s%n", e.getMessage()); }
			urlStr = String.format("%s?dirname=%s", urlStr, dir_);
		}
		System.out.printf("get MovieList: \"%s\"%n", urlStr);
		
		return getContentAndParseIt(urlStr, err->{
			err.printf("   readMovieList(baseURL, dir)%n");
			err.printf("      baseURL: \"%s\"%n", baseURL);
			err.printf("      dir    : \"%s\"%n", dir);
		}, MovieList::new, movieListReadInterface::setIndeterminateProgressTask);
	}

	interface ParseIt<ResultType> {
		ResultType parseIt(Value<NV,V> result) throws TraverseException;
	}

	static <ResultType> ResultType getContentForStationAndParseIt(String url, String commandLabel, String baseURL, StationID stationID, ParseIt<ResultType> parseIt, Consumer<String> setIndeterminateProgressTask) {
		return getContentAndParseIt(url, err->{
			err.printf("   %s(baseURL, stationID)%n", commandLabel);
			err.printf("      baseURL  : \"%s\"%n", baseURL);
			err.printf("      stationID: %s%n", stationID.toIDStr(true));
		}, parseIt, setIndeterminateProgressTask);
	}

	static <ResultType> ResultType getContentAndParseIt(String url, Consumer<PrintStream> writeTaskInfoOnError, ParseIt<ResultType> parseIt, Consumer<String> setIndeterminateProgressTask) {
		if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Get Content from URL");
		String content = getContent(url);
		if (content==null) return null;
		
		if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Parse JSON Code");
		Value<NV,V> result;
		try {
			result = new JSON_Parser<NV,V>(content,null).parse_withParseException();
		} catch (ParseException e) {
			System.err.printf("ParseException while parsing JSON code: %s%n", e.getMessage());
			writeTaskInfoOnError.accept(System.err);
			return null;
		}
		
		if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Parse JSON Structure");
		try {
			return parseIt.parseIt(result);
		} catch (TraverseException e) {
			System.err.printf("Exception while parsing JSON structure: %s%n", e.getMessage());
			writeTaskInfoOnError.accept(System.err);
			return null;
		}
	}

	static String removeAllTrailingSlashes(String baseURL) {
		return removeAllTrailingStrs(baseURL, "/");
	}

	static String removeAllTrailingStrs(String str, String suffix) {
		while (str.endsWith(suffix)) str = str.substring(0, str.length()-suffix.length());
		return str;
	}

	static String removeTrailingStr(String str, String suffix) {
		if (str.endsWith(suffix)) str = str.substring(0, str.length()-suffix.length());
		return str;
	}

	public static String decodeUnicode(String str) {
		if (str==null) return null;
		int pos;
		int startPos = 0;
		while ( (pos=str.indexOf("\\u",startPos))>=0 ) {
			if (str.length()<pos+6) break;
			String prefix = str.substring(0, pos);
			String suffix = str.substring(pos+6);
			String codeStr = str.substring(pos+2,pos+6);
			int code;
			try { code = Integer.parseUnsignedInt(codeStr,16); }
			catch (NumberFormatException e) { startPos = pos+2; continue; }
			str = prefix + ((char)code) + suffix;
		}
		return str;
	}

	static String getContent(String urlStr) {
		URL url;
		try { url = new URL(urlStr); }
		catch (MalformedURLException e) { System.err.printf("MalformedURL: %s%n", e.getMessage()); return null; }
		
		URLConnection conn;
		try { conn = url.openConnection(); }
		catch (IOException e) { System.err.printf("url.openConnection -> IOException: %s%n", e.getMessage()); return null; }
		
		conn.setConnectTimeout(5000);
		conn.setDoInput(true);
		try { conn.connect(); }
		catch (IOException e) { System.err.printf("conn.connect -> IOException: %s%n", e.getMessage()); return null; }
		
		ByteArrayOutputStream storage = new ByteArrayOutputStream();
		try (BufferedInputStream in = new BufferedInputStream( conn.getInputStream() )) {
			byte[] buffer = new byte[100000];
			int n;
			while ( (n=in.read(buffer))>=0 )
				if (n>0) storage.write(buffer, 0, n);
			
		} catch (IOException e) {
			System.err.printf("IOException: %s%n", e.getMessage());
		}
		
		return new String(storage.toByteArray());
	}

}
