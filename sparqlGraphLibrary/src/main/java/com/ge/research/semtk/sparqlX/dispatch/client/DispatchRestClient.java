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

package com.ge.research.semtk.sparqlX.dispatch.client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.ge.research.semtk.belmont.AutoGeneratedQueryTypes;
import com.ge.research.semtk.belmont.NodeGroup;
import com.ge.research.semtk.resultSet.SimpleResultSet;
import com.ge.research.semtk.resultSet.Table;
import com.ge.research.semtk.resultSet.TableResultSet;
import com.ge.research.semtk.services.client.RestClient;
import com.ge.research.semtk.sparqlX.SparqlConnection;
import com.ge.research.semtk.sparqlX.dispatch.QueryFlags;
import com.ge.research.semtk.utility.LocalLogger;

public class DispatchRestClient extends RestClient{

	@Override
	public void buildParametersJSON() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEmptyResponse() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public DispatchRestClient (DispatchClientConfig config) {
		this.conf = config;
	}
	
	
	public SimpleResultSet executeSelectQueryFromNodeGroup(JSONObject nodeGroupWithConnection, JSONObject externalConstraints, QueryFlags flags) throws Exception{
		SimpleResultSet retval = null;
		
		// in the event a null set of constraints was passed, create the minimally valid set.
		// they are json formatted like this:
		// {"@constraintSet":{"@op":"AND","@constraints":[]}}
		
		if(externalConstraints == null){
			JSONParser jParse = new JSONParser();
			externalConstraints = (JSONObject) jParse.parse("{\"@constraintSet\":{\"@op\":\"AND\",\"@constraints\":[]}}");	
		}
		
		// setup the arguments we intend to send.
		conf.setServiceEndpoint("dispatcher/querySelectFromNodeGroup");
		this.parametersJSON.put("jsonRenderedNodeGroup", nodeGroupWithConnection.toJSONString());
		this.parametersJSON.put("constraintSet", externalConstraints.toJSONString());
		if (flags != null) {
			this.parametersJSON.put("flags", flags.toJSONString());
		}
		
		try{
			retval = SimpleResultSet.fromJson((JSONObject) this.execute());
			retval.throwExceptionIfUnsuccessful();
		} 
		finally {
			// reset conf and parametersJSON
			conf.setServiceEndpoint(null);
			this.parametersJSON.remove("jsonRenderedNodeGroup");
			this.parametersJSON.remove("constraintSet");
			this.parametersJSON.remove("flags");
		}
		
		return retval;
	}
	
	public SimpleResultSet executeConstructQueryFromNodeGroup(JSONObject nodeGroupWithConnection, JSONObject externalConstraints) throws Exception{
		SimpleResultSet retval = null;
		
		// in the event a null set of constraints was passed, create the minimally valid set.
		// they are json formatted like this:
		// {"@constraintSet":{"@op":"AND","@constraints":[]}}
		
		if(externalConstraints == null){
			JSONParser jParse = new JSONParser();
			externalConstraints = (JSONObject) jParse.parse("{\"@constraintSet\":{\"@op\":\"AND\",\"@constraints\":[]}}");	
		}
		
		// setup the arguments we intend to send.
		conf.setServiceEndpoint("dispatcher/queryConstructFromNodeGroup");
		this.parametersJSON.put("jsonRenderedNodeGroup", nodeGroupWithConnection.toJSONString());
		this.parametersJSON.put("constraintSet", externalConstraints.toJSONString());
		
		try{
			retval = SimpleResultSet.fromJson((JSONObject) this.execute());
			retval.throwExceptionIfUnsuccessful();
		} 
		finally {
			// reset conf and parametersJSON
			conf.setServiceEndpoint(null);
			this.parametersJSON.remove("jsonRenderedNodeGroup");
			this.parametersJSON.remove("constraintSet");
		}
		
		return retval;
	}

	public SimpleResultSet executeConstructQueryForInstanceManipulationFromNodeGroup(JSONObject nodeGroupWithConnection, JSONObject externalConstraints) throws Exception{
		SimpleResultSet retval = null;
		
		// in the event a null set of constraints was passed, create the minimally valid set.
		// they are json formatted like this:
		// {"@constraintSet":{"@op":"AND","@constraints":[]}}
		
		if(externalConstraints == null){
			JSONParser jParse = new JSONParser();
			externalConstraints = (JSONObject) jParse.parse("{\"@constraintSet\":{\"@op\":\"AND\",\"@constraints\":[]}}");	
		}
		
		// setup the arguments we intend to send.
		conf.setServiceEndpoint("dispatcher/queryConstructFromNodeGroupForInstanceManipulation");
		this.parametersJSON.put("jsonRenderedNodeGroup", nodeGroupWithConnection.toJSONString());
		this.parametersJSON.put("constraintSet", externalConstraints.toJSONString());
		
		try{
			retval = SimpleResultSet.fromJson((JSONObject) this.execute());
			retval.throwExceptionIfUnsuccessful();
		} 
		finally {
			// reset conf and parametersJSON
			conf.setServiceEndpoint(null);
			this.parametersJSON.remove("jsonRenderedNodeGroup");
			this.parametersJSON.remove("constraintSet");
		}
		
		return retval;
	}
	
	public SimpleResultSet executeCountQueryFromNodeGroup(JSONObject nodeGroupWithConnection, JSONObject externalConstraints) throws Exception{
		SimpleResultSet retval = null;
		
		// in the event a null set of constraints was passed, create the minimally valid set.
		// they are json formatted like this:
		// {"@constraintSet":{"@op":"AND","@constraints":[]}}
		
		if(externalConstraints == null){
			JSONParser jParse = new JSONParser();
			externalConstraints = (JSONObject) jParse.parse("{\"@constraintSet\":{\"@op\":\"AND\",\"@constraints\":[]}}");	
		}
		
		// setup the arguments we intend to send.
		conf.setServiceEndpoint("dispatcher/queryCountFromNodeGroup");
		this.parametersJSON.put("jsonRenderedNodeGroup", nodeGroupWithConnection.toJSONString());
		this.parametersJSON.put("constraintSet", externalConstraints.toJSONString());
		
		try{
			retval = SimpleResultSet.fromJson((JSONObject) this.execute());
			retval.throwExceptionIfUnsuccessful();
		} 
		finally {
			// reset conf and parametersJSON
			conf.setServiceEndpoint(null);
			this.parametersJSON.remove("jsonRenderedNodeGroup");
			this.parametersJSON.remove("constraintSet");
		}
		
		return retval;
	}
	
	public SimpleResultSet executeDeleteQueryFromNodeGroup(JSONObject nodeGroupWithConnection, JSONObject externalConstraints) throws Exception{
		SimpleResultSet retval = null;
		
		// in the event a null set of constraints was passed, create the minimally valid set.
		// they are json formatted like this:
		// {"@constraintSet":{"@op":"AND","@constraints":[]}}
		
		if(externalConstraints == null){
			JSONParser jParse = new JSONParser();
			externalConstraints = (JSONObject) jParse.parse("{\"@constraintSet\":{\"@op\":\"AND\",\"@constraints\":[]}}");	
		}
		
		// setup the arguments we intend to send.
		conf.setServiceEndpoint("dispatcher/queryDeleteFromNodeGroup");
		this.parametersJSON.put("jsonRenderedNodeGroup", nodeGroupWithConnection.toJSONString());
		this.parametersJSON.put("constraintSet", externalConstraints.toJSONString());
		
		
		try{
			retval = SimpleResultSet.fromJson((JSONObject) this.execute());
			retval.throwExceptionIfUnsuccessful();
		} 
		finally {
			// reset conf and parametersJSON
			conf.setServiceEndpoint(null);
			this.parametersJSON.remove("jsonRenderedNodeGroup");
			this.parametersJSON.remove("constraintSet");
		}
		
		return retval;
	}
	
	public SimpleResultSet executeFilterQueryFromNodeGroup(JSONObject nodeGroupWithConnection, String targetObjectSparqlId, JSONObject externalConstraints) throws Exception{
		SimpleResultSet retval = null;
		
		// in the event a null set of constraints was passed, create the minimally valid set.
		// they are json formatted like this:
		// {"@constraintSet":{"@op":"AND","@constraints":[]}}
		
		if(externalConstraints == null){
			JSONParser jParse = new JSONParser();
			externalConstraints = (JSONObject) jParse.parse("{\"@constraintSet\":{\"@op\":\"AND\",\"@constraints\":[]}}");	
		}
		
		// setup the arguments we intend to send.
		conf.setServiceEndpoint("dispatcher/queryFilterFromNodeGroup");
		this.parametersJSON.put("jsonRenderedNodeGroup", nodeGroupWithConnection.toJSONString());
		this.parametersJSON.put("constraintSet", externalConstraints.toJSONString());
		this.parametersJSON.put("targetObjectSparqlID", targetObjectSparqlId);
		
		
		try{
			retval = SimpleResultSet.fromJson((JSONObject) this.execute());
			retval.throwExceptionIfUnsuccessful();
		} 
		finally {
			// reset conf and parametersJSON
			conf.setServiceEndpoint(null);
			this.parametersJSON.remove("jsonRenderedNodeGroup");
			this.parametersJSON.remove("constraintSet");
			this.parametersJSON.remove("targetObjectSparqlID");
		}
		
		return retval;
	}
	
	public SimpleResultSet executeRawSparqlQuery(SparqlConnection sc, String rawSparqlQuery) throws Exception{
		SimpleResultSet retval = null;
		
		// setup the arguments we intend to send.
		conf.setServiceEndpoint("dispatcher/asynchronousDirectQuery");
		this.parametersJSON.put("sparqlConnectionJson", sc.toJson().toJSONString());
		this.parametersJSON.put("rawSparqlQuery", rawSparqlQuery );
	
		
		LocalLogger.logToStdErr("-- the outgoing connection json was: ---");
		LocalLogger.logToStdErr(sc.toJson().toString());
		LocalLogger.logToStdErr("-- the outgoing connection json closed ---");
		
		try{
			retval = SimpleResultSet.fromJson((JSONObject) this.execute());
			retval.throwExceptionIfUnsuccessful();
		} 
		finally {
			// reset conf and parametersJSON
			conf.setServiceEndpoint(null);
			this.parametersJSON.remove("sparqlConnectionJson");
			this.parametersJSON.remove("rawSparqlQuery");
		}
		
		return retval;
	}

	public TableResultSet executeGetConstraintInfo(JSONObject nodeGroup) throws Exception{
		TableResultSet retval = new TableResultSet();
		
		conf.setServiceEndpoint("dispatcher/getConstraintInfo");
		this.parametersJSON.put("jsonRenderedNodeGroup", nodeGroup.toJSONString());
		
		try{
			JSONObject jobj = (JSONObject) this.execute();
			retval.readJson(jobj);
			retval.throwExceptionIfUnsuccessful();
		}
		finally{
			// reset conf and parametersJSON
			conf.setServiceEndpoint(null);
			this.parametersJSON.remove("jsonRenderedNodeGroup");
		}
		
		return retval;
	}
}