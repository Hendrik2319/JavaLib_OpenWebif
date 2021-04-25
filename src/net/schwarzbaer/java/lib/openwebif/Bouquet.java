package net.schwarzbaer.java.lib.openwebif;

import java.util.Vector;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Array;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.BouquetReadInterface;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class Bouquet {

	public final String name;
	public final String servicereference;
	public final Vector<SubService> subservices;

	public Bouquet(String name, String servicereference) {
		this.name = name;
		this.servicereference = servicereference;
		this.subservices = new Vector<>();
	}

	static Bouquet parse(JSON_Object<NV,V> object, BouquetReadInterface bouquetReadInterface) {
		if (object==null) return null;
		String servicereference       =                    JSON_Data.getStringValue(object.getValue("servicereference"));
		String servicename = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue(object.getValue("servicename"     )) );
		JSON_Array<NV,V> subservices  =                    JSON_Data.getArrayValue (object.getValue("subservices"     ));
		if (subservices==null) return null;
		
		Bouquet bouquet = new Bouquet(servicename,servicereference);
		for (JSON_Data.Value<NV,V> subserviceValue:subservices) {
			SubService subservice = SubService.parse(JSON_Data.getObjectValue(subserviceValue),bouquetReadInterface);
			if (subservice!=null) bouquet.subservices.add(subservice);
		}
		
		return bouquet;
	}

	@Override
	public String toString() {
		if (name!=null) return name;
		if (servicereference!=null) return servicereference;
		return String.format("Bouquet [%d SubServices]", subservices.size());
	}
	
	public SubService getSubService(int i) {
		if (i<0 || i>=subservices.size()) return null;
		return subservices.get(i);
	}
	
	public static class SubService {

		public final Service service;
		final String  servicereference;
		final Long    program;
		final String  name;
		public final Long    pos;

		public SubService(Service service, String servicereference, Long program, String name, Long pos) {
			this.service = service;
			this.servicereference = servicereference;
			this.program = program;
			this.name = name;
			this.pos = pos;
		}

		public static SubService parse(JSON_Object<NV,V> object, BouquetReadInterface bouquetReadInterface) {
			if (object==null) return null;
			String servicereference =                          JSON_Data.getStringValue (object.getValue("servicereference"));
			Long   program          =                          JSON_Data.getIntegerValue(object.getValue("program"         ));
			String servicename = OpenWebifTools.decodeUnicode( JSON_Data.getStringValue (object.getValue("servicename"     )) );
			Long   pos              =                          JSON_Data.getIntegerValue(object.getValue("pos"             ));
			Service service = null;
			if (servicereference!=null) service = Service.parseServiceStr(servicereference);
			if (service==null) return null;
			
			if (bouquetReadInterface!=null)
				bouquetReadInterface.addStationName(service.stationID,servicename);
			
			return new SubService(service,servicereference,program,servicename,pos);
		}
	}}