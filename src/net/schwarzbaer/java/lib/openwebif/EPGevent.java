package net.schwarzbaer.java.lib.openwebif;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Null;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Value;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class EPGevent {
	
	public final String station_name;
	public final String title;
	public final String shortdesc;
	public final String longdesc;
	public final String genre;
	public final long   genreid;
	public final String date;
	public final String begin;
	public final String end;
	public final Long   begin_timestamp;
	public final long   now_timestamp;
	public final Long   duration_min;
	public final Long   duration_sec;
	public final Long   remaining;
	public final Long   progress;
	public final Double computedProgress;
	public final Long   tleft;
	public final String provider;
	public final String picon;
	public final String sref;
	public final Long   id;
	public boolean isUpToDate;
	
	static               EPGevent parse(Value<NV, V> value                                    ) throws TraverseException { return parse(value,null,EPGevent::new); }
	static <T extends EPGevent> T parse(Value<NV, V> value, EPGeventConstructor<T> constructor) throws TraverseException { return parse(value,null,constructor); }
	static               EPGevent parse(Value<NV, V> value, String debugOutputPrefixStr       ) throws TraverseException { return parse(value,debugOutputPrefixStr,EPGevent::new); }
	static <T extends EPGevent> T parse(Value<NV, V> value, String debugOutputPrefixStr, EPGeventConstructor<T> constructor) throws TraverseException {
		if (debugOutputPrefixStr==null) debugOutputPrefixStr = "EPGevent";
		JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
		return constructor.create(object, debugOutputPrefixStr);
	}
	
	public interface EPGeventConstructor<T extends EPGevent> {
		T create(JSON_Object<NV, V> object, String debugOutputPrefixStr) throws TraverseException;
	}

	public EPGevent(EPGevent other) {
		this.station_name     = other.station_name;    
		this.title            = other.title;           
		this.shortdesc        = other.shortdesc;       
		this.longdesc         = other.longdesc;        
		this.genre            = other.genre;           
		this.genreid          = other.genreid;         
		this.date             = other.date;            
		this.begin            = other.begin;           
		this.end              = other.end;             
		this.begin_timestamp  = other.begin_timestamp; 
		this.now_timestamp    = other.now_timestamp;   
		this.duration_min     = other.duration_min;    
		this.duration_sec     = other.duration_sec;    
		this.remaining        = other.remaining;       
		this.progress         = other.progress;
		this.computedProgress = other.computedProgress;
		this.tleft            = other.tleft;           
		this.provider         = other.provider;        
		this.picon            = other.picon;           
		this.sref             = other.sref;            
		this.id               = other.id;              
		this.isUpToDate       = other.isUpToDate;              
	}
	
	public EPGevent(JSON_Object<NV, V> object, String debugOutputPrefixStr) throws TraverseException {
		Null id_null, title_null, begin_timestamp_null, shortdesc_null, longdesc_null, duration_sec_null;
		isUpToDate      = true;
		station_name    = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "sname"                       , debugOutputPrefixStr) ); // "sname"          : "ZDF HD",                           
		title           = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "title"          , false, true, debugOutputPrefixStr) ); // "title"          : "Bares f\u00fcr Rares",
		title_null      =                                 JSON_Data.getNullValue   (object, "title"          , false, true, debugOutputPrefixStr);   // "title"          : null,
		shortdesc       = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "shortdesc"      , false, true, debugOutputPrefixStr) ); // "shortdesc"      : "Die Tr\u00f6del-Show mit Horst ... 
		shortdesc_null  =                                 JSON_Data.getNullValue   (object, "shortdesc"      , false, true, debugOutputPrefixStr);   // "shortdesc"      : null 
		longdesc        = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "longdesc"       , false, true, debugOutputPrefixStr) ); // "longdesc"       : "Tr\u00f6del-Show, Deutschland 20...
		longdesc_null   =                                 JSON_Data.getNullValue   (object, "longdesc"       , false, true, debugOutputPrefixStr);   // "longdesc"       : null
		genre           = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "genre"                       , debugOutputPrefixStr) ); // "genre"          : "Show Games show: Show/Spielshow (al...
		genreid         =                                 JSON_Data.getIntegerValue(object, "genreid"                     , debugOutputPrefixStr);   // "genreid"        : 48,                               
		date            =                                 JSON_Data.getStringValue (object, "date"           , true, false, debugOutputPrefixStr);   // "date"           : "Mi 12.05.2021",                                                              
		begin           =                                 JSON_Data.getStringValue (object, "begin"          , true, false, debugOutputPrefixStr);   // "begin"          : "15:05",                                                                      
		end             =                                 JSON_Data.getStringValue (object, "end"            , true, false, debugOutputPrefixStr);   // "end"            : "16:00",                                                                      
		begin_timestamp =                                 JSON_Data.getIntegerValue(object, "begin_timestamp", false, true, debugOutputPrefixStr);   // "begin_timestamp": 1620824700,               
		begin_timestamp_null =                            JSON_Data.getNullValue   (object, "begin_timestamp", false, true, debugOutputPrefixStr);   // "begin_timestamp": null,               
		now_timestamp   =                                 JSON_Data.getIntegerValue(object, "now_timestamp"               , debugOutputPrefixStr);   // "now_timestamp"  : 1620830628,                 
		duration_min    =                                 JSON_Data.getIntegerValue(object, "duration"       , true, false, debugOutputPrefixStr);   // "duration"       : 55,                                                                           
		duration_sec    =                                 JSON_Data.getIntegerValue(object, "duration_sec"   , false, true, debugOutputPrefixStr);   // "duration_sec"   : 3300,                        
		duration_sec_null =                               JSON_Data.getNullValue   (object, "duration_sec"   , false, true, debugOutputPrefixStr);   // "duration_sec"   : null,                        
		remaining       =                                 JSON_Data.getIntegerValue(object, "remaining"      , true, false, debugOutputPrefixStr);   //                                              
		progress        =                                 JSON_Data.getIntegerValue(object, "progress"       , true, false, debugOutputPrefixStr);   // "progress"       : 716,                                                                          
		tleft           =                                 JSON_Data.getIntegerValue(object, "tleft"          , true, false, debugOutputPrefixStr);   // "tleft"          : -44,                                                                          
		provider        = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "provider"       , true, false, debugOutputPrefixStr) ); //                                              
		picon           =                                 JSON_Data.getStringValue (object, "picon"          , true, false, debugOutputPrefixStr);   // "picon"          : "/picon/1_0_19_2B66_3F3_1_C00000_0_0_0.png",                                  
		sref            =                                 JSON_Data.getStringValue (object, "sref"                        , debugOutputPrefixStr);   // "sref"           : "1:0:19:2B66:3F3:1:C00000:0:0:0:",   
		id              =                                 JSON_Data.getIntegerValue(object, "id"             , false, true, debugOutputPrefixStr);   // "id"             : 23528                                  
		id_null         =                                 JSON_Data.getNullValue   (object, "id"             , false, true, debugOutputPrefixStr);   // "id"             : null
		
		
		if (begin_timestamp==null || now_timestamp==0 || duration_sec==null)
			computedProgress = null;
		
		else if (now_timestamp < begin_timestamp)
			computedProgress = 0.0;
		
		else if (now_timestamp > begin_timestamp + duration_sec)
			computedProgress = 1.0;
		
		else
			computedProgress = (now_timestamp - begin_timestamp) / duration_sec.doubleValue();
		
		
		checkValueOrNull(             id,              id_null,              "id", "IntegerValue", debugOutputPrefixStr);
		checkValueOrNull(begin_timestamp, begin_timestamp_null, "begin_timestamp", "IntegerValue", debugOutputPrefixStr);
		checkValueOrNull(   duration_sec,    duration_sec_null,    "duration_sec", "IntegerValue", debugOutputPrefixStr);
		checkValueOrNull(          title,           title_null,           "title",  "StringValue", debugOutputPrefixStr);
		checkValueOrNull(      shortdesc,       shortdesc_null,       "shortdesc",  "StringValue", debugOutputPrefixStr);
		checkValueOrNull(       longdesc,        longdesc_null,        "longdesc",  "StringValue", debugOutputPrefixStr);
	}
	
	private void checkValueOrNull(Object value, Null value_null, String fieldName, String typeName, String debugOutputPrefixStr) throws TraverseException
	{
		if (value==null && value_null==null)
			throw new TraverseException("%s.%s isn't either a %s or a NullValue", debugOutputPrefixStr, fieldName, typeName);
	}
}