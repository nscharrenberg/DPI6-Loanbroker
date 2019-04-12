package com.nscharrenberg.elect.broker.gateways.application;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.nscharrenberg.elect.broker.data.CompanyList;
import com.nscharrenberg.elect.broker.domain.*;
import com.nscharrenberg.elect.broker.gateways.messaging.MessageReceiverGateway;
import com.nscharrenberg.elect.broker.gateways.messaging.MessageSenderGateway;
import com.nscharrenberg.elect.broker.shared.MessageReader;
import com.nscharrenberg.elect.broker.shared.MessageWriter;
import com.udojava.evalex.AbstractLazyFunction;
import com.udojava.evalex.Expression;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class ApplicationGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private BiMap<String, ListLine> resumeRequestBiMap = HashBiMap.create();

    public ApplicationGateway() {
        populateMessageList();
        this.sender = new MessageSenderGateway();
        this.receiver = new MessageReceiverGateway();
        
        receiveResumeRequest();
        receiveOfferReply();
    }

    /**
     * A MessageListener to listen to resume's from clients.
     * When a message from a client is received it'll convert this into the appropriate object so the resume can be send to companies in the hopes to receive offers.
     */
    private void receiveResumeRequest() {
        MessageConsumer messageConsumer = receiver.consume(QueueName.SEEK_JOB_REQUEST);

        try {
            //TODO: MessageListener to receive a ResumeRequest no the "seekRequestQueue" queue.
            messageConsumer.setMessageListener(message -> {
                ResumeRequest resumeRequest = null;
                ListLine ll = null;
                String messageId;

                try {
                    Gson gson = new Gson();
                    String json = (String)((ObjectMessage) message).getObject();

                    // Convert json to ResumeRequest Object.
                    resumeRequest = gson.fromJson(json, ResumeRequest.class);

                    messageId = message.getJMSMessageID();
                    ll = new ListLine(resumeRequest);
                    resumeRequestBiMap.put(messageId, ll);

                    MessageWriter.add(messageId, ll);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                onResumeRequestArrived(ll);
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * A MessageListener to listen to offers from companies.
     * When a message from a company is received it'll convert this into the appropriate objects so an offer can be send back to the client.
     */
    private void receiveOfferReply() {
        MessageConsumer messageConsumer = receiver.consume(QueueName.OFFER_JOB_REPLY);

        try {
            //TODO: MessageListener to receive a OfferReply no the "offerReplyQueue" queue.
            messageConsumer.setMessageListener(message -> {
                OfferReply offerReply = null;
                String messageId;
                ListLine resumeRequest = null;

                try {
                    Gson gson = new Gson();
                    String json = (String)((ObjectMessage) message).getObject();

                    // Convert JSON to OfferReply Object
                    offerReply = gson.fromJson(json, OfferReply.class);
                    messageId = message.getJMSCorrelationID();
                    resumeRequest = resumeRequestBiMap.get(messageId);

                    resumeRequest.addReply(offerReply);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                onOfferReplyArrived(resumeRequest, offerReply);
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a an offer to the client's resume.
     * @param resumeReply - the offer
     */
    public void sendResumeReply(ResumeReply resumeReply, ListLine ll) {
        //TODO: Send an offer to the client. Here the ResumeReply and it's correlationId is send.
        // The resumeRequestBiMap is Inversed so we can get the correlationId by the value (ResumeRequest).
        sender.produce(QueueName.SEEK_JOB_REPLY, resumeReply, resumeRequestBiMap.inverse().get(ll));
    }

    /**
     * Send a request for an offer to the companies.
     * Here the list of acceptedCompanies from the Message Filter are used to send the messages.
     */
    public void sendOfferRequest(OfferRequest offerRequest, ListLine listLine) {
        //TODO: Evaluate Companies by Sector
        List<String> sendTo = evaluateSector(offerRequest);

        // The resumeRequestBiMap is Inversed so we can get the correlationId by the value (ResumeRequest).
        String messageId = resumeRequestBiMap.inverse().get(listLine);

        for(String company : sendTo) {
            sender.produce(String.format("%s_%s", QueueName.OFFER_JOB_REQUEST, company), offerRequest, messageId);
        }
    }

    /**
     * Message Filter
     * Messages are being filtered by certain criteria that each company has.
     * If the message meets the companies criteria, the message will be send to those companies.
     * @param request
     * @return a list of all companies that are potentially interested into the message.
     */
    private List<String> evaluateSector(OfferRequest request) {
        List<String> acceptedCompanies = new ArrayList<>();

        //TODO: Compare the OfferRequest sector with the companies criteria.
        CompanyList.stream().forEach((c) -> {
            Expression e = new Expression(c.getSector());
            e.addLazyFunction(new AbstractLazyFunction("SECTOR", 1) {
                private Expression.LazyNumber ZERO = new Expression.LazyNumber() {
                    @Override
                    public BigDecimal eval() {
                        return BigDecimal.ZERO;
                    }

                    @Override
                    public String getString() {
                        return "0";
                    }
                };

                private Expression.LazyNumber ONE = new Expression.LazyNumber() {
                    @Override
                    public BigDecimal eval() {
                        return BigDecimal.ONE;
                    }

                    @Override
                    public String getString() {
                        return "1";
                    }
                };

                @Override
                public Expression.LazyNumber lazyEval(List<Expression.LazyNumber> list) {
                    if(request.getSector().equals(list.get(0).getString())) {
                        return ONE;
                    }

                    return ZERO;
                }
            });

            //TODO: Only add companies where the OfferRequest meets their criteria.
           if(e.eval().equals(BigDecimal.ONE)) {
                acceptedCompanies.add(c.getName());
           }
        });

        return acceptedCompanies;
    }

    /**
     * Repopulate all RequestReplies from the JSON file (used as in-memory database).
     * It'll add each RequestReply back to the BiMap.
     */
    private void populateMessageList() {
        //TODO: Prepopulate the list with the RequestReplies that are saved in the in-memory database.
        HashBiMap<String, ListLine> requests = MessageReader.getRequests();

        //TODO: Put these items back to the BiMap
        requests.forEach((c, r) -> {
            resumeRequestBiMap.put(c, r);
        });
    }

    protected abstract void onOfferReplyArrived(ListLine ll, OfferReply offerReply);
    protected abstract void onResumeRequestArrived(ListLine listLine);
}
