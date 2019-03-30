package com.nscharrenberg.elect.broker.controllers;

import com.nscharrenberg.elect.broker.domain.*;
import com.nscharrenberg.elect.broker.gateways.application.ApplicationGateway;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class Controller {

    private ApplicationGateway applicationGateway;

    @FXML
    private ListView<ListLine> messageList;

    private ObservableList<ListLine> observableList;

    public Controller() {
        observableList = FXCollections.observableArrayList();

        observableList.addListener((InvalidationListener) c -> {
            messageList.setItems(null);
            messageList.setItems(observableList);
            System.out.println("ObservableList Listener Triggered");
        });

        applicationGateway = new ApplicationGateway() {
            @Override
            protected void onOfferReplyArrived(ResumeRequest resumeRequest, OfferReply offerReply) {
                add(resumeRequest, offerReply);
                applicationGateway.sendResumeReply(new ResumeReply(offerReply.getCompanyId(), offerReply.getFunctionTitle(), offerReply.getSalary(), offerReply.getDuration(), offerReply.getContactEmail(), offerReply.getContactPersonName(), offerReply.getFunctionDescription()), resumeRequest);
                messageList.refresh();
            }

            @Override
            protected void onResumeRequestArrived(ResumeRequest resumeRequest) {
                add(resumeRequest);
                OfferRequest offerRequest = new OfferRequest(resumeRequest.getFirstName(), resumeRequest.getLastName(), resumeRequest.getSector(), resumeRequest.getRegion(), resumeRequest.getSkills());
                add(resumeRequest, offerRequest);
                applicationGateway.sendOfferRequeest(offerRequest, resumeRequest);
                messageList.refresh();
            }
        };
    }

    public void add(ResumeRequest resumeRequest) {
        observableList.add(new ListLine(resumeRequest));
        messageList.refresh();
    }

    public void add(ResumeRequest resumeRequest, OfferRequest offerRequest) {
        ListLine rr = getRequestReply(resumeRequest);

        if(rr != null && offerRequest != null) {
            rr.setOfferRequest(offerRequest);
            messageList.refresh();
        }
    }

    public void add(ResumeRequest resumeRequest, OfferReply offerReply) {
        ListLine rr = getRequestReply(resumeRequest);

        if(rr != null && offerReply != null) {
            rr.setOfferReply(offerReply);
            messageList.refresh();
        }
    }

    private ListLine getRequestReply(ResumeRequest resumeRequest) {
        for (ListLine rr : observableList) {
            if (rr.getResumeRequest() == resumeRequest) {
                return rr;
            }
        }

        return null;
    }
}
