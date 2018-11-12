# myschools
MySchools data extraction

This project is set up as a Maven Eclipse project under Eclipse Oxygen.

If you export the project to a runnable JAR file, myschools.jar, the JAR file will be runnable using Java 1.8 or above using:

java -jar myschools.jar ~/datafile.csv

where the ~/datafile.csv should be the full path and file name of the file that you want the data stored in.

Note that there seems to be a slow memory leak at the moment so I tend to run the project with 1GB of ram and to do 
the data extraction for one year at a time.

