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

/***
 * This algorithms identifies all minimal unique column combinations (ucc's) in a table.
 * Therefore column combinations (cc's) are generated as candidates and checked for uniqueness.
 * 
 *  For more information: look in the comments in the code and read our documentary.
 * 
 * @author Kerstin Günther, Lucie Omar
 */
public class LucieKerstinUCCAlgorithm {

	protected RelationalInputGenerator inputGenerator = null;
	protected UniqueColumnCombinationResultReceiver resultReceiver = null;
	protected List<List<String>> columns = new ArrayList<List<String>>();
	protected List<List<Integer>> unique = new ArrayList<List<Integer>>();

	public void execute() throws AlgorithmExecutionException {

		// 1. read data
		readData(this.inputGenerator.generateNewCopy());
		// 2. candidate generation + uniqueness checks
		generateCandidates();
		// 3. output unique column combinations
		System.out.println("UCCs: " + unique);
	}

	/***
	 * Reads data from a relational input.
	 * The values of each column are stored in a separate list.
	 * All column lists are added to another list.
	 * In that way, the data can be accessed column by column.
	 * 
	 * @param input: RelationalInput: data of table
	 * 
	 * @throws AlgorithmExecutionException
	 */
	private void readData(RelationalInput input) throws AlgorithmExecutionException {
		List<String> temp;

		// get column names (are not needed for the algorithm)
		List<String> record = input.next();

		// add a list for each column in the table
		for(int i = 0, limit = record.size(); i < limit; i++) {
			columns.add(new ArrayList<String>());
		}

		// loop over all rows
		while (input.hasNext()) {

			// get next row
			record = input.next();

			// clear temporary list for column values
			temp = new ArrayList<String>();

			// loop over all values (columns) in a row
			for(int c = 0, limit = record.size(); c < limit; c++) {
				// get already read values of current column
				temp = columns.get(c);
				// add current value to column
				temp.add(record.get(c));
				// write modified column back
				columns.set(c, temp);
			}
		}
	}

	/***
	 * Generates candidates (column combinations) for uniqueness checks.
	 * Therefore all already identified ucc's and all non-unique cc's from the previous level are stored.
	 * In that way, the fact that a superset of an ucc is also an ucc is used.
	 * The approach is apriori and bottom-up.
	 */
	protected void generateCandidates() {

		List<List<Integer>> nonunique = new ArrayList<List<Integer>>();
		Set<List<Integer>> candidates = new HashSet<List<Integer>>();;
		Set<Integer> cand;	// temporary candidate list
		
		// first candidates = single columns
		for(int c = 0, limit = columns.size(); c < limit; c++) {
			candidates.add(Arrays.asList(c));
		}

		// look for ucc's as long as there can be found at least one candidate
		while(candidates.size() > 0) {

			// clear list of non-unique cc's
			// (only non-unique cc's of last level are needed for candidate generation)
			nonunique.clear();

			// loop over candidates
			for(List<Integer> list: candidates) {
				// check if cc is unique and store it to appropriate list
				if(checkColumnCombination(list)) {
					unique.add(list);
				} else {
					nonunique.add(list);
				}
			}

			// clear candidate list
			// note: this list is a HashSet to avoid duplicates
			candidates.clear();

			// loop over all non-unique cc's:
			// generate new candidates by combine non-unique cc's
			for(int i = 0, limit = nonunique.size(); i < limit; i++) {
				for(int j = i+1; j < limit; j++) {
					cand = new HashSet<Integer>(nonunique.get(i));
					cand.addAll(nonunique.get(j));
					// only a candidate if no superset of ucc
					if(!checkIfPartlyUnique(cand)) {
						candidates.add(new ArrayList<Integer>(cand));
					}
				}
			}
		}
	}

	/***
	 * Checks if a given cc is a possible candidate for an ucc.
	 * This check is based on the fact that each superset of an ucc is also an ucc.
	 * 
	 * @param cand: Set<Integer>: possible candidate (cc) for uniqueness
	 * 
	 * @return boolean: true  - given cc is a candidate for ucc
	 * 					 false - given cc is not unique
	 */
	private boolean checkIfPartlyUnique(Set<Integer> cand) {
		for(List<Integer> u: unique) {
			if(cand.containsAll(u)) return true;
		}
		return false;
	}

	/***
	 * Checks if all values in a list (e.g. column or column combination) are unique.
	 * 
	 * @param list
	 * @return boolean: true  - given list is unique
	 * 					 false - given list is not unique
	 */
	private boolean isUnique(List<String> list) {
		// HashSet is used because of feature:
		// value is not added to set if the set already contains that value.
		// (return value: false)
		// In that way, duplicates can be identified.
		Set<String> set = new HashSet<String>();
		for (String each: list)
			// if value is not added to set and it is not a null-value: duplicate
			if (!set.add(each) && each!=null)
				return false;
		return true;
	}

	/***
	 * Checks if a cc is unique (=ucc).
	 * Therefore the given columns are merged and the resulting column is check for uniqueness.
	 * 
	 * @param list: List<Integer>: list with column indices
	 * 
	 * @return boolean: true  - cc is unique
	 * 					 false - cc is not unique
	 */
	private boolean checkColumnCombination(List<Integer> list) {
		List<String> columnCombination = mergeColumns(list);
		return isUnique(columnCombination);
	}

	/***
	 * Merges the values of the given columns into one column.
	 * 
	 * @param list: List<Integer>: list with column indices
	 * 
	 * @return List<String>: merged column with values as String
	 */
	private List<String> mergeColumns(List<Integer> list) {
		List<String> columnCombination = new ArrayList<String>();
		List<List<String>> tempColumns = new ArrayList<List<String>>();
		String cell = "";

		// get values of each column (given by its index)
		for(int i = 0, limit = list.size(); i < limit; i++) {
			tempColumns.add(columns.get(list.get(i)));
		}

		// loop over all rows
		for(int i = 0, c = tempColumns.get(0).size(); i < c; i++) {
			cell = "";
			// loop over all given columns
			for(List<String> column: tempColumns) {
				// add column value to value of the merged cell
				// values are separated by spaces (more: see documentation)
				/*
				 * TODO DOKU: Trenner zwischen Spalten in String muss sinnvoll gewählt sein..
				 * idealerweise kommt dieses Zeichen nirgends in der Tabelle sonst vor
				 * - beim Einfachen zusammenhängen wird aus "AN"-"A" und "A"-"NA" das gleiche..
				 */
				cell += column.get(i) + " ";
			}
			// remove last space
			cell = cell.trim();
			// add merged cell to resulting column
			columnCombination.add(cell);
		}
		return columnCombination;
	}
}



//protected void generateCandidates() {
//	
//	List<List<Integer>> nonunique = new ArrayList<List<Integer>>();
//	
//	// check each single column for uniqueness and store it to the appropriate list
//	for(int c = 0; c < columns.size(); c++) {
//		if(isUnique(columns.get(c))) unique.add(Arrays.asList(c));
//		else nonunique.add(Arrays.asList(c));
//	}
//
//	Set<List<Integer>> candidates;
//	Set<Integer> cand;	// temporary candidate list
//	
//	// look for ucc's as long as there can be found at least one candidate
//	while(true) {
//		
//		// clear candidate list
//		// note: this list is a HashSet to avoid duplicates
//		candidates = new HashSet<List<Integer>>();
//		
//		// loop over all non-unique cc's:
//		// generate new candidates by combine non-unique cc's
//		for(int i = 0; i < nonunique.size(); i++) {
//			for(int j = i+1; j < nonunique.size(); j++) {
//				cand = new HashSet<Integer>(nonunique.get(i));
//				cand.addAll(nonunique.get(j));
//				// only a candidate if no superset of ucc
//				if(!checkIfPartlyUnique(cand)) {
//					candidates.add(new ArrayList<Integer>(cand));
//				}
//			}
//		}
//
//		// if no candidate was found: break loop
//		if(candidates.size() == 0) break;
//		
//		// clear list of non-unique cc's
//		// (only non-unique cc's of last level are needed for candidate generation)
//		nonunique.clear();
//		
//		// loop over candidates
//		for(List<Integer> list: candidates) {
//			// check if cc is unique and store it to appropriate list
//			if(checkColumnCombination(list)) {
//				unique.add(list);
//			} else {
//				nonunique.add(list);
//			}
//		}
//	}
//}