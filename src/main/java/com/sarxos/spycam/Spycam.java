package com.sarxos.spycam;

import com.sarxos.webcam.Webcam;


public class Spycam {

	private Webcam webcam = Webcam.getDefault();

	private boolean running = false;

	public boolean isRunning() {
		return running;
	}

	public void start() {
		// start
		running = true;
	}

	public void stop() {
		// stop
		running = false;
	}
}
