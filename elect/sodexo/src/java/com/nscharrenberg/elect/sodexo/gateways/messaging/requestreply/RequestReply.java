package com.nscharrenberg.elect.sodexo.gateways.messaging.requestreply;

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

    @Override
    public boolean equals(Object obj) {
        if(reply == null) {
            if(
                    obj instanceof RequestReply &&
                            request.equals(((RequestReply) obj).getRequest()) &&
                            reply == null &&
                            ((RequestReply) obj).getReply() == null
                    ) {
                return true;
            }
        } else {
            if(
                    obj instanceof RequestReply &&
                            request.equals(((RequestReply) obj).getRequest()) &&
                            reply.equals(((RequestReply) obj).getReply())
                    ) {
                return true;
            }
        }

        return false;
    }
}