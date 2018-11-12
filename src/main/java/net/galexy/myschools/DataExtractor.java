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
	
	
	private int maxThreads = 25;
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
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
    public static void main( String[] args )
    {
    	
    	if (args.length == 0) {
    		System.out.println("Include the full path and file name of the file where the downloaded data will be stored.");
    		return;
    	}
    	
    	File file = new File(args[0]);
    	
    	if (! file.getParentFile().exists()) {
        	if (!file.mkdirs()) {
        		System.out.println("Could not create folder for " + file + ".");
        		return;
        	}
    	}

    	if (!file.exists()) {
    		try {
        		if (!file.createNewFile() || !file.delete()) {
            		System.out.println("Cannot write to " + file + ".");
            		return;
        		}
    		} catch (IOException e) {
    			System.out.println("Cannot write to " + file + ".");
        		return;
    		}
    	}
    	
    	if (file.exists() && !file.delete()) {
    		System.out.println("Unable to delete a pre-existing old version of " + file + ".");
    		return;
    	}

//    	if (! file.canWrite()) {
//    		System.out.println("Unable to update " + file + ".");
//    		return;
//    	}
    	
    	new DataExtractor(file).run();
    	return;

    }
}
