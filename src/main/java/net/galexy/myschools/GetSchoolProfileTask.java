package net.galexy.myschools;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

public class GetSchoolProfileTask implements Callable<School> {

	int schoolId;
	int year;
	
	private School school = new School();
	
	public GetSchoolProfileTask(int schoolId, int year) {
		super();
		this.schoolId = schoolId;
		this.year = year;
	}

	private URL getNaplanProfileUrl() {
		try {
			return URI.create("https://www.myschool.edu.au/school/" + String.format("%05d" , schoolId) + "/profile/" + year).toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public School call() {
	    school.id = schoolId;
	    school.year = year;
	    if (!getSchoolProfile()) return null;
	    return school;
	}
	

	public boolean getSchoolProfile() {

		try {
			
			URL url = getNaplanProfileUrl();
			String html = IOUtils.toString(url, "UTF-8");
		    if (html == null) {
		    	System.out.println("No HTML "  + url);
		    	return false;
		    }
		    Document doc = Jsoup.parse(html);

		    Elements elements = doc.select("h1");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	school.name = element.text().trim();
		    	school.state = school.name.substring(school.name.lastIndexOf(',') + 2);
		    	break;
		    }
		    
		    if (StringUtils.isBlank(school.name)) return false;
		    
		    
		    elements = doc.select("div[class=col1]:contains(School sector) + div[class=col2]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	school.sector = element.text();
		    	break;
		    }

		    elements = doc.select("div[class=col1]:contains(School type) + div[class=col2]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	school.type = element.text();
		    	break;
		    }

		    elements = doc.select("div[class=col1]:contains(Year range) + div[class=col2]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	school.yearRange = element.text();
		    	break;
		    }

		    elements = doc.select("div[class=col1]:contains(Location) + div[class=col2]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	school.location = element.text();
		    	break;
		    }

		    elements = doc.select("div[class=col1]:contains(Full-time equivalent teaching staff) + div[class=col2]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	if (StringUtils.isNotBlank(element.text())) 
		    		school.fteTeachingStaff = Double.valueOf(element.text());
		    	break;
		    }

		    elements = doc.select("div[class=col1]:contains(Full-time equivalent non-teaching staff) + div[class=col2]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	if (StringUtils.isNotBlank(element.text())) 
		    		school.fteNonTeachingStaff = Double.valueOf(element.text());
		    	break;
		    }
		    
		    elements = doc.select("div[class=col1]:contains(School ICSEA value) + div[class=col2]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	if (StringUtils.isNotBlank(element.text())) 
		    		school.icsea = Integer.valueOf(element.text());
		    	break;
		    }

		    elements = doc.select("strong:contains(Total enrolments: )");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	String content = element.text();
		    	content = content.replace("Total enrolments: ", "").trim();
		    	if (StringUtils.isNotBlank(element.text()) && !element.text().equals("-")) 
		    		school.enrolments = Integer.valueOf(content);
		    	break;
		    }

		    elements = doc.select("td[class=enrolment-label]:contains(Boys) + td[class=enrolment-count]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	if (StringUtils.isNotBlank(element.text()) && !element.text().equals("-")) 
		    		school.boyEnrolments = Integer.valueOf(element.text());
		    	break;
		    }

		    elements = doc.select("td[class=enrolment-label]:contains(Girls) + td[class=enrolment-count]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	if (StringUtils.isNotBlank(element.text()) && !element.text().equals("-")) 
		    		school.girlEnrolments = Integer.valueOf(element.text());
		    	break;
		    }

		    // Get Javascript
		    elements = doc.select("script[type='text/javascript']");
		    for (Element element: elements) {
		    	String javascript = element.toString();
		    	if (! javascript.contains("createChartindigenousStudents")) continue;
		    	int index = javascript.indexOf("\"data\":[{\"y\":");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(index);
		    	javascript = javascript.replace("\"data\":[{\"y\":", "");
		    	index = javascript.indexOf(",\"name\":");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(0, index);
		    	school.indigenousEnrolments = Integer.valueOf(javascript);
		    	break;
		    }		  
		    
		    for (Element element: elements) {
		    	String javascript = element.toString();
		    	if (! javascript.contains("createChartnonEnglishSpeakingStudents")) continue;
		    	int index = javascript.indexOf("\"data\":[{\"y\":");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(index);
		    	javascript = javascript.replace("\"data\":[{\"y\":", "");
		    	index = javascript.indexOf(",\"name\":");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(0, index);
		    	school.loteEnrolments = Integer.valueOf(javascript);
		    	break;
		    }	
		    
		    elements = doc.select("div[class=col3]:contains(Language background other than English) + div[class=col4]");
		    if (elements.isEmpty()) return false;
		    for (Element element: elements) {
		    	if (StringUtils.isNotBlank(element.text()) && !element.text().equals("-")) 
		    		school.loteEnrolments = Integer.valueOf(element.text());
		    	break;
		    }
		    
		    // Get Javascript
		    elements = doc.select("script[type='text/javascript']");
		    for (Element element: elements) {
		    	
		    	String javascript = element.toString();
		    	if (! javascript.contains("createChartstudentDistributionGraph")) continue;
		    	
		    	int index = javascript.indexOf("\"series\":[{\"type\":\"column\",");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(index);
		    	javascript = javascript.replace("\"series\":[{\"type\":\"column\",", "");
		    	index = javascript.indexOf("},{\"type\":\"column\",\"data\":");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(0, index);
		    	Values values = new Gson().fromJson("{" + javascript + "}", Values.class); 
		    	if (values.data.size() == 4) {
	    			if (values.data.get(0) != null && values.data.get(0).get("y") != null) school.firstQuartile = Double.valueOf(values.data.get(0).get("y")) / 100.0;
	    			if (values.data.get(1) != null && values.data.get(1).get("y") != null) school.secondQuartile = Double.valueOf(values.data.get(1).get("y")) / 100.0;
	    			if (values.data.get(2) != null && values.data.get(2).get("y") != null) school.thirdQuartile = Double.valueOf(values.data.get(2).get("y")) / 100.0;
	    			if (values.data.get(3) != null && values.data.get(3).get("y") != null) school.fourthQuartile = Double.valueOf(values.data.get(3).get("y")) / 100.0;
		    	}
		    	break;
		    }
		    
			for (int yearGroup: DataExtractor.yearGroups) {
				for (int domain: DataExtractor.domains) {
					
					int myDomain = domain;
					if (year == 2010 && domain == DataExtractor.DOMAIN_WRITING) myDomain = DataExtractor.DOMAIN_NARRATIVE_WRITING;
					Results results = new GetSchoolResultsTask(schoolId, yearGroup, year, myDomain).call();							
					if (results != null) {
						school.results.add(results);
					}
				}
			}
			
			if (school.results.isEmpty()) return false;

		    return true;
		    			
		} catch (Exception e) {
			return false;
		}    	
	}

	public static class Values {
        public List<Map<String, Integer>> data;
    }
	
}
