package net.schwarzbaer.java.lib.openwebif;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Array;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Parser;
import net.schwarzbaer.java.lib.jsonparser.JSON_Parser.ParseException;

public class OpenWebifTools {

	public static String getStationStreamURL(String baseURL, StationID stationID) {
		while (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length()-1);
		// http://192.168.2.75:8001/1:0:19:2B66:3F3:1:C00000:0:0:0:
		return String.format("%s:8001/%s:", baseURL, stationID.toIDStr());
	}
	
	static class NV extends JSON_Data.NamedValueExtra.Dummy{}
	static class V extends JSON_Data.ValueExtra.Dummy{}
	
	public interface BouquetReadInterface {
		void setIndeterminateProgressTask(String taskTitle);
		void addBouquet(Bouquet Bouquet);
		void addStationName(StationID stationID, String name);
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
