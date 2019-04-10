package com.nscharrenberg.elect.amazon.gateways.application;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.nscharrenberg.elect.amazon.domain.OfferReply;
import com.nscharrenberg.elect.amazon.domain.OfferRequest;
import com.nscharrenberg.elect.amazon.gateways.messaging.MessageReceiverGateway;
import com.nscharrenberg.elect.amazon.gateways.messaging.MessageSenderGateway;
import com.nscharrenberg.elect.amazon.gateways.messaging.requestreply.RequestReply;

import javax.jms.*;

public abstract class ApplicationGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private BiMap<OfferRequest, String> offerRequestStringBiMap = HashBiMap.create();

    public ApplicationGateway() {
        this.sender = new MessageSenderGateway();
        this.receiver = new MessageReceiverGateway();

        MessageConsumer messageConsumer = receiver.consume(String.format("%s_%s", QueueName.OFFER_JOB_REQUEST, "amazon"));

        try {
            messageConsumer.setMessageListener(message -> {
                OfferRequest offerRequest = null;
                String messageId = null;

                try {
                    Gson gson = new Gson();
                    String json = (String) ((ObjectMessage) message).getObject();
                    offerRequest = gson.fromJson(json, OfferRequest.class);
                    messageId = message.getJMSCorrelationID();

                    offerRequestStringBiMap.put(offerRequest, messageId);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                onOfferRequestArrived(messageId, offerRequest);
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendOfferReply(RequestReply<OfferRequest, OfferReply> requestReply) {
        String correlationId = offerRequestStringBiMap.get(requestReply.getRequest());
        sender.produce(QueueName.OFFER_JOB_REPLY, requestReply.getReply(), correlationId);
    }

    public abstract void onOfferRequestArrived(String correlationId, OfferRequest offerRequest);
}
