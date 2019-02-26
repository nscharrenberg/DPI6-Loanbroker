package loanclient.loanclient.gateways.application;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import loanclient.loanclient.gateways.messaging.MessageReceiverGateway;
import loanclient.loanclient.gateways.messaging.MessageSenderGateway;
import messaging.QueueNames;
import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.*;
import java.util.Map;
import java.util.Observable;

public abstract class LoanClientApplicationGateway {
    private MessageSenderGateway sender;

    private BiMap<String, RequestReply<LoanRequest, LoanReply>> requestReplyHashMap = HashBiMap.create();

    public LoanClientApplicationGateway() {
        this.sender = new MessageSenderGateway();
        MessageReceiverGateway receiver = new MessageReceiverGateway();

        /**
         * LoanReply
         */
        MessageConsumer mc = receiver.consume(QueueNames.loanReply);
        try {
            mc.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    LoanReply loanReply = null;
                    String messageId = null;
                    RequestReply<LoanRequest, LoanReply> requestReply = null;


                    try {
                        loanReply = (LoanReply)((ObjectMessage) message).getObject();
                        messageId = message.getJMSMessageID();
                        requestReply = requestReplyHashMap.get(messageId);
                        requestReply.setReply(loanReply);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                    onLoanReplyArrived(requestReply);
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public String sendLoanRequest(LoanRequest loanRequest) {
        RequestReply<LoanRequest, LoanReply> requestReply = new RequestReply<>(loanRequest, null);
        String messageId = sender.produce(QueueNames.loanRequest, loanRequest, null);

        if(messageId != null) {
            requestReplyHashMap.put(messageId, requestReply);
        }

        return messageId;
    }

    public BiMap<String, RequestReply<LoanRequest, LoanReply>> getRequestReplyHashMap() {
        return requestReplyHashMap;
    }

    public abstract void onLoanReplyArrived(RequestReply<LoanRequest, LoanReply> requestReply);
}
