package net.schwarzbaer.java.lib.openwebif;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Value;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class EPGevent {
	private static boolean SHOW_PARSE_PROGRESS = false;
	
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
	public final String provider;
	public final String sref;
	public final Long id;

	static EPGevent parse(Value<NV, V> value) throws TraverseException { return parse(value,null); }
	static EPGevent parse(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
		if (debugOutputPrefixStr==null) debugOutputPrefixStr = "EPGevent";
		JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
		return new EPGevent(object, debugOutputPrefixStr);
	}
	
	public EPGevent(JSON_Object<NV, V> object, String debugOutputPrefixStr) throws TraverseException {
		Object id_null;
		station_name    = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "sname"          , debugOutputPrefixStr) ); if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: sname"          );
		title           = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "title"          , debugOutputPrefixStr) ); if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: title"          );
		shortdesc       = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "shortdesc"      , debugOutputPrefixStr) ); if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: shortdesc"      );
		longdesc        = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "longdesc"       , debugOutputPrefixStr) ); if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: longdesc"       );
		genre           = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "genre"          , debugOutputPrefixStr) ); if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: genre"          );
		genreid         =                               JSON_Data.getIntegerValue(object, "genreid"        , debugOutputPrefixStr);   if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: genreid"        );
		begin_timestamp =                               JSON_Data.getIntegerValue(object, "begin_timestamp", debugOutputPrefixStr);   if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: begin_timestamp");
		now_timestamp   =                               JSON_Data.getIntegerValue(object, "now_timestamp"  , debugOutputPrefixStr);   if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: now_timestamp"  );
		duration_sec    =                               JSON_Data.getIntegerValue(object, "duration_sec"   , debugOutputPrefixStr);   if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: duration_sec"   );
		remaining       =                               JSON_Data.getIntegerValue(object, "remaining"      , debugOutputPrefixStr);   if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: remaining"      );
		sref            =                               JSON_Data.getStringValue (object, "sref"           , debugOutputPrefixStr);   if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: sref"           );
		provider        = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "provider"       , true, false, debugOutputPrefixStr) ); if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: provider"       );
		id              =                               JSON_Data.getIntegerValue(object, "id", false, true, debugOutputPrefixStr);   if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: id");
		id_null         =                               JSON_Data.getNullValue   (object, "id", false, true, debugOutputPrefixStr);   if (SHOW_PARSE_PROGRESS) System.out.println("EPGevent: id (NULL)");
		if (id==null && id_null==null)
			throw new TraverseException("%s.id isn't either an IntegerValue or a NullValue", debugOutputPrefixStr);
	}
	
}