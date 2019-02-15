package gateways.application;

import gateways.messaging.MessageReceiverGateway;
import gateways.messaging.MessageSenderGateway;
import messaging.QueueNames;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.*;

public class LoanBrokerApplicationGateway extends Observable {
    private MessageSenderGateway clientSender;
    private MessageReceiverGateway clientReceiver;
    private MessageSenderGateway bankSender;
    private MessageReceiverGateway bankReceiver;

    // Mapping the messaging id's of BankInterestRequest with LoanRequest
    private Map<String, String> requestsWithMessageIds = new HashMap<>();
    private Map<String, LoanRequest> loanRequestWithMessageIds = new HashMap<>();
    private Map<String, BankInterestRequest> bankInterestRequestWithMessageIds = new HashMap<>();
    private List<RequestReply<BankInterestRequest, LoanReply>> requestReplies = new ArrayList<>();

    public LoanBrokerApplicationGateway() {
        this.clientSender = new MessageSenderGateway(QueueNames.bankInterestRequest);
        this.clientReceiver = new MessageReceiverGateway(QueueNames.loanRequest);
        this.bankSender = new MessageSenderGateway(QueueNames.loanReply);
        this.bankReceiver = new MessageReceiverGateway(QueueNames.bankInterestReply);

        /**
         * LoanRequest Broker
         */
        this.clientReceiver.consume(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                LoanRequest loanRequest = null;
                String correlationId = null;

                try {
                    loanRequest = (LoanRequest)((ObjectMessage) message).getObject();
                    correlationId = message.getJMSMessageID();
                    loanRequestWithMessageIds.put(correlationId, loanRequest);

                    BankInterestRequest bankInterestRequest = new BankInterestRequest(loanRequest.getAmount(), loanRequest.getTime());
                    RequestReply<BankInterestRequest, LoanReply> requestReply = new RequestReply<>(bankInterestRequest, null);
                    requestReplies.add(requestReply);

                    String messageId = clientSender.produce(bankInterestRequest, null);

                    if(messageId != null) {
                        bankInterestRequestWithMessageIds.put(messageId, bankInterestRequest);
                    }

                    requestsWithMessageIds.put(messageId, correlationId);

                    Map<String, Object> maps = new HashMap<>();
                    maps.put("requestReply", requestReply);
                    maps.put("loanRequest", loanRequestWithMessageIds.get(messageId));

                    setChanged();
                    notifyObservers(maps);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * BankInterestReply Broker
         */
        this.bankReceiver.consume(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                BankInterestReply bankInterestReply = null;
                String correlationId = null;

                try {
                    bankInterestReply = (BankInterestReply) ((ObjectMessage) message).getObject();
                    correlationId = message.getJMSCorrelationID();

                    String messageId = requestsWithMessageIds.get(correlationId);
                    BankInterestRequest bankInterestRequest = bankInterestRequestWithMessageIds.get(correlationId);
                    LoanReply loanReply = new LoanReply(bankInterestReply.getInterest(), bankInterestReply.getQuoteId());

                    RequestReply<BankInterestRequest, LoanReply> requestReply = requestReplies.stream().filter(o -> o.getRequest().equals(bankInterestRequest)).findFirst().get();
                    requestReply.setReply(loanReply);

                    String sendMessagId = bankSender.produce(loanReply, messageId);

                    Map<String, Object> maps = new HashMap<>();
                    maps.put("requestReply", requestReply);
                    maps.put("bankInterestReply", bankInterestReply);
                    maps.put("loanRequest", loanRequestWithMessageIds.get(messageId));

                    setChanged();
                    notifyObservers(maps);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Map<String, String> getRequestsWithMessageIds() {
        return requestsWithMessageIds;
    }

    public Map<String, LoanRequest> getLoanRequestWithMessageIds() {
        return loanRequestWithMessageIds;
    }

    public Map<String, BankInterestRequest> getBankInterestRequestWithMessageIds() {
        return bankInterestRequestWithMessageIds;
    }

    public List<RequestReply<BankInterestRequest, LoanReply>> getRequestReplies() {
        return requestReplies;
    }
}
