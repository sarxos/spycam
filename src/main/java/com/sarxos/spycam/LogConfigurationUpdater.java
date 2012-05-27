package com.sarxos.spycam;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;


/**
 * The goal of this class is to perform periodic SLF4J update when configuration
 * file has been changed.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class LogConfigurationUpdater extends Thread {

	/**
	 * Logger instance.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(LogConfigurationUpdater.class);

	/**
	 * Logger configuration file.
	 */
	private File file = null;

	/**
	 * Update interval.
	 */
	private int interval = 10000;

	public LogConfigurationUpdater(File file) {
		super("log-config-updater");

		if (!file.exists()) {
			throw new IllegalArgumentException("File " + file + " does not exist");
		}

		this.file = file;
		this.setDaemon(true);
		this.start();
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	@Override
	public void run() {

		LOG.info("Log configuration updater has been started");

		long updated = 0;
		long tmp = 0;

		while (true) {

			tmp = file.lastModified();

			if (tmp != updated) {
				configure();
				updated = tmp;
			}

			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Configure SLF4J.
	 */
	protected void configure() {

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);

			// the context was probably already configured by default
			// configuration rules, so it needs to be reset
			context.reset();

			configurator.doConfigure(file);

		} catch (JoranException e) {
			LOG.error("Joran configuration exception", e);
			e.printStackTrace();
		}
	}
}
