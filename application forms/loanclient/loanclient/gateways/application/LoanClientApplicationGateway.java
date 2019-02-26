package gateways.application;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import gateways.messaging.MessageReceiverGateway;
import gateways.messaging.MessageSenderGateway;
import messaging.QueueNames;
import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class LoanClientApplicationGateway extends Observable {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private BiMap<String, RequestReply<LoanRequest, LoanReply>> requestReplyHashMap = HashBiMap.create();

    public LoanClientApplicationGateway() {
        this.sender = new MessageSenderGateway();
        this.receiver = new MessageReceiverGateway();

        /**
         * LoanReply
         */
        MessageConsumer mc = receiver.consume(QueueNames.loanReply);
        try {
            mc.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    LoanReply loanReply;
                    String messageId;

                    try {
                        loanReply = (LoanReply)((ObjectMessage) message).getObject();
                        messageId = message.getJMSMessageID();
                        RequestReply<LoanRequest, LoanReply> requestReply = requestReplyHashMap.get(messageId);
                        requestReply.setReply(loanReply);



                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }

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
