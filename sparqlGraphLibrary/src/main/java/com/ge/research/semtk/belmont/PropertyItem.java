/**
 ** Copyright 2016 General Electric Company
 **
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 ** 
 **     http://www.apache.org/licenses/LICENSE-2.0
 ** 
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */


package com.ge.research.semtk.belmont;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ge.research.semtk.belmont.Returnable;
import com.ge.research.semtk.belmont.ValueConstraint;

public class PropertyItem extends Returnable {
	
	private String keyName = null;
	private String valueType = null;
	private String valueTypeURI = null;  
	private String uriRelationship = null; // the full URI of the relationship
	private ValueConstraint constraints = null;
	private String fullURIName = null;
	private Boolean isOptional = false;
	private ArrayList<String> instanceValues = new ArrayList<String>();
	
	/**
	 * Constructor
	 * @param nome (e.g. pasteMaterial)
	 * @param valueType (e.g. string)
	 * @param valueTypeURI (e.g. http://www.w3.org/2001/XMLSchema#string)
	 * @param uriRelationship (e.g. http://research.ge.com/sofc/testconfig#pasteMaterial)
	 */
	public PropertyItem(String nome, String valueType, String valueTypeURI, String uriRelationship){
		this.keyName = nome;
		this.valueType = valueType;
		this.valueTypeURI = valueTypeURI;
		this.uriRelationship = uriRelationship;
	}
		
	public PropertyItem(JSONObject next) {
		// keeps track of the properties who are in the domain of a given node.
		
		this.keyName = next.get("KeyName").toString();
		this.valueType = next.get("ValueType").toString();
		this.valueTypeURI = next.get("relationship").toString();  // note that label "relationship" in the JSON is misleading
		this.uriRelationship = next.get("UriRelationship").toString();
		
		String vStr = (String) next.get("Constraints");
		if (vStr != null && ! vStr.isEmpty()) { 
			this.constraints = new ValueConstraint(vStr); 
		} else {
			this.constraints = null;
		}
		
		this.fullURIName = (String) next.get("fullURIName");
		this.sparqlID = (String) next.get("SparqlID");
		this.isOptional = (Boolean)next.get("isOptional");
		this.isReturned = (Boolean)next.get("isReturned");
		
		JSONArray instArr = (JSONArray)next.get("instanceValues");
		Iterator<String> it = instArr.iterator();
		while(it.hasNext()){
			this.instanceValues.add(it.next());
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONArray iVals = new JSONArray();
		for (int i=0; i < this.instanceValues.size(); i++) {
			iVals.add(this.instanceValues.get(i));
		}
		
		JSONObject ret = new JSONObject();
		ret.put("KeyName", this.keyName);
		ret.put("ValueType", this.valueType);
		ret.put("relationship", this.valueTypeURI);
		ret.put("UriRelationship", this.uriRelationship);
		ret.put("Constraints", this.constraints != null ? this.constraints.toString() : "");
		ret.put("fullURIName", this.fullURIName);
		ret.put("SparqlID", this.sparqlID);
		ret.put("isReturned", this.isReturned);
		ret.put("isOptional", this.isOptional);
		ret.put("instanceValues", iVals);

		return ret;
	}

	public boolean getIsOptional() {
		return this.isOptional;
	}
	
	public String getKeyName() {
		return this.keyName;
	}
	
	public String getUriRelationship() {
		return this.uriRelationship;
	}

	public String getConstraints() {
		if (constraints != null) {
			String constraintStr =  this.constraints.getConstraint();
			constraintStr = constraintStr.replaceAll("%id", this.sparqlID);
			return constraintStr;
		}
		else {
			return null;
		}
		
	}

	public ArrayList<String> getInstanceValues() {
		return this.instanceValues;
	}

	public String getValueType() {
		return this.valueType;
	}

	public void addInstanceValue(String value) {
		this.instanceValues.add(value);
	}
	
	public void setIsReturned(boolean b){
		this.isReturned = b;
	}
	
	public void addConstraint(String str) {
		this.constraints = new ValueConstraint(str);
	}


}
