package com.nscharrenberg.elect.google.domain;

public class ListLine {
    private ResumeRequest resumeRequest;
    private OfferRequest offerRequest;
    private OfferReply offerReply;

    public ListLine(ResumeRequest resumeRequest) {
        this.resumeRequest = resumeRequest;
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

    public OfferReply getOfferReply() {
        return offerReply;
    }

    public void setOfferReply(OfferReply offerReply) {
        this.offerReply = offerReply;
    }

    @Override
    public String toString() {
        return String.format("%s || %s", resumeRequest.toString(), ((offerReply != null) ? offerReply.toString() : "Awaiting reply"));
    }
}
