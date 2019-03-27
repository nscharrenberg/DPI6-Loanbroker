package com.nscharrenberg.elect.jobseeker.controllers;

import com.nscharrenberg.elect.jobseeker.domain.ResumeReply;
import com.nscharrenberg.elect.jobseeker.domain.ResumeRequest;
import com.nscharrenberg.elect.jobseeker.gateways.application.ApplicationGateway;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReply;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class Controller {

    private ApplicationGateway applicationGateway;

    @FXML
    private TextField firstnameTxt;

    @FXML
    private TextField lastnameTxt;

    @FXML
    private TextField emailTxt;

    @FXML
    private TextField sectorTxt;

    @FXML
    private TextField regionTxt;

    @FXML
    private TextField skillTxt;

    @FXML
    private Button sendBtn;

    @FXML
    private ListView<RequestReply<ResumeRequest, ResumeReply>> messageList;

    private ObservableList<RequestReply<ResumeRequest, ResumeReply>> observableList;

    public Controller() {
        observableList = FXCollections.observableArrayList();

        observableList.addListener((ListChangeListener<RequestReply<ResumeRequest, ResumeReply>>) c -> {
            messageList.setItems(null);
            messageList.setItems(observableList);
        });


        applicationGateway = new ApplicationGateway() {
            @Override
            public void onReplyArrived(RequestReply<ResumeRequest, ResumeReply> requestReply) {
                RequestReply<ResumeRequest, ResumeReply> rr = getRequestReply(requestReply.getRequest());
                if (rr != null) {
                    rr.setReply(requestReply.getReply());
                }
            }
        };
    }

    @FXML
    void sendBtn(ActionEvent event) {
        ResumeRequest resumeRequest = new ResumeRequest();
        resumeRequest.setEmail(getTextFromFxmlTextField(emailTxt));
        resumeRequest.setFirstName(getTextFromFxmlTextField(firstnameTxt));
        resumeRequest.setLastName(getTextFromFxmlTextField(lastnameTxt));
        resumeRequest.setRegion(getTextFromFxmlTextField(regionTxt));
        resumeRequest.setSector(getTextFromFxmlTextField(sectorTxt));
        resumeRequest.setSkills(getTextFromFxmlTextField(skillTxt));

        String messageId = applicationGateway.sendResumeRequest(resumeRequest);
        RequestReply<ResumeRequest, ResumeReply> requestReply = applicationGateway.getRequestReplyBiMap().get(messageId);
        observableList.add(requestReply);
    }

    private String getTextFromFxmlTextField(TextField textField) {
        return textField.getText();
    }

    private RequestReply<ResumeRequest, ResumeReply> getRequestReply(ResumeRequest resumeRequest) {
        for (RequestReply<ResumeRequest, ResumeReply> rr : observableList) {
            if (rr.getRequest() == resumeRequest) {
                return rr;
            }
        }

        return null;
    }
}
