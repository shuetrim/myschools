package net.galexy.myschools;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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

public class GetSchoolResultsTask implements Callable<Results> {

	int schoolId;
	int yearGroup;
	int year;
	int domain;
	
	public GetSchoolResultsTask(int schoolId, int yearGroup, int year, int domain) {
		super();
		this.schoolId = schoolId;
		this.yearGroup = yearGroup;
		this.year = year;
		this.domain = domain;
	}

	Results results = new Results();
	
	public Results call() {

	    results.id = schoolId;
	    results.year = year;
	    results.yearGroup = yearGroup;
	    results.domain = domain;
	    if (!getNaplanResults()) return null;
	    return results;

	}
	
	public boolean getNaplanResults() {

		try {
			
			URL url = getNaplanBandsUrl();
			String html = IOUtils.toString(url, "UTF-8");
		    if (html == null) {
		    	return false;
		    }
		    Document doc = Jsoup.parse(html);

		    // Get Javascript
		    Elements elements = doc.select("script[type='text/javascript']");
		    for (Element element: elements) {
		    	
		    	String javascript = element.toString();
		    	if (! javascript.contains("createChartnaplanBands")) continue;
		    	
		    	int index = javascript.indexOf("\"series\":[{\"type\":\"column\",\"data\":");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(index);
		    	javascript = javascript.replace("\"series\":[{\"type\":\"column\",\"data\":", "");
		    	index = javascript.indexOf(",\"color\":\"#a45e71\"}");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(0, index);
		    	Values values = new Gson().fromJson("{'data':" + javascript + "}", Values.class); 
		    	List<Integer> percentages = new ArrayList<Integer>();
		    	for (Map<String, Integer> map: values.data) {
			    	percentages.add(map.get("y"));
		    	}
		    	
		    	javascript = element.toString();
		    	index = javascript.indexOf("\"xAxis\":[{\"categories\":");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(index);
		    	javascript = javascript.replace("\"xAxis\":[{\"categories\":", "");
		    	index = javascript.indexOf(",\"title\":{\"margin\":20,\"text\":\"Band\"},\"tickLength\":0}]");
		    	if (index == -1) continue;
		    	javascript = javascript.substring(0, index);

		    	Bands bands = new Gson().fromJson("{'data':" + javascript + "}", Bands.class); 
		    	int i = 0;
		    	for (String band: bands.data) {
		    		results.addBand(band);
		    		results.results.add(percentages.get(i));
		    		i++;
		    	}

		    	break;
		    }
		    
		    if (results.bands.isEmpty()) return false;
		    
		    elements = doc.select("span[id='SelectedSchoolParticipated']");
		    for (Element element: elements) {
		    	String value = element.text();
		    	if (StringUtils.isBlank(value)) continue;
	    		value = value.replace("%", "");
	    		results.participated = Integer.valueOf(value);
	    		break;
		    }

		    elements = doc.select("span[id='SelectedSchoolAssessed']");
		    for (Element element: elements) {
		    	String value = element.text();
		    	if (StringUtils.isBlank(value)) continue;
	    		value = value.replace("%", "");
	    		results.assessed = Integer.valueOf(value);
	    		break;
		    }

		    elements = doc.select("span[id='SelectedSchoolExempt']");
		    for (Element element: elements) {
		    	String value = element.text();
		    	if (StringUtils.isBlank(value)) continue;
	    		value = value.replace("%", "");
	    		results.exempt = Integer.valueOf(value);
	    		break;
		    }

		    elements = doc.select("span[id='SelectedSchoolAbsent']");
		    for (Element element: elements) {
		    	String value = element.text();
		    	if (StringUtils.isBlank(value)) continue;
	    		value = value.replace("%", "");
	    		results.absent = Integer.valueOf(value);
	    		break;
		    }

		    elements = doc.select("span[id='SelectedSchoolWithdrawn']");
		    for (Element element: elements) {
		    	String value = element.text();
		    	if (StringUtils.isBlank(value)) continue;
	    		value = value.replace("%", "");
	    		results.withdrawn = Integer.valueOf(value);
	    		break;
		    }
		    

		    return true;
		    			
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
    	
	}
	
	private URL getNaplanBandsUrl() {
		try {
			return URI.create("https://www.myschool.edu.au/school/" + String.format("%05d" , schoolId) + "/naplan/bands/" + year + "?SchoolYearId=" + yearGroup + "&DomainId=" + domain).toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public static class Values {
        public List<Map<String, Integer>> data;
    }

	public static class Bands {
	    public List<String> data;
	}
	
}
