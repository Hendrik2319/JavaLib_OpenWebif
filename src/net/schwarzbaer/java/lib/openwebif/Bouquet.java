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
		String servicereference       = JSON_Data.getStringValue(object.getValue("servicereference"));
		String servicename            = JSON_Data.getStringValue(object.getValue("servicename"));
		JSON_Array<NV,V> subservices  = JSON_Data.getArrayValue (object.getValue("subservices"));
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
	
}