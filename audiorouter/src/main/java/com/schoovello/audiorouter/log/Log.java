package com.schoovello.audiorouter.log;

public class Log {

	private static long sStartTime;

	public static void startNow() {
		sStartTime = System.currentTimeMillis();
	}

	public static void d(String message) {
		System.out.println(getCurrentTimeFormatted() + " " + message);
	}

	public static String getCurrentTimeFormatted() {
		long diff = System.currentTimeMillis() - sStartTime;

		int minutes = (int) (diff / (1000 * 60));
		diff -= minutes * 1000 * 60;
		int seconds = (int) (diff / 1000);
		diff -= seconds * 1000;
		int millis = (int) diff;

		return String.format("%02d:%02d.%03d", minutes, seconds, millis);
	}
}
