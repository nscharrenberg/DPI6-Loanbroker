package gateways.application;

import gateways.messaging.MessageReceiverGateway;
import gateways.messaging.MessageSenderGateway;
import messaging.QueueNames;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class BankApplicationGateway extends Observable {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private Map<RequestReply<BankInterestRequest, BankInterestReply>, String> bankInterestReply = new HashMap<>();

    public BankApplicationGateway() {
        this.sender = new MessageSenderGateway(QueueNames.bankInterestReply);
        this.receiver = new MessageReceiverGateway(QueueNames.bankInterestRequest);

        this.receiver.consume(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                BankInterestRequest bankInterestRequest = null;
                String correlationId = null;

                try {
                    bankInterestRequest = (BankInterestRequest)((ObjectMessage) message).getObject();
                    correlationId = message.getJMSMessageID();
                    System.out.println("BankApplication Received ID: " + correlationId);

                    RequestReply<BankInterestRequest, BankInterestReply> requestReply = new RequestReply<>(bankInterestRequest, null);
                    bankInterestReply.put(requestReply, correlationId);

                    setChanged();
                    notifyObservers(correlationId);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendBankInterestReply(RequestReply<BankInterestRequest, BankInterestReply> requestReply) {
        String messageId = bankInterestReply.get(requestReply);
        sender.produce(requestReply.getReply(), messageId);
        System.out.println("BankApplication Sended ID: " + messageId);

        setChanged();
        notifyObservers(messageId);
    }

    public Map<RequestReply<BankInterestRequest, BankInterestReply>, String> getBankInterestReply() {
        return bankInterestReply;
    }
}
