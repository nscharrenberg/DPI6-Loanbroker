package com.nscharrenberg.elect.broker.gateways.application;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.nscharrenberg.elect.broker.domain.OfferReply;
import com.nscharrenberg.elect.broker.domain.OfferRequest;
import com.nscharrenberg.elect.broker.domain.ResumeReply;
import com.nscharrenberg.elect.broker.domain.ResumeRequest;
import com.nscharrenberg.elect.broker.gateways.messaging.MessageReceiverGateway;
import com.nscharrenberg.elect.broker.gateways.messaging.MessageSenderGateway;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ApplicationGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private BiMap<String, ResumeRequest> resumeRequestBiMap = HashBiMap.create();
    private BiMap<String, List<OfferReply>> offerReplies = HashBiMap.create();

    public ApplicationGateway() {
        this.sender = new MessageSenderGateway();
        this.receiver = new MessageReceiverGateway();
        
        receiveResumeRequest();
        receiveOfferReply();
    }

    private void receiveResumeRequest() {
        MessageConsumer messageConsumer = receiver.consume(QueueName.SEEK_JOB_REQUEST);

        try {
            messageConsumer.setMessageListener(message -> {
                ResumeRequest resumeRequest = null;
                String messageId = null;

                try {
                    Gson gson = new Gson();
                    String json = (String)((ObjectMessage) message).getObject();
                    resumeRequest = gson.fromJson(json, ResumeRequest.class);

                    messageId = message.getJMSMessageID();
                    resumeRequestBiMap.put(messageId, resumeRequest);
                    System.out.println(String.format("Message received in listener with msgId %s", messageId));
                } catch (JMSException e) {
                    e.printStackTrace();
                    System.out.println(String.format("Message received in listener with exception %s", e.getMessage()));
                }

                onResumeRequestArrived(resumeRequest);
            });
        } catch (JMSException e) {
            e.printStackTrace();
            System.out.println(String.format("Message received in listener with exception %s", e.getMessage()));
        }
    }

    private void receiveOfferReply() {
        MessageConsumer messageConsumer = receiver.consume(QueueName.OFFER_JOB_REPLY);

        try {
            messageConsumer.setMessageListener(message -> {
                OfferReply offerReply = null;
                String messageId = null;
                ResumeRequest resumeRequest = null;
                List<OfferReply> currentOffers = null;

                try {
                    Gson gson = new Gson();
                    String json = (String)((ObjectMessage) message).getObject();
                    offerReply = gson.fromJson(json, OfferReply.class);
                    messageId = message.getJMSCorrelationID();
                    resumeRequest = resumeRequestBiMap.get(messageId);
                    currentOffers = offerReplies.get(messageId);

                    if(currentOffers == null) {
                        currentOffers = new ArrayList<>();
                    }

                    currentOffers.add(offerReply);
                    offerReplies.put(messageId, currentOffers);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                onOfferReplyArrived(resumeRequest, offerReply);
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendResumeReply(ResumeReply resumeReply, ResumeRequest resumeRequest) {
        sender.produce(QueueName.SEEK_JOB_REPLY, resumeReply, resumeRequestBiMap.inverse().get(resumeRequest));
    }

    public void sendOfferRequeest(OfferRequest offerRequest, ResumeRequest resumeRequest) {
        List<String> sendTo = acceptedCompanies(offerRequest);
        String messageId = resumeRequestBiMap.inverse().get(resumeRequest);

        for(String company : sendTo) {
            sender.produce(String.format("%s", QueueName.OFFER_JOB_REQUEST), offerRequest, messageId);
        }
    }

    public List<String> acceptedCompanies(OfferRequest offerRequest) {
        BiMap<String, String> companies = HashBiMap.create();
        companies.put("google", "#{sector} = IT1");
//        companies.put("microsoft", "#{sector} = IT");
//        companies.put("mcdonalds", "#{sector} = horeca");
//        companies.put("cleaning", "#{sector} = cleaning");

//        Evaluator evaluator = new Evaluator();
//        evaluator.putVariable("sector", offerRequest.getSector());

        List<String> acceptedCompanies = new ArrayList<>();
        acceptedCompanies.add("google");
//        acceptedCompanies.add("microsoft");
//        acceptedCompanies.add("mcdonalds");
//        acceptedCompanies.add("cleaning");

//        String[] keys = (String[])companies.keySet().toArray();
//
//        try{
//            String result;
//            for(String key : keys) {
//                String entry = companies.get(key);
//
////                result = evaluator.evaluate(entry);
////                if(result.equals("1.0")) {
////                    acceptedCompanies.add(key);
////                }
//                acceptedCompanies.add(key);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return acceptedCompanies;
    }

    protected abstract void onOfferReplyArrived(ResumeRequest resumeRequest, OfferReply offerReply);
    protected abstract void onResumeRequestArrived(ResumeRequest resumeRequest);
}
