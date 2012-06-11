package com.sarxos.spycam;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jhlabs.image.BoxBlurFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.PixelUtils;
import com.sarxos.image.ImageUtils;
import com.sarxos.webcam.Webcam;


public class MotionDetector implements ThreadFactory {

	private static final Logger LOG = LoggerFactory.getLogger(MotionDetector.class);

	/**
	 * Run motion detector.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private class Runner implements Runnable {

		@Override
		public void run() {
			LOG.debug("Motion detector runner has been started");
			running = true;
			while (running) {
				detect();
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Change motion to false after specified number of seconds.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private class Changer implements Runnable {

		@Override
		public void run() {
			LOG.debug("Motion change has been sheduled in " + inertia + "ms");
			try {
				Thread.sleep(inertia);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			motion = false;
		}
	}

	private boolean running = false;

	/**
	 * Is motion?
	 */
	private boolean motion = false;

	/**
	 * Previously captured image.
	 */
	private BufferedImage previous = null;

	/**
	 * Webcam to be used to detect motion.
	 */
	private Webcam webcam = null;

	/**
	 * Motion check interval (1000 ms by default).
	 */
	private int interval = 1000;

	/**
	 * Pixel intensity threshold (0 - 255).
	 */
	private int threshold = 10;

	/**
	 * How long motion is valid.
	 */
	private int inertia = 10000;

	private int strength = 0;

	/**
	 * Blur filter instance.
	 */
	private BoxBlurFilter blur = new BoxBlurFilter(3, 3, 1);

	/**
	 * Grayscale filter instance.
	 */
	private GrayscaleFilter gray = new GrayscaleFilter();

	/**
	 * Thread number for thread factory.
	 */
	private int number = 0;

	/**
	 * Executor.
	 */
	private ExecutorService executor = Executors.newCachedThreadPool(this);

	/**
	 * 
	 * @param webcam web camera instance
	 * @param threshold intensity threshold (0 - 255)
	 * @param inertia how long motion is valid (seconds)
	 */
	public MotionDetector(Webcam webcam, int threshold, int inertia) {
		this.webcam = webcam;
		this.threshold = threshold;
		this.inertia = inertia * 1000;
	}

	public void start() {
		LOG.debug("Starting motion detector");
		executor.submit(new Runner());
	}

	public void stop() {
		running = false;
	}

	protected void detect() {

		LOG.debug("Detect motion");

		if (motion) {
			LOG.debug("There is motion, no need to check");
			return;
		}

		BufferedImage current = webcam.getImage();

		current = blur.filter(current, null);
		current = gray.filter(current, null);

		if (previous != null) {

			int w = current.getWidth();
			int h = current.getHeight();

			int strength = 0;

			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {

					int c = current.getRGB(i, j);
					int p = previous.getRGB(i, j);

					int rgb = PixelUtils.combinePixels(c, p, PixelUtils.DIFFERENCE);

					int cr = (rgb & 0x00ff0000) >> 16;
					int cg = (rgb & 0x0000ff00) >> 8;
					int cb = (rgb & 0x000000ff);

					int max = Math.max(Math.max(cr, cg), cb);

					if (max > threshold) {

						if (!motion) {
							executor.submit(new Changer());
						}

						motion = true;
						strength++;
					}
				}
			}

			this.strength = strength;
		}

		previous = current;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public Webcam getWebcam() {
		return webcam;
	}

	public boolean isMotion() {
		if (!running) {
			LOG.warn("Motion cannot be detected when detector is not running!");
		}
		return motion;
	}

	public int getMotionStrength() {
		return strength;
	}

	public static void main(String[] args) throws IOException {

		BoxBlurFilter blur = new BoxBlurFilter(3, 3, 1);
		GrayscaleFilter gray = new GrayscaleFilter();

		BufferedImage a = ImageIO.read(new File("a.jpg"));
		BufferedImage b = ImageIO.read(new File("b.jpg"));

		a = blur.filter(a, null);
		a = gray.filter(a, null);

		b = blur.filter(b, null);
		b = gray.filter(b, null);

		BufferedImage c = ImageUtils.diff(a, b, 10);

		ImageIO.write(a, "jpg", new File("ap.jpg"));
		ImageIO.write(b, "jpg", new File("bp.jpg"));
		ImageIO.write(c, "jpg", new File("c.jpg"));
	}

	@Override
	public Thread newThread(Runnable runnable) {
		Thread t = new Thread(runnable, "motion-detector-" + (number++));
		t.setDaemon(true);
		return t;
	}
}
