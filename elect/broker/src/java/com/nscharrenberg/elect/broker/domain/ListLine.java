package com.nscharrenberg.elect.broker.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ListLine implements Serializable {
    private ResumeRequest resumeRequest;
    private OfferRequest offerRequest;
    private Set<OfferReply> offerReplies;

    public ListLine(ResumeRequest resumeRequest) {
        this.resumeRequest = resumeRequest;
        this.offerReplies = new HashSet<>();
    }

    public ResumeRequest getResumeRequest() {
        return resumeRequest;
    }

    public void setResumeRequest(ResumeRequest resumeRequest) {
        this.resumeRequest = resumeRequest;
    }

    public OfferRequest getOfferRequest() {
        return offerRequest;
    }

    public void setOfferRequest(OfferRequest offerRequest) {
        this.offerRequest = offerRequest;
    }

    public Set<OfferReply> getOfferReplies() {
        return offerReplies;
    }

    public void setOfferReplies(Set<OfferReply> offerReplies) {
        this.offerReplies = offerReplies;
    }

    public void addReply(OfferReply offerReply) {
        this.offerReplies.add(offerReply);
    }

    public void removeReply(OfferReply offerReply) {
        this.offerReplies.remove(offerReply);
    }

    @Override
    public String toString() {
        return String.format("%s || %s", resumeRequest.toString(), ((offerReplies == null || offerReplies.size() <= 0) ?  "Awaiting reply" : offerReplies.toString()));
    }
}
