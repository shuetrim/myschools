package net.galexy.myschools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Results {
	
	public Integer id;

	public int year;
	
	public int yearGroup;
	
	public int domain;
	
	List<String> bands = new ArrayList<String>(10);

	List<Integer> results = new ArrayList<Integer>(10);
	
	public Integer participated;
	public Integer assessed;
	public Integer exempt;
	public Integer absent;
	public Integer withdrawn;
	
	public void addBand(String band) {
		if (StringUtils.isNotBlank(band)) {
			if (band.length() == 1) band = "0" + band;
			if (band.length() > 2) band = "0" + band;
		}
		bands.add("band_" + band);
	}
	
	public Double getResult(String bandLabel) {
		int index = bands.indexOf(bandLabel);
		if (index < 0) return null;
		if (index >= results.size()) return null;
		if (results.get(index) == null) return null;
		return (Double.valueOf(results.get(index)) / 100.0);
	}
	
	private String resultsToString() { 
		
		StringBuilder builder = new StringBuilder();
		Collections.sort(bands);
		for (String band: bands) {
			if (StringUtils.isNotBlank(band)) {
				builder.append(" ").append(band).append("=").append(getResult(band));
			}
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return "Results [id=" + id + ", year=" + year + ", yearGroup=" + yearGroup + ", domain=" + domain + ", results:" + resultsToString() + "]";
	}

}
