package gateways.application;

import gateways.messaging.MessageReceiverGateway;
import gateways.messaging.MessageSenderGateway;
import messaging.requestreply.RequestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.HashMap;
import java.util.Map;

public class LoanClientApplicationGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private Map<String, RequestReply<LoanRequest, LoanReply>> requestReplyHashMap = new HashMap<>();

    public LoanClientApplicationGateway(String senderChannel, String receiverChannel) {
        this.sender = new MessageSenderGateway(senderChannel);
        this.receiver = new MessageReceiverGateway(receiverChannel);

        this.receiver.consume(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                LoanReply loanReply;
                String correlationId;

                try {
                    loanReply = (LoanReply)((ObjectMessage) message).getObject();
                    correlationId = message.getJMSCorrelationID();

                    RequestReply<LoanRequest, LoanReply> requestReply = requestReplyHashMap.get(correlationId);
                    requestReply.setReply(loanReply);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendLoanRequest(LoanRequest loanRequest) {
        RequestReply<LoanRequest, LoanReply> requestReply = new RequestReply<>(loanRequest, null);

        String messageId = this.sender.produce(loanRequest, null);

        if(messageId != null) {
            requestReplyHashMap.put(messageId, requestReply);
        }
    }

    public Map<String, RequestReply<LoanRequest, LoanReply>> getRequestReplyHashMap() {
        return requestReplyHashMap;
    }
}
