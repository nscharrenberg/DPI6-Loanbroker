package com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply;

import com.nscharrenberg.elect.jobseeker.domain.ResumeReply;
import com.nscharrenberg.elect.jobseeker.domain.ResumeRequest;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class RequestReplyList implements Serializable {
    private ResumeRequest request;
    private Set<ResumeReply> reply;

    public RequestReplyList(ResumeRequest request, Set<ResumeReply> reply) {
        this.request = request;

        if(reply == null) {
            this.reply = new HashSet<>();
        } else {
            this.reply = reply;
        }
    }

    public ResumeRequest getRequest() {
        return request;
    }

    public void setRequest(ResumeRequest request) {
        this.request = request;
    }

    public Set<ResumeReply> getReply() {
        return reply;
    }

    public void setReply(Set<ResumeReply> reply) {
        this.reply = reply;
    }

    public void addReply(ResumeReply reply) {
        this.reply.add(reply);
    }

    public void removeReply(ResumeReply reply) {
        this.reply.remove(reply);
    }

    @Override
    public String toString() {
        return String.format("%s ---> %s",request.toString(), ((reply.size() > 0) ? String.format("%s replies received", this.reply.size()) : "awaiting reply..."));
    }
}
