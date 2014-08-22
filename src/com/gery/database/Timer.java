package com.gery.database;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Timer {
	private static long startTime;
	private static long endTime;
	
	public static void StartTimer(String message)
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		startTime = System.currentTimeMillis();
		System.out.println("TIMER-- STARTING time for: "+message+ " AT: " +sdf.format(cal.getTime()));
	}
	
	public static void EndTimer(String message)
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		endTime = System.currentTimeMillis();
		System.out.println("TIMER-- ENDING time for: "+message+ " AT: " +sdf.format(cal.getTime())+ " DIFF: " +(endTime - startTime)+"MS");
		
	}
	

}
