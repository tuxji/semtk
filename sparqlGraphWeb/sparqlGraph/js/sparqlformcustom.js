/**
 ** Copyright 2016 General Electric Company
 **
 ** Authors:  Paul Cuddihy, Justin McHugh
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

// This file is not a typical javascript object, but a script
// that needs to be loaded by the HTML.
// Its other half must also be loaded byt the HTML:  sparqlform.js

/**
 *   This file is meant to be CONSTANT across different custom forms.
 *   Put local functions in customconfig.js.
 */
require([	'sparqlgraph/js/sparqlform',
         	'sparqlgraph/js/modaliidx',
         	'sparqlgraph/js/htmlformgroup',  
         	'sparqlgraph/js/msiclientquery',
         	
         	'local/sparqlformconfig',
         	
         	'jquery',
		
			// rest are shimmed
         	
         	
		],

	function(SparqlForm, ModalIidx, HtmlFormGroup, MsiClientQuery, Config, $) {

		var g = null;
		
		// ****   Start on load proceedure ****
	
		onLoadCustom = function () {
			
			// set "g" before calling anything
			require(['custom/customconfig'],
				function(Global) {
					g = Global;
					
					kdlLogEvent("Custom: Page Load");
					
					// load customDiv.html
					$("#customDiv").load(gCustom + "/customDiv.html", {},
					    function (responseText, textStatus, req) {
					        if (textStatus == "error") {
					    		alert("Bad 'form' parameter on URL.  Can't find file: " + gCustom + "/customDiv.html");
					        }
					});
					gConnSetup();   // sparqlform.js
					
					refreshHtmlFormGroup();
			});
			
			
		};
		
		refreshHtmlFormGroup = function () {
			clearResults();
			
			formConstraintsNew();
			formConstraintsInit();
			
			var queryClient = new MsiClientQuery(Config.services.query.url, gConn.getDataInterface());
			gHtmlFormGroup = new HtmlFormGroup(this.document, queryClient, g.conn.domain, g.fields, g.query, setStatus, alertUser, beforeUpdatingCallback, doneUpdatingCallback,  undefined, false);
			
		};
		
		htmlFormGroupLoadCallback = function () {
			// after initial load of formgroup and refreshes.
			
			
			doneUpdatingCallback();
			gHtmlFormGroup.doneUpdatingCallback = doneUpdatingCallback;
		};
		
		runButtonCallback = function() {
			if (g.preQueryCheck()) {
				// this is a sparqlformlocal.js function
				
				if (formConstraintsValidate() > 0) {
					alertUser("Fix highlighted constraint errors before running query.", "Constraint Errors");
				} else {
					doQuery(gConn, gHtmlFormGroup.getNodegroup(), gFormConstraint);
				}
			}
		};
		
		clearButtonCallback = function() {
			g.clearButtonCallback();
			refreshHtmlFormGroup();		
		};
		
		beforeUpdatingCallback = function() {
			setStatus("Updating choices...");
			btnFormClear.disabled = true;
			btnFormExecute.disabled = true;
			g.beforeUpdatingCallback();
		};
		
		doneUpdatingCallback = function() {
			gNodeGroup = gHtmlFormGroup.getNodegroup();
			formConstraintsUpdateTable();
			
			setStatus("");
			btnFormClear.disabled = false;
			btnFormExecute.disabled = false;
			g.doneUpdatingCallback();
		};
		
	}

);