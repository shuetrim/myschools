# myschools

MySchools data extraction from the ACARA website (https://www.myschool.edu.au/)

This project is set up as a Maven Eclipse project under Eclipse Oxygen.

When using Eclipse to export the project to the JAR file, ensure that you extract required libraries into generated JAR to make portability less of an issue.

If you export the project to a runnable JAR file, ``myschools.jar``, the JAR file will be runnable using Java 1.8 or above using:

```
java -Xmx1024m -jar myschools.jar --output=~/datafile.csv
```

where the ``~/datafile.csv`` should be the path and file name of the file that you want the data stored in.

Note that there seems to be a slow memory leak at the moment so I tend to run the project with 1GB of ram and to do 
the data extraction for one year at a time.

To see all commandline options, run:

```
java -jar myschools.jar
```

