package net.schwarzbaer.java.lib.openwebif;

import java.util.HashMap;
import java.util.function.Consumer;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Array;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class BoxSettings {
	
	public static HashMap<String,BoxSettingsValue> getSettings(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		
		// http://et7x00/api/settings
		String url = String.format("%s/api/settings", baseURL);
		
		String baseURLStr = baseURL;
		BoxSettings result = OpenWebifTools.getContentAndParseIt(url, err->{
				err.printf("   getSystemInfo(baseURL)%n");
				err.printf("      baseURL: \"%s\"%n", baseURLStr);
			},
			BoxSettings::new,
			setIndeterminateProgressTask
		);
		
		if (result==null || !result.result) return null;
		return result.settings;
	}
	
	private final boolean result;
	private final HashMap<String, BoxSettingsValue> settings;

	BoxSettings(JSON_Data.Value<NV, V> value) throws TraverseException { this(value,null); }
	BoxSettings(JSON_Data.Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
		if (debugOutputPrefixStr==null) debugOutputPrefixStr = "BoxSettings";
		
		//JSON_Helper.OptionalValues<NV, V> optVal = new JSON_Helper.OptionalValues<NV,V>();
		//optVal.scan(value, debugOutputPrefixStr);
		//optVal.show(System.err);
		
		JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);			
		// result:Bool
		// settings:Array
		// settings[]:Array
		// settings[][]:[String, Integer]
		
		JSON_Array<NV, V> settingsArr;
		result      = JSON_Data.getBoolValue (object, "result"  , debugOutputPrefixStr)  ; // :Bool
		settingsArr = JSON_Data.getArrayValue(object, "settings", debugOutputPrefixStr)  ; // :Array   []:Array   [][]:[String, Integer]
		
		
		settings = new HashMap<String, BoxSettingsValue>();
		for (int i=0; i<settingsArr.size(); i++) {
			JSON_Array<NV, V> arrayValue = JSON_Data.getArrayValue(settingsArr.get(i), debugOutputPrefixStr+".settings["+i+"]");
			if (arrayValue.size()!=2) {
				System.err.printf("BoxSettings.Parse: Found wrong count of values in setting value [%d]: 2 expected, but %d found%n", i, arrayValue.size());
				continue;
			}
			
			JSON_Data.Value<NV, V> nameValue  = arrayValue.get(0);
			JSON_Data.Value<NV, V> valueValue = arrayValue.get(1);
			
			String name = JSON_Data.getStringValue(nameValue);
			if (name==null) {
				System.err.printf("BoxSettings.Parse: Found wrong type of first value in setting value [%d]: <String> expected, but <%s> found%n", i, nameValue==null ? null : nameValue.type);
				continue;
			}
			
			String strValue = JSON_Data.getStringValue (valueValue);
			Long   intValue = JSON_Data.getIntegerValue(valueValue);
			
			BoxSettingsValue boxSettingsValue = null;
			if      (strValue!=null) boxSettingsValue = new BoxSettingsValue(name,strValue);
			else if (intValue!=null) boxSettingsValue = new BoxSettingsValue(name,intValue.longValue());
			if (boxSettingsValue == null) {
				System.err.printf("BoxSettings.Parse: Found wrong type of second value in setting value [%d]: <String> or <Integer> expected, but <%s> found%n", i, valueValue==null ? null : valueValue.type);
				continue;
			}
			
			if (settings.containsKey(name)) {
				System.err.printf("BoxSettings.Parse: Found redundant setting value [%d]: \"%s\" already in map%n", i, name);
				continue;
			}
			
			settings.put(name, boxSettingsValue);
		}
		
	}
	
	public static class BoxSettingsValue {
		
		public enum Type { String, Integer }
		
		public final Type type;
		public final String name;
		public final String strValue;
		public final long intValue;

		private BoxSettingsValue(String name, String strValue) {
			this.type = Type.String;
			this.name = name;
			this.strValue = strValue;
			this.intValue = -1;
		}

		private BoxSettingsValue(String name, long intValue) {
			this.type = Type.Integer;
			this.name = name;
			this.strValue = null;
			this.intValue = intValue;
		}

		public String getValueStr() {
			switch (type) {
			case Integer: return Long.toString(intValue);
			case String : return String.format("\"%s\"", strValue);
			}
			throw new IllegalStateException();
		}
		
	}
}
