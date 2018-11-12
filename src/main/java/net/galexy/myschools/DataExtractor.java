package net.galexy.myschools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

/**
 * Run the download process.
 */
public class DataExtractor {
	
	private File file = null;
	
	public DataExtractor(File file) {
		super();
		this.file = file;
	}
	
	
	private  static int maxThreads = 10;
	public static void setMaxThreads(int maxThreads) {
		DataExtractor.maxThreads = maxThreads;
	}

	ExecutorService executorService = new ThreadPoolExecutor(
			    maxThreads, // core thread pool size
			    maxThreads, // maximum thread pool size
			    10, // time to wait before resizing pool
			    TimeUnit.SECONDS, 
			    new ArrayBlockingQueue<Runnable>(maxThreads * 5, true),
			    new ThreadPoolExecutor.CallerRunsPolicy());
	
	public static int MIN_SCHOOL_ID = 40000;
	public static int MAX_SCHOOL_ID = 50730;

//	private final static int MIN_SCHOOL_ID = 40054;
//	private final static int MAX_SCHOOL_ID = 40054;

	public final static int DOMAIN_READING = 1;
	public final static int DOMAIN_WRITING = 2;
	public final static int DOMAIN_NARRATIVE_WRITING = 3;
	public final static int DOMAIN_SPELLING = 4;
	public final static int DOMAIN_GRAMMAR = 5;
	public final static int DOMAIN_NUMERACY = 6;

	public static Integer[] years = {2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017};
	/**
	 * @param years array of integers within the range 2010 to 2017
	 */
	public static void setYears(Integer[] years) {
		DataExtractor.years = years;
	}
	
	public static Integer[] domains = {DOMAIN_READING, DOMAIN_WRITING, DOMAIN_SPELLING, DOMAIN_GRAMMAR, DOMAIN_NUMERACY};
	
	/**
	 * @param domains Array of integer constants defined in this class and prefixed with DOMAIN_
	 */
	public static void setDomains(Integer[] domains) {
		DataExtractor.domains = domains;
	}

	public static Integer[] yearGroups = {3, 5, 7, 9};
	/**
	 * @param yearGroups array of integers in the set 3, 5, 7, 9.
	 */
	public static void setYearGroups(Integer[] yearGroups) {
		DataExtractor.yearGroups = yearGroups;
	}

	public final static List<String> bandLabels = new ArrayList<String>(30);
	
	public static String domainName(int domain) {
		switch (domain) {
		case 1:
			return "reading";
		case 2:
			return "writing";
		case 3:
			return "narrative writing";
		case 4:
			return "spelling";
		case 5:
			return "grammar";
		case 6:
			return "numeracy";
		default:
			return "" + domain;
		}
	}

	List<Future<School>> futures = new ArrayList<Future<School>>();
	
    public boolean run() {
    	
    	if (file == null) return false;
    	
    	System.out.println("Writing data to " + file.getAbsolutePath());
    	
		// Set up band labels.
		for (int i=1; i<=10; i++) {
			String number = String.format("%02d" , i);
			bandLabels.add("band_" + number + " or below");
			bandLabels.add("band_" + number);
			bandLabels.add("band_" + number + " or above");
		}

		try {
			
			FileUtils.writeStringToFile(file, csvHeader(), "UTF-8");
			
			// Fake being a chrome browser - version 60 ...
			System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");

			for (int year: years) {
				for (int schoolId=MIN_SCHOOL_ID; schoolId <= MAX_SCHOOL_ID; schoolId++) {

					System.out.println("Starting School: " + schoolId);
					futures.add(executorService.submit(new GetSchoolProfileTask(schoolId, year)));
					
					if (futures.size() >= maxThreads) {
						writeOutData();
					}
					
				}
			}
			
			while (!futures.isEmpty()) {
				writeOutData();				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			executorService.shutdownNow();
		}
		
		return true;
    	
    }

	private void writeOutData() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException x) {	
			return;
		}
		List<Future<School>> doneFutures = new ArrayList<Future<School>>();
		FUTURES: for (Future<School> future: futures) {
			try {
				if (future.isDone()) {
					doneFutures.add(future);
					School school = future.get();
					if (school.results.isEmpty()) continue FUTURES;
					for (Results results: school.results) {
						FileUtils.writeStringToFile(file, toCSV(school, results), "UTF-8", true);
					}
					System.out.println("\tFinished " + school.id + " and still working on " + (futures.size() - doneFutures.size()));
				}
			} catch (Exception e) {
				;
			}		
		}
		try {
			if (doneFutures.isEmpty()) {
				Thread.sleep(1000);
				writeOutData();
			} else {
				futures.removeAll(doneFutures);
			}
		} catch (InterruptedException x) {
			return;
		}
	} 
	
	private static String csvHeader() {
		List<String> headings = new ArrayList<String>();
		headings.add("schoolId");
		headings.add("name");
		headings.add("state");
		headings.add("sector");
		headings.add("yearRange");
		headings.add("location");
		headings.add("type");
		headings.add("fteTeachingStaff");
		headings.add("fteNonTeachingStaff");
		headings.add("enrolments");
		headings.add("boyEnrolments");
		headings.add("girlEnrolments");
		headings.add("indigenousEnrolments");
		headings.add("loteEnrolments");
		headings.add("icsea");
		headings.add("firstQuartile");
		headings.add("secondQuartile");
		headings.add("thirdQuartile");
		headings.add("fourthQuartile");
		headings.add("year");
		headings.add("yearGroup");
		headings.add("domain");		
		headings.addAll(bandLabels);
		headings.add("participated");
		headings.add("assessed");
		headings.add("exempt");
		headings.add("absent");
		headings.add("withdrawn");
		return "\"" + String.join("\",\"",headings) + "\"\n";
	}
	
	protected <T extends Object> void appendCSVValue(StringBuilder builder, T value) {
		if (value == null) {
			builder.append("\"NULL\"");
			return;
		}
		if (value instanceof String) {
			builder.append('"').append((value != null) ? value : "NULL").append('"');
			return;
		}
		builder.append((value != null) ? value : "NULL");
	}
	
	public String toCSV(School school, Results result) {
		StringBuilder builder = new StringBuilder();
		this.<String>appendCSVValue(builder, String.format("%05d" , school.id)); builder.append(",");
		this.<String>appendCSVValue(builder, school.name); builder.append(",");
		this.<String>appendCSVValue(builder, school.state); builder.append(",");
		this.<String>appendCSVValue(builder, school.sector); builder.append(",");
		this.<String>appendCSVValue(builder, school.yearRange); builder.append(",");
		this.<String>appendCSVValue(builder, school.location); builder.append(",");
		this.<String>appendCSVValue(builder, school.type); builder.append(",");
		this.<Double>appendCSVValue(builder, school.fteTeachingStaff); builder.append(",");
		this.<Double>appendCSVValue(builder, school.fteNonTeachingStaff); builder.append(",");
		this.<Integer>appendCSVValue(builder, school.enrolments); builder.append(",");
		this.<Integer>appendCSVValue(builder, school.boyEnrolments); builder.append(",");
		this.<Integer>appendCSVValue(builder, school.girlEnrolments); builder.append(",");
		this.<Integer>appendCSVValue(builder, school.indigenousEnrolments); builder.append(",");
		this.<Integer>appendCSVValue(builder, school.loteEnrolments); builder.append(",");
		this.<Integer>appendCSVValue(builder, school.icsea); builder.append(",");
		this.<Double>appendCSVValue(builder, school.firstQuartile); builder.append(",");
		this.<Double>appendCSVValue(builder, school.secondQuartile); builder.append(",");
		this.<Double>appendCSVValue(builder, school.thirdQuartile); builder.append(",");
		this.<Double>appendCSVValue(builder, school.fourthQuartile); builder.append(",");
		this.<Integer>appendCSVValue(builder,  school.year); builder.append(",");
		this.<Integer>appendCSVValue(builder,  result.yearGroup); builder.append(",");
		this.<String>appendCSVValue(builder,  domainName(result.domain)); builder.append(",");
		for (String bandLabel: bandLabels) {
			this.<Double>appendCSVValue(builder,  result.getResult(bandLabel)); builder.append(",");
		}
		this.<Integer>appendCSVValue(builder,  result.participated); builder.append(",");
		this.<Integer>appendCSVValue(builder,  result.assessed); builder.append(",");
		this.<Integer>appendCSVValue(builder,  result.exempt); builder.append(",");
		this.<Integer>appendCSVValue(builder,  result.absent); builder.append(",");
		this.<Integer>appendCSVValue(builder,  result.withdrawn);
		String line = builder.toString() + "\n";
		return line;
	}

	/**
	 * Command line execution
	 * @param args Just one argument, that being the full path and name of the file to be written to.
	 */
    public static void main( String[] args ) {
    	
    	
    	Options options = new Options();

        Option output = new Option("o", "output", true, "REQUIRED: output CSV file path and file name");
        output.setRequired(true);
        options.addOption(output);

        Option year = new Option("y", "year", true, "OPTIONAL calendar year (yyyy format)");
        year.setRequired(false);
        options.addOption(year);

        Option yearGroup = new Option("c", "class", true, "OPTIONAL class (3, 5, 7, or 9)");
        yearGroup.setRequired(false);
        options.addOption(yearGroup);

        Option test = new Option("e", "exam", true, "OPTIONAL exam (numeracy, reading, writing, spelling, or grammar)");
        test.setRequired(false);
        options.addOption(test);

        Option school = new Option("s", "school", true, "OPTIONAL school ID 5 digit number at or above 40000 (and in 2017 below 50730)");
        school.setRequired(false);
        options.addOption(school);

        Option threads = new Option("t", "threads", true, "OPTIONAL maximum number of parallel threads to use to do the extraction.");
        threads.setRequired(false);
        options.addOption(threads);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
        	reportProblem("Problem with the command line arguments: " + e.getMessage(), options);
        }
        

        String outputFilePath = cmd.getOptionValue("output");

    	File file = new File(outputFilePath);
    	
//    	File parentFile = file.getParentFile();
//    	if (parentFile == null) {
//        	reportProblem(file.getAbsolutePath() + " has no parent directory.", options);
//    	}
    	
//    	if (! file.getParentFile().exists()) {
//        	if (!file.mkdirs()) {
//            	reportProblem("Could not create folder for " + file + ".", options);
//        	}
//    	}

    	if (!file.exists()) {
    		try {
        		if (!file.createNewFile() || !file.delete()) {
                	reportProblem("Could not write to " + file + ".", options);
        		}
    		} catch (IOException e) {
            	reportProblem("IO problem with " + file + ". " + e.getMessage(), options);
    		}
    	}
    	
    	if (file.exists() && !file.delete()) {
        	reportProblem("Unable to delete " + file + ".", options);
    	}

    	if (cmd.hasOption("year")) {
        	String myYear = cmd.getOptionValue("year");
        	try {
        		Integer[] myYears = { Integer.valueOf(myYear) };
        		if (myYears[0] < 2010) {
                	reportProblem("Data is not available before 2010.", options);
        		}
        		DataExtractor.setYears(myYears);
        	} catch (Exception e) {
            	reportProblem("The year must be a 4 digit number above 2010.", options);
        	}
    	}

    	if (cmd.hasOption("class")) {
        	String myClass = cmd.getOptionValue("class");
        	try {
        		Integer[] myClasses = { Integer.valueOf(myClass) };
        		if (myClasses[0] != 3 && myClasses[0] != 5 && myClasses[0] != 7 && myClasses[0] != 9) {
                	reportProblem("The class must be one of 3, 5, 7, or 9.", options);
        		}
        		DataExtractor.setYearGroups(myClasses);
        	} catch (Exception e) {
            	reportProblem("The class must be one of 3, 5, 7, or 9. " + e.getMessage(), options);
        	}
    	}

    	if (cmd.hasOption("test")) {
        	String myTest = cmd.getOptionValue("test");
        	Integer testChoice = null;
        	switch (myTest) {
        	case "numeracy":
        		testChoice = DOMAIN_NUMERACY;
        		break;
        	case "reading":
        		testChoice = DOMAIN_READING;
        		break;
        	case "writing":
        		testChoice = DOMAIN_WRITING;
        		break;
        	case "spelling":
        		testChoice = DOMAIN_SPELLING;
        		break;
        	case "grammar":
        		testChoice = DOMAIN_GRAMMAR;
        		break;
        	default:
            	reportProblem("The test must be one of numeracy, reading, writing, spelling, or grammar.", options);
        	}
        	Integer[] myDomains = {testChoice};
        	DataExtractor.setDomains(myDomains);
    	}
    	
    	if (cmd.hasOption("school")) {
        	String mySchool = cmd.getOptionValue("school");
        	try {
        		Integer chosenSchool = Integer.valueOf(mySchool);
        		if (chosenSchool < 40000) {
                	reportProblem("No school IDs below 40000.", options);
        		}
        		DataExtractor.MIN_SCHOOL_ID = chosenSchool;
        		DataExtractor.MAX_SCHOOL_ID = chosenSchool;
        	} catch (Exception e) {
            	reportProblem("The school must be a 5 digit number above 40000.", options);
        	}
    	}

    	if (cmd.hasOption("threads")) {
        	String myThreads = cmd.getOptionValue("threads");
        	try {
        		Integer myThreadCount = Integer.valueOf(myThreads);
        		if (myThreadCount < 1) {
                	reportProblem("Thread count must be positive.", options);
        		}
        		if (myThreadCount > 100) {
                	System.out.println("Thread counts above 100 tend to be challenging for the computer.");
        		}
        		DataExtractor.setMaxThreads(myThreadCount);
        	} catch (Exception e) {
            	reportProblem("Thread count problem. " + e.getMessage(), options);
        	}
    	}
    	DataExtractor extractor = new DataExtractor(file);
    	extractor.run();
    	
    	System.exit(0);
    	
    }
    
    private static void reportProblem(String message, Options options) {
        System.out.println(message);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("utility-name", options);
        System.exit(1);
    }
}
