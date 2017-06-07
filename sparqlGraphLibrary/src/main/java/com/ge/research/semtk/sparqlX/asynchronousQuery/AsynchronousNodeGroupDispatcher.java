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

package com.ge.research.semtk.sparqlX.asynchronousQuery;

import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.ge.research.semtk.belmont.AutoGeneratedQueryTypes;
import com.ge.research.semtk.belmont.NodeGroup;
import com.ge.research.semtk.edc.client.EndpointNotFoundException;
import com.ge.research.semtk.edc.client.ResultsClient;
import com.ge.research.semtk.edc.client.StatusClient;
import com.ge.research.semtk.load.utility.SparqlGraphJson;
import com.ge.research.semtk.ontologyTools.OntologyInfo;
import com.ge.research.semtk.resultSet.Table;
import com.ge.research.semtk.resultSet.TableResultSet;
import com.ge.research.semtk.sparqlX.SparqlConnection;
import com.ge.research.semtk.sparqlX.SparqlEndpointInterface;
import com.ge.research.semtk.sparqlX.SparqlResultTypes;
import com.ge.research.semtk.sparqlX.client.SparqlQueryClient;

public class AsynchronousNodeGroupDispatcher extends AsynchronousNodeGroupBasedQueryDispatcher {
private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	protected static final int MAX_NUMBER_SIMULTANEOUS_QUERIES_PER_USER = 50;  // maybe move this to a configured value?
	

	
	/**
	 * create a new instance of the AsynchronousNodeGroupExecutor.
	 * @param encodedNodeGroup
	 * @throws Exception
	 */
	public AsynchronousNodeGroupDispatcher(String jobId, JSONObject encodedNodeGroupWithConnection, ResultsClient rClient, StatusClient sClient, SparqlQueryClient queryClient) throws Exception{
		super(jobId, encodedNodeGroupWithConnection, rClient, sClient, queryClient);
		
	}
	
	/**
	 * return the JobID. the clients will need this.
	 * @return
	 */
	public String getJobId(){
		return this.jobID;
	}
	
	
	public TableResultSet execute() throws Exception{
		
		TableResultSet retval = null; // expect this to get instantiated with the appropriate subclass.		
		
		try{
		Calendar cal = Calendar.getInstance();
		System.err.println("Job " + this.jobID + ": AsynchronousNodeGroupExecutor start @ " + DATE_FORMAT.format(cal.getTime()));

		retval = this.executePlainSparqlQuery();
	
		cal = Calendar.getInstance();
		System.err.println("Job " + this.jobID + ": AsynchronousNodeGroupExecutor end   @ " + DATE_FORMAT.format(cal.getTime()));

		}
		catch(Exception e){
				// something went awry. set the job to failure. 
				this.updateStatusToFailed(e.getMessage());
				e.printStackTrace();
				throw new Exception("Query failed: " + e.getMessage() );
		}
		
		// ship the results.
		return retval;
	}

	protected TableResultSet executePlainSparqlQuery() throws Exception{
		TableResultSet retval = null;
		SparqlQueryClient nodegroupQueryClient = this.retrievalClient;
		
		
		try{

			String sparqlQuery = this.queryNodeGroup.generateSparql(AutoGeneratedQueryTypes.QUERY_DISTINCT, false, null, null, false);
		
			System.err.println("Sparql Query to execute: ");
			System.err.println(sparqlQuery);
			
			// run the actual query and get a result. 
			retval = (TableResultSet) nodegroupQueryClient.execute(sparqlQuery, SparqlResultTypes.TABLE);	
			
			if (retval.getSuccess()) {
				System.err.println("Query returned " + retval.getTable().getNumRows() + " results.");
				
				System.err.println("about to write results for " + this.jobID);
				this.sendResultsToService(retval);
				
				this.updateStatus(100);		// work's done
			}
			else {
				this.updateStatusToFailed("Query client returned error to dispatch client: \n" + retval.getRationaleAsString("\n"));
			}
		}
		catch(Exception e){
				// something went awry. set the job to failure. 
				this.updateStatusToFailed(e.getMessage());
				e.printStackTrace();
				throw new Exception("Query failed: " + e.getMessage() );
		}
		
		return retval;
	}

	@Override
	public TableResultSet execute(Object ExecutionSpecificConfigObject) throws Exception {
		
		return this.execute();
		
	}

	@Override
	public String getConstraintType() {
		// not supported in this sub-type.
		return null;
	}

	@Override
	public String[] getConstraintVariableNames() throws Exception {
		// not supported in this sub-type.
		return null;
	}
	

}
