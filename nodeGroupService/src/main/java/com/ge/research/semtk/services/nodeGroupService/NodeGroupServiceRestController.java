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

package com.ge.research.semtk.services.nodeGroupService;

import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ge.research.semtk.belmont.AutoGeneratedQueryTypes;
import com.ge.research.semtk.belmont.NoValidSparqlException;
import com.ge.research.semtk.belmont.NodeGroup;
import com.ge.research.semtk.belmont.Returnable;
import com.ge.research.semtk.belmont.runtimeConstraints.RuntimeConstraintManager;
import com.ge.research.semtk.load.utility.SparqlGraphJson;
import com.ge.research.semtk.nodeGroupService.SparqlIdReturnedTuple;
import com.ge.research.semtk.nodeGroupService.SparqlIdTuple;
import com.ge.research.semtk.resultSet.SimpleResultSet;
import com.ge.research.semtk.resultSet.Table;
import com.ge.research.semtk.resultSet.TableResultSet;
import com.ge.research.semtk.services.nodeGroupService.requests.NodegroupRequest;
import com.ge.research.semtk.services.nodeGroupService.requests.NodegroupSparqlIdReturnedRequest;
import com.ge.research.semtk.services.nodeGroupService.requests.NodegroupSparqlIdTupleRequest;
import com.ge.research.semtk.services.nodeGroupService.requests.NodegroupSparqlIdRequest;
import com.ge.research.semtk.utility.LocalLogger;

@RestController
@RequestMapping("/nodeGroup")
@CrossOrigin
public class NodeGroupServiceRestController {
 	static final String SERVICE_NAME = "nodeGroupService";

	public static final String QUERYFIELDLABEL = "SparqlQuery";
	public static final String QUERYTYPELABEL = "QueryType";
	public static final String INVALID_SPARQL_RATIONALE_LABEL = "InvalidSparqlRationale";


	@CrossOrigin
	@RequestMapping(value= "/**", method=RequestMethod.OPTIONS)
	public void corsHeaders(HttpServletResponse response) {
	    response.addHeader("Access-Control-Allow-Origin", "*");
	    response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
	    response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
	    response.addHeader("Access-Control-Max-Age", "3600");
	}
	
	/*
	 * generate** endpoints return a SimpleResultSet
	 *     failure - unexpected exception 
	 *     		rationale - the exception message
	 *     no valid sparql - succeeds
	 *         SparqlQuery - is a short SPARQL comment
	 *         QueryType - "INVALID"
	 *     success -
	 *       	SparqlQuery - the SPARQL
	 *          QueryType - same "SELECT" "COUNT_ALL" "DELETE" "FILTER" "ASK" "CONSTRUCT"
	 */
	@CrossOrigin
	@RequestMapping(value="/generateSelect", method=RequestMethod.POST)
	public JSONObject generateSelectSparql(@RequestBody NodegroupRequest requestBody){
		SimpleResultSet retval = null;
		
		try{
			NodeGroup ng = requestBody.getNodeGroup();
			String query = ng.generateSparql(AutoGeneratedQueryTypes.QUERY_DISTINCT, false, null, null);
			
			retval = this.generateSuccessOutput("SELECT", query);
			
		}
		catch(NoValidSparqlException ise) {
			retval = this.generateNoValidSparqlOutput("generateSelect", ise.getMessage());
		}
		catch(Exception eee){
			retval = new SimpleResultSet(false);
			retval.addRationaleMessage(SERVICE_NAME, "generateSelect", eee);
			LocalLogger.printStackTrace(eee);
		}
		
		return retval.toJson();
	}
	
	@CrossOrigin
	@RequestMapping(value="/generateCountAll", method=RequestMethod.POST)
	public JSONObject generateCountAllSparql(@RequestBody NodegroupRequest requestBody){
		SimpleResultSet retval = null;
		
		try{
			NodeGroup ng = requestBody.getNodeGroup();
			String query = ng.generateSparql(AutoGeneratedQueryTypes.QUERY_COUNT, false, null, null);
			
			retval = this.generateSuccessOutput("COUNT_ALL", query);
			
		}
		catch(NoValidSparqlException ise) {
			retval = this.generateNoValidSparqlOutput("generateCountAll", ise.getMessage());
		}
		catch(Exception eee){
			retval = new SimpleResultSet(false);
			retval.addRationaleMessage(SERVICE_NAME, "generateCountAll", eee);
			LocalLogger.printStackTrace(eee);
		}
		
		return retval.toJson();
	}	
	
	@CrossOrigin
	@RequestMapping(value="/generateDelete", method=RequestMethod.POST)
	public JSONObject generateDeleteSparql(@RequestBody NodegroupRequest requestBody){
		SimpleResultSet retval = null;
		
		try{
			NodeGroup ng = requestBody.getNodeGroup();
			String query = ng.generateSparqlDelete(null);
			
			retval = this.generateSuccessOutput("DELETE", query);
			
		}
		catch(NoValidSparqlException ise) {
			retval = this.generateNoValidSparqlOutput("generateDelete", ise.getMessage());
		}
		catch(Exception eee){
			retval = new SimpleResultSet(false);
			retval.addRationaleMessage(SERVICE_NAME, "generateDelete", eee);
			LocalLogger.printStackTrace(eee);
		}
		
		return retval.toJson();
	}
	
	@CrossOrigin
	@RequestMapping(value="/generateFilter", method=RequestMethod.POST)
	public JSONObject generateFilterSparql(@RequestBody NodegroupSparqlIdRequest requestBody){
		SimpleResultSet retval = null;
		
		try{
			NodeGroup ng = requestBody.getNodeGroup();
			Returnable item = ng.getNodeBySparqlID(requestBody.getTargetObjectSparqlId());
			if (item == null) {
				item = ng.getPropertyItemBySparqlID(requestBody.getTargetObjectSparqlId());
			}
			String query = ng.generateSparql(AutoGeneratedQueryTypes.QUERY_CONSTRAINT, false, -1, item);
			
			retval = this.generateSuccessOutput("FILTER", query);
			
		}
		catch(NoValidSparqlException ise) {
			retval = this.generateNoValidSparqlOutput("generateFilter", ise.getMessage());
		}
		catch(Exception eee){
			retval = new SimpleResultSet(false);
			retval.addRationaleMessage(SERVICE_NAME, "generateFilter", eee);
			LocalLogger.printStackTrace(eee);
		}
		
		return retval.toJson();
	}
	
	@CrossOrigin
	@RequestMapping(value="/generateAsk", method=RequestMethod.POST)
	public JSONObject generateAskSparql(@RequestBody NodegroupRequest requestBody){
		SimpleResultSet retval = null;
		
		try{
			NodeGroup ng = requestBody.getNodeGroup();
			String query = ng.generateSparqlAsk();
			
			retval = this.generateSuccessOutput("ASK", query);
			
		}
		catch(NoValidSparqlException ise) {
			retval = this.generateNoValidSparqlOutput("generateAsk", ise.getMessage());
		}
		catch(Exception eee){
			retval = new SimpleResultSet(false);
			retval.addRationaleMessage(SERVICE_NAME, "generateAsk", eee);
			LocalLogger.printStackTrace(eee);
		}
		
		return retval.toJson();
	}
	
	@CrossOrigin
	@RequestMapping(value="/generateConstructForInstanceManipulation", method=RequestMethod.POST)
	public JSONObject generateConstructSparqlForInstanceManipulation(@RequestBody NodegroupRequest requestBody){
		SimpleResultSet retval = null;
		
		try{
			NodeGroup ng = requestBody.getNodeGroup();
			String query = ng.generateSparqlConstruct(true);
			
			retval = this.generateSuccessOutput("CONSTRUCT", query);
			
		}
		catch(NoValidSparqlException ise) {
			retval = this.generateNoValidSparqlOutput("generateConstruct", ise.getMessage());
		}
		catch(Exception eee){
			retval = new SimpleResultSet(false);
			retval.addRationaleMessage(SERVICE_NAME, "generateConstruct", eee);
			LocalLogger.printStackTrace(eee);
		}
		
		return retval.toJson();
	}
	
	@CrossOrigin
	@RequestMapping(value="/generateConstruct", method=RequestMethod.POST)
	public JSONObject generateConstructSparql(@RequestBody NodegroupRequest requestBody){
		SimpleResultSet retval = null;
		
		try{
			NodeGroup ng = requestBody.getNodeGroup();
			String query = ng.generateSparqlConstruct(false);
			
			retval = this.generateSuccessOutput("CONSTRUCT", query);
			
		}
		catch(NoValidSparqlException ise) {
			retval = this.generateNoValidSparqlOutput("generateConstruct", ise.getMessage());
		}
		catch(Exception eee){
			retval = new SimpleResultSet(false);
			retval.addRationaleMessage(SERVICE_NAME, "generateConstruct", eee);
			LocalLogger.printStackTrace(eee);
		}
		
		return retval.toJson();
	}
	
	@CrossOrigin
	@RequestMapping(value="/getRuntimeConstraints", method=RequestMethod.POST)
	public JSONObject getRuntimeConstraints(@RequestBody NodegroupRequest requestBody){
		TableResultSet retval = null;
		
		try{
			NodeGroup ng = requestBody.getNodeGroup();
			RuntimeConstraintManager rtci = new RuntimeConstraintManager(ng);
			retval = new TableResultSet(true); 
			
			// TODO: it is awful that this returns a table of descriptions
			//       the RunTimeConstrainedItems needs fromJson but it has a nodegroup pointer
			//       Serious re-design may be needed
			retval.addResults(rtci.getConstrainedItemsDescription() );
		}
		catch(Exception eee){
			retval = new TableResultSet(false);
			retval.addRationaleMessage(SERVICE_NAME, "getRuntimeConstraints", eee);
			LocalLogger.printStackTrace(eee);
		}
		
		return retval.toJson();		
	}
	
	@CrossOrigin
	@RequestMapping(value="/setIsReturned", method=RequestMethod.POST)
	public JSONObject setReturnsBySparqlId(@RequestBody NodegroupSparqlIdReturnedRequest requestBody){
		SimpleResultSet retval = new SimpleResultSet(false);
		
		try{
			requestBody.validate();
			
			SparqlGraphJson sgJson = requestBody.getSparqlGraphJson();
			NodeGroup ng = sgJson.getNodeGroup();
			
			for (SparqlIdReturnedTuple tuple : requestBody.getSparqlIdReturnedTuples()) {
				Returnable item = ng.getItemBySparqlID(tuple.getSparqlId());
				if (item == null) {
					throw new Exception("sparqlId was not found: " + tuple.getSparqlId());
				}
				item.setIsReturned(tuple.isReturned());
			}
			
			// put modified nodegroup back into sgJson and return
			sgJson.setNodeGroup(ng);
			retval.addResult("nodegroup", sgJson.toJson());
			retval.setSuccess(true);
		}
		catch(Exception e){
			retval.addRationaleMessage(SERVICE_NAME, "setIsReturned", e);
			retval.setSuccess(false);
			LocalLogger.printStackTrace(e);
		}
		
		return retval.toJson();		
	}
	
	@CrossOrigin
	@RequestMapping(value="/changeSparqlIds", method=RequestMethod.POST)
	public JSONObject renameItems(@RequestBody NodegroupSparqlIdTupleRequest requestBody){
		SimpleResultSet retval = new SimpleResultSet(false);
		
		try{
			requestBody.validate();
			
			SparqlGraphJson sgJson = requestBody.getSparqlGraphJson();
			NodeGroup ng = sgJson.getNodeGroup();
			
			
			for (SparqlIdTuple tuple : requestBody.getSparqlIdTuples()) {
				Returnable itemFrom = ng.getItemBySparqlID(tuple.getSparqlIdFrom());
				if (itemFrom == null) {
					throw new Exception("sparqlId was not found: " + tuple.getSparqlIdFrom());
				}
				String newId = ng.changeSparqlID(itemFrom, tuple.getSparqlIdTo());
				if (!newId.equals(tuple.getSparqlIdTo())) {
					throw new Exception("sparqlId is already in use: " + tuple.getSparqlIdTo() + ". System suggested: " + newId);
				}
			}
			
			// put modified nodegroup back into sgJson and return
			sgJson.setNodeGroup(ng);
			retval.addResult("nodegroup", sgJson.toJson());
			retval.setSuccess(true);
		}
		catch(Exception e){
			retval.addRationaleMessage(SERVICE_NAME, "changeSparqlIds", e);
			retval.setSuccess(false);
			LocalLogger.printStackTrace(e);
		}
		
		return retval.toJson();		
	}
	
	@CrossOrigin
	@RequestMapping(value="/getReturnedSparqlIds", method=RequestMethod.POST)
	public JSONObject getReturns(@RequestBody NodegroupRequest requestBody){
		TableResultSet retval = new TableResultSet(false);

		try {
			requestBody.validate();
			
			SparqlGraphJson sgJson = requestBody.getSparqlGraphJson();
			NodeGroup ng = sgJson.getNodeGroup();
			
			ArrayList<Returnable> items = ng.getReturnedItems();
			
			// create a new table of sparqlId strings
			Table table = new Table(new String[]{"sparqlId"}, new String[]{"string"});
			for (Returnable item : items) {
				table.addRow(new String[] { item.getSparqlID() } );
			}
			retval.addResults(table);
			retval.setSuccess(true);
		}
		catch (Exception e) {
			retval.addRationaleMessage(SERVICE_NAME, "getReturnedSparqlIds", e);
			retval.setSuccess(false);
			LocalLogger.printStackTrace(e);
		}

		return retval.toJson();		
	}
	
	/*
	 * SPARQL can't be generated. 
	 * e.g. SELECT DISTINCT but nothing isReturned in the nodegroup
	 */
	private SimpleResultSet generateNoValidSparqlOutput(String endpoint, String message) {
		// Failure SimpleResultSet
		SimpleResultSet retval = new SimpleResultSet(false);
		retval.addRationaleMessage(SERVICE_NAME, endpoint, "SPARQL query can't be generated from this nodegroup");

		// InvalidSparqlRationale
		retval.addResult(INVALID_SPARQL_RATIONALE_LABEL, message);
		
		// Query type "Invalid"
		retval.addResult(QUERYTYPELABEL, "INVALID");
		
		return retval;
	}
	
	private SimpleResultSet generateSuccessOutput(String queryType, String query) throws Exception {
		SimpleResultSet retval = new SimpleResultSet(true);
		retval.addResult(QUERYFIELDLABEL, query);
		retval.addResult(QUERYTYPELABEL, queryType);
		
		return retval;
	}
}
