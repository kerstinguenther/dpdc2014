/*
 * Copyright 2014 by the Metanome project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.metanome.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;

public class LucieKerstinUCCAlgorithm {
	
	protected RelationalInputGenerator inputGenerator = null;
	protected UniqueColumnCombinationResultReceiver resultReceiver = null;
	// Liste mit allen Spalten als Liste
    List<List<String>> columns = new ArrayList<List<String>>();
	
	public void execute() throws AlgorithmExecutionException {
		
		//////////////////////////////////
		// THE ALGORITHM LIVES HERE :-) //
		//////////////////////////////////
		
		// To test if the algorithm gets data
		this.print();
	}
	
	public boolean isUnique(List<String> list) {
	    Set<String> set = new HashSet<String>();
	    // Set#add returns false if the set does not change, which
	    // indicates that a duplicate element has been added.
	    for (String each: list)
	        // TODO: check if "" as NULL works
	        if (!set.add(each) && each!="")
	            return false;
	    return true;
	}
	
	protected void print() throws InputGenerationException, InputIterationException {
		RelationalInput input = this.inputGenerator.generateNewCopy();
		
		List<String> temp;
		
		// Liste mit uniques
		List<Integer> unique = new ArrayList<Integer>();
		
		// Liste mit non-uniques
		List<Integer> nonunique = new ArrayList<Integer>();
				
		System.out.println(input.relationName());
				
		List<String> record = input.next();
        System.out.println(record);
                
        for(String s: record) {
            columns.add(new ArrayList<String>());
        }
		
        // Vorarbeit: alle Spalten lesen und wegschreiben
		int c;
		while (input.hasNext()) {
			
			record = input.next();
			
			temp = new ArrayList<String>();
			c = 0;
			for(String s:record) {
			    temp = columns.get(c);
	            temp.add(s);
	            columns.set(c, temp);
	            c++;
			}
		}
		
		// 1. Runde: einzelne Spalten auf uniqueness prüfen und in Listen einordnen
		c = 0;
		for(List<String> list: columns) {
	        if(isUnique(list)) unique.add(c);
	        else nonunique.add(c);
	        c++;
		}
		System.out.println("unique: " + unique);
		System.out.println("non-unique: " + nonunique);
		
		// Kandidatenliste
		List<List<Integer>> candidates = new ArrayList<List<Integer>>();
		for(int i = 0; i < nonunique.size(); i++) {
		    for(int j = i+1; j < nonunique.size(); j++) {
		        candidates.add(Arrays.asList(nonunique.get(i), nonunique.get(j)));
	        }
		}
		System.out.println("candidates: " + candidates);
		
		for(List<Integer> list: candidates) {
		    //if(checkColumnCombination(list)) TODO
		    System.out.println(list + "...." + checkColumnCombination(list));
		}
	}

  private boolean checkColumnCombination(List<Integer> list) {
      String value = "";
      List<String> columnCombination = mergeColumns(list);
      System.out.println(columnCombination);
      return isUnique(columnCombination);
  }

  private List<String> mergeColumns(List<Integer> list) {
      List<String> columnCombination = new ArrayList<String>();
      List<List<String>> tempColumns = new ArrayList<List<String>>();
      String cell = "";
      
      for(int i = 0; i < list.size(); i++) {
          tempColumns.add(columns.get(list.get(i)));
      }
      // number of rows
      int c = tempColumns.get(0).size();
      
      // gehe über alle Zeilen
      for(int i = 0; i < c; i++) {
          cell = "";
          // gehe über alle Spalten
          for(List<String> column: tempColumns) {
              cell += column.get(i) + " ";
          }
          // DOKU: Trenner zwischen Spalten in String muss sinnvoll gewählt sein..
          // idealerweise kommt dieses Zeichen nirgends in der Tabelle sonst vor
          // - beim Einfachen zusammenhängen wird aus "AN"-"A" und "A"-"NA" das gleiche..
          cell = cell.trim();
          columnCombination.add(cell);
      }
      return columnCombination;
  }
}
