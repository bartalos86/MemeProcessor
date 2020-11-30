package bartalos86.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import bartalos86.VideoProcessor;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class FileController implements Initializable {
    @FXML
    ImageView imageView;
    @FXML
    TextField videoPathField;
    @FXML
    TextField splitterPathField;
    @FXML
    TextField saveNameField;
    @FXML
    TextField frameskipField;
    @FXML
    Button selectVideoPathBtn;
    @FXML
    Button selectSplitterPathBtn;
    @FXML
    Label statusLabel;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    void chooseVideo(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File choosenFile = chooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
        videoPathField.setText(choosenFile.getAbsolutePath());
    }

    @FXML
    void chooseSplitter(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File choosenFile = chooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
        splitterPathField.setText(choosenFile.getAbsolutePath());
    }

    @FXML
    void startExtraction(ActionEvent event) {

        try {
            beginVideoProcessing(videoPathField.getText(), splitterPathField.getText());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void beginVideoProcessing(String videoPath, String splitterPath) throws Exception {
        Task<Void> videoTask;
        if (!frameskipField.getText().equals("")) {
            videoTask = new VideoProcessor(imageView).processVideoAsTask(videoPath,
                    "/Projektek/MemeCollection",
                    splitterPath,
                    Integer.parseInt(frameskipField.getText()),
                    saveNameField.getText());
        } else {
            videoTask = new VideoProcessor(imageView).processVideoAsTask(videoPath,
                    "/Projektek/MemeCollection",
                    splitterPath,
                    saveNameField.getText());
        }

        statusLabel.textProperty().bind(videoTask.messageProperty());

        Thread thread = new Thread(videoTask);
        thread.setDaemon(true);


        thread.start();

        /// ImageProcessor.processImage("Resources/process3.png");

    }

    public void updateStatus(String status) {
        statusLabel.setText(status);

    }

}
