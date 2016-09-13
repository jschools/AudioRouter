package com.schoovello.audiorouter.error;

public class ExceptionHandler {

	public static void handleException(Exception e) {
		e.printStackTrace();
		System.exit(-1);
	}
}
