package com.nscharrenberg.elect.google.controllers;

import com.nscharrenberg.elect.google.domain.OfferReply;
import com.nscharrenberg.elect.google.domain.OfferRequest;
import com.nscharrenberg.elect.google.domain.ResumeReply;
import com.nscharrenberg.elect.google.domain.ResumeRequest;
import com.nscharrenberg.elect.google.gateways.application.ApplicationGateway;
import com.nscharrenberg.elect.google.gateways.messaging.requestreply.RequestReply;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {

    private ApplicationGateway applicationGateway;

    @FXML
    private TextField functionTxt;

    @FXML
    private TextField salarayTxt;

    @FXML
    private TextField durationTxt;

    @FXML
    private TextField emailTxt;

    @FXML
    private TextField personTxt;

    @FXML
    private TextArea descriptionTxt;

    @FXML
    private Button sendBtn;

    @FXML
    private ListView<RequestReply<OfferRequest, OfferReply>> messageList;

    private ObservableList<RequestReply<OfferRequest, OfferReply>> observableList;

    public Controller() {
        observableList = FXCollections.observableArrayList();

        observableList.addListener((ListChangeListener<RequestReply<OfferRequest, OfferReply>>) c -> {
            messageList.setItems(null);
            messageList.setItems(observableList);
        });


        applicationGateway = new ApplicationGateway() {
            @Override
            public void onOfferRequestArrived(OfferRequest offerRequest) {
               observableList.add(new RequestReply<>(offerRequest, null));
                messageList.refresh();
            }
        };
    }

    @FXML
    void sendBtn(ActionEvent event) {
        RequestReply<OfferRequest, OfferReply> rr = messageList.getSelectionModel().getSelectedItem();
        OfferReply offerReply = new OfferReply();
        offerReply.setCompanyId("google");
        offerReply.setContactEmail(getTextFromFxmlTextField(emailTxt));
        offerReply.setContactPersonName(getTextFromFxmlTextField(personTxt));
        offerReply.setFunctionTitle(getTextFromFxmlTextField(functionTxt));
        offerReply.setSalary(Double.parseDouble(getTextFromFxmlTextField(salarayTxt)));
        offerReply.setDuration(getTextFromFxmlTextField(durationTxt));
        offerReply.setFunctionDescription(descriptionTxt.getText());

        if(rr != null && offerReply != null) {
            rr.setReply(offerReply);
            applicationGateway.sendOfferReply(rr);
            messageList.refresh();
        }
    }

    private String getTextFromFxmlTextField(TextField textField) {
        return textField.getText();
    }

}
