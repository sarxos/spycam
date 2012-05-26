package com.sarxos.spycam.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.sarxos.spycam.SpycamService;


public class SpycamStop {

	private static final ConnectionFactory FACTORY = new ActiveMQConnectionFactory(SpycamService.ENDPOINT);

	public static void main(String[] args) throws Exception {

		Connection connection = FACTORY.createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic(SpycamService.TOPIC);
		MessageProducer producer = session.createProducer(topic);

		ObjectMessage message = session.createObjectMessage();
		message.setIntProperty("command", SpycamCommand.STOP);

		producer.send(message);

		producer.close();
		session.close();
		connection.close();
	}
}
