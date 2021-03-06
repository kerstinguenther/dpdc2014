package de.uni_potsdam.hpi.metanome_test_runner.config;

import java.io.File;

public class Config {

	public enum Algorithm {
		MYUCC
	}
	
	public enum Dataset {
		PLANETS, VOTER, VOTER_1K, TEST
	}
	
	public Config.Algorithm algorithm = Config.Algorithm.MYUCC;
	
	public String databaseName = null;
	public String[] tableNames = null;
	
	public String inputFolderPath = "data" + File.separator;
	public String inputFileEnding = ".csv";
	public char inputFileSeparator = ',';
	public char inputFileQuotechar = '\"';
	public char inputFileEscape = '\\';
	public int inputFileSkipLines = 0;
	public boolean inputFileStrictQuotes = false;
	public boolean inputFileIgnoreLeadingWhiteSpace = true;
	public boolean inputFileHasHeader = false;
	public boolean inputFileSkipDifferingLines = true; // Skip lines that differ from the dataset's schema
	
	public String measurementsFolderPath = "io" + File.separator + "measurements" + File.separator; // + "BINDER" + File.separator;
	
	public String statisticsFileName = "statistics.txt";
	public String resultFileName = "results.txt";
	
	public boolean writeResults = true;
	
	public Config() {
		this(Config.Algorithm.MYUCC, Config.Dataset.PLANETS);
	}

	public Config(Config.Algorithm algorithm, Config.Dataset dataset) {
		this.algorithm = algorithm;
		this.setDataset(dataset);
	}

	private void setDataset(Config.Dataset dataset) {
		switch (dataset) {
			case PLANETS:
				this.databaseName = "planets";
				this.tableNames = new String[] {"WDC_planets"}; // TODO: Add all tables of the planets data set here for the IND detection task!
				break;
			case VOTER:
                this.databaseName = "huge";
                this.tableNames = new String[] {"ncvoter"}; // TODO: Add all tables of the planets data set here for the IND detection task!
                break;
			case VOTER_1K:
                this.databaseName = "huge";
                this.tableNames = new String[] {"ncvoter-1k"}; // TODO: Add all tables of the planets data set here for the IND detection task!
                break;
			case TEST:
                this.databaseName = "planets";
                this.tableNames = new String[] {"WDC_planets_modified"}; // TODO: Add all tables of the planets data set here for the IND detection task!
                break;
		}
	}

	@Override
	public String toString() {
		return "Config:\r\n\t" +
			"databaseName: " + this.databaseName + "\r\n\t" +
			"tableNames: " + this.tableNames[0];
	}
}
