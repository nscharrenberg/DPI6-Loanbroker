package loanbroker.loanbroker.gateways.application;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import loanbroker.loanbroker.gateways.messaging.MessageReceiverGateway;
import loanbroker.loanbroker.gateways.messaging.MessageSenderGateway;
import messaging.QueueNames;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import javax.jms.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.Ordering.natural;

public abstract class LoanBrokerApplicationGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    // Mapping the messaging id's of BankInterestRequest with LoanRequest

    BiMap<String, LoanRequest> loanRequestWithMessageId = HashBiMap.create();
    BiMap<String, Integer> aggregationAmount = HashBiMap.create();
    BiMap<String, List<BankInterestReply>> interestReplies = HashBiMap.create();

    public LoanBrokerApplicationGateway() {
        receiver = new MessageReceiverGateway();
        sender = new MessageSenderGateway();

        receiveLoanRequest();
        receiveBankInterestReply();
    }

    private BankInterestReply GetReplyWithLowestInterest(List<BankInterestReply> replies) {
       return replies.stream().min(Comparator.comparingDouble(BankInterestReply::getInterest)).orElseThrow(NoSuchElementException::new);
    }

    private void receiveLoanRequest() {
        MessageConsumer consumer = receiver.consume(QueueNames.loanRequest);
        try {
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    LoanRequest loanRequest = null;
                    String correlationId = null;

                    try {
                        loanRequest = (LoanRequest)((ObjectMessage) message).getObject();
                        correlationId = message.getJMSMessageID();
                        loanRequestWithMessageId.put(correlationId, loanRequest);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                    OnLoanRequestArrived(loanRequest);
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void receiveBankInterestReply() {
        MessageConsumer consumer = receiver.consume(QueueNames.bankInterestReply);
        try {
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    BankInterestReply bankInterestReply = null;
                    String correlationId = null;
                    LoanRequest loanRequest = null;
                    List<BankInterestReply> currentReplies = null;

                    try {
                        bankInterestReply = (BankInterestReply) ((ObjectMessage) message).getObject();
                        correlationId = message.getJMSCorrelationID();
                        loanRequest = loanRequestWithMessageId.get(correlationId);
                        currentReplies = interestReplies.get(correlationId);

                        if(currentReplies == null) {
                            currentReplies = new ArrayList<>();
                        }

                        currentReplies.add(bankInterestReply);
                        interestReplies.put(correlationId, currentReplies);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                    if(interestReplies.get(correlationId).size() == aggregationAmount.get(correlationId) && currentReplies != null) {
                        OnInterestReplyArrived(loanRequest, GetReplyWithLowestInterest(currentReplies));
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendLoanReply(LoanReply loanReply, LoanRequest loanRequest) {
        sender.produce(QueueNames.loanReply, loanReply, loanRequestWithMessageId.inverse().get(loanRequest), 0);
    }

    public void sendBankInterestRequest(BankInterestRequest bankInterestRequest, LoanRequest loanRequest) {
        List<String> sendTo = acceptedBanks(bankInterestRequest);
        String correlationId = loanRequestWithMessageId.inverse().get(loanRequest);
        aggregationAmount.put(correlationId, sendTo.size());

        boolean bool = sendTo.stream().allMatch(bank -> sender.produce(QueueNames.bankInterestRequest + "_" + bank, bankInterestRequest, correlationId, aggregationAmount.size()) != null);
    }

    private List<String> acceptedBanks(BankInterestRequest bankInterestRequest) {
        String ING = "#{amount} <= 100000 && #{time} <= 10";
        String ABNAMRO = "#{amount} >= 200000 && #{amount} <= 300000  && #{time} <= 20";
        String RABOBANK = "#{amount} <= 250000 && #{time} <= 15";

        Evaluator evaluator = new Evaluator();
        evaluator.putVariable("amount", Integer.toString(bankInterestRequest.getAmount()));
        evaluator.putVariable("time", Integer.toString(bankInterestRequest.getTime()));

        String result;
        List<String> acceptedBanks = new ArrayList<>();

        try {
            result = evaluator.evaluate(ING);
            if(result.equals("1.0"))
                acceptedBanks.add("ING");
        }
        catch (EvaluationException e) {
            e.printStackTrace();
        }

        try {
            result = evaluator.evaluate(ABNAMRO);
            if(result.equals("1.0"))
                acceptedBanks.add("ABN_AMRO");
        }
        catch (EvaluationException e) {
            e.printStackTrace();
        }

        try {
            result = evaluator.evaluate(RABOBANK);
            if(result.equals("1.0"))
                acceptedBanks.add("Rabobank");
        } catch (EvaluationException e) {
            e.printStackTrace();
        }

        return acceptedBanks;

    }

    protected abstract void OnInterestReplyArrived(LoanRequest loanRequest, BankInterestReply interestReply);
    protected abstract void OnLoanRequestArrived(LoanRequest loanRequest);
}
