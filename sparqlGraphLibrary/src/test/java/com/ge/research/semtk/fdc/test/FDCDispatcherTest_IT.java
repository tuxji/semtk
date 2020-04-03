/**
 ** Copyright 2016-2020 General Electric Company
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
package com.ge.research.semtk.fdc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ge.research.semtk.edc.JobTracker;
import com.ge.research.semtk.edc.client.ResultsClient;
import com.ge.research.semtk.edc.client.StatusClient;
import com.ge.research.semtk.fdc.FdcClient;
import com.ge.research.semtk.fdc.FdcClientConfig;
import com.ge.research.semtk.load.utility.SparqlGraphJson;
import com.ge.research.semtk.ontologyTools.OntologyInfo;
import com.ge.research.semtk.resultSet.Table;
import com.ge.research.semtk.sparqlX.asynchronousQuery.DispatcherSupportedQueryTypes;
import com.ge.research.semtk.sparqlX.dispatch.FdcDispatcher;
import com.ge.research.semtk.sparqlX.dispatch.FdcServiceManager;
import com.ge.research.semtk.test.IntegrationTestUtility;
import com.ge.research.semtk.test.TestGraph;
import com.ge.research.semtk.utility.LocalLogger;
import com.ge.research.semtk.utility.Utility;

/**
 * Run multiple tests on a single FDC configuration with distance and location
 * @author 200001934
 *
 */
public class FDCDispatcherTest_IT {
	private static ResultsClient resultsClient;
	/**
	 * Load an FDC config with distance and location objects
	 * Load all the required owl for models
	 * Load a single aircraft called demo
	 * 
	 * These will be used for all tests in this file.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setup() throws Exception {
		
		IntegrationTestUtility.setupFdcTests(FDCDispatcherTest_IT.class);
		
		resultsClient = IntegrationTestUtility.getResultsClient();
	}
	
	
	@Test 
	public void testRetrieveNodegroups() throws Exception {
		
		for (String existId : new String [] {"fdcSampleElevation","fdcSampleDistance"}) {
			int port = IntegrationTestUtility.getInt("fdcsampleservice.port");
			String server = IntegrationTestUtility.get("fdcsampleservice.server");
			FdcClient fdcClient = new FdcClient(FdcClientConfig.buildGetNodegroup("http://" + server + ":" + String.valueOf(port) + "/fdcSample/anything", existId));
			SparqlGraphJson sgjson = fdcClient.executeGetNodegroup();
			if (sgjson == null) {
				throw new Exception("Error retrieving nodegroup from fdcSampleService: " + existId);
			}
		}
		
		for (String badId : new String [] {"does-not-exist"}) {
			int port = IntegrationTestUtility.getInt("fdcsampleservice.port");
			String server = IntegrationTestUtility.get("fdcsampleservice.server");
			FdcClient fdcClient = new FdcClient(FdcClientConfig.buildGetNodegroup("http://" + server + ":" + String.valueOf(port) + "/fdcSample/anything", badId));
		
			try {
				SparqlGraphJson sgjson = fdcClient.executeGetNodegroup();
				if (sgjson != null) {
					throw new Exception("Retrieved non-existent nodegroup from fdcSampleService: " + badId);
				}
			} catch (Exception e) {
				throw new Exception("Retrieving bad nodegroup threw an exception", e);
			}
		}
	}
	
	/**
	 * Test non-FDC query with FDC configured
	 * @throws Exception
	 */
	@Test
	public void testNonFdcQuery() throws Exception {
		// simple query of the demo aircraft
		Table t = this.runFdc("src/test/resources/fdc_sample_location_select.json");
		assertTrue("Did not find expected single aircraft named 'demo'", t.getCell(0, 0).equals("demo"));
	}
	
	@Test
	public void testLocationFdcQuery() throws Exception {
		// simple single-FDC query
		Table t = this.runFdc("src/test/resources/fdc_sample_location_select.json");

		assertEquals("Did not find expected aircraft, latitude, longitude", 1, t.getNumRows());
	}
	
	@Test
	public void testChainedFdcQuery() throws Exception {
		// computes location then
		// distance to 133 airports
		// closest should be BDL
		Table t = this.runFdc("src/test/resources/fdc_sample_distance_select.json");

		assertEquals("Did not recieve distance for each airport", 133, t.getNumRows());
		assertTrue("First airport is not BDL", t.getCell(0, "code").equals("BDL"));
	}
	
	@Test
	public void testCircularishFdcQuery() throws Exception {
		// One of the distance input nodegroups also contains FDC Elevation
		// Make sure FDC works around "circular dependency"
		Table t = this.runFdc("src/test/resources/fdc_sample_distance_one_elev.json");

		assertEquals("Did not recieve one row", 1, t.getNumRows());
		assertTrue("Distance is not correct", Math.abs(2010.18 - t.getCellAsFloat(0, "distanceNm")) < 0.1);
		assertEquals("Elevation is not correct", 1004, t.getCellAsInt(0, "elevationFt"));

	}
	
	@Test
	public void testTwoElevFdcQuery() throws Exception {
		// One of the distance input nodegroups also contains FDC Elevation
		// Make sure FDC works around "circular dependency"
		Table t = this.runFdc("src/test/resources/fdc_sample_distance_two_elev.json");

		assertEquals("Did not recieve one row", 1, t.getNumRows());
		assertTrue("Distance is not correct", Math.abs(2010.18 - t.getCellAsFloat(0, "distanceNm")) < 0.1);
		assertEquals("Elevation is not correct", 1004, t.getCellAsInt(0, "elevationFt"));
		assertEquals("Elevation_0 is not correct", 1036, t.getCellAsInt(0, "elevationFt_0"));

	}
	
	@Test
	public void testMissingInputs() throws Exception {
		// distance when one location is missing
		try {
			Table t = this.runFdc("src/test/resources/fdc_sample_distance_one_loc.json");
			assertEquals("Distance with only one location returned non-empty: " + t.toCSVString(), 0, t.getNumRows());

		
		} catch (Exception e) {
			e.printStackTrace();
			fail("Distance with only one location threw exception instead of returning empty");
		}
	}
	
	
	/** 
	 * Execute an FDC query 
	 * @param sgjsonPath
	 * @return Table
	 * @throws Exception
	 */
	private Table runFdc(String sgjsonPath) throws Exception {
		// simple query of the demo aircraft
		SparqlGraphJson sgjson = TestGraph.getSparqlGraphJsonFromFile(sgjsonPath);
		
		String jobId = JobTracker.generateJobId();
		ResultsClient rClient = IntegrationTestUtility.getResultsClient();
		StatusClient sClient = IntegrationTestUtility.getStatusClient(jobId);
		FdcDispatcher fdc = new FdcDispatcher(jobId, 
												sgjson, 
												sClient.getJobTrackerSei(IntegrationTestUtility.get("sparqlendpoint.username"), IntegrationTestUtility.get("sparqlendpoint.password")), 
												IntegrationTestUtility.getResultsClientConfig(), 
												TestGraph.getSei(),   // fdc services sei
												false, 
												IntegrationTestUtility.getOntologyInfoClient(), 
												IntegrationTestUtility.getNodeGroupStoreRestClient());
		fdc.execute(null, null, DispatcherSupportedQueryTypes.SELECT_DISTINCT, null);
		
		if (! sClient.execIsSuccess() ) {
			fail(sClient.execGetStatusMessage());
		}
		return rClient.getTableResultsJson(jobId, 1000);
	}
	
}
