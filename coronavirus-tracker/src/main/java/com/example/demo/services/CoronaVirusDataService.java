package com.example.demo.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.models.LocationStats;

@Service //it is a spring service
public class CoronaVirusDataService { //the stereotype service will make this class a spring service

	
	private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	
	public List <LocationStats> allStats = new ArrayList<>();
	public int extreme;
	public int latestCases;
	public int prevDayCases;
	
	
	public List<LocationStats> getAllStats(){
		return allStats;
	}
	
	@PostConstruct //call the method after Spring creates an instance of the class (after the application starts)
	@Scheduled(cron = "* * 1 * * *") //runs the method on a scheduled basis
	public void fetchVirusData() throws IOException, InterruptedException //throwsException when client.send fails
	{
		List <LocationStats> newStats = new ArrayList<>();
		HttpClient client = HttpClient.newHttpClient(); //to call the http link
		HttpRequest request = HttpRequest.newBuilder() //where to make a http request
				.uri(URI.create(VIRUS_DATA_URL)) //creates a URI of the given string passed
				.build();
		HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
		StringReader csvBodyReader = new StringReader(httpResponse.body()); //getting the string using httpclient and reading the string using the StringReader class
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader); //parsing it using the open source library for parsing csv file which is Apache csv
		for (CSVRecord record : records) { //looping through the records
			LocationStats locationStat = new LocationStats();
		    locationStat.setState(record.get("Province/State")); //pulling out the first column value
		    locationStat.setCountry(record.get("Country/Region"));
            latestCases = Integer.parseInt(record.get(record.size() - 1));
            prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
            if(latestCases>=1000000)
            	extreme = latestCases;
              
            locationStat.setDanger(extreme);
            
            
           // List<Integer> a1 = new ArrayList<Integer>(); 
           // int sortCases = Integer.parseInt(record.get(record.size() - 1));
           // a1.add(sortCases);
           // Collections.reverse(a1);
           // System.out.println(a1);
            newStats.add(locationStat);
            
		    }
		
		   
		
		this.allStats = newStats;
	}
}
