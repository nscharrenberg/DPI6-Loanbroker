package com.nscharrenberg.elect.google.controllers;

import com.google.common.collect.HashBiMap;
import com.nscharrenberg.elect.google.domain.OfferReply;
import com.nscharrenberg.elect.google.domain.OfferRequest;
import com.nscharrenberg.elect.google.gateways.application.ApplicationGateway;
import com.nscharrenberg.elect.google.gateways.messaging.requestreply.RequestReply;
import com.nscharrenberg.elect.google.shared.MessageReader;
import com.nscharrenberg.elect.google.shared.MessageWriter;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

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
    }

    @FXML
    void sendBtn(ActionEvent event) {
        try {
            RequestReply<OfferRequest, OfferReply> rr = messageList.getSelectionModel().getSelectedItem();
            OfferReply offerReply = new OfferReply();
            offerReply.setCompanyId("google");
            offerReply.setContactEmail(getTextFromFxmlTextField(emailTxt));
            offerReply.setContactPersonName(getTextFromFxmlTextField(personTxt));
            offerReply.setFunctionTitle(getTextFromFxmlTextField(functionTxt));
            offerReply.setSalary(Double.parseDouble(getTextFromFxmlTextField(salarayTxt)));
            offerReply.setDuration(getTextFromFxmlTextField(durationTxt));
            offerReply.setFunctionDescription(descriptionTxt.getText());

            if (rr != null && offerReply != null) {
                rr.setReply(offerReply);

                //TODO: Send an OfferReply uppon form submit
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

        //TODO: Listen if a OfferRequest has arrived from the applicationGateway. Perform some functionality to update the list.
        applicationGateway = new ApplicationGateway() {
            @Override
            public void onOfferRequestArrived(String correlationId, OfferRequest offerRequest) {
                observableList.add(new RequestReply<>(offerRequest, null));
                messageList.refresh();

                MessageWriter.add(correlationId, offerRequest);
            }
        };
    }

    private void populateMessageList() {
        HashBiMap<String, OfferRequest> requests = MessageReader.getRequests();

        requests.forEach((c, r) -> {
            observableList.add(new RequestReply<>(r, null));
            messageList.refresh();
        });
    }
}
