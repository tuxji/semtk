/**
 ** Copyright 2020 General Electric Company
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

package com.ge.research.semtk.fdccache;

import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;

import org.json.simple.JSONObject;

import com.ge.research.semtk.api.nodeGroupExecution.client.NodeGroupExecutionClient;
import com.ge.research.semtk.auth.AuthorizationException;
import com.ge.research.semtk.auth.AuthorizationManager;
import com.ge.research.semtk.auth.HeaderTable;
import com.ge.research.semtk.auth.ThreadAuthenticator;
import com.ge.research.semtk.belmont.runtimeConstraints.RuntimeConstraintManager;
import com.ge.research.semtk.edc.JobTracker;
import com.ge.research.semtk.edc.client.OntologyInfoClient;
import com.ge.research.semtk.fdc.FdcClient;
import com.ge.research.semtk.fdc.FdcClientConfig;
import com.ge.research.semtk.load.utility.SparqlGraphJson;
import com.ge.research.semtk.nodeGroupStore.client.NodeGroupStoreRestClient;
import com.ge.research.semtk.resultSet.Table;
import com.ge.research.semtk.resultSet.TableResultSet;
import com.ge.research.semtk.sparqlX.SparqlConnection;
import com.ge.research.semtk.sparqlX.SparqlEndpointInterface;
import com.ge.research.semtk.sparqlX.SparqlToXUtils;
import com.ge.research.semtk.utility.LocalLogger;
import com.ge.research.semtk.utility.Utility;

public class FdcCacheSpecRunner extends Thread {
	static boolean firstContruct = true;
	
	String specId = null;
	SparqlConnection conn = null;
	Table specTable = null;
	NodeGroupExecutionClient ngExecClient = null;
	NodeGroupStoreRestClient ngStoreClient = null;
	SparqlEndpointInterface servicesSei = null;
	HeaderTable headerTable = null;
	JobTracker tracker = null;
	String jobId = null;
	int curStep = 0;
	Table bootstrapTable = null;
	long maxEpochSeconds = 0;
	
	public FdcCacheSpecRunner(String specId, SparqlConnection conn, long maxEpochSec, SparqlEndpointInterface servicesSei, OntologyInfoClient oInfoClient, NodeGroupExecutionClient ngExecClient, NodeGroupStoreRestClient ngStoreClient) throws Exception {
		this.specId = specId;
		this.conn = conn;
		this.maxEpochSeconds = maxEpochSec;
		this.specTable = this.loadSpecTable(specId, servicesSei, oInfoClient);
		this.servicesSei = servicesSei;
		this.ngExecClient = ngExecClient;
		this.ngStoreClient = ngStoreClient;
		this.headerTable = ThreadAuthenticator.getThreadHeaderTable();
		this.tracker = new JobTracker(servicesSei);
		
		
		// establish the job
		this.jobId = JobTracker.generateJobId();
		this.tracker.createJob(jobId);
	}

	/**
	 * Load specId from services graph
	 * @param specId
	 * @param servicesSei
	 * @param oInfoClient
	 * @return
	 * @throws Exception
	 */
	private Table loadSpecTable(String specId, SparqlEndpointInterface servicesSei, OntologyInfoClient oInfoClient) throws Exception {

		// load the owl to services graph if needed, so that nodegroups will work
		if (FdcCacheSpecRunner.firstContruct) {
			
			InputStream owlStream = FdcCacheSpecRunner.class.getResourceAsStream("/semantics/OwlModels/fdcCacheSpec.owl");
			try {
				AuthorizationManager.setSemtkSuper();
				servicesSei.uploadOwlModelIfNeeded(owlStream);
			} finally {
				AuthorizationManager.clearSemtkSuper();
			}
			owlStream.close();
			FdcCacheSpecRunner.firstContruct = false;
		}
		
		// run query
		Table ret = null;
		try {
			AuthorizationManager.setSemtkSuper();
			
			ret = SparqlGraphJson.executeSelectToTable(
						Utility.getResourceAsJson(servicesSei, "/nodegroups/GetFdcCacheSpec.json"), 
						new SparqlConnection("services", servicesSei), 
						oInfoClient,
						"?id", "=", this.specId);
		} finally {
			AuthorizationManager.clearSemtkSuper();
		}
		
		// check success and return
		if (ret.getNumRows() == 0) {
			throw new Exception("Can not find cache spec named: " + specId);
		}
		return ret;
	}
	
	public int getNumSteps() {
		return this.specTable.getNumRows();
	}

	public String getJobId() {
		return jobId;
	}

	private String getStepInputNg() throws Exception {
		return this.specTable.getCell(this.curStep, "inputNodegroupId");
	}
	
	private String getStepServiceUrl() throws Exception {
		return this.specTable.getCell(this.curStep, "serviceURL");
	}
	
	private String getStepIngestNgId() throws Exception {
		return this.specTable.getCell(this.curStep, "ingestNodeGroupId");
	}
	
	private FdcClient getStepFdcClient(Table t) throws Exception {
		String endpoint = this.getStepServiceUrl();
		HashMap<String,Table> tableHashMap = new HashMap<String,Table>();
		tableHashMap.put("1", t);
		return new FdcClient(FdcClientConfig.fromFullEndpoint(endpoint, tableHashMap));
	}
	
	public void setBootstrapTable(Table bootstrapTable) throws Exception {
		this.bootstrapTable = bootstrapTable;
		
	}
	
	/**
	 * Check if data is already cached to this sei (connection's dataInterface 0)
	 * If so and recent enough, return true
	 * If so and too old, clear data graph and insert triples for hash defining cache and epoch
	 * If graph is empty then ................ insert triples for hash defining cache and epoch
	 * 
	 * Failures (dis-allowed use of cache)
	 *  - storing two caches in same graph
	 *  - requesting cache into non-empty graph
	 * 
	 * @return boolean - is cache fine as-is, or job failed
	 * @throws Exception
	 */
	private boolean checkAlreadyCached() throws Exception {
		final String SUBJECT = "http://fdc/cache#info";
		final String PRED_HASH = "http://fdc/cache#hash";
		final String PRED_EPOCH = "http://fdc/cache#epoch";
		String storedHash = null;
		long storedEpoch = 0;
		long nowEpoch = Instant.now().getEpochSecond();
		SparqlEndpointInterface sei = this.conn.getDataInterface(0);
		String bootstrapHash = this.specId + ":" + this.bootstrapTable.hashMD5();
		
		// borrow password from services graph
		sei.setUserAndPassword(this.servicesSei.getUserName(), this.servicesSei.getPassword());
		
		// get stored info if any
		Table infoTab = sei.executeQueryToTable(SparqlToXUtils.generateSelectBySubjectQuery(sei, "<" + SUBJECT + ">"));
		for (int i=0; i < infoTab.getNumRows(); i++) {
			if (infoTab.getCell(i, 0).equals(PRED_HASH)) {
				storedHash = infoTab.getCell(i, 1);
			} else if (infoTab.getCell(i, 0).equals(PRED_EPOCH)) {
				storedEpoch = infoTab.getCellAsLong(i, 1);
			}
		}
		
		if (storedHash != null) {
			// fail if cache hash is different
			if (!storedHash.equals(bootstrapHash)) {
				tracker.setJobFailure(this.jobId,"Proposed cache graph contains data for a different cache." + sei.getGraph());
				return true;
			}
			
			if (storedEpoch == 0) {
				tracker.setJobFailure(this.jobId,"Internal error: data is stored in cache without a time " + sei.getGraph());
				return true;
			}
			
			// succeed if epoch is recent enough
			if (nowEpoch - storedEpoch < this.maxEpochSeconds) {
				tracker.setJobSuccess(this.jobId, "Retaining previously cached data age = " + String.valueOf(nowEpoch - storedEpoch) + " sec");
				return true;
			}
			
			// clear old stuff
			sei.clearGraph();
		} else {
			String countQuery = SparqlToXUtils.generateCountTriplesSparql(sei);
			Table t = sei.executeQueryToTable(countQuery);
			if (t.getCellAsInt(0, 0) != 0) {
				tracker.setJobFailure(this.jobId,"Proposed cache graph is not empty." + sei.getGraph());
				return true;
			}
		}
		
		// ----  At this point graph is empty and we're about to fill it ----
		
		// add hash and epoch triples
		String sparql1 = SparqlToXUtils.generateInsertTripleQuery(sei, "<" + SUBJECT + ">", "<" + PRED_HASH + ">", "\"" + bootstrapHash + "\"");
		String sparql2 = SparqlToXUtils.generateInsertTripleQuery(sei, "<" + SUBJECT + ">", "<" + PRED_EPOCH + ">", String.valueOf(nowEpoch));
		// presume services sei credentials work sei insert
		sei.executeQueryAndConfirm(sparql1);
		sei.executeQueryAndConfirm(sparql2);
		
		return false;

	}
	
	/**
	 * Run the thread.
	 * Note: always produces a status message in the job tracker.
	 */
	public void run() {
		ThreadAuthenticator.authenticateThisThread(this.headerTable);
		
		try {
			if (this.checkAlreadyCached()) {
				return;
			}
			
			FdcClient fdcClient = null;
			int percentStep = 100 / this.getNumSteps();
			int percentComplete = 0;
			
			for (this.curStep = 0; this.curStep < this.getNumSteps(); this.curStep++) {
				
				//----- get fdc inputs -----
				if (curStep == 0) {
					// bootstrap first step
					if (bootstrapTable == null) {
						throw new Exception("Runs without bootstrap table are"
								+ " not yet implemented.");
					} else { 
						tracker.setJobPercentComplete(jobId, percentComplete, "loading bootstrap table");
						fdcClient = getStepFdcClient(this.bootstrapTable);
					}
				} else {
					// normal select 
					tracker.setJobPercentComplete(jobId, percentComplete, "run select, nodegroup = " + this.getStepInputNg());
					
					// try retrieving nodegroup from fdcClient first, failures will be silent
					String selectId = this.getStepInputNg();
					
					FdcClient fdcGetNgClient = new FdcClient(FdcClientConfig.buildGetNodegroup(this.getStepServiceUrl(), selectId));
					SparqlGraphJson sgjson = fdcGetNgClient.executeGetNodegroup();
					Table selectTab = null;
					if (sgjson != null) {
						// run nodegroup from fdcClient
						sgjson.setSparqlConn(this.conn);
						selectTab = this.ngExecClient.dispatchSelectFromNodeGroup(sgjson, null, null);
					} else {
						// run from nodegroup store instead
						selectTab = this.ngExecClient.execDispatchSelectByIdToTable(
								selectId, 
								this.conn,
								null,null);
					}
					fdcClient = getStepFdcClient(selectTab);
				}
				percentComplete += percentStep/3;
				
				
				//----- run fdc client -----
				tracker.setJobPercentComplete(jobId, percentComplete, "run client = " + this.getStepServiceUrl());
				TableResultSet res = fdcClient.executeWithTableResultReturn();
				res.throwExceptionIfUnsuccessful();
				percentComplete += percentStep/3;

				
				//----- ingest results -----
				if (res.getTable().getNumRows() > 0) {
					String ingestId = this.getStepIngestNgId();
					tracker.setJobPercentComplete(jobId, percentComplete, "ingest nodegroup = " + ingestId);
					
					// try retrieving nodegroup from fdcClient first, failures will be silent
					FdcClient fdcGetNgClient = new FdcClient(FdcClientConfig.buildGetNodegroup(this.getStepServiceUrl(), ingestId));
					SparqlGraphJson sgjson = fdcGetNgClient.executeGetNodegroup();
					if (sgjson != null) {
						// run nodegroup from fdcClient
						sgjson.setSparqlConn(this.conn);
						this.ngExecClient.dispatchIngestFromCsvStringsSync(sgjson, res.getTableCSVString());
					} else {
						// else run nodegroup from store by id
						this.ngExecClient.dispatchIngestFromCsvStringsByIdSync(
								this.getStepIngestNgId(), 
								res.getTableCSVString(), 
								this.conn);
					}
				}
				percentComplete += percentStep/3;

			}
			
			this.tracker.setJobSuccess(jobId, "Successfully cached data");
			
		} catch (Exception e) {
			// finish job on any exception
			try {
				LocalLogger.printStackTrace(e);
				this.tracker.setJobFailure(this.jobId, e.getMessage() == null ? "null" : e.getMessage());
			} catch (Exception ee) {
				LocalLogger.printStackTrace(ee);
			}
			
		}
	}
}
