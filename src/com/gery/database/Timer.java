package com.gery.database;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Timer {
	private static long startTime;
	private static long endTime;

	@SuppressLint("SimpleDateFormat")
	public static void StartTimer(String message) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		startTime = System.currentTimeMillis();
		System.out.println("TIMER-- STARTING time for: " + message + " AT: " + sdf.format(cal.getTime()));
	}

	@SuppressLint("SimpleDateFormat")
	public static void EndTimer(String message) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		endTime = System.currentTimeMillis();
		System.out.println("TIMER-- ENDING time for: " + message + " AT: " + sdf.format(cal.getTime()) + " DIFF: " + (endTime - startTime) + "MS");

	}

}
