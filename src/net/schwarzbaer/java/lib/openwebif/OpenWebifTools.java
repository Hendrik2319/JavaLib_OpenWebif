package net.schwarzbaer.java.lib.openwebif;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
		while (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length()-1);
		
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
		String response = getContent(url);
		JSON_Parser<NV,V> parser = new JSON_Parser<>(response,null);
		try {
			JSON_Data.Value<NV,V> result = parser.parse_withParseException();
			if (result!=null) return new ResponseMessage(result);
		} catch (ParseException e) {
			System.err.printf("ParseException: %s%n", e.getMessage());
			//e.printStackTrace();
		} catch (TraverseException e) {
			System.err.printf("TraverseException while parsing: %s%n", e.getMessage());
			//e.printStackTrace();
		}
		return null;
		
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
		while (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length()-1);
		// http://et7x00/api/zap?sRef=1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s/api/zap?sRef=%s", baseURL, stationID.toIDStr(true));
	}
	
	public static String getStationStreamURL(String baseURL, StationID stationID) {
		while (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length()-1);
		// http://192.168.2.75:8001/1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s:8001/%s", baseURL, stationID.toIDStr(true));
	}

	public static String getMovieURL(String baseURL, MovieList.Movie movie) {
		while (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length()-1);
		String movieURL = null;
		try { movieURL = URLEncoder.encode(movie.filename, "UTF-8");
		} catch (UnsupportedEncodingException e) { System.err.printf("Exception while creating movie URL: [UnsupportedEncodingException] %s%n", e.getMessage()); }
		
		return String.format("%s/file?file=%s", baseURL, movieURL);
	}
	
	public static String getIsServicePlayableURL(String baseURL, StationID stationID) {
		while (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length()-1);
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
		while (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length()-1);
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
	
	public static class CurrentEPGeventResult {
	
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
				events.add(new EPGevent(eventsRaw.get(i), debugOutputPrefixStr+"["+i+"]"));
		}
	}

	public static class EPGevent {
		public final String station_name;
		public final String title;
		public final String shortdesc;
		public final String longdesc;
		public final String genre;
		public final long genreid;
		public final long begin_timestamp;
		public final long now_timestamp;
		public final long duration_sec;
		public final long remaining;
		public final String sref;
		public final Long id;
	
		public EPGevent(Value<NV, V> value) throws TraverseException { this(value,null); }
		public EPGevent(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "EPGevent";
			
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			
			Object id_null;
			station_name    = decodeUnicode( JSON_Data.getStringValue (object, "sname"          , debugOutputPrefixStr) );
			title           = decodeUnicode( JSON_Data.getStringValue (object, "title"          , debugOutputPrefixStr) );
			shortdesc       = decodeUnicode( JSON_Data.getStringValue (object, "shortdesc"      , debugOutputPrefixStr) );
			longdesc        = decodeUnicode( JSON_Data.getStringValue (object, "longdesc"       , debugOutputPrefixStr) );
			genre           = decodeUnicode( JSON_Data.getStringValue (object, "genre"          , debugOutputPrefixStr) );
			genreid         =                JSON_Data.getIntegerValue(object, "genreid"        , debugOutputPrefixStr);
			begin_timestamp =                JSON_Data.getIntegerValue(object, "begin_timestamp", debugOutputPrefixStr);
			now_timestamp   =                JSON_Data.getIntegerValue(object, "now_timestamp"  , debugOutputPrefixStr);
			duration_sec    =                JSON_Data.getIntegerValue(object, "duration_sec"   , debugOutputPrefixStr);
			remaining       =                JSON_Data.getIntegerValue(object, "remaining"      , debugOutputPrefixStr);
			sref            =                JSON_Data.getStringValue (object, "sref"           , debugOutputPrefixStr);
			id              = JSON_Data.getValue(object, "id", false, JSON_Data.Value.Type.Integer, JSON_Data.Value::castToIntegerValue, true, debugOutputPrefixStr);
			id_null         = JSON_Data.getValue(object, "id", false, JSON_Data.Value.Type.Null   , JSON_Data.Value::castToNullValue   , true, debugOutputPrefixStr);
			if (id==null && id_null==null)
				throw new TraverseException("%s.id isn't either an IntegerValue or a NullValue", debugOutputPrefixStr);
		}
		
	}

	interface ParseIt<ResultType> {
		ResultType parseIt(Value<NV,V> result) throws TraverseException;
	}

	static <ResultType> ResultType getContentForStationAndParseIt(String url, String taskLabel, String baseURL, StationID stationID, ParseIt<ResultType> parseIt, Consumer<String> setIndeterminateProgressTask) {
		return getContentAndParseIt(url, err->{
			err.printf("   %s(baseURL, stationID)%n", taskLabel);
			err.printf("      baseURL  : \"%s\"%n", baseURL);
			err.printf("      stationID: %s%n", stationID.toIDStr(true));
		}, parseIt, setIndeterminateProgressTask);
	}

	static <ResultType> ResultType getContentAndParseIt(String url, Consumer<PrintStream> writeTaskInfoOnError, ParseIt<ResultType> parseIt, Consumer<String> setIndeterminateProgressTask) {
		if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Get Content from URL");
		String content = getContent(url);
		Value<NV,V> result;
		try {
			if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Parse JSON Code");
			result = new JSON_Parser<NV,V>(content,null).parse_withParseException();
		} catch (ParseException e) {
			System.err.printf("ParseException while parsing JSON code: %s%n", e.getMessage());
			writeTaskInfoOnError.accept(System.err);
			return null;
		}
		
		try {
			if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Parse JSON Structure");
			return parseIt.parseIt(result);
		} catch (TraverseException e) {
			System.err.printf("Exception while parsing JSON structure: %s%n", e.getMessage());
			writeTaskInfoOnError.accept(System.err);
			return null;
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
		String jsonStr = null;
		if (baseURL!=null) {
			// http://et7x00/api/getallservices
			localInterface.setIndeterminateProgressTask("Read Content From URL");
			String url = baseURL;
			while (url.endsWith("/")) url = url.substring(0, url.length()-1);
			url += "/api/getallservices";
			jsonStr = getContentFromURL(url, true, false, true);
		}
		
		JSON_Object<NV,V> rawData = null;
		if (jsonStr!=null) {
			localInterface.setIndeterminateProgressTask("Parse Text Content");
			JSON_Parser<NV,V> parser = new JSON_Parser<>(jsonStr,null);
			try {
				JSON_Data.Value<NV,V> result = parser.parse_withParseException();
				if (result!=null) rawData = JSON_Data.getObjectValue(result);
			} catch (ParseException e) {
				System.err.printf("ParseException: %s%n", e.getMessage());
				//e.printStackTrace();
			}
		}
		
		if (rawData!=null) {
			localInterface.setIndeterminateProgressTask("Parse JSON Data");
			Boolean result = JSON_Data.getBoolValue(rawData.getValue("result"));
			if (result!=null && result==true) {
				JSON_Array<NV,V> services = JSON_Data.getArrayValue(rawData.getValue("services"));
				if (services!=null)
					for (JSON_Data.Value<NV,V> service:services) {
						Bouquet bouquet = Bouquet.parse(JSON_Data.getObjectValue(service),localInterface);
						if (bouquet!=null) {
							localInterface.addBouquet(bouquet);
						}
					}
			}
		}
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
		
		movieListReadInterface.setIndeterminateProgressTask("Read Content from URL");
		String content = getContent(urlStr);
		
		movieListReadInterface.setIndeterminateProgressTask("Parse Content");
		Value<NV, V> result = new JSON_Parser<NV,V>(content,null).parse();
		
		movieListReadInterface.setIndeterminateProgressTask("Create MovieList");
		try {
			return new MovieList(result);
		} catch (TraverseException e) {
			System.err.printf("Exception while parsing JSON structure: %s%n", e.getMessage());
			return null;
		}
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
	
	private static String getContentFromURL(String urlStr, boolean verbose, boolean showContent, boolean verboseOnError) {
		
		if (verbose) System.out.println("URL: "+urlStr);
		URL url;
		try { url = new URL(urlStr); }
		catch (MalformedURLException e2) { e2.printStackTrace(); return null; }
		
		if (verbose) System.out.println("Open Connection ...");
		HttpURLConnection connection;
		try { connection = (HttpURLConnection)url.openConnection(); }
		catch (IOException e) { e.printStackTrace(); return null; }
		
		connection.setDoInput(true);
		
		connection.setConnectTimeout(2000);
		
		if (verbose) System.out.println("Connect ...");
		try { connection.connect(); }
		catch (ConnectException e) {
			switch (e.getMessage()) {
			case "Connection refused: connect":
				if (verboseOnError) System.err.println("Connection refused at connect");
				break;
			case "Connection timed out: connect":
				if (verboseOnError) System.err.println("Connection timed out at connect");
				break;
			default:
				e.printStackTrace();
			}
			return null;
		}
		catch (SocketTimeoutException e) {
			if (e.getMessage().equals("connect timed out")) {
				if (verboseOnError) System.err.println("Socket Timeout");
			} else
				e.printStackTrace();
			return null;
		}
		catch (IOException e) { e.printStackTrace(); return null; }
		
		Integer httpResponseCode;
		try { httpResponseCode = connection.getResponseCode(); } catch (IOException e1) { httpResponseCode=null; }
		if (verbose || (verboseOnError && (httpResponseCode==null || httpResponseCode!=200))) {
			String responseMessage;
			try { responseMessage = connection.getResponseMessage(); } catch (IOException e) { responseMessage="????"; }
			if (httpResponseCode!=200) {
				System.err.printf("HTTP Response: %s \"%s\"%n", httpResponseCode==null ? "???" : httpResponseCode.intValue(), responseMessage);
				return null;
			} else {
				System.out.printf("HTTP Response: %s \"%s\"%n", httpResponseCode==null ? "???" : httpResponseCode.intValue(), responseMessage);
			}
		}
		
		if (verbose) System.out.println("Read Content ...");
		Object content;
		if (connection.getContentLength()>0)
			try { content = connection.getContent(); }
			catch (IOException e) { if (verboseOnError) /*showConnection(connection);*/ e.printStackTrace(); connection.disconnect(); return null; }
		else
			content = null;
		if (verbose && showContent) System.out.println("Content: "+content);
		
		String string = null;
		if (content instanceof InputStream) {
			InputStream input = (InputStream)content;
			byte[] bytes = new byte[connection.getContentLength()];
			int n,pos=0;
			try { while ( (n=input.read(bytes, pos, bytes.length-pos))>=0 ) pos += n; }
			catch (IOException e) { e.printStackTrace(); if (verbose) System.out.println("abort reading response");}
			
			if (verbose && showContent) {
				String bytesReadStr = pos!=bytes.length ? (" "+pos+" of "+bytes.length+" bytes "):"";
				if (pos<1000) {
					System.out.println("Content (bytes read): "+bytesReadStr+""+Arrays.toString(bytes));
				}
			}
			
			string = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes, 0, pos)).toString();
			
			if (verbose && showContent) {
				if (string.length()>1000) {
					System.out.println("Content (as String): "+string.substring(0, 100)+" ...");
					System.out.println("                     ... "+string.substring(string.length()-100,string.length()));
				} else
					System.out.println("Content (as String): "+string);
			}
		} else
			if (verboseOnError) /*showConnection(connection)*/; 
		
		connection.disconnect();
		
		return string;
	}

}
