package com.nscharrenberg.elect.jobseeker.controllers;

import com.nscharrenberg.elect.jobseeker.domain.ResumeReply;
import com.nscharrenberg.elect.jobseeker.domain.ResumeRequest;
import com.nscharrenberg.elect.jobseeker.gateways.application.ApplicationGateway;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReply;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

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
                    messageList.refresh();
                }
            }
        };
    }

    @FXML
    void messageListMouseClickedEvent(MouseEvent event) {
        if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("View Offer Information");
            RequestReply<ResumeRequest, ResumeReply> rr = messageList.getSelectionModel().getSelectedItem();
            alert.setHeaderText(rr.getReply() == null ? "Awaiting Offer" : "Offer received");
            alert.setContentText(String.format(
                    "Name %s %s \n Email: %s \n Sector: %s \n Region: %s \n Skills: %s",
                    rr.getRequest().getFirstName(), rr.getRequest().getLastName(), rr.getRequest().getEmail(), rr.getRequest().getSector(), rr.getRequest().getRegion(), rr.getRequest().getSector()));

            Label label = new Label("Offer:");
            TextArea textArea;
            if(rr.getReply() != null) {
                textArea = new TextArea(String.format(
                        "Company: %s \n Function: %s \n Salary: %s \n Duration: %s \n contact email: %s \n contact person: %s \n function Description: %s",
                        rr.getReply().getCompanyId(), rr.getReply().getFunctionTitle(), rr.getReply().getSalary(), rr.getReply().getDuration(), rr.getReply().getContactEmail(), rr.getReply().getContactPersonName(), rr.getReply().getFunctionDescription()

                ));
            } else {
                textArea = new TextArea("No offer received yet!");
            }

            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        }
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
        messageList.refresh();
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
