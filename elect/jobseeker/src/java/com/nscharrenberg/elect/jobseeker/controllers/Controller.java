package com.nscharrenberg.elect.jobseeker.controllers;

import com.google.common.collect.HashBiMap;
import com.nscharrenberg.elect.jobseeker.domain.ResumeReply;
import com.nscharrenberg.elect.jobseeker.domain.ResumeRequest;
import com.nscharrenberg.elect.jobseeker.gateways.application.ApplicationGateway;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReply;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReplyList;
import com.nscharrenberg.elect.jobseeker.shared.MessageReader;
import com.nscharrenberg.elect.jobseeker.shared.MessageWriter;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private ApplicationGateway applicationGateway;

    @FXML
    private TextField firstnameTxt;

    @FXML
    private TextField lastnameTxt;

    @FXML
    private TextField emailTxt;

    @FXML
    private ComboBox<String> sectorTxt;

    @FXML
    private TextField regionTxt;

    @FXML
    private TextField skillTxt;

    @FXML
    private Button sendBtn;

    @FXML
    private ListView<RequestReplyList> messageList;

    private ObservableList<RequestReplyList> observableList;
    private ObservableList<String> sectorList;

    public Controller() {
        observableList = FXCollections.observableArrayList();
        sectorList = FXCollections.observableArrayList("IT", "HORECA", "CLEANING", "LOGISTIEK");
        sectorTxt = new ComboBox<>(sectorList);

        observableList.addListener((ListChangeListener<RequestReplyList>) c -> {
            messageList.setItems(null);
            messageList.setItems(observableList);
        });
    }

    @FXML
    void messageListMouseClickedEvent(MouseEvent event) {
        if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("View Offer Information");
            RequestReplyList rr = messageList.getSelectionModel().getSelectedItem();
            alert.setHeaderText(rr.getReply().size() > 0 ? "Offer received" : "Awaiting Offer");
            alert.setContentText(String.format(
                    "Name %s %s \n Email: %s \n Sector: %s \n Region: %s \n Skills: %s",
                    rr.getRequest().getFirstName(), rr.getRequest().getLastName(), rr.getRequest().getEmail(), rr.getRequest().getSector(), rr.getRequest().getRegion(), rr.getRequest().getSector()));

            Label label = new Label("Offer:");
            TextArea textArea;
            if(rr.getReply() != null) {
                textArea = new TextArea();

                rr.getReply().forEach(r -> textArea.appendText(String.format(
                        "Company: %s \n Function: %s \n Salary: %s \n Duration: %s \n contact email: %s \n contact person: %s \n function Description: %s \n \n ---- \n \n",
                        r.getCompanyId(), r.getFunctionTitle(), r.getSalary(), r.getDuration(), r.getContactEmail(), r.getContactPersonName(), r.getFunctionDescription()
                )));
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
        resumeRequest.setSector(sectorTxt.getValue());
        resumeRequest.setSkills(getTextFromFxmlTextField(skillTxt));

        //TODO: Send a ResumeRequest uppon form submit
        String messageId = applicationGateway.sendResumeRequest(resumeRequest);
        RequestReplyList RequestReplyList = applicationGateway.getRequestReplyBiMap().get(messageId);
        observableList.add(RequestReplyList);
        messageList.refresh();
    }

    private String getTextFromFxmlTextField(TextField textField) {
        return textField.getText();
    }

    /**
     * Get RequestReply by a ResumeRequest in the ObservableList
     * @param resumeRequest
     * @return
     */
    private RequestReplyList getRequestReply(ResumeRequest resumeRequest) {
        for (RequestReplyList rr : observableList) {
            if (rr.getRequest().equals(resumeRequest)) {
                return rr;
            }
        }

        return null;
    }

    /**
     * Repopulate all RequestReplies from the JSON file (used as in-memory database).
     * It'll add each RequestReply back to the List.
     */
    private void populateMessageList() {
        HashBiMap<String, RequestReplyList> requests = MessageReader.getRequests();

        requests.forEach((c, r) -> {
            observableList.add(r);
            messageList.refresh();
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

        //TODO: Listen if a ReplyHasArrived from the applicationGateway. Perform some functionality to update the list.
        applicationGateway = new ApplicationGateway() {
            @Override
            public void onReplyArrived(String correlationId, RequestReply<ResumeRequest, ResumeReply> requestReplyList) {
                RequestReplyList rr = getRequestReply(requestReplyList.getRequest());
                if (rr != null) {
                    rr.addReply(requestReplyList.getReply());
                    messageList.refresh();

                    // Update the in-memory database.
                    MessageWriter.update(correlationId, requestReplyList.getReply());
                }
            }
        };
    }
}
