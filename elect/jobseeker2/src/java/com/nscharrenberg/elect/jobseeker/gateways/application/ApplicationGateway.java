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
            messageConsumer.setMessageListener(message -> {
                ResumeReply resumeReply = null;
                String messageId = null;
                RequestReplyList requestReply = null;
                RequestReply<ResumeRequest, ResumeReply> replyArrived = null;

                try {
                    Gson gson = new Gson();
                    String json = (String)((ObjectMessage) message).getObject();
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

    public String sendResumeRequest(ResumeRequest resumeRequest) {
        RequestReplyList requestReply = new RequestReplyList(resumeRequest, null);
        String messageId = sender.produce(QueueName.SEEK_JOB_REQUEST, resumeRequest, null);
        MessageWriter.add(messageId, requestReply);

        if(messageId != null) {
            requestReplyBiMap.put(messageId, requestReply);
        }

        return messageId;
    }

    private void populateMessageList() {
        HashBiMap<String, RequestReplyList> requests = MessageReader.getRequests();

        requests.forEach((c, r) -> {
            requestReplyBiMap.put(c, r);
        });
    }

    public BiMap<String, RequestReplyList> getRequestReplyBiMap() {
        return requestReplyBiMap;
    }

    public abstract void onReplyArrived(String correlationId, RequestReply<ResumeRequest, ResumeReply> requestReply);
}
