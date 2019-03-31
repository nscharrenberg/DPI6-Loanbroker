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

import javax.jms.*;

public abstract class ApplicationGateway {
    private MessageSenderGateway sender;

    private BiMap<String, RequestReplyList> requestReplyBiMap = HashBiMap.create();

    public ApplicationGateway() {
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
                    requestReply.addReply(resumeReply);
                    replyArrived = new RequestReply<>(requestReply.getRequest(), resumeReply);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

                onReplyArrived(replyArrived);
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public String sendResumeRequest(ResumeRequest resumeRequest) {
        RequestReplyList requestReply = new RequestReplyList(resumeRequest, null);
        String messageId = sender.produce(QueueName.SEEK_JOB_REQUEST, resumeRequest, null);

        if(messageId != null) {
            requestReplyBiMap.put(messageId, requestReply);
        }

        return messageId;
    }

    public BiMap<String, RequestReplyList> getRequestReplyBiMap() {
        return requestReplyBiMap;
    }

    public abstract void onReplyArrived(RequestReply<ResumeRequest, ResumeReply> requestReply);
}
