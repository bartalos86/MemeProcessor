package sample.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import sample.ImageProcessor;
import sample.VideoProcessor;

import java.io.File;

public class StreamController {

    @FXML
    ImageView imageView;
    @FXML
    TextField urlField;
    @FXML
    TextField splitterPathField;
    @FXML
    TextField rateField;
    @FXML
    TextField saveNameField;
    @FXML
    Button selectSplitterPathBtn;
    @FXML
    Label statusLabel;

    @FXML
    void chooseSplitter(javafx.event.ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File choosenFile = chooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
        splitterPathField.setText(choosenFile.getAbsolutePath());
    }

    @FXML
    public void startStreamExtraction(ActionEvent event) throws Exception {
        beginStreamProcessing(urlField.getText(), splitterPathField.getText());

    }

    private void beginStreamProcessing(String url, String splitterPath) throws Exception {
        Task<Void> videoTask;
        if (!rateField.getText().equals("")) {
            videoTask = new VideoProcessor(imageView).processStreamAsTask(url,
                    splitterPath,
                    "/Projektek/MemeCollection",
                    Float.parseFloat(rateField.getText()),
                    saveNameField.getText());
        } else {
            videoTask = new VideoProcessor(imageView).processStreamAsTask(url,
                    splitterPath,
                    "/Projektek/MemeCollection",
                    1,
                    saveNameField.getText());
        }

        statusLabel.textProperty().bind(videoTask.messageProperty());

        Thread thread = new Thread(videoTask);
        thread.setDaemon(true);
        thread.start();
       // videoTask.run();

         //ImageProcessor.processImage("Resources/process3.png");

    }


}
