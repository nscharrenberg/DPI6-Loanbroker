package com.nscharrenberg.elect.amazon.controllers;

import com.nscharrenberg.elect.amazon.domain.OfferReply;
import com.nscharrenberg.elect.amazon.domain.OfferRequest;
import com.nscharrenberg.elect.amazon.gateways.application.ApplicationGateway;
import com.nscharrenberg.elect.amazon.gateways.messaging.requestreply.RequestReply;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

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
        try {
            RequestReply<OfferRequest, OfferReply> rr = messageList.getSelectionModel().getSelectedItem();
            OfferReply offerReply = new OfferReply();
            offerReply.setCompanyId("amazon");
            offerReply.setContactEmail(getTextFromFxmlTextField(emailTxt));
            offerReply.setContactPersonName(getTextFromFxmlTextField(personTxt));
            offerReply.setFunctionTitle(getTextFromFxmlTextField(functionTxt));
            offerReply.setSalary(Double.parseDouble(getTextFromFxmlTextField(salarayTxt)));
            offerReply.setDuration(getTextFromFxmlTextField(durationTxt));
            offerReply.setFunctionDescription(descriptionTxt.getText());

            if (rr != null && offerReply != null) {
                rr.setReply(offerReply);
                applicationGateway.sendOfferReply(rr);
                messageList.refresh();
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Something went wrong!");
            alert.setContentText(ex.getMessage());

// Create expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
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

    private String getTextFromFxmlTextField(TextField textField) {
        return textField.getText();
    }

}