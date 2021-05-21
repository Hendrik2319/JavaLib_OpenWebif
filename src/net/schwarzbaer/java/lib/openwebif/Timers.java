package net.schwarzbaer.java.lib.openwebif;

import java.util.Vector;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Array;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Null;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Value;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class Timers {
	
	final boolean result;
	final Vector<Timer> timers;
	final Vector<String> locations;
	
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
		public final long    duration;
		public final long    eit;
		public final long    end;
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
		
		Timer(Value<NV, V> value) throws TraverseException { this(value,null); }
		Timer(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "Timer";
			
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
	        duration            =                                 JSON_Data.getIntegerValue (object, "duration"                   , debugOutputPrefixStr);   // Integer
	        eit                 =                                 JSON_Data.getIntegerValue (object, "eit"                        , debugOutputPrefixStr);   // Integer
	        end                 =                                 JSON_Data.getIntegerValue (object, "end"                        , debugOutputPrefixStr);   // Integer
	        filename            =                                 JSON_Data.getStringValue  (object, "filename"      , false, true, debugOutputPrefixStr);   // [String, Null]
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
	        repeated            =                                 JSON_Data.getIntegerValue (object, "repeated"                   , debugOutputPrefixStr);   // Integer
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
	        
	        logentries = new Vector<LogEntry>();
			for (int i=0; i<logentriesRaw.size(); i++)
				logentries.add(new LogEntry(logentriesRaw.get(i), debugOutputPrefixStr+".logentries["+i+"]"));
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
