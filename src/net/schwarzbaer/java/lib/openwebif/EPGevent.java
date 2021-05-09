package net.schwarzbaer.java.lib.openwebif;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
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
		station_name    = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "sname"          , debugOutputPrefixStr) );
		title           = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "title"          , debugOutputPrefixStr) );
		shortdesc       = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "shortdesc"      , debugOutputPrefixStr) );
		longdesc        = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "longdesc"       , debugOutputPrefixStr) );
		genre           = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "genre"          , debugOutputPrefixStr) );
		genreid         =                               JSON_Data.getIntegerValue(object, "genreid"        , debugOutputPrefixStr);
		begin_timestamp =                               JSON_Data.getIntegerValue(object, "begin_timestamp", debugOutputPrefixStr);
		now_timestamp   =                               JSON_Data.getIntegerValue(object, "now_timestamp"  , debugOutputPrefixStr);
		duration_sec    =                               JSON_Data.getIntegerValue(object, "duration_sec"   , debugOutputPrefixStr);
		remaining       =                               JSON_Data.getIntegerValue(object, "remaining"      , debugOutputPrefixStr);
		sref            =                               JSON_Data.getStringValue (object, "sref"           , debugOutputPrefixStr);
		provider        = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "provider"       , true, false, debugOutputPrefixStr) );
		id              = JSON_Data.getValue(object, "id", false, JSON_Data.Value.Type.Integer, JSON_Data.Value::castToIntegerValue, true, debugOutputPrefixStr);
		id_null         = JSON_Data.getValue(object, "id", false, JSON_Data.Value.Type.Null   , JSON_Data.Value::castToNullValue   , true, debugOutputPrefixStr);
		if (id==null && id_null==null)
			throw new TraverseException("%s.id isn't either an IntegerValue or a NullValue", debugOutputPrefixStr);
	}
	
}