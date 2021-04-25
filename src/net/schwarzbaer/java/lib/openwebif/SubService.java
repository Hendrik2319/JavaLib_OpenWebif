package net.schwarzbaer.java.lib.openwebif;

import net.schwarzbaer.java.lib.jsonparser.JSON_Data;
import net.schwarzbaer.java.lib.jsonparser.JSON_Data.JSON_Object;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.BouquetReadInterface;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.NV;
import net.schwarzbaer.java.lib.openwebif.OpenWebifTools.V;

public class SubService {

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
		String servicereference = JSON_Data.getStringValue (object.getValue("servicereference"));
		Long   program          = JSON_Data.getIntegerValue(object.getValue("program"));
		String servicename      = JSON_Data.getStringValue (object.getValue("servicename"));
		Long   pos              = JSON_Data.getIntegerValue(object.getValue("pos"));
		Service service = null;
		if (servicereference!=null) service = Service.parseServiceStr(servicereference);
		if (service==null) return null;
		
		if (bouquetReadInterface!=null)
			bouquetReadInterface.addStationName(service.stationID,servicename);
		
		return new SubService(service,servicereference,program,servicename,pos);
	}
}