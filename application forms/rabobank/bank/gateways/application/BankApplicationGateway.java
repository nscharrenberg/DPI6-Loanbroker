package rabobank.bank.gateways.application;

import abnamro.bank.gateways.messaging.MessageReceiverGateway;
import abnamro.bank.gateways.messaging.MessageSenderGateway;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import messaging.QueueNames;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public abstract class BankApplicationGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private BiMap<BankInterestRequest, String> bankInterestRequestWithMessageId = HashBiMap.create();
    private BiMap<String, Integer> aggregatorId = HashBiMap.create();

    public BankApplicationGateway() {
        this.sender = new MessageSenderGateway();
        this.receiver = new MessageReceiverGateway();

        MessageConsumer consumer = receiver.consume(QueueNames.bankInterestRequest + "_RABOBANK");
        try {
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    BankInterestRequest bankInterestRequest = null;
                    String correlationId = null;

                    try {
                        bankInterestRequest = (BankInterestRequest)((ObjectMessage) message).getObject();
                        correlationId = message.getJMSCorrelationID();
                        bankInterestRequestWithMessageId.put(bankInterestRequest, correlationId);
                        aggregatorId.put(correlationId, message.getIntProperty(message.getJMSCorrelationID()));
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                    OnInterestRequestArrived(bankInterestRequest);
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendInterestReply(RequestReply<BankInterestRequest, BankInterestReply> bankRequestReply) {
        String correlationId = bankInterestRequestWithMessageId.get(bankRequestReply.getRequest());
        sender.produce(QueueNames.bankInterestReply, bankRequestReply.getReply(), correlationId, 0);
    }

    public abstract void OnInterestRequestArrived(BankInterestRequest bankInterestRequest);
}
