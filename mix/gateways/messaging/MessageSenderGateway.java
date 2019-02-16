package gateways.messaging;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import java.io.Serializable;

public class MessageSenderGateway extends MessageConnectionGateway {
    private MessageProducer producer;

    public MessageSenderGateway(String channel) {
        super(channel);

        try {
            this.producer = this.getSession().createProducer(this.getDestination());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public String produce(Serializable obj, String messageId) {
        try {
            Message msg = this.getSession().createObjectMessage(obj);

            if(messageId != null) {
                msg.setJMSCorrelationID(messageId);
            }

            producer.send(msg);
            return msg.getJMSMessageID();
        } catch (JMSException e) {
            e.printStackTrace();
            return null;
        }
    }
}
