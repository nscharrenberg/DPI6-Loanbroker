package com.nscharrenberg.elect.broker.controllers;

import com.google.common.collect.HashBiMap;
import com.nscharrenberg.elect.broker.domain.*;
import com.nscharrenberg.elect.broker.gateways.application.ApplicationGateway;
import com.nscharrenberg.elect.broker.shared.MessageReader;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

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
            rr.addReply(offerReply);
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

    /**
     * Repopulate all RequestReplies from the JSON file (used as in-memory database).
     * It'll add each RequestReply back to the BiMap.
     */
    private void populateMessageList() {
        //TODO: Prepopulate the list with the RequestReplies that are saved in the in-memory database.
        HashBiMap<String, ListLine> requests = MessageReader.getRequests();

        //TODO: Put these items back to the BiMap
        requests.forEach((c, r) -> {
            observableList.add(r);
        });
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateMessageList();
        applicationGateway = new ApplicationGateway() {
            @Override
            protected void onOfferReplyArrived(ListLine ll, OfferReply offerReply) {
                add(ll.getResumeRequest(), offerReply);

                //TODO: Send a ResumeReply uppon arrival of an OfferReply
                applicationGateway.sendResumeReply(new ResumeReply(offerReply.getCompanyId(), offerReply.getFunctionTitle(), offerReply.getSalary(), offerReply.getDuration(), offerReply.getContactEmail(), offerReply.getContactPersonName(), offerReply.getFunctionDescription()), ll);
                messageList.refresh();
            }

            @Override
            protected void onResumeRequestArrived(ListLine ll) {
                add(ll.getResumeRequest());
                OfferRequest offerRequest = new OfferRequest(ll.getResumeRequest().getFirstName(), ll.getResumeRequest().getLastName(), ll.getResumeRequest().getSector(), ll.getResumeRequest().getRegion(), ll.getResumeRequest().getSkills());
                add(ll.getResumeRequest(), offerRequest);

                //TODO: Send an OfferRequest uppon arrival of a ResumeRequest
                applicationGateway.sendOfferRequest(offerRequest, ll);
                messageList.refresh();
            }
        };
    }
}
