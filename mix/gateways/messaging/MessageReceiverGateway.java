package gateways.messaging;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

public class MessageReceiverGateway extends MessageConnectionGateway {
    private MessageConsumer consumer;


    public MessageReceiverGateway(String channel) {
        super(channel);

        try {
            this.consumer = this.getSession().createConsumer(this.getDestination());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void consume(MessageListener listener) {

        try {
            this.getConnection().start();
            this.consumer.setMessageListener(listener);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
