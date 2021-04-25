package net.schwarzbaer.java.lib.openwebif;

import java.util.Vector;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Array;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.TraverseException;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.Value;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class MovieList {
	
	/*
	    Block "MovieList" [0]
	        <Base>:Object
	    Block "MovieList.<Base>" [3]
	        bookmarks:Array
	        bookmarks[]:String
	        directory:String
	        movies:Array
	        movies[]:Object
	 */

	public final String directory;
	public final Vector<String> bookmarks;
	public final Vector<Movie> movies;
	
	public MovieList(Value<NV, V> result) throws TraverseException {
		//JSON_Helper.OptionalValues<NV, V> optionalValueScan = new JSON_Helper.OptionalValues<NV,V>();
		//optionalValueScan.scan(result, "MovieList");
		//optionalValueScan.show(System.out);
		
		String debugOutputPrefixStr = "MovieList";
		JSON_Object<NV, V> object = JSON_Data.getObjectValue(result, debugOutputPrefixStr);
		
		directory = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue(object, "directory", debugOutputPrefixStr) );
		JSON_Array<NV, V> bookmarks = JSON_Data.getArrayValue(object, "bookmarks", debugOutputPrefixStr);
		JSON_Array<NV, V> movies    = JSON_Data.getArrayValue(object, "movies", debugOutputPrefixStr);
		
		this.bookmarks = new Vector<>();
		for (int i=0; i<bookmarks.size(); i++) {
			String str = JSON_Data.getStringValue(bookmarks.get(i), debugOutputPrefixStr+".bookmarks["+i+"]");
			this.bookmarks.add( OpenWebifTools.decodeUnicode( str ) );
		}
		
		this.movies = new Vector<>();
		for (int i=0; i<movies.size(); i++)
			this.movies.add(new Movie(movies.get(i), debugOutputPrefixStr+".movies["+i+"]"));
	}
	
	public static class Movie {
		public final String begintime;
		public final String description;
		public final String descriptionExtended;
		public final String eventname;
		public final String filename;
		public final String filename_stripped;
		public final long filesize;
		public final String filesize_readable;
		public final String fullname;
		public final long lastseen;
		public final String lengthStr;
		public final long recordingtime;
		public final String servicename;
		public final String serviceref;
		public final String tags;
		public final Integer length_s;

		/*		
		    Block "MovieList.<Base>.movies[]" [15]
		        begintime:String
		        description:String
		        descriptionExtended:String
		        eventname:String
		        filename:String
		        filename_stripped:String
		        filesize:Integer
		        filesize_readable:String
		        fullname:String
		        lastseen:Integer
		        length:String
		        recordingtime:Integer
		        servicename:String
		        serviceref:String
		        tags:String
		 */

		public Movie(Value<NV, V> value, String debugOutputPrefixStr) throws TraverseException {
			JSON_Object<NV, V> object = JSON_Data.getObjectValue(value, debugOutputPrefixStr);
			
			begintime           = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "begintime"          , debugOutputPrefixStr) ); 
			description         = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "description"        , debugOutputPrefixStr) );
			descriptionExtended = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "descriptionExtended", debugOutputPrefixStr) );
			eventname           = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "eventname"          , debugOutputPrefixStr) );
			filename            = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "filename"           , debugOutputPrefixStr) );
			filename_stripped   = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "filename_stripped"  , debugOutputPrefixStr) );
			filesize            =                               JSON_Data.getIntegerValue(object, "filesize"           , debugOutputPrefixStr)  ;
			filesize_readable   = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "filesize_readable"  , debugOutputPrefixStr) );
			fullname            = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "fullname"           , debugOutputPrefixStr) );
			lastseen            =                               JSON_Data.getIntegerValue(object, "lastseen"           , debugOutputPrefixStr)  ;
			lengthStr           = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "length"             , debugOutputPrefixStr) );
			recordingtime       =                               JSON_Data.getIntegerValue(object, "recordingtime"      , debugOutputPrefixStr)  ;
			servicename         = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "servicename"        , debugOutputPrefixStr) );
			serviceref          = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "serviceref"         , debugOutputPrefixStr) );
			tags                = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object, "tags"               , debugOutputPrefixStr) );
			
			length_s = parseLength(lengthStr);
		}

		private static Integer parseLength(String lengthStr) {
			if (lengthStr==null) return null;
			lengthStr = lengthStr.trim();
			
			int pos = lengthStr.indexOf(':');
			if (pos<0) return parseInt(lengthStr);
			
			Integer min = parseInt(lengthStr.substring(0, pos));
			Integer sec = parseInt(lengthStr.substring(pos+1));
			if (min==null || sec==null) return null;
			
			int sign = min<0 ? -1 : 1;
			return sign * (Math.abs(min)*60 + Math.abs(sec));
		}

		private static Integer parseInt(String str) {
			str = str.trim();
			try {
				int n = Integer.parseInt(str);
				String newStr = String.format("%0"+str.length()+"d", n);
				if (newStr.equals(str)) return n;
			} catch (NumberFormatException e) {}
			return null;
		}
		
	}
}