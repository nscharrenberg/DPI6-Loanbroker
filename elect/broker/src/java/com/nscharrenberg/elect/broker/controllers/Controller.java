package com.nscharrenberg.elect.broker.controllers;

import com.nscharrenberg.elect.broker.domain.*;
import com.nscharrenberg.elect.broker.gateways.application.ApplicationGateway;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class Controller {

    @FXML
    private ListView<ListLine> messageList;

    private ApplicationGateway applicationGateway;

    private ObservableList<ListLine> observableList;

    public Controller() {
        observableList = FXCollections.observableArrayList();

        // Update listview when an item is added or changed
        observableList.addListener((InvalidationListener) c -> {
            messageList.setItems(null);
            messageList.setItems(observableList);
        });

        applicationGateway = new ApplicationGateway() {
            @Override
            protected void onOfferReplyArrived(ResumeRequest resumeRequest, OfferReply offerReply) {
                add(resumeRequest, offerReply);

                //TODO: Send a ResumeReply uppon arrival of an OfferReply
                applicationGateway.sendResumeReply(new ResumeReply(offerReply.getCompanyId(), offerReply.getFunctionTitle(), offerReply.getSalary(), offerReply.getDuration(), offerReply.getContactEmail(), offerReply.getContactPersonName(), offerReply.getFunctionDescription()), resumeRequest);
                messageList.refresh();
            }

            @Override
            protected void onResumeRequestArrived(ResumeRequest resumeRequest) {
                add(resumeRequest);
                OfferRequest offerRequest = new OfferRequest(resumeRequest.getFirstName(), resumeRequest.getLastName(), resumeRequest.getSector(), resumeRequest.getRegion(), resumeRequest.getSkills());
                add(resumeRequest, offerRequest);

                //TODO: Send an OfferRequest uppon arrival of a ResumeRequest
                applicationGateway.sendOfferRequest(offerRequest, resumeRequest);
                messageList.refresh();
            }
        };
    }

    /**
     * Add a new Request to the listview
     * This shows that the resume is received by the broker.
     * @param resumeRequest
     */
    private void add(ResumeRequest resumeRequest) {
        observableList.add(new ListLine(resumeRequest));
        messageList.refresh();
    }

    /**
     * Update the Resume and add a request for an offer.
     * This shows that the resume is forwarded to the appropriate companies.
     * @param resumeRequest
     * @param offerRequest
     */
    private void add(ResumeRequest resumeRequest, OfferRequest offerRequest) {
        ListLine rr = getRequestReply(resumeRequest);

        if(rr != null && offerRequest != null) {
            rr.setOfferRequest(offerRequest);
            messageList.refresh();
        }
    }

    /**
     * Update the Resume and add an offer.
     * This shows that an offer is received from the companies.
     * @param resumeRequest
     * @param offerReply
     */
    private void add(ResumeRequest resumeRequest, OfferReply offerReply) {
        ListLine rr = getRequestReply(resumeRequest);

        if(rr != null && offerReply != null) {
            rr.setOfferReply(offerReply);
            messageList.refresh();
        }
    }

    /**
     * Get the correct ListLine from a resume.
     * @param resumeRequest
     * @return
     */
    private ListLine getRequestReply(ResumeRequest resumeRequest) {
        for (ListLine rr : observableList) {
            if (rr.getResumeRequest() == resumeRequest) {
                return rr;
            }
        }

        return null;
    }
}
