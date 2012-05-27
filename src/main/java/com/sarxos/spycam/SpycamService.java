package com.sarxos.spycam;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarxos.spycam.jms.SpycamCommand;


/**
 * Spy service
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class SpycamService implements Daemon, MessageListener {

	private static final Logger LOG = LoggerFactory.getLogger(SpycamService.class);

	public static final String ENDPOINT = "tcp://localhost:61616";
	public static final String TOPIC = "spycam";

	private Spycam spy = new Spycam();
	private BrokerService broker = new BrokerService();

	private ConnectionFactory factory = null;
	private Connection connection = null;
	private Session session = null;
	private MessageConsumer consumer = null;

	public SpycamService() throws Exception {
		LOG.info("Spy service ctor");
	}

	@Override
	public void destroy() {
		LOG.info("Spy service destroy");
	}

	@Override
	public void init(DaemonContext ctx) throws DaemonInitException, Exception {
		LOG.info("Spy service init");
	}

	@Override
	public void start() throws Exception {
		LOG.info("Spy service start");

		broker.addConnector("tcp://localhost:61616");
		broker.setPersistent(false);
		broker.start();

		factory = new ActiveMQConnectionFactory(ENDPOINT);
		connection = factory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(session.createTopic(TOPIC));
		consumer.setMessageListener(this);

		connection.start();

		spy.setUploadURL("http://localhost/upload/");
		spy.start();
	}

	@Override
	public void stop() throws Exception {
		LOG.info("Spy service stop");

		spy.stop();
		session.close();
		consumer.close();
		connection.close();
		broker.stop();
	}

	public static void main(String[] args) throws Exception {

		SpycamService svc = new SpycamService();
		svc.start();

		while (svc.spy.isRunning()) {
			Thread.sleep(5000);
		}
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof ObjectMessage) {

			int command = -1;
			try {
				command = ((ObjectMessage) message).getIntProperty("command");
			} catch (JMSException e) {
				e.printStackTrace();
			}

			System.out.println("Command is: " + command);

			switch (command) {
				case SpycamCommand.STOP:
					try {
						stop();
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;

				// only one command for now, but i can add more in the future

				default:
					throw new RuntimeException("Unknown command");
			}

		} else {
			throw new RuntimeException("Incorrect message " + message.getClass());
		}
	}

}
