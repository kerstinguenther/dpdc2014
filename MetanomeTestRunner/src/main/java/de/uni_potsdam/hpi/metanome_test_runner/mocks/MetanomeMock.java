package de.uni_potsdam.hpi.metanome_test_runner.mocks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.InclusionDependency;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithms.LucieKerstinUCC;
import de.metanome.backend.input.csv.DefaultFileInputGenerator;
import de.metanome.backend.result_receiver.ResultsCache;
import de.uni_potsdam.hpi.metanome_test_runner.config.Config;

public class MetanomeMock {

	public static void execute(Config conf) {
		try {
			RelationalInputGenerator inputGenerator = new DefaultFileInputGenerator(new ConfigurationSettingFileInput(
					conf.inputFolderPath + conf.databaseName + File.separator + conf.tableNames[0] + conf.inputFileEnding, true,
					conf.inputFileSeparator, conf.inputFileQuotechar, conf.inputFileEscape, conf.inputFileStrictQuotes, 
					conf.inputFileIgnoreLeadingWhiteSpace, conf.inputFileSkipLines, conf.inputFileHasHeader, conf.inputFileSkipDifferingLines));
			ResultsCache resultReceiver = new ResultsCache();
			
			LucieKerstinUCC ucc = new LucieKerstinUCC();
			ucc.setRelationalInputConfigurationValue(LucieKerstinUCC.Identifier.INPUT_GENERATOR.name(), inputGenerator);
			ucc.setResultReceiver(resultReceiver);
			
			long time = System.currentTimeMillis();
			ucc.execute();
			time = System.currentTimeMillis() - time;
			
			if (conf.writeResults) {
				writeToFile(ucc.toString() + "\r\n\r\n" + "Runtime: " + time + "\r\n\r\n" + conf.toString(), conf.measurementsFolderPath + conf.statisticsFileName);
				writeToFile(format(resultReceiver.getNewResults()), conf.measurementsFolderPath + conf.resultFileName);
			}
		}
		catch (AlgorithmExecutionException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String format(List<Result> results) {
		HashMap<String, List<String>> ref2Deps = new HashMap<String, List<String>>();

		for (Result result : results) {
			InclusionDependency ind = (InclusionDependency) result;
			
			StringBuilder refBuilder = new StringBuilder("(");
			Iterator<ColumnIdentifier> refIterator = ind.getReferenced().getColumnIdentifiers().iterator();
			while (refIterator.hasNext()) {
				refBuilder.append(refIterator.next().toString());
				if (refIterator.hasNext())
					refBuilder.append(",");
				else
					refBuilder.append(")");
			}
			String ref = refBuilder.toString();
			
			StringBuilder depBuilder = new StringBuilder("(");
			Iterator<ColumnIdentifier> depIterator = ind.getDependant().getColumnIdentifiers().iterator();
			while (depIterator.hasNext()) {
				depBuilder.append(depIterator.next().toString());
				if (depIterator.hasNext())
					depBuilder.append(",");
				else
					depBuilder.append(")");
			}
			String dep = depBuilder.toString();
			
			if (!ref2Deps.containsKey(ref))
				ref2Deps.put(ref, new ArrayList<String>());
			ref2Deps.get(ref).add(dep);
		}
		
		StringBuilder builder = new StringBuilder();
		ArrayList<String> referenced = new ArrayList<String>(ref2Deps.keySet());
		Collections.sort(referenced);
		for (String ref : referenced) {
			List<String> dependants = ref2Deps.get(ref);
			Collections.sort(dependants);
			
			if (!dependants.isEmpty())
				builder.append(ref + " > ");
			for (String dependant : dependants)
				builder.append(dependant + "  ");
			if (!dependants.isEmpty())
				builder.append("\r\n");
		}
		return builder.toString();
	}
	
	private static void writeToFile(String content, String filePath) throws IOException {
		Writer writer = null;
		try {
			writer = buildFileWriter(filePath, false);
			writer.write(content);
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}
	
	private static BufferedWriter buildFileWriter(String filePath, boolean append) throws IOException {
		createFile(filePath, !append);
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath), append), Charset.forName("ISO-8859-1")));
	}
	
	private static void createFile(String filePath, boolean recreateIfExists) throws IOException {
		File file = new File(filePath);
		File folder = file.getParentFile();
		
		if (!folder.exists()) {
			folder.mkdirs();
			while (!folder.exists()) {}
		}
		
		if (recreateIfExists && file.exists())
			file.delete();
		
		if (!file.exists()) {
			file.createNewFile();
			while (!file.exists()) {}
		}
	}
}
