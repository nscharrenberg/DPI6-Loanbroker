package gateways.application;

import gateways.messaging.MessageReceiverGateway;
import gateways.messaging.MessageSenderGateway;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.HashMap;
import java.util.Map;

public class BankApplicationGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private Map<RequestReply<BankInterestRequest, BankInterestReply>, String> bankInterestReply = new HashMap<>();

    public BankApplicationGateway(String senderChannel, String receiverChannel) {
        this.sender = new MessageSenderGateway(senderChannel);
        this.receiver = new MessageReceiverGateway(receiverChannel);

        this.receiver.consume(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                BankInterestRequest bankInterestRequest = null;
                String correlationId = null;

                try {
                    bankInterestRequest = (BankInterestRequest)((ObjectMessage) message).getObject();
                    correlationId = message.getJMSMessageID();

                    RequestReply<BankInterestRequest, BankInterestReply> requestReply = new RequestReply<>(bankInterestRequest, null);

                    bankInterestReply.put(requestReply, correlationId);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendBankInterestReply(RequestReply<BankInterestRequest, BankInterestReply> requestReply) {
        String messageId = bankInterestReply.get(requestReply);
        sender.produce(requestReply, messageId);
    }
}
