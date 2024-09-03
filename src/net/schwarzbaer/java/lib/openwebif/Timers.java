package net.schwarzbaer.java.lib.openwebif;

import java.util.Vector;
import java.util.function.Consumer;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Array;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Null;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Value;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class Timers {
	
	public final boolean result;
	public final Vector<Timer> timers;
	public final Vector<String> locations;
	
	Timers(Value<NV, V> value) throws TraverseException { this(value,null); }
	Timers(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
		if (debugOutputPrefixStr==null) debugOutputPrefixStr = "Timers";
		
		//JSON_Helper.OptionalValues<NV, V> optVal = new JSON_Helper.OptionalValues<NV,V>();
		//optVal.scan(value, debugOutputPrefixStr);
		//optVal.show(System.err);
		//result = false;
		//timers = null;
		//locations = null;
		
		JSON_Array<NV, V> timersRaw, locationsRaw;
		JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);			
		result       = JSON_Data.getBoolValue (object, "result", debugOutputPrefixStr);
		timersRaw    = JSON_Data.getArrayValue(object, "timers", debugOutputPrefixStr);
		locationsRaw = JSON_Data.getArrayValue(object, "locations", debugOutputPrefixStr);
		
		timers = new Vector<>();
		for (int i=0; i<timersRaw.size(); i++)
			timers.add(new Timer(timersRaw.get(i), debugOutputPrefixStr+".timers["+i+"]"));
		
		locations = new Vector<>();
		for (int i=0; i<locationsRaw.size(); i++)
			locations.add(JSON_Data.getStringValue(locationsRaw.get(i), debugOutputPrefixStr+".locations["+i+"]"));
	}
	
	public Vector<Timer> getStationTimers(String sref) {
		Vector<Timer> stationTimers = new Vector<>();
		for (Timer timer : timers)
			if (sref.equalsIgnoreCase( timer.serviceref ))
				stationTimers.add(timer);
		return stationTimers;
	}
	
	public static Timers read(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		String url = baseURL+API.API_TIMERLIST;
		
		return OpenWebifTools.getContentAndParseJSON(url, err->{
			err.printf("   readTimers(url)%n");
			err.printf("      url: \"%s\"%n", url);
		}, Timers::new, setIndeterminateProgressTask);
	}
	
	public static OpenWebifTools.MessageResponse addTimer(String baseURL, String sRef, int eventid, Timer.Type type, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		
		String extraParameter = "";
		if (type!=null)
			switch (type) {
				case Record       : break;
				case Switch       : extraParameter = "&justplay=1"; break;
				case RecordNSwitch: extraParameter = "&always_zap=1"; break;
				case Unknown      : break;
			}
		
		String url = String.format("%s%s?sRef=%s&eventid=%d%s", baseURL, API.API_TIMERADDBYEVENTID, sRef, eventid, extraParameter);
		
		String baseURLStr = baseURL;
		return OpenWebifTools.getContentAndParseJSON(url, err->{
				err.printf("   addTimer(baseURL, sRef, eventid, type)%n");
				err.printf("      baseURL: \"%s\"%n", baseURLStr);
				err.printf("      sRef   : \"%s\"%n", sRef);
				err.printf("      eventid: %d%n", eventid);
				err.printf("      type   : %s%n", type);
				err.printf("      -> url : \"%s\"%n", url);
			},
			OpenWebifTools.MessageResponse::new,
			setIndeterminateProgressTask
		);
	}
	
	public static OpenWebifTools.MessageResponse deleteTimer(String baseURL, String sRef, long begin, long end, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		
		String url = String.format("%s%s?sRef=%s&begin=%d&end=%d", baseURL, API.API_TIMERDELETE, sRef, begin, end);
		
		String baseURLStr = baseURL;
		return OpenWebifTools.getContentAndParseJSON(url, err->{
				err.printf("   deleteTimer(baseURL, sRef, begin, end)%n");
				err.printf("      baseURL: \"%s\"%n", baseURLStr);
				err.printf("      sRef   : \"%s\"%n", sRef);
				err.printf("      begin  : %d%n", begin);
				err.printf("      end    : %d%n", end);
				err.printf("      -> url : \"%s\"%n", url);
			},
			OpenWebifTools.MessageResponse::new,
			setIndeterminateProgressTask
		);
	}
	
	public static OpenWebifTools.MessageResponse toggleTimer(String baseURL, String sRef, long begin, long end, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		
		String url = String.format("%s%s?sRef=%s&begin=%d&end=%d", baseURL, API.API_TIMERTOGGLESTATUS, sRef, begin, end);
		
		String baseURLStr = baseURL;
		return OpenWebifTools.getContentAndParseJSON(url, err->{
				err.printf("   toggleTimer(baseURL, sRef, begin, end)%n");
				err.printf("      baseURL: \"%s\"%n", baseURLStr);
				err.printf("      sRef   : \"%s\"%n", sRef);
				err.printf("      begin  : %d%n", begin);
				err.printf("      end    : %d%n", end);
				err.printf("      -> url : \"%s\"%n", url);
			},
			OpenWebifTools.MessageResponse::new,
			setIndeterminateProgressTask
		);
	}
	
	public static OpenWebifTools.MessageResponse cleanup(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		if (baseURL==null) return null;
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		String url = baseURL+API.API_TIMERCLEANUP;
		
		String baseURL_ = baseURL;
		return OpenWebifTools.getContentAndParseJSON(url, err->{
			err.printf("   cleanup(baseURL)%n");
			err.printf("      baseURL: \"%s\"%n", baseURL_);
			err.printf("      -> url : \"%s\"%n", url);
		}, OpenWebifTools.MessageResponse::new, setIndeterminateProgressTask);
	}

	public static class Timer {
		
		public final long    afterevent;
		public final long    allow_duplicate;
		public final long    always_zap;
		public final String  asrefs;
		public final long    autoadjust;
		public final long    backoff;
		public final long    begin;
		public final boolean cancelled;
		public final String  description;
		public final String  descriptionextended;
		public final String  dirname;
		public final long    disabled;
		public final long    dontsave;
		public final double  duration;
		public final long    eit;
		public final long    end;
		public final double  end_double;
		public final String  filename;
		public final long    firsttryprepare;
		public final long    isAutoTimer;
		public final long    justplay;
		public final String  name;
		public final Long    nextactivation;
		public final long    pipzap;
		public final String  realbegin;
		public final String  realend;
		public final long    repeated;
		public final String  servicename;
		public final String  serviceref;
		public final double  startprepare;
		public final long    state;
		public final String  tags;
		public final long    toggledisabled;
		public final String  toggledisabledimg;
		public final boolean vpsplugin_enabled;
		public final boolean vpsplugin_overwrite;
		public final long    vpsplugin_time;
		public final Vector<LogEntry> logentries;
		public final Type type;
		public final State state2;
		
		Timer(Value<NV, V> value) throws TraverseException { this(value,null); }
		Timer(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "Timer";
			
	        Boolean repeatedBool = null;
	        Long    repeatedInt = null; 
	        @SuppressWarnings("unused")
			Null nextactivationNull, filenameNull;
	        JSON_Array<NV, V> logentriesRaw;
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
	        afterevent          =                                 JSON_Data.getIntegerValue (object, "afterevent"                 , debugOutputPrefixStr);   // Integer
	        allow_duplicate     =                                 JSON_Data.getIntegerValue (object, "allow_duplicate"            , debugOutputPrefixStr);   // Integer
	        always_zap          =                                 JSON_Data.getIntegerValue (object, "always_zap"                 , debugOutputPrefixStr);   // Integer
	        asrefs              = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "asrefs"                     , debugOutputPrefixStr) ); // String
	        autoadjust          =                                 JSON_Data.getIntegerValue (object, "autoadjust"                 , debugOutputPrefixStr);   // Integer
	        backoff             =                                 JSON_Data.getIntegerValue (object, "backoff"                    , debugOutputPrefixStr);   // Integer
	        begin               =                                 JSON_Data.getIntegerValue (object, "begin"                      , debugOutputPrefixStr);   // Integer
	        cancelled           =                                 JSON_Data.getBoolValue    (object, "cancelled"                  , debugOutputPrefixStr);   // Bool
	        description         = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "description"                , debugOutputPrefixStr) ); // String
	        descriptionextended = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "descriptionextended"        , debugOutputPrefixStr) ); // String
	        dirname             = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "dirname"                    , debugOutputPrefixStr) ); // String
	        disabled            =                                 JSON_Data.getIntegerValue (object, "disabled"                   , debugOutputPrefixStr);   // Integer
	        dontsave            =                                 JSON_Data.getIntegerValue (object, "dontsave"                   , debugOutputPrefixStr);   // Integer
	        duration            =                                 JSON_Data.getNumber       (object, "duration"                   , debugOutputPrefixStr);   // [Integer, Float]
	        eit                 =                                 JSON_Data.getIntegerValue (object, "eit"                        , debugOutputPrefixStr);   // Integer
	        end_double          =                                 JSON_Data.getNumber       (object, "end"                        , debugOutputPrefixStr);   // [Integer, Float]
	        filename            = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "filename"      , false, true, debugOutputPrefixStr) ); // [String, Null]
			filenameNull        =                                 JSON_Data.getNullValue    (object, "filename"      , false, true, debugOutputPrefixStr);   // [String, Null]
	        firsttryprepare     =                                 JSON_Data.getIntegerValue (object, "firsttryprepare"            , debugOutputPrefixStr);   // Integer
	        isAutoTimer         =                                 JSON_Data.getIntegerValue (object, "isAutoTimer"                , debugOutputPrefixStr);   // Integer
	        justplay            =                                 JSON_Data.getIntegerValue (object, "justplay"                   , debugOutputPrefixStr);   // Integer
			logentriesRaw       =                                 JSON_Data.getArrayValue   (object, "logentries"                 , debugOutputPrefixStr);   // Array
	        name                = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "name"                       , debugOutputPrefixStr) ); // String
	        nextactivation      =                                 JSON_Data.getIntegerValue (object, "nextactivation", false, true, debugOutputPrefixStr);   // [Integer, Null]
			nextactivationNull  =                                 JSON_Data.getNullValue    (object, "nextactivation", false, true, debugOutputPrefixStr);   // [Integer, Null]
	        pipzap              =                                 JSON_Data.getIntegerValue (object, "pipzap"                     , debugOutputPrefixStr);   // Integer
	        realbegin           = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "realbegin"                  , debugOutputPrefixStr) ); // String
	        realend             = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "realend"                    , debugOutputPrefixStr) ); // String
	        repeatedInt         =                                 JSON_Data.getIntegerValue (object, "repeated"      , false, true, debugOutputPrefixStr);   // Integer
			repeatedBool        =                                 JSON_Data.getBoolValue    (object, "repeated"      , false, true, debugOutputPrefixStr);   // Bool
	        servicename         = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "servicename"                , debugOutputPrefixStr) ); // String
	        serviceref          = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "serviceref"                 , debugOutputPrefixStr) ); // String
	        startprepare        =                                 JSON_Data.getNumber       (object, "startprepare"               , debugOutputPrefixStr);   // [Integer, Float]
	        state               =                                 JSON_Data.getIntegerValue (object, "state"                      , debugOutputPrefixStr);   // Integer
	        tags                = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "tags"                       , debugOutputPrefixStr) ); // String
	        toggledisabled      =                                 JSON_Data.getIntegerValue (object, "toggledisabled"             , debugOutputPrefixStr);   // Integer
	        toggledisabledimg   = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue  (object, "toggledisabledimg"          , debugOutputPrefixStr) ); // String
	        vpsplugin_enabled   =                                 JSON_Data.getBoolValue    (object, "vpsplugin_enabled"          , debugOutputPrefixStr);   // Bool
	        vpsplugin_overwrite =                                 JSON_Data.getBoolValue    (object, "vpsplugin_overwrite"        , debugOutputPrefixStr);   // Bool
	        vpsplugin_time      =                                 JSON_Data.getIntegerValue (object, "vpsplugin_time"             , debugOutputPrefixStr);   // Integer
	        
	        end = Math.round(end_double);
	        
	        if      (repeatedInt  != null) repeated = repeatedInt;
			else if (repeatedBool != null) repeated = repeatedBool ? 1 : 0;
			else throw new TraverseException("%s.repeated isn't an IntegerValue nor a BoolValue", debugOutputPrefixStr);
	        
	        logentries = new Vector<>();
			for (int i=0; i<logentriesRaw.size(); i++)
				logentries.add(new LogEntry(logentriesRaw.get(i), debugOutputPrefixStr+".logentries["+i+"]"));
			
			type = Type.get(this);
			state2 = State.get(this);
		}
		
		public enum Type {
			
			Record, Switch, RecordNSwitch, Unknown;
			
			private static Type get(Timer timer) {
				if (timer==null) throw new IllegalArgumentException();
				if (timer.justplay==0 && timer.always_zap==0) return Record;
				if (timer.justplay==0 && timer.always_zap==1) return RecordNSwitch;
				if (timer.justplay==1 && timer.always_zap==0) return Switch;
				return Unknown;
			}
		}
		
		public enum State {
			
			Running, Waiting, Deactivated, Finished, Unknown;
			
			private static State get(Timer timer) {
				if (timer==null) throw new IllegalArgumentException();
				switch ((int)timer.disabled) {
				case 0:
					switch ((int)timer.state) {
					case 0: return Waiting;
					case 2: return Running;
					case 3: return Finished;
					}
					break;
				case 1:
					switch ((int)timer.state) {
					case 3: return Deactivated;
					}
					break;
				}
				return Unknown;
			}
		}
	}
	
	public static class LogEntry {
		/*
    [
     1620793017, 
     500, 
     "[AutoTimer] Try to add new timer based on AutoTimer K\u00e4pt'ns Dinner."
    ], 
		 */
		public final long   when;
		public final long   type;
		public final String text;
		
		LogEntry(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "LogEntry";
			
			JSON_Array<NV, V> array = JSON_Data.getArrayValue(value, debugOutputPrefixStr);
			if (array.size()!=3) throw new TraverseException("");
			when =                                 JSON_Data.getIntegerValue(array.get(0), debugOutputPrefixStr+"[0]<when>");
			type =                                 JSON_Data.getIntegerValue(array.get(1), debugOutputPrefixStr+"[1]<type>");
			text = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (array.get(2), debugOutputPrefixStr+"[2]<text>") );
		}
	}
	
	
/*
Optional JSON Values: [3 blocks]
    Block "Timers" [0]
        <Base>:Object
    Block "Timers.<Base>" [3]
        locations:Array
        locations[]:String
        result:Bool
        timers:Array
        timers[]:Object
    Block "Timers.<Base>.timers[]" [37]
        afterevent:Integer
        allow_duplicate:Integer
        always_zap:Integer
        asrefs:String
        autoadjust:Integer
        backoff:Integer
        begin:Integer
        cancelled:Bool
        description:String
        descriptionextended:String
        dirname:String
        disabled:Integer
        dontsave:Integer
        duration:Integer
        eit:Integer
        end:Integer
        filename:[String, Null]
        firsttryprepare:Integer
        isAutoTimer:Integer
        justplay:Integer
        logentries:Array
        logentries[]:Array or empty array
        logentries[][]:[String, Integer]
        name:String
        nextactivation:[Integer, Null]
        pipzap:Integer
        realbegin:String
        realend:String
        repeated:Integer
        servicename:String
        serviceref:String
        startprepare:[Integer, Float]
        state:Integer
        tags:String
        toggledisabled:Integer
        toggledisabledimg:String
        vpsplugin_enabled:Bool
        vpsplugin_overwrite:Bool
        vpsplugin_time:Integer

 */
	
}
