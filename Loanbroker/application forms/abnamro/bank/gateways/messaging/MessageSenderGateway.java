package abnamro.bank.gateways.messaging;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.naming.NamingException;
import java.io.Serializable;

public class MessageSenderGateway extends MessageConnectionGateway {
    private MessageProducer producer;

    public MessageSenderGateway() {
    }

    public String produce(String queue, Serializable obj, String messageId, int aggregatorId) {
        try {
            setDestination((Destination) getJndiContext().lookup(queue));
            this.producer = this.getSession().createProducer(this.getDestination());
            Message msg = this.getSession().createObjectMessage(obj);

            if(messageId != null) {
                msg.setJMSCorrelationID(messageId);
            }

            if(aggregatorId != 0 && messageId != null) {
                msg.setIntProperty(msg.getJMSCorrelationID(), aggregatorId);
            }

            producer.send(msg);
            return msg.getJMSMessageID();
        } catch (JMSException | NamingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
