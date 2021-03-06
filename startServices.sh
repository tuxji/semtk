#!/bin/bash
#
# Copyright 2018 General Electric Company
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#

#
# Starts microservices, including the ones needed for SparqlGraph.
# Optional argument:  full path of dir with alternate versions of:
#                       .env, .fun, logs/ ENV_OVERRIDE


# SEMTK = directory holding this script
# try windows style pwd -W first, then default to unix
SEMTK="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd -W 2>/dev/null)"
if [ -z $SEMTK ]
then
        SEMTK="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
fi
echo SEMTK is $SEMTK

# Handle $1 optional arg of alternate semtk dir that contains
#    ENV_OVERRIDE 
if [ $# -eq 1 ]; then
	LOGS=${1}/logs  
	cp ./.env ${1}
	cp ./.fun ${1}
	pushd ${1}; . .env; popd

elif [ $# -eq 0 ]; then
	LOGS=${SEMTK}/logs
	pushd ${SEMTK}; . .env; popd

else 
	echo Usage: startServices.sh [alt_env_dir]
fi

# exit if no JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
        >&2 echo No JAVA_HOME
        exit 1
fi

# exit if services are still running
NUM_SERVICES_RUNNING=`ps aux | grep jar | grep $SEMTK | grep Service | wc -l`
if [ $NUM_SERVICES_RUNNING -gt 0 ]; then
        >&2 echo "$NUM_SERVICES_RUNNING services are running in $SEMTK, cannot restart"
        exit 1
else
        >&2 echo "Confirmed no services running in $SEMTK"
fi

# logs/
mkdir -p $LOGS

#
# "Legacy" startServices will not config webapps
#
if [ -n "${WEBAPPS}" ]; then
	./configWebapps.sh
fi


echo "=== START MICROSERVICES... ==="

PID_ARRAY=()
PROC_ARRAY=()

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/ontologyInfoService/target/ontologyInfoService-*.jar > "$LOGS"/ontologyInfoService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("ontologyInfoService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/nodeGroupStoreService/target/nodeGroupStoreService-*.jar > "$LOGS"/nodeGroupStoreService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("nodeGroupStoreService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/sparqlGraphStatusService/target/sparqlGraphStatusService-*.jar > "$LOGS"/sparqlGraphStatusService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("sparqlGraphStatusService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS_LARGE_MEMORY -jar "$SEMTK"/sparqlGraphResultsService/target/sparqlGraphResultsService-*.jar > "$LOGS"/sparqlGraphResultsService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("sparqlGraphResultsService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/hiveService/target/hiveService-*.jar > "$LOGS"/hiveService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("hiveService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS_LARGE_MEMORY -Dloader.path="${LOCATION_ADDITIONAL_DISPATCHER_JARS}" -jar "$SEMTK"/sparqlExtDispatchService/target/sparqlExtDispatchService-*.jar > "$LOGS"/sparqlExtDispatchService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("sparqlExtDispatchService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/nodeGroupExecutionService/target/nodeGroupExecutionService-*.jar > "$LOGS"/nodeGroupExecutionService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("nodeGroupExecutionService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/sparqlQueryService/target/sparqlQueryService-*.jar > "$LOGS"/sparqlQueryService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("sparqlQueryService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/sparqlGraphIngestionService/target/sparqlGraphIngestionService-*.jar > "$LOGS"/sparqlGraphIngestionService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("sparqlGraphIngestionService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/nodeGroupService/target/nodeGroupService-*.jar > "$LOGS"/nodeGroupService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("nodeGroupService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/fdcSampleService/target/fdcSampleService-*.jar > "$LOGS"/fdcSampleService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("fdcSampleService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/fdcCacheService/target/fdcCacheService-*.jar > "$LOGS"/fdcCacheService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("fdcCacheService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/edcQueryGenerationService/target/edcQueryGenerationService-*.jar > "$LOGS"/edcQueryGenerationService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("edcQueryGenerationService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS_LARGE_MEMORY -jar "$SEMTK"/athenaService/target/athenaService-*.jar > "$LOGS"/athenaService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("athenaService");

"$JAVA_HOME"/bin/java -jar "$SEMTK"/arangoDbService/target/arangoDbService-*.jar > "$LOGS"/arangoDbService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("arangoDbService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/utilityService/target/utilityService-*.jar > "$LOGS"/utilityService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("utilityService");

"$JAVA_HOME"/bin/java $JVM_OPTIONS -jar "$SEMTK"/fileStagingService/target/fileStagingService-*.jar > "$LOGS"/fileStagingService.log 2>&1 &
PID_ARRAY+=($!)
PROC_ARRAY+=("fileStagingService");

#
# wait for services
#
MAX_SEC=300
declare -a PORTS=($PORT_SPARQLGRAPH_STATUS_SERVICE
                  $PORT_SPARQLGRAPH_RESULTS_SERVICE
                  $PORT_DISPATCH_SERVICE
                  $PORT_HIVE_SERVICE
                  $PORT_NODEGROUPSTORE_SERVICE
                  $PORT_ONTOLOGYINFO_SERVICE
                  $PORT_NODEGROUPEXECUTION_SERVICE
                  $PORT_SPARQL_QUERY_SERVICE
                  $PORT_INGESTION_SERVICE
                  $PORT_NODEGROUP_SERVICE
                  $PORT_FDCSAMPLE_SERVICE
                  $PORT_FDCCACHE_SERVICE
				  $PORT_EDCQUERYGEN_SERVICE
				  $PORT_ATHENA_SERVICE
                  $PORT_ARANGODB_SERVICE
				  $PORT_UTILITY_SERVICE
				  $PORT_FILESTAGING_SERVICE
                 )
# protocol for ping
if [ "$SSL_ENABLED" == "false" ]; then
    PROTOCOL="http"
else
	PROTOCOL="https"
fi				 

echo Using no_proxy: $no_proxy

# check for each service				 
for port in "${PORTS[@]}"; do
   while ! curl --insecure --noproxy $no_proxy -X POST ${PROTOCOL}://${HOST_NAME}:${port}/serviceInfo/ping 2>>/dev/null | grep -q yes ; do

	echo waiting for service at ${PROTOCOL}://${HOST_NAME}:${port}
        if (($SECONDS > $MAX_SEC)) ; then
        	echo ERROR: Took to longer than $MAX_SEC seconds to start services
        	exit 1
        fi
        sleep 3

	# check that no processes have died yet
	for i in "${!PID_ARRAY[@]}"; do
		if (! (ps -p ${PID_ARRAY[$i]} >> /dev/null)) ; then
			echo ERROR: Process ${PID_ARRAY[$i]} ${PROC_ARRAY[$i]} has died
			exit 1
		fi
	done
   done
   echo service at ${PROTOCOL}://${HOST_NAME}:${port} is up
done
echo "=== DONE ==="
exit 0
