//$Id$
package com.barani.strava;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jstrava.*;
import org.jstrava.connector.JStravaV3;
import org.jstrava.entities.activity.Activity;
import org.jstrava.entities.activity.ActivityTypes;
import org.jstrava.entities.athlete.Athlete;

import com.google.gson.util.BufferedReader;

public class DistChallengeImpl {
	private static String accessToken = "e9928ba8a332458ff4d6b658025bd6d80a60e208";
	private static JStravaV3 apiClass = new JStravaV3(accessToken);
	private static List<Integer> trackedAthletes = new ArrayList<Integer>();
	
	public static void main(String []args) throws Exception {
		fetchListFromConf();
		HashMap<String, Float> hm = new HashMap<String,Float>();
		List<Activity> list = apiClass.getCurrentFriendsActivities(1, 200);
		for(Activity a : list) {
			Athlete athlete = a.getAthlete();
			Integer athleteId = athlete.getId();
			//System.out.println(a.getType()+"\t"+athlete.getFirstname()+"\t"+a.getStart_date_local());
			if(a.getMap()!=null && trackedAthletes.contains(athleteId) && (ActivityTypes.Ride.toString().toLowerCase().equals(a.getType().toLowerCase()) || a.isCommute())) {
				Float distance = a.getDistance()/1000;
				String date = a.getStart_date_local();
				date = date.split("T")[0];
				
				if(hm.containsKey(athleteId+"&#&"+date)) {
					distance = distance + hm.get(athleteId+"&#&"+date);
				}
				hm.put(athleteId+"&#&"+date, distance);
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String previousDay = sdf.format(cal.getTime());
		System.out.println(previousDay);
		printPreviousDayResults(hm, previousDay);

		
		
	}
	
	private static void printPreviousDayResults(HashMap<String, Float> hm, String previousDay) {
		for(int i=0;i<trackedAthletes.size();i++) {
			Integer athleteId = trackedAthletes.get(i);
			Athlete athlete = apiClass.findAthlete(athleteId);
			String athleteName = athlete.getFirstname()+" "+athlete.getLastname();
			Float distance = hm.get(athleteId+"&#&"+previousDay);
			if(distance == null) {
				distance = 0.0F;
			}
			System.out.println(athleteName+"\t"+distance);
		}
	}
	
	private static void fetchListFromConf() throws Exception {
		File file = new File("conf/friends.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = "";
		while((line = br.readLine())!=null) {
			String[] strArray = line.split("athletes/");
			Integer athleteId = Integer.parseInt(strArray[strArray.length-1]);
			trackedAthletes.add(athleteId);
		}
	}
	
	

}
