package net.galexy.myschools;

import java.util.ArrayList;
import java.util.List;

public class School {
	
	public Integer id;
	
	public String name;
	
	public String state;
	
	public String sector;
	
	public String yearRange;
	
	public String location;
	
	public Double fteTeachingStaff;
	
	public Double fteNonTeachingStaff;

	public String type;

	public Integer enrolments;

	public Integer boyEnrolments = 0;

	public Integer girlEnrolments = 0;

	public Integer indigenousEnrolments = 0;

	public Integer loteEnrolments = 0;
	
	public Integer icsea;
	
	public Double firstQuartile;

	public Double secondQuartile;

	public Double thirdQuartile;

	public Double fourthQuartile;

	public int year;
	
	public List<Results> results = new ArrayList<Results>();
	
	@Override
	public String toString() {
		return "School [id=" + id + ", name=" + name + ", state=" + state + ", year=" + year + ", sector=" + sector + ", yearRange=" + yearRange + ", location="
				+ location + ", fteTeachingStaff=" + fteTeachingStaff + ", fteNonTeachingStaff=" + fteNonTeachingStaff
				+ ", type=" + type + ", enrolments=" + enrolments + ", boyEnrolments=" + boyEnrolments
				+ ", girlEnrolments=" + girlEnrolments + ", indigenousEnrolments=" + indigenousEnrolments
				+ ", loteEnrolments=" + loteEnrolments + ", icsea=" + icsea + ", firstQuartile=" + firstQuartile
				+ ", secondQuartile=" + secondQuartile + ", thirdQuartile=" + thirdQuartile + ", fourthQuartile="
				+ fourthQuartile + "]";
	}
	
	
	
}
