package net.schwarzbaer.java.lib.openwebif;

import java.util.function.Consumer;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Value;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class Volume {
	
	private enum Commands {
		state, up, down, mute, set
	}
	
	public static Values getState  (String baseURL, Consumer<String> setIndeterminateProgressTask) { return getContentAndParseIt(getURL(baseURL, Commands.state), setIndeterminateProgressTask); }
	public static Values setVolUp  (String baseURL, Consumer<String> setIndeterminateProgressTask) { return getContentAndParseIt(getURL(baseURL, Commands.up   ), setIndeterminateProgressTask); }
	public static Values setVolDown(String baseURL, Consumer<String> setIndeterminateProgressTask) { return getContentAndParseIt(getURL(baseURL, Commands.down ), setIndeterminateProgressTask); }
	public static Values setVolMute(String baseURL, Consumer<String> setIndeterminateProgressTask) { return getContentAndParseIt(getURL(baseURL, Commands.mute ), setIndeterminateProgressTask); }
	public static Values setVol(String baseURL, int value, Consumer<String> setIndeterminateProgressTask) { return getContentAndParseIt(getURL(baseURL, Commands.set, value), setIndeterminateProgressTask); }

	private static Values getContentAndParseIt(String url, Consumer<String> setIndeterminateProgressTask) {
		return OpenWebifTools.getContentAndParseIt(url, out->{}, result -> {
			ParseResult parseResult = new ParseResult(result);
			if (!parseResult.result) return null;
			return parseResult.values;
		}, setIndeterminateProgressTask);
	}
	
	private static String getURL(String baseURL, Commands cmd) {
		return getURL(baseURL, cmd, null);
	}
	private static String getURL(String baseURL, Commands cmd, Integer value) {
		while (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length()-1);
		// "http://et7x00/api/vol?set=mute"
		return String.format("%s/api/vol?set=%s%s", baseURL, cmd.toString(), value==null ? "" : value);
	}

	private static class ParseResult {
		
		private final boolean result;
		private final Values values;
		
		ParseResult(Value<NV, V> value) throws TraverseException { this(value,null); }
		ParseResult(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "Volume.ParseResult";
			
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			result = JSON_Data.getBoolValue (object, "result", debugOutputPrefixStr);
			values = new Values(object,debugOutputPrefixStr+".Values");
		}
		
	}

	public static class Values {

		public final boolean ismute;
		public final long current;
		public final String message;

		private Values(JSON_Object<NV, V> object, String debugOutputPrefixStr) throws TraverseException {
			// current	95
			// message	"Volume changed"
			// ismute	false
			
			ismute  =                               JSON_Data.getBoolValue   (object, "ismute" , debugOutputPrefixStr);
			current =                               JSON_Data.getIntegerValue(object, "current", debugOutputPrefixStr);
			message = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "message", debugOutputPrefixStr) );
		}
		
	}
	
}
