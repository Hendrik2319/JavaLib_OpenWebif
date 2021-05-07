package net.schwarzbaer.java.lib.openwebif;

import java.util.function.Consumer;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Value;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class Power {
	
	public enum Commands {
		ToggleStandBy(0,"Toggle StandBy"),
		DeepStandBy  (1,"Switch Off (Deep StandBy)"),
		Reboot       (2,"Restart Box (Linux)"),
		RestartEnigma(3,"Restart GUI (Enigma)"),
		Wakeup       (4,"Switch On"),
		Standby      (5,"Switch to Standby"),
		;
		
		private final int value;
		public final String title;
		
		Commands() { this(0,""); }
		Commands(int value) { this(value,null); }
		Commands(int value, String title) {
			this.value = value;
			this.title = title;
		}
		@Override public String toString() { return title==null ? name() : title; }
		
	}
	
	public static Values getState(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		return getContentAndParseIt(getURL(baseURL, null), setIndeterminateProgressTask);
	}
	
	public static Values setState(String baseURL, Commands cmd, Consumer<String> setIndeterminateProgressTask) {
		return getContentAndParseIt(getURL(baseURL, cmd), setIndeterminateProgressTask);
	}

	private static Values getContentAndParseIt(String url, Consumer<String> setIndeterminateProgressTask) {
		return OpenWebifTools.getContentAndParseIt(url, out->{
			// TODO
		}, result -> {
			ParseResult parseResult = new ParseResult(result);
			if (!parseResult.result) return null;
			return parseResult.values;
		}, setIndeterminateProgressTask);
	}
	
	private static String getURL(String baseURL, Commands cmd) {
		while (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length()-1);
		// "http://et7x00/api/powerstate?newstate=0"
		if (cmd==null)
			return String.format("%s/api/powerstate", baseURL);
		return String.format("%s/api/powerstate?newstate=%d", baseURL, cmd.value);
	}

	private static class ParseResult {
		
		private final boolean result;
		private final Values values;
		
		ParseResult(Value<NV, V> value) throws TraverseException { this(value,null); }
		ParseResult(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "Power.ParseResult";
			
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			result = JSON_Data.getBoolValue (object, "result", debugOutputPrefixStr);
			values = new Values(object,debugOutputPrefixStr+".Values");
		}
		
	}

	public static class Values {
		
		public final boolean instandby;

		private Values(JSON_Object<NV, V> object, String debugOutputPrefixStr) throws TraverseException {
			instandby = JSON_Data.getBoolValue(object, "instandby" , debugOutputPrefixStr);
		}
	}
	
}
