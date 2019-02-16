package gateways.application;

import gateways.messaging.MessageReceiverGateway;
import gateways.messaging.MessageSenderGateway;
import messaging.QueueNames;
import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class LoanClientApplicationGateway extends Observable {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private Map<String, RequestReply<LoanRequest, LoanReply>> requestReplyHashMap = new HashMap<>();

    public LoanClientApplicationGateway() {
        this.sender = new MessageSenderGateway(QueueNames.loanRequest);
        this.receiver = new MessageReceiverGateway(QueueNames.loanReply);

        this.receiver.consume(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                LoanReply loanReply;
                String correlationId;

                try {
                    loanReply = (LoanReply)((ObjectMessage) message).getObject();
                    correlationId = message.getJMSCorrelationID();

                    System.out.println("LoanClientBroker Received ID: " + correlationId);

                    RequestReply<LoanRequest, LoanReply> requestReply = requestReplyHashMap.get(correlationId);
                    requestReply.setReply(loanReply);

                    setChanged();
                    notifyObservers(correlationId);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String sendLoanRequest(LoanRequest loanRequest) {
        RequestReply<LoanRequest, LoanReply> requestReply = new RequestReply<>(loanRequest, null);

        String messageId = this.sender.produce(loanRequest, null);
        System.out.println("LoanClientBroker Sended ID: " + messageId);
        int test = 1;

        if(messageId != null) {
            requestReplyHashMap.put(messageId, requestReply);
        }

        setChanged();
        notifyObservers(messageId);

        return messageId;
    }

    public Map<String, RequestReply<LoanRequest, LoanReply>> getRequestReplyHashMap() {
        return requestReplyHashMap;
    }
}
