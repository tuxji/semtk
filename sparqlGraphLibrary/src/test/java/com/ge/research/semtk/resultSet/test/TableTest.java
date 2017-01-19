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


package com.ge.research.semtk.resultSet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import com.ge.research.semtk.resultSet.Table;

public class TableTest {

	@Test
	public void testTableToCSV() throws Exception {
		
		String[] cols = {"colA","colB","colC"};
		String[] colTypes = {"String","String","String"};
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		ArrayList<String> rowFruit = new ArrayList<String>();
		rowFruit.add("apple");
		rowFruit.add("banana");
		rowFruit.add("coconut,comma");
		rows.add(rowFruit);
		ArrayList<String> rowNames = new ArrayList<String>();
		rowNames.add("adam");
		rowNames.add("barbara");
		rowNames.add("chester");
		rows.add(rowNames);
		Table table = new Table(cols, colTypes, rows);
		
		assertEquals(table.toCSVString(),"colA,colB,colC\napple,banana,\"coconut,comma\"\nadam,barbara,chester\n");
		
	}
	
	@Test
	public void testTableWithCommas() throws Exception {
		
		String[] cols = {"colA","colB","colC"};
		String[] colTypes = {"String","String","String"};
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		ArrayList<String> rowFruit = new ArrayList<String>();
		rowFruit.add("apple");
		rowFruit.add("banana");
		rowFruit.add("coconut");
		rows.add(rowFruit);
		ArrayList<String> rowNames = new ArrayList<String>();
		rowNames.add("adam,adamson");
		rowNames.add("barbara,barbrason");
		rowNames.add("chester,chesterton");
		rows.add(rowNames);
		Table table = new Table(cols, colTypes, rows);
		
		assertEquals(table.toCSVString(),"colA,colB,colC\napple,banana,coconut\n\"adam,adamson\",\"barbara,barbrason\",\"chester,chesterton\"\n");
		assertEquals(table.getRowAsCSVString(0),"apple,banana,coconut");
		assertEquals(table.getRowAsCSVString(1),"\"adam,adamson\",\"barbara,barbrason\",\"chester,chesterton\"");
	}
	
	@Test
	public void testTableToCSV_1Row1Col() throws Exception {
		
		String[] cols = {"colA"};
		String[] colTypes = {"String"};
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		ArrayList<String> rowFruit = new ArrayList<String>();
		rowFruit.add("apple");
		rows.add(rowFruit);
		Table table = new Table(cols, colTypes, rows);
		
		assertEquals(table.toCSVString(),"colA\napple\n");
		assertEquals(table.getRowAsCSVString(0),"apple");
	}
	
	@Test
	public void testTableToCSV_0Cols0Rows() throws Exception {
		String[] cols = {};
		String[] colTypes = {};
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		Table table = new Table(cols, colTypes, rows);
		
		assertEquals(table.toCSVString(),"");
	}

	@Test
	public void testTableToJson() throws Exception {
		
		String[] cols = {"colA","colB","colC"};
		String[] colTypes = {"String","String","String"};
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		ArrayList<String> rowFruit = new ArrayList<String>();
		rowFruit.add("apple");
		rowFruit.add("banana");
		rowFruit.add("coconut");
		rows.add(rowFruit);
		ArrayList<String> rowNames = new ArrayList<String>();
		rowNames.add("adam");
		rowNames.add("barbara");
		rowNames.add("chester");
		rows.add(rowNames);
		Table table = new Table(cols, colTypes, rows);
		assertEquals(table.toJson().get(Table.JSON_KEY_COL_COUNT), 3);
		
		// test getColumn()
		String[] columnA = {"apple","adam"};
		assertEquals(table.getColumn("colA")[0], columnA[0]);
		assertEquals(table.getColumn("colA")[1], columnA[1]);
	}	
	
	@Test
	public void testTableToJsonTruncate() throws Exception {
		
		String[] cols = {"colA","colB","colC"};
		String[] colTypes = {"String","String","String"};
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		ArrayList<String> rowFruit = new ArrayList<String>();
		rowFruit.add("apple");
		rowFruit.add("banana");
		rowFruit.add("coconut");
		rows.add(rowFruit);
		ArrayList<String> rowNames = new ArrayList<String>();
		rowNames.add("adam");
		rowNames.add("barbara");
		rowNames.add("chester");
		rows.add(rowNames);
		Table table = new Table(cols, colTypes, rows);
		
		assertEquals(table.toJson().get(Table.JSON_KEY_COL_COUNT), 3);
		assertEquals(table.toJson().get(Table.JSON_KEY_ROW_COUNT), 2);
		
		table.truncate(10);
		assertEquals(table.toJson().get(Table.JSON_KEY_COL_COUNT), 3);
		assertEquals(table.toJson().get(Table.JSON_KEY_ROW_COUNT), 2);
		
		table.truncate(1);
		assertEquals(table.toJson().get(Table.JSON_KEY_COL_COUNT), 3);
		assertEquals(table.toJson().get(Table.JSON_KEY_ROW_COUNT), 1);
	}	
	
	

	@Test
	public void testTableToJson_1Row1Col() throws Exception {
		
		String[] cols = {"colA"};
		String[] colTypes = {"String"};
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		ArrayList<String> rowFruit = new ArrayList<String>();
		rowFruit.add("apple");
		rows.add(rowFruit);
		Table table = new Table(cols, colTypes, rows);
		assertEquals(table.toJson().get(Table.JSON_KEY_COL_COUNT), 1);
		assertEquals(table.toJson().get(Table.JSON_KEY_ROW_COUNT), 1);
		System.out.println(table.toJson());
	}	
	
	@Test
	public void testTableFromJson() throws Exception{
		String jsonStr = "{\"col_names\":[\"colA\",\"colB\",\"colC\"],\"rows\":[[\"apple\",\"banana\",\"coconut\"],[\"adam\",\"barbara\",\"chester\"]],\"col_type\":[\"String\",\"String\",\"String\"],\"col_count\":3,\"row_count\":2}";
		JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonStr);
		Table table = Table.fromJson(jsonObj);
		
		assertEquals(table.getColumnNames().length,3);
		assertEquals(table.getColumnNames()[0],"colA");
		assertEquals(table.getColumnNames()[1],"colB");
		assertEquals(table.getColumnNames()[2],"colC");
		assertEquals(table.getColumnTypes()[0],"String");
		assertEquals(table.getRows().get(0).get(0),"apple");
		assertEquals(table.getRows().get(1).get(2),"chester");
	}
	
	@Test
	public void testTableMerge() throws Exception {
		String jsonStr1 = "{\"col_names\":[\"colA\",\"colB\",\"colC\"],\"rows\":[[\"apple\",\"banana\",\"coconut\"],[\"adam\",\"barbara\",\"chester\"]],\"col_type\":[\"String\",\"String\",\"String\"],\"col_count\":3,\"row_count\":2}";
		JSONObject jsonObj1 = (JSONObject) new JSONParser().parse(jsonStr1);
		Table table1 = Table.fromJson(jsonObj1);

		String jsonStr2 = "{\"col_names\":[\"colC\",\"colB\",\"colA\"],\"rows\":[[\"cheesewhiz\",\"bonbons\",\"apple pie\"],[\"cider\",\"bourbon\",\"apple juice\"],[\"Chisholm\",\"Bobberson\",\"Anderson\"]],\"col_type\":[\"String\",\"String\",\"String\"],\"col_count\":3,\"row_count\":3}";
		JSONObject jsonObj2 = (JSONObject) new JSONParser().parse(jsonStr2);
		Table table2 = Table.fromJson(jsonObj2);
		
		ArrayList<Table> tables = new ArrayList<Table>();
		tables.add(table1);
		tables.add(table2);
		Table tableMerged = Table.merge(tables);
		
		assertEquals(tableMerged.getNumRows(),5);		
		assertEquals(tableMerged.getNumColumns(),3);
		System.out.println(tableMerged.toJson());
		String res = "{\"col_names\":[\"colA\",\"colB\",\"colC\"],\"rows\":[[\"apple\",\"banana\",\"coconut\"],[\"adam\",\"barbara\",\"chester\"],[\"apple pie\",\"bonbons\",\"cheesewhiz\"],[\"apple juice\",\"bourbon\",\"cider\"],[\"Anderson\",\"Bobberson\",\"Chisholm\"]],\"col_type\":[\"String\",\"String\",\"String\"],\"col_count\":3,\"row_count\":5}";
		assertEquals(tableMerged.toJson().toString(),res);
	}
	
	
	@Test
	public void testTableGetSubsetWhereMatches() throws Exception {
		String jsonStr = "{\"col_names\":[\"colA\",\"colB\",\"colC\"],\"rows\":[[\"apple\",\"banana\",\"coconut\"],[\"adam\",\"barbara\",\"chester\"]],\"col_type\":[\"String\",\"String\",\"String\"],\"col_count\":3,\"row_count\":2}";
		JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonStr);
		Table table = Table.fromJson(jsonObj);
		
		String[] retColNames = {"colC"};
		Table tableSubset = table.getSubsetWhereMatches("colB", "banana", retColNames);
		assertEquals(tableSubset.getNumColumns(),1);
		assertEquals(tableSubset.getNumRows(), 1);
		assertEquals(tableSubset.getRows().get(0).get(0), "coconut");
	}
	
	@Test
	public void testTable_WrongNumColTypes() throws Exception {
		boolean thrown = false;
		try{
			String jsonStr = "{\"col_names\":[\"colA\",\"colB\",\"colC\"],\"rows\":[[\"apple\",\"banana\",\"coconut\"],[\"adam\",\"barbara\",\"chester\"]],\"col_type\":[\"String\",\"String\"],\"col_count\":3,\"row_count\":2}";
			JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonStr);
			Table.fromJson(jsonObj);
		}catch(Exception e){
			thrown = true;
		}
		assertTrue(thrown);
	}
	
	@Test
	public void testTable_ColumnsAndIndexes() throws Exception {
		boolean thrown = false;
		try{
			String jsonStr = "{\"col_names\":[\"colA\",\"colB\",\"colC\"],\"rows\":[[\"apple\",\"banana\",\"coconut\"],[\"adam\",\"barbara\",\"chester\"]],\"col_type\":[\"String\",\"String\"],\"col_count\":3,\"row_count\":2}";
			JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonStr);
			Table table = Table.fromJson(jsonObj);
			
			assertTrue(table.hasColumn("colA"));
			assertFalse(table.hasColumn("colZ"));
			assertEquals(table.getColumnIndex("colB"), 1);
			assertEquals(table.getColumnIndex("colX"), -1);
		}catch(Exception e){
			thrown = true;
		}
		assertTrue(thrown);
	}
	
	
	@Test
	public void testTable_getColumnUniqueValues() throws Exception {
		boolean thrown = false;
		try{
			String jsonStr = "{\"col_names\":[\"colA\",\"colB\",\"colC\"],\"rows\":[[\"apple\",\"banana\",\"coconut\"],[\"adam\",\"barbara\",\"chester\"],[\"adam\",\"barbara\",\"chester\"],[\"adam\",\"barbara\",\"chester\"]],\"col_type\":[\"String\",\"String\"],\"col_count\":3,\"row_count\":4}";
			JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonStr);
			Table table = Table.fromJson(jsonObj);
			
			assertEquals(table.getColumn("colC").length,4);
			assertEquals(table.getColumnUniqueValues("colC").length,2);
		}catch(Exception e){
			thrown = true;
		}
		assertTrue(thrown);
	}
	
	
	@Test
	public void testTable_WrongNumRowEntries() throws Exception {
		boolean thrown = false;
		try{
			String jsonStr = "{\"col_names\":[\"colA\",\"colB\",\"colC\"],\"rows\":[[\"apple\",\"banana\"],[\"adam\",\"barbara\"]],\"col_type\":[\"String\",\"String\"],\"col_count\":3,\"row_count\":2}";
			JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonStr);
			Table.fromJson(jsonObj);
		}catch(Exception e){
			thrown = true;
		}
		assertTrue(thrown);
	}
	
	@Test
	public void testTableMerge_MismatchedColumns() throws Exception {
		
		boolean thrown = false;
		try{
			String jsonStr1 = "{\"col_names\":[\"colA\",\"colB\",\"colC\"],\"rows\":[[\"apple\",\"banana\",\"coconut\"],[\"adam\",\"barbara\",\"chester\"]],\"col_type\":[\"String\",\"String\",\"String\"],\"col_count\":3,\"row_count\":2}";
			JSONObject jsonObj1 = (JSONObject) new JSONParser().parse(jsonStr1);
			Table table1 = Table.fromJson(jsonObj1);
	
			String jsonStr2 = "{\"col_names\":[\"colD\",\"colB\",\"colA\"],\"rows\":[[\"cheesewhiz\",\"bonbons\",\"apple pie\"],[\"cider\",\"bourbon\",\"apple juice\"],[\"Chisholm\",\"Bobberson\",\"Anderson\"]],\"col_type\":[\"String\",\"String\",\"String\"],\"col_count\":3,\"row_count\":3}";
			JSONObject jsonObj2 = (JSONObject) new JSONParser().parse(jsonStr2);
			Table table2 = Table.fromJson(jsonObj2);
			
			ArrayList<Table> tables = new ArrayList<Table>();		
			tables.add(table1);
			tables.add(table2);
			Table tableMerged = Table.merge(tables);  // should fail, because col names don't match across tables
		}catch(Exception e){
			thrown = true;
		}
		assertTrue(thrown);
		
	}
	
	
}