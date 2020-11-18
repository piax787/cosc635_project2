package com.cosc635.project2_gui;

import java.io.IOException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    Stage window;
    String protocolChoice = "";

    @Override
    public void start(Stage primaryStage) {

        window = primaryStage;
        window.setTitle("Project 2");
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();

        // Give information to user
        Label headingLabel = new Label("Error Control Program, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Label directionLabel = new Label("Please select the communication protocal and click to send or receive file");
        Label enterRandomNumLabel = new Label("Please enter a number from 0-99:");
        
        // Give dropdown selection of protocol choices
        ChoiceBox protocolChoiceBox = new ChoiceBox();
        protocolChoiceBox.getItems().addAll("SAW Protocol", "GBN Protocol");
        protocolChoiceBox.getSelectionModel().select(0);

        // Random number text field
        TextField randomInput = new TextField();
        
        // Action buttons to start receiving and sending files
        Button sendButton = new Button("Send File");
        Button receiveButton = new Button("Receive File");
        sendButton.setOnAction(e -> {
            protocolChoice = (String) protocolChoiceBox.getValue();
            sendFile(protocolChoice, randomInput.getText());
        });
        receiveButton.setOnAction(e -> {
            protocolChoice = (String) protocolChoiceBox.getValue();
            receiveFile(protocolChoice);
        });

        VBox vBoxLayout = new VBox(10);
        vBoxLayout.setPadding(new Insets(20, 20, 20, 20));
        vBoxLayout.getChildren().addAll(headingLabel, directionLabel, protocolChoiceBox, enterRandomNumLabel, randomInput, sendButton, receiveButton);

        Scene mainScene = new Scene(vBoxLayout, 640, 480);
        window.setScene(mainScene);
        window.show();
    }

    public void sendFile(String protocolName, String randomNum) {
        if (protocolName.isBlank()) {
            System.out.println("Please select communication protocol");
        } else {
            System.out.println("Sending file..." + "using " + protocolName);
            try {
                Sender.start(randomNum);
                closeProgramAlert("Program is Finished");
            } catch (IOException ex) {
                System.out.println("Unable to send file");
                ex.printStackTrace();
            }
        }
    }

    public void receiveFile(String protocolName) {
        if (protocolName.isBlank()) {
            System.out.println("Please select communication protocol");
        } else {
            System.out.println("Receiving file..." + "using " + protocolName);
            try {
                Receiver.start();
            } catch (IOException ex) {
                System.out.println("Unable to recieve file");
                ex.printStackTrace();
            }
        }

    }

    public static void closeProgramAlert(String title) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Alert");
        window.setMinWidth(500);
        Label lbl1 = new Label();
        lbl1.setText(title);

        Button closeButton = new Button("Close Program");
        closeButton.setOnAction(e -> {
            window.close();
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20, 20, 20, 20));
        layout.getChildren().addAll(lbl1, closeButton);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }

}
