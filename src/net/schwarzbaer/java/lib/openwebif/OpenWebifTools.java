package net.schwarzbaer.java.lib.openwebif;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
	
	public enum ScreenFormat { JPG_Default, PNG }
	public enum ScreenShotType { TVnOSD, TVonly, OSDonly }
	public enum ScreenShotResolution { R720, HighRes }
	
	public static BufferedImage getScreenShot(String baseURL, ScreenShotType type, ScreenShotResolution res) {
		return getScreenShot(baseURL, ScreenFormat.JPG_Default, type, res);
	}
	public static BufferedImage getScreenShot(String baseURL, ScreenFormat format, ScreenShotType type, ScreenShotResolution res) {
		if (format==null || type==null || res==null) return null;
		if (baseURL==null) return null;
		baseURL = removeAllTrailingSlashes(baseURL);
		
		// http://192.168.2.75/grab?format=jpg&r=720&mode=all
		// http://192.168.2.75/grab?format=jpg&r=720&mode=all  &t=1625500839991
		// http://192.168.2.75/grab?format=jpg      &mode=all  &t=1625500857642
		// http://192.168.2.75/grab?format=jpg      &mode=video&t=1625500876819
		// http://192.168.2.75/grab?format=jpg      &mode=osd  &t=1625500891870
		String formatStr = "";
		switch (format) {
		case JPG_Default: formatStr = "format=jpg"; break;
		case PNG        : formatStr = "format=png"; break;
		}
		
		String resStr = "";
		switch (res) {
		case R720   : resStr = "&r=720"; break;
		case HighRes: resStr = ""      ; break;
		}
		
		String typeStr = "";
		switch (type) {
		case TVnOSD : typeStr = "&mode=all"  ; break;
		case TVonly : typeStr = "&mode=video"; break;
		case OSDonly: typeStr = "&mode=osd"  ; break;
		}
		
		return getImage(String.format("%s/grab?%s%s%s", baseURL, formatStr, resStr, typeStr));
	}
	
	public static BufferedImage getPicon(String baseURL, StationID stationID) {
		if (baseURL==null || stationID==null) return null;
		baseURL = removeAllTrailingSlashes(baseURL);
		
		// http://192.168.2.75/picon/1_0_19_2B66_3F3_1_C00000_0_0_0.png
		return getImage(String.format("%s/picon/%s", baseURL, stationID.toPiconImageFileName()));
	}
	
	static BufferedImage getImage(String urlStr) {
		URL url;
		try {
			url = new URI(urlStr).toURL();
		} catch (MalformedURLException ex) {
			System.err.printf("MalformedURLException in URL(\"%s\"): %s%n", urlStr, ex.getMessage());
			return null;
		} catch (URISyntaxException ex) {
			System.err.printf("URISyntaxException in URL(\"%s\"): %s%n", urlStr, ex.getMessage());
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
		String encodedMessage = encodeForURL(message);
		
		// http://et7x00/api/message?text=text&type=1&timeout=15
		String url = String.format("%s%s?text=%s&type=%d", baseURL, API.API_MESSAGE, encodedMessage, type.value);
		if (timeOut_sec!=null) url += String.format("&timeout=%d", timeOut_sec.intValue());
		
		String baseURLStr = baseURL;
		String url_ = url;
		return getContentAndParseJSON(url, err->{
				err.printf("   sendMessage(baseURL, message, type, timeOut)%n");
				err.printf("      baseURL: \"%s\"%n", baseURLStr);
				err.printf("      message: \"%s\" -> \"%s\"%n", message, encodedMessage);
				err.printf("      type   : %s[%d]%n", type, type.value);
				if (timeOut_sec!=null)
					err.printf("      timeOut: %d%n", timeOut_sec);
				err.printf("      -> url : \"%s\"%n", url_);
			},
			MessageResponse::new,
			setIndeterminateProgressTask
		);
	}
	
	public static MessageResponse getMessageAnswer(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		baseURL = removeAllTrailingSlashes(baseURL);
		
		// http://et7x00/api/messageanswer
		String url = baseURL+API.API_MESSAGEANSWER;
		
		String baseURLStr = baseURL;
		return getContentAndParseJSON(url, err->{
				err.printf("   getMessageAnswer(baseURL)%n");
				err.printf("      baseURL: \"%s\"%n", baseURLStr);
				err.printf("      -> url : \"%s\"%n", url);
			},
			MessageResponse::new,
			setIndeterminateProgressTask
		);
	}

	public static MessageResponse zapToStation(String baseURL, StationID stationID) {
		String url = getStationZapURL(baseURL, stationID);
		return getContentForStationAndParseIt(url, "zapToStation", baseURL, stationID, MessageResponse::new, null);
	}

	public static MessageResponse zapToMovie(String baseURL, MovieList.Movie movie, Consumer<String> setIndeterminateProgressTask) {
		setIndeterminateProgressTask.accept("Build URL");
		String url = getMovieZapURL(baseURL, movie);
		return getContentAndParseJSON(url, err->{
			err.printf("   zapToMovie(baseURL, serviceref)%n");
			err.printf("      baseURL   : \"%s\"%n", baseURL);
			err.printf("      serviceref: %s%n", movie.serviceref);
			err.printf("      -> url    : \"%s\"%n", url);
		}, MessageResponse::new, setIndeterminateProgressTask);
	}

	public static MessageResponse deleteMovie(String baseURL, MovieList.Movie movie, Consumer<String> setIndeterminateProgressTask) {
		setIndeterminateProgressTask.accept("Build URL");
		String url = getMovieDeleteURL(baseURL, movie);
		return getContentAndParseJSON(url, err->{
			err.printf("   deleteMovie(baseURL, serviceref)%n");
			err.printf("      baseURL   : \"%s\"%n", baseURL);
			err.printf("      serviceref: %s%n", movie.serviceref);
			err.printf("      -> url    : \"%s\"%n", url);
		}, MessageResponse::new, setIndeterminateProgressTask);
	}
	
	public static class MessageResponse {
		public final String message;
		public final boolean result;
		MessageResponse(JSON_Data.Value<NV,V> response) throws TraverseException {
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(response, "Response");
			message = JSON_Data.decodeUnicode( JSON_Data.getStringValue(object, "message", "Response") );
			result  =                          JSON_Data.getBoolValue  (object, "result" , "Response");
		}
		public void printTo(PrintStream out) { printTo(out,""); }
		public void printTo(PrintStream out, String indent) {
			out.printf("%sResponse:%n", indent);
			out.printf("%s   Result: %s%n", indent, result);
			out.printf("%s   Message: \"%s\"%n", indent, message);
		}
	}
	
	public static String getStationZapURL(String baseURL, StationID stationID) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://et7x00/api/zap?sRef=1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s%s?sRef=%s", baseURL, API.API_ZAP, stationID.toIDStr(true));
	}
	
	public static String getStationStreamURL(String baseURL, StationID stationID) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://192.168.2.75:8001/1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s:8001/%s", baseURL, stationID.toIDStr(true));
	}
	
	public static String getMovieDeleteURL(String baseURL, MovieList.Movie movie) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://et7x00/api/moviedelete?sRef=...
		String encodedServiceRef = encodeForURL(movie.serviceref);
		return String.format("%s%s?sRef=%s", baseURL, API.API_MOVIEDELETE, encodedServiceRef);
	}
	
	public static String getMovieZapURL(String baseURL, MovieList.Movie movie) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://et7x00/api/zap?sRef=1%3A0%3A0%3A0%3A0%3A0%3A0%3A0%3A0%3A0%3A/media/hdd/movie-storage/_unsortiert/20230626%202342%20-%20zdf_neo%20HD%20-%20B%C3%B6hmi%20brutzelt%20mit%20Klaas%20Heufer-Umlauf.ts
		String encodedServiceRef = encodeForURL(movie.serviceref);
		return String.format("%s%s?sRef=%s", baseURL, API.API_ZAP, encodedServiceRef);
	}

	public static String getMovieURL(String baseURL, MovieList.Movie movie) {
		return getFileURL(baseURL, movie.filename);
	}
	
	public static String getFileURL(String baseURL, String filepath) {
		baseURL = removeAllTrailingSlashes(baseURL);
		String encodedfilepath = encodeForURL(filepath);
		return String.format("%s/file?file=%s", baseURL, encodedfilepath);
	}
	
	public static String getIsServicePlayableURL(String baseURL, StationID stationID) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://et7x00/api/serviceplayable?sRef=1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s%s?sRef=%s", baseURL, API.API_SERVICEPLAYABLE, stationID.toIDStr(true));
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
			sRef       = JSON_Data.decodeUnicode( JSON_Data.getStringValue(service, "servicereference", debugOutputPrefixStr) );
			isplayable =                          JSON_Data.getBoolValue  (service, "isplayable"      , debugOutputPrefixStr);
		}
		
	}

	public static String getCurrentEPGeventURL(String baseURL, StationID stationID) {
		baseURL = removeAllTrailingSlashes(baseURL);
		// http://et7x00/api/epgservicenow?sRef=1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s%s?sRef=%s", baseURL, API.API_EPGSERVICENOW, stationID.toIDStr(true));
	}

	public static Vector<EPGevent> getCurrentEPGevent(String baseURL, StationID stationID, Consumer<String> setIndeterminateProgressTask) {
		String url = getCurrentEPGeventURL(baseURL, stationID);
		return getContentForStationAndParseIt(url, "getCurrentEPGevent", baseURL, stationID, result->{
			EPGeventListResult parseResult = new EPGeventListResult(result);
			if (!parseResult.result) return null;
			return parseResult.events;
		}, setIndeterminateProgressTask);
	}
	
	public static class EPGeventListResult {
	
		public final boolean result;
		public final Vector<EPGevent> events;
	
		public EPGeventListResult(Value<NV, V> value) throws TraverseException { this(value,null); }
		public EPGeventListResult(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
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
			
			events = new Vector<>();
			for (int i=0; i<eventsRaw.size(); i++)
				events.add(EPGevent.parse(eventsRaw.get(i), debugOutputPrefixStr+"["+i+"]"));
		}
	}

	public static CurrentStation getCurrentStation(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		baseURL = removeAllTrailingSlashes(baseURL);
		String url = baseURL+API.API_GETCURRENT;
		
		return getContentAndParseJSON(url, err->{
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
	
	public record OptionalValue(Long value, String str) {
		public static OptionalValue parse(JSON_Object<NV, V> object, String field, String debugOutputPrefixStr) throws TraverseException {
			Long   value_ = JSON_Data.getIntegerValue(object, field, false, true, debugOutputPrefixStr);
			String str_   = JSON_Data.getStringValue (object, field, false, true, debugOutputPrefixStr);
			return new OptionalValue(value_, str_);
		}
	}
	
	public static class StationInfo {
		public final String        bouquetName;
		public final String        bouquetRef;
		public final String        serviceName;
		public final String        serviceRef;
		public final String        provider;
		public final OptionalValue width;
		public final OptionalValue height;
		public final OptionalValue aspect;
		public final boolean       isWideScreen;
		public final OptionalValue onid;
		public final OptionalValue txtpid;
		public final OptionalValue pmtpid;
		public final OptionalValue tsid;
		public final OptionalValue pcrpid;
		public final long          sid;
		public final OptionalValue namespace;
		public final OptionalValue apid;
		public final OptionalValue vpid;
		public final boolean       result;
		public final StationID     stationID;
		
		public StationInfo(JSON_Object<NV, V> object, String debugOutputPrefixStr) throws TraverseException {
			bouquetName  = JSON_Data.decodeUnicode( JSON_Data.getStringValue (object, "bqname"                , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: bqname"      ); // "bqname"      : "",    "Sender (DVB-S2)",
			bouquetRef   = JSON_Data.decodeUnicode( JSON_Data.getStringValue (object, "bqref"                 , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: bqref"       ); // "bqref"       : "",    "1:7:1:0:0:0:0:0:0:0:FROM BOUQUET %22userbouquet.sender_dvb_s2.tv%22 ORDER BY bouquet",
			serviceName  = JSON_Data.decodeUnicode( JSON_Data.getStringValue (object, "name"                  , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: name"        ); // "name"        : "",    "WELT",
			serviceRef   = JSON_Data.decodeUnicode( JSON_Data.getStringValue (object, "ref"                   , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: ref"         ); // "ref"         : "",    "1:0:1:445F:453:1:C00000:0:0:0:",
			provider     = JSON_Data.decodeUnicode( JSON_Data.getStringValue (object, "provider"              , debugOutputPrefixStr) );  if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: provider"    ); // "provider"    : "",    "Sonstige Astra",
			width        =                                OptionalValue.parse(object, "width"                 , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: width"       ); // "width"       : "N/A", 0,     720,
			height       =                                OptionalValue.parse(object, "height"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: height"      ); // "height"      : "N/A", 0,     576,
			aspect       =                                OptionalValue.parse(object, "aspect"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: aspect"      ); // "aspect"      : "N/A", 0,     3,
			isWideScreen =                          JSON_Data.getBoolValue   (object, "iswidescreen"          , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: iswidescreen"); // "iswidescreen": false, true,
			onid         =                                OptionalValue.parse(object, "onid"                  , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: onid"        ); // "onid"        : "N/A", 0,     1,
			txtpid       =                                OptionalValue.parse(object, "txtpid"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: txtpid"      ); // "txtpid"      : "N/A", 35,
			pmtpid       =                                OptionalValue.parse(object, "pmtpid"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: pmtpid"      ); // "pmtpid"      : "N/A", 0,     99,
			tsid         =                                OptionalValue.parse(object, "tsid"                  , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: tsid"        ); // "tsid"        : "N/A", 0,     1107,
			pcrpid       =                                OptionalValue.parse(object, "pcrpid"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: pcrpid"      ); // "pcrpid"      : "N/A", 0,     1023,
			sid          =                          JSON_Data.getIntegerValue(object, "sid"                   , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: sid"         ); // "sid"         : 0,     17503,
			namespace    =                                OptionalValue.parse(object, "namespace"             , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: namespace"   ); // "namespace"   : "",    12582912,
			apid         =                                OptionalValue.parse(object, "apid"                  , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: apid"        ); // "apid"        : "N/A", 0,     1024,
			vpid         =                                OptionalValue.parse(object, "vpid"                  , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: vpid"        ); // "vpid"        : "N/A", 0,     1023,
			result       =                          JSON_Data.getBoolValue   (object, "result"                , debugOutputPrefixStr);    if (SHOW_PARSE_PROGRESS) System.out.println("StationInfo: result"      ); // "result"      : false, true,
			
			stationID = serviceRef==null || serviceRef.isEmpty() ? null : StationID.parseIDStr( serviceRef );
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
		String url = baseURL+API.API_GETALLSERVICES;
		
		getContentAndParseJSON(url, err->{
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

	public static MovieList readMovieList(String baseURL, String dir, Consumer<String> setIndeterminateProgressTask) {
		setIndeterminateProgressTask.accept("Build URL");
		String url;
		if (dir!=null)
			url = String.format("%s%s?dirname=%s", baseURL, API.API_MOVIELIST, encodeForURL(dir));
		else
			url = String.format("%s%s"           , baseURL, API.API_MOVIELIST);
		
		System.out.printf("get MovieList: \"%s\"%n", url);
		
		return getContentAndParseJSON(url, err->{
			err.printf("   readMovieList(baseURL, dir)%n");
			err.printf("      baseURL: \"%s\"%n", baseURL);
			err.printf("      dir    : \"%s\"%n", dir);
			err.printf("      -> url : \"%s\"%n", url);
		}, MovieList::new, setIndeterminateProgressTask);
	}

	interface ParseJSON<ResultType> {
		ResultType parseIt(Value<NV,V> result) throws TraverseException;
	}

	static <ResultType> ResultType getContentForStationAndParseIt(String url, String commandLabel, String baseURL, StationID stationID, ParseJSON<ResultType> parseIt, Consumer<String> setIndeterminateProgressTask) {
		return getContentAndParseJSON(url, err->{
			err.printf("   %s(baseURL, stationID)%n", commandLabel);
			err.printf("      baseURL  : \"%s\"%n", baseURL);
			err.printf("      stationID: %s%n", stationID.toIDStr(true));
			err.printf("      -> url   : \"%s\"%n", url);
		}, parseIt, setIndeterminateProgressTask);
	}

	static <ResultType> ResultType getContentAndParseJSON(String url, Consumer<PrintStream> writeTaskInfoOnError, ParseJSON<ResultType> parseIt, Consumer<String> setIndeterminateProgressTask) {
		if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Get Content from URL");
		String content = getContent(url);
		if (content==null) return null;
		
		if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Parse JSON Code");
		Value<NV,V> result;
		try {
			result = JSON_Parser.<NV,V>parse_withParseException(content,null,null);
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

	static String encodeForURL(String message)
	{
		return URLEncoder.encode(message, StandardCharsets.UTF_8);
	}

	static String getContent(String urlStr) {
		URL url;
		try { url = new URI(urlStr).toURL(); }
		catch (MalformedURLException e) { System.err.printf("MalformedURL: %s%n", e.getMessage()); return null; }
		catch (URISyntaxException    e) { System.err.printf("WrongURISyntax: %s%n", urlStr, e.getMessage()); return null; }
		
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
	
	public static String getContent(File file, Charset cs) {
		try {
			return String.join("\r\n", Files.readAllLines(file.toPath(), cs));
		} catch (IOException e) {
			System.err.printf("IOException while reading file \"%s\": %s%n", file.getAbsolutePath(), e.getMessage());
		}
		return null;
	}

}
