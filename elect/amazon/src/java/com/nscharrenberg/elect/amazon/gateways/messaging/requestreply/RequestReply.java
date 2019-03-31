package com.nscharrenberg.elect.amazon.gateways.messaging.requestreply;

import java.io.Serializable;

public class RequestReply<REQUEST, REPLY> implements Serializable {
    private REQUEST request;
    private REPLY reply;

    public RequestReply(REQUEST request, REPLY reply) {
        this.request = request;
        this.reply = reply;
    }

    public REQUEST getRequest() {
        return request;
    }

    public void setRequest(REQUEST request) {
        this.request = request;
    }

    public REPLY getReply() {
        return reply;
    }

    public void setReply(REPLY reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        return String.format("%s ---> %s",request.toString(), ((reply != null) ? reply.toString() : "awaiting reply..."));
    }
}
