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
	
	private static final boolean SHOW_PARSE_PROGRESS = false;

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
	
	public enum MessageType {
		YESNO(0), INFO(1), WARNING(2), ERROR(3);
		private int value;
		MessageType(int value) { this.value = value; }
	}
	
	public static MessageResponse sendMessage(String baseURL, String message, MessageType type, Integer timeOut_sec, Consumer<String> setIndeterminateProgressTask) {
		baseURL = removeAllTrailingSlashes(baseURL);
		String encodedMessage = encodeForURL(message,"preparing message");
		
		// http://et7x00/api/message?text=text&type=1&timeout=15
		String url = String.format("%s/api/message?text=%s&type=%d", baseURL, encodedMessage, type.value);
		if (timeOut_sec!=null) url += String.format("&timeout=%d", timeOut_sec.intValue());
		
		String baseURLStr = baseURL;
		return getContentAndParseIt(url, err->{
				err.printf("   sendMessage(baseURL, message, type, timeOut)%n");
				err.printf("      baseURL: \"%s\"%n", baseURLStr);
				err.printf("      message: \"%s\" -> \"%s\"%n", message, encodedMessage);
				err.printf("      type   : %s[%d]%n", type, type.value);
				if (timeOut_sec!=null)
					err.printf("      timeOut: %d%n", timeOut_sec);
			},
			MessageResponse::new,
			setIndeterminateProgressTask
		);
	}
	
	public static MessageResponse getMessageAnswer(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		baseURL = removeAllTrailingSlashes(baseURL);
		
		// http://et7x00/api/messageanswer
		String url = String.format("%s/api/messageanswer", baseURL);
		
		String baseURLStr = baseURL;
		return getContentAndParseIt(url, err->{
				err.printf("   getMessageAnswer(baseURL)%n");
				err.printf("      baseURL: \"%s\"%n", baseURLStr);
			},
			MessageResponse::new,
			setIndeterminateProgressTask
		);
	}

	public static MessageResponse zapToStation(String baseURL, StationID stationID) {
		String url = getStationZapURL(baseURL, stationID);
		return getContentForStationAndParseIt(url, "zapToStation", baseURL, stationID, MessageResponse::new, null);
	}
	
	public static class MessageResponse {
		public final String message;
		public final boolean result;
		MessageResponse(JSON_Data.Value<NV,V> response) throws TraverseException {
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
		String movieURL = encodeForURL(movie.filename, "creating movie URL");
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
			
			
			try {
				stationInfo     = new StationInfo(JSON_Data.getObjectValue(object,"info", debugOutputPrefixStr),debugOutputPrefixStr+".info"); if (SHOW_PARSE_PROGRESS) System.out.println("CurrentStation: info");
				currentEPGevent = new EPGevent   (JSON_Data.getObjectValue(object,"now" , debugOutputPrefixStr),debugOutputPrefixStr+".now" ); if (SHOW_PARSE_PROGRESS) System.out.println("CurrentStation: now" );
				nextEPGevent    = new EPGevent   (JSON_Data.getObjectValue(object,"next", debugOutputPrefixStr),debugOutputPrefixStr+".next"); if (SHOW_PARSE_PROGRESS) System.out.println("CurrentStation: next");
			} catch (Exception e) {
				System.err.printf("Exception while parse CurrentStation: [%s] %s%n", e, e.getMessage());
				throw e;
			}
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
		public final Long txtpid;
		public final String txtpidStr;
		public final long pmtpid;
		public final long tsid;
		public final long pcrpid;
		public final long sid;
		public final Long namespace;
		public final String namespaceStr;
		public final long apid;
		public final long vpid;
		public final boolean result;
		public final StationID stationID;
		
		public StationInfo(JSON_Object<NV, V> object, String debugOutputPrefixStr) throws TraverseException {
			bouquetName  = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "bqname"                , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: bqname"      ); // "bqname"      : "",    "Sender (DVB-S2)",
			bouquetRef   = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "bqref"                 , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: bqref"       ); // "bqref"       : "",    "1:7:1:0:0:0:0:0:0:0:FROM BOUQUET %22userbouquet.sender_dvb_s2.tv%22 ORDER BY bouquet",
			serviceName  = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "name"                  , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: name"        ); // "name"        : "",    "WELT",
			serviceRef   = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "ref"                   , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: ref"         ); // "ref"         : "",    "1:0:1:445F:453:1:C00000:0:0:0:",
			provider     = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "provider"              , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: provider"    ); // "provider"    : "",    "Sonstige Astra",
			width        =                               JSON_Data.getIntegerValue(object, "width"                 , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: width"       ); // "width"       : 0,     720,
			height       =                               JSON_Data.getIntegerValue(object, "height"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: height"      ); // "height"      : 0,     576,
			aspect       =                               JSON_Data.getIntegerValue(object, "aspect"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: aspect"      ); // "aspect"      : 0,     3,
			isWideScreen =                               JSON_Data.getBoolValue   (object, "iswidescreen"          , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: iswidescreen"); // "iswidescreen": false  true
			onid         =                               JSON_Data.getIntegerValue(object, "onid"                  , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: onid"        ); // "onid"        : 0,     1,
			txtpid       =                               JSON_Data.getIntegerValue(object, "txtpid"   , false, true, debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: txtpid I"    ); // "txtpid"      :        35,
			txtpidStr    =                               JSON_Data.getStringValue (object, "txtpid"   , false, true, debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: txtpid S"    ); // "txtpid"      : "N/A",
			pmtpid       =                               JSON_Data.getIntegerValue(object, "pmtpid"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: pmtpid"      ); // "pmtpid"      : 0,     99,
			tsid         =                               JSON_Data.getIntegerValue(object, "tsid"                  , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: tsid"        ); // "tsid"        : 0,     1107,
			pcrpid       =                               JSON_Data.getIntegerValue(object, "pcrpid"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: pcrpid"      ); // "pcrpid"      : 0,     1023,
			sid          =                               JSON_Data.getIntegerValue(object, "sid"                   , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: sid"         ); // "sid"         : 0,     17503,
			namespace    =                               JSON_Data.getIntegerValue(object, "namespace", false, true, debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: namespace I" ); // "namespace"   :        12582912,
			namespaceStr =                               JSON_Data.getStringValue (object, "namespace", false, true, debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: namespace S" ); // "namespace"   : "",
			apid         =                               JSON_Data.getIntegerValue(object, "apid"                  , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: apid"        ); // "apid"        : 0,     1024,
			vpid         =                               JSON_Data.getIntegerValue(object, "vpid"                  , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: vpid"        ); // "vpid"        : 0,     1023,
			result       =                               JSON_Data.getBoolValue   (object, "result"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: result"      ); // "result"      : false, true,
			
			stationID = serviceRef==null || serviceRef.isEmpty() ? null : StationID.parseIDStr( removeTrailingStr(serviceRef, ":") );
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
			System.err.printf("TraverseException while parsing JSON structure: %s%n", e.getMessage());
			writeTaskInfoOnError.accept(System.err);
			return null;
		} catch (Exception e) {
			System.err.printf("Exception while parsing JSON structure: %s%n", e.getMessage());
			writeTaskInfoOnError.accept(System.err);
			e.printStackTrace();
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

	static String encodeForURL(String message, String taskLabel) {
		try {
			return URLEncoder.encode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.err.printf("UnsupportedEncodingException while %s: %s%n", taskLabel, e.getMessage());
		}
		return message;
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
