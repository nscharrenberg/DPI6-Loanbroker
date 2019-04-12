package com.nscharrenberg.elect.microsoft.gateways.application;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nscharrenberg.elect.microsoft.domain.OfferReply;
import com.nscharrenberg.elect.microsoft.domain.OfferRequest;
import com.nscharrenberg.elect.microsoft.gateways.messaging.MessageReceiverGateway;
import com.nscharrenberg.elect.microsoft.gateways.messaging.MessageSenderGateway;
import com.nscharrenberg.elect.microsoft.gateways.messaging.requestreply.RequestReply;
import com.nscharrenberg.elect.microsoft.shared.MessageReader;
import com.nscharrenberg.elect.microsoft.shared.MessageWriter;

import javax.jms.*;

public abstract class ApplicationGateway {
    private MessageSenderGateway sender;
    private MessageReceiverGateway receiver;

    private BiMap<String, OfferRequest> offerRequestStringBiMap = HashBiMap.create();

    public ApplicationGateway() {
        populateMessageList();

        this.sender = new MessageSenderGateway();
        this.receiver = new MessageReceiverGateway();

        MessageConsumer messageConsumer = receiver.consume(String.format("%s_%s", QueueName.OFFER_JOB_REQUEST, "microsoft"));

        try {
            messageConsumer.setMessageListener(message -> {
                OfferRequest offerRequest = null;
                String messageId = null;

                try {
                    Gson gson = new Gson();
                    String json = (String) ((ObjectMessage) message).getObject();
                    offerRequest = gson.fromJson(json, new TypeToken<OfferRequest>(){}.getType());
                    messageId = message.getJMSCorrelationID();

                    offerRequestStringBiMap.put(messageId, offerRequest);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                onOfferRequestArrived(messageId, new RequestReply<>(offerRequest, null));
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void populateMessageList() {
        HashBiMap<String, RequestReply<OfferRequest, OfferReply>> requests = MessageReader.getRequests();

        requests.forEach((c, r) -> {
            offerRequestStringBiMap.put(c, r.getRequest());
        });
    }

    public void sendOfferReply(RequestReply<OfferRequest, OfferReply> requestReply) {
        String correlationId = offerRequestStringBiMap.inverse().get(requestReply.getRequest());
        sender.produce(QueueName.OFFER_JOB_REPLY, requestReply.getReply(), correlationId);

        // Update the in-memory database.
        MessageWriter.update(correlationId, requestReply.getReply());
    }

    public abstract void onOfferRequestArrived(String correlationId, RequestReply<OfferRequest, OfferReply> offerRequest);
}
