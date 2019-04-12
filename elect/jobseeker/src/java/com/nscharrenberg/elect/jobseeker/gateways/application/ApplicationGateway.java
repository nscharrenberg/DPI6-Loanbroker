package com.nscharrenberg.elect.jobseeker.gateways.application;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.nscharrenberg.elect.jobseeker.domain.ResumeReply;
import com.nscharrenberg.elect.jobseeker.domain.ResumeRequest;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.MessageReceiverGateway;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.MessageSenderGateway;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReply;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReplyList;
import com.nscharrenberg.elect.jobseeker.shared.MessageReader;
import com.nscharrenberg.elect.jobseeker.shared.MessageWriter;

import javax.jms.*;

public abstract class ApplicationGateway {
    private MessageSenderGateway sender;

    private BiMap<String, RequestReplyList> requestReplyBiMap = HashBiMap.create();

    public ApplicationGateway() {
        populateMessageList();

        this.sender = new MessageSenderGateway();
        MessageReceiverGateway receiver = new MessageReceiverGateway();

        MessageConsumer messageConsumer = receiver.consume(QueueName.SEEK_JOB_REPLY);

        try {
            //TODO: MessageListener to receive Replies on the "seekReplyQueue" queue.
            messageConsumer.setMessageListener(message -> {
                ResumeReply resumeReply = null;
                String messageId = null;
                RequestReplyList requestReply = null;
                RequestReply<ResumeRequest, ResumeReply> replyArrived = null;

                try {
                    Gson gson = new Gson();
                    String json = (String)((ObjectMessage) message).getObject();

                    // Convert JSON to ResumeReply Object
                    resumeReply = gson.fromJson(json, ResumeReply.class);

                    messageId = message.getJMSCorrelationID();
                    requestReply = requestReplyBiMap.get(messageId);

                    if(requestReply == null) {
                        return;
                    }

                    requestReply.addReply(resumeReply);
                    replyArrived = new RequestReply<>(requestReply.getRequest(), resumeReply);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                onReplyArrived(messageId, replyArrived);
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a ResumeRequest to the Broker
     * @param resumeRequest
     * @return
     */
    public String sendResumeRequest(ResumeRequest resumeRequest) {
        RequestReplyList requestReply = new RequestReplyList(resumeRequest, null);

        //TODO: Call producer to send a ResumeRequest through the "seekRequestQueue" queue. This has no CorrelationId as it's the first message.
        String messageId = sender.produce(QueueName.SEEK_JOB_REQUEST, resumeRequest, null);

        //TODO: Add the send request to the in-memory database.
        MessageWriter.add(messageId, requestReply);

        if(messageId != null) {
            requestReplyBiMap.put(messageId, requestReply);
        }

        return messageId;
    }

    /**
     * Repopulate all RequestReplies from the JSON file (used as in-memory database).
     * It'll add each RequestReply back to the BiMap.
     */
    private void populateMessageList() {
        //TODO: Prepopulate the list with the RequestReplies that are saved in the in-memory database.
        HashBiMap<String, RequestReplyList> requests = MessageReader.getRequests();

        //TODO: Put these items back to the BiMap
        requests.forEach((c, r) -> {
            requestReplyBiMap.put(c, r);
        });
    }

    public BiMap<String, RequestReplyList> getRequestReplyBiMap() {
        return requestReplyBiMap;
    }

    public abstract void onReplyArrived(String correlationId, RequestReply<ResumeRequest, ResumeReply> requestReply);
}
