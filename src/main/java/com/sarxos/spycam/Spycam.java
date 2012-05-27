package com.sarxos.spycam;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarxos.webcam.Webcam;


/**
 * Spy engine. This class is responsible for taking pictures in specified time
 * interval and uploading them to server. Pictures are being stored as JPG
 * images.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class Spycam implements Runnable, ThreadFactory {

	private static final Logger LOG = LoggerFactory.getLogger(Spycam.class);

	private Webcam webcam = Webcam.getDefault();

	private boolean running = false;
	private URL url = null;
	private ExecutorService executor = Executors.newSingleThreadExecutor(this);
	private DefaultHttpClient client = new DefaultHttpClient();

	public Spycam() {
		client.setRedirectStrategy(new BrowserRedirectStrategy());
	}

	public Spycam(String uploadUrl) {
		this();
		setUploadURL(uploadUrl);
	}

	public boolean isRunning() {
		return running;
	}

	public void start() {
		LOG.debug("Spycam start");
		webcam.open();
		executor.submit(this);
		running = true;
	}

	public void stop() {
		LOG.debug("Spycam stop");
		running = false;
		webcam.close();
	}

	@Override
	public void run() {
		while (running) {
			tick();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void tick() {
		LOG.info("Spycam tick picture");
		try {
			File tmp = File.createTempFile("sct", null);
			BufferedImage image = webcam.getImage();
			ImageIO.write(image, "JPG", tmp);
			upload(tmp);
			tmp.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setUploadURL(String url) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private void upload(File file) throws ClientProtocolException, IOException, ParseException, URISyntaxException {

		LOG.debug("Spycam picture upload");

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("picture", new FileBody(file, "image/jpg"));
		entity.addPart("passwd", new StringBody("test1234"));

		HttpPost post = new HttpPost(url.toURI());
		post.setEntity(entity);

		HttpResponse response = client.execute(post);
		String picture = EntityUtils.toString(response.getEntity());

		LOG.debug("Tick picture stored as " + picture);
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, "spycam-thread");
		t.setDaemon(true);
		return t;
	}

}
