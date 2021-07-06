package net.schwarzbaer.java.lib.openwebif;

import java.util.Vector;
import java.util.function.Consumer;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Array;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Null;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.StationInfo;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class SystemInfo {
	
	public static SystemInfo getSystemInfo(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		
		// http://et7x00/api/about
		String url = String.format("%s/api/about", baseURL);
		
		String baseURLStr = baseURL;
		return OpenWebifTools.getContentAndParseIt(url, err->{
				err.printf("   getSystemInfo(baseURL)%n");
				err.printf("      baseURL: \"%s\"%n", baseURLStr);
			},
			SystemInfo::new,
			setIndeterminateProgressTask
		);
	}
	
	public final StationInfo service;
	public final Info info;
	
	SystemInfo(JSON_Data.Value<NV, V> value) throws TraverseException { this(value,null); }
	SystemInfo(JSON_Data.Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
		if (debugOutputPrefixStr==null) debugOutputPrefixStr = "SystemInfo";
		
		//JSON_Helper.OptionalValues<NV, V> optVal = new JSON_Helper.OptionalValues<NV,V>();
		//optVal.scan(value, debugOutputPrefixStr);
		//optVal.show(System.err);
		
		JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);			
		
		service = new StationInfo(JSON_Data.getObjectValue(object,"service", debugOutputPrefixStr), debugOutputPrefixStr+".service");
		info    = new Info(JSON_Data.getObjectValue(object,"info", debugOutputPrefixStr), debugOutputPrefixStr+".info");
	}
	
	public static class Info {
		
		public final String EX;
		public final String boxtype;
		public final String brand;
		public final String chipset;
		public final String driverdate;
		public final String enigmaver;
		public final Null fp_version;
		public final String friendlychipsetdescription;
		public final String friendlychipsettext;
		public final String friendlyimagedistro;
		public final long grabpip;
		public final String imagedistro;
		public final String imagever;
		public final String kernelver;
		public final long lcd;
		public final String machinebuild;
		public final String mem1;
		public final String mem2;
		public final String mem3;
		public final String model;
		public final String oever;
		public final boolean timerautoadjust;
		public final boolean timerpipzap;
		public final boolean transcoding;
		public final String uptime;
		public final String webifver;
		
		public final Vector<Hdd> hdd;
		public final Vector<Interface> ifaces;
		public final Vector<Stream> streams;
		public final Vector<Tuner> tuners;

		Info(JSON_Object<NV, V> object, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "SystemInfo.Info";
			
			JSON_Array<NV, V> hddArr, ifacesArr, sharesArr, streamsArr, tunersArr;
			EX                        = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "EX"                        , debugOutputPrefixStr) ); // :String
			boxtype                   = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "boxtype"                   , debugOutputPrefixStr) ); // :String
			brand                     = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "brand"                     , debugOutputPrefixStr) ); // :String
			chipset                   = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "chipset"                   , debugOutputPrefixStr) ); // :String
			driverdate                = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "driverdate"                , debugOutputPrefixStr) ); // :String
			enigmaver                 = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "enigmaver"                 , debugOutputPrefixStr) ); // :String
			fp_version                =                                 JSON_Data.getNullValue   (object, "fp_version"                , debugOutputPrefixStr)  ; // :Null
			friendlychipsetdescription= JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "friendlychipsetdescription", debugOutputPrefixStr) ); // :String
			friendlychipsettext       = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "friendlychipsettext"       , debugOutputPrefixStr) ); // :String
			friendlyimagedistro       = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "friendlyimagedistro"       , debugOutputPrefixStr) ); // :String
			grabpip                   =                                 JSON_Data.getIntegerValue(object, "grabpip"                   , debugOutputPrefixStr)  ; // :Integer
			hddArr                    =                                 JSON_Data.getArrayValue  (object, "hdd"                       , debugOutputPrefixStr)  ; // :Array   []:Object
			ifacesArr                 =                                 JSON_Data.getArrayValue  (object, "ifaces"                    , debugOutputPrefixStr)  ; // :Array   []:Object
			imagedistro               = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "imagedistro"               , debugOutputPrefixStr) ); // :String
			imagever                  = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "imagever"                  , debugOutputPrefixStr) ); // :String
			kernelver                 = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "kernelver"                 , debugOutputPrefixStr) ); // :String
			lcd                       =                                 JSON_Data.getIntegerValue(object, "lcd"                       , debugOutputPrefixStr)  ; // :Integer
			machinebuild              = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "machinebuild"              , debugOutputPrefixStr) ); // :String
			mem1                      = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "mem1"                      , debugOutputPrefixStr) ); // :String
			mem2                      = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "mem2"                      , debugOutputPrefixStr) ); // :String
			mem3                      = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "mem3"                      , debugOutputPrefixStr) ); // :String
			model                     = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "model"                     , debugOutputPrefixStr) ); // :String
			oever                     = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "oever"                     , debugOutputPrefixStr) ); // :String
			sharesArr                 =                                 JSON_Data.getArrayValue  (object, "shares"                    , debugOutputPrefixStr)  ; // :Array   []:empty array
			streamsArr                =                                 JSON_Data.getArrayValue  (object, "streams"                   , debugOutputPrefixStr)  ; // :Array   []:Object
			timerautoadjust           =                                 JSON_Data.getBoolValue   (object, "timerautoadjust"           , debugOutputPrefixStr)  ; // :Bool
			timerpipzap               =                                 JSON_Data.getBoolValue   (object, "timerpipzap"               , debugOutputPrefixStr)  ; // :Bool
			transcoding               =                                 JSON_Data.getBoolValue   (object, "transcoding"               , debugOutputPrefixStr)  ; // :Bool
			tunersArr                 =                                 JSON_Data.getArrayValue  (object, "tuners"                    , debugOutputPrefixStr)  ; // :Array   []:Object
			uptime                    = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "uptime"                    , debugOutputPrefixStr) ); // :String
			webifver                  = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "webifver"                  , debugOutputPrefixStr) ); // :String
			
			//shares  = sharesArr .parseContent(Hdd::new, debugOutputPrefixStr, "shares" );
			if (!sharesArr.isEmpty())
				System.err.printf("Found unexpected, nonempty array \"\": %d items%n", "shares", sharesArr.size());
			
			hdd     = hddArr    .parseContent(Hdd      ::new, debugOutputPrefixStr, "hdd"    );
			ifaces  = ifacesArr .parseContent(Interface::new, debugOutputPrefixStr, "ifaces" );
			streams = streamsArr.parseContent(Stream   ::new, debugOutputPrefixStr, "streams");
			tuners  = tunersArr .parseContent(Tuner    ::new, debugOutputPrefixStr, "tuners" );
		}
	}
	
	public static class Hdd {
		
		public final String capacity;
		public final String free;
		public final String friendlycapacity;
		public final String labelled_capacity;
		public final String model;
		public final String mount;

		Hdd(JSON_Data.Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "SystemInfo.Info.Hdd";
			
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			capacity          = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "capacity"         , debugOutputPrefixStr) ); // :String
			free              = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "free"             , debugOutputPrefixStr) ); // :String
			friendlycapacity  = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "friendlycapacity" , debugOutputPrefixStr) ); // :String
			labelled_capacity = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "labelled_capacity", debugOutputPrefixStr) ); // :String
			model             = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "model"            , debugOutputPrefixStr) ); // :String
			mount             = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "mount"            , debugOutputPrefixStr) ); // :String
		
		}
	}
	
	public static class Interface {
		
		public final boolean dhcp;
		public final Null firstpublic;
		public final String friendlynic;
		public final String gw;
		public final String ip;
		public final String ipmethod;
		public final String ipv4method;
		public final String ipv6;
		public final String linkspeed;
		public final String mac;
		public final String mask;
		public final String name;
		public final long v4prefix;

		Interface(JSON_Data.Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "SystemInfo.Info.Interface";
			
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			dhcp        =                                 JSON_Data.getBoolValue   (object, "dhcp"       , debugOutputPrefixStr)  ; // :Bool   
			firstpublic =                                 JSON_Data.getNullValue   (object, "firstpublic", debugOutputPrefixStr)  ; // :Null   
			friendlynic = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "friendlynic", debugOutputPrefixStr) ); // :String 
			gw          = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "gw"         , debugOutputPrefixStr) ); // :String 
			ip          = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "ip"         , debugOutputPrefixStr) ); // :String 
			ipmethod    = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "ipmethod"   , debugOutputPrefixStr) ); // :String 
			ipv4method  = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "ipv4method" , debugOutputPrefixStr) ); // :String 
			ipv6        = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "ipv6"       , debugOutputPrefixStr) ); // :String 
			linkspeed   = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "linkspeed"  , debugOutputPrefixStr) ); // :String 
			mac         = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "mac"        , debugOutputPrefixStr) ); // :String 
			mask        = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "mask"       , debugOutputPrefixStr) ); // :String 
			name        = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "name"       , debugOutputPrefixStr) ); // :String 
			v4prefix    =                                 JSON_Data.getIntegerValue(object, "v4prefix"   , debugOutputPrefixStr)  ; // :Integer
		}
	}
	
	public static class Stream {
		
		public final String eventname;
		public final String ip;
		public final String name;
		public final String ref;
		public final String type;

		Stream(JSON_Data.Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "SystemInfo.Info.Stream";
			
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			eventname = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "eventname", debugOutputPrefixStr) ); // :String 
			ip        = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "ip"       , debugOutputPrefixStr) ); // :String 
			name      = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "name"     , debugOutputPrefixStr) ); // :String 
			ref       = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "ref"      , debugOutputPrefixStr) ); // :String 
			type      = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "type"     , debugOutputPrefixStr) ); // :String 
		}
	}
	
	public static class Tuner {
		
		public final String live;
		public final String name;
		public final String rec;
		public final String type;

		Tuner(JSON_Data.Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			if (debugOutputPrefixStr==null) debugOutputPrefixStr = "SystemInfo.Info.Tuner";
			
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			live = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "live", debugOutputPrefixStr) ); // :String 
			name = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "name", debugOutputPrefixStr) ); // :String 
			rec  = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "rec" , debugOutputPrefixStr) ); // :String 
			type = JSON_Data.decodeUnicodeAndHTML( JSON_Data.getStringValue (object, "type", debugOutputPrefixStr) ); // :String 
		}
	}
}
