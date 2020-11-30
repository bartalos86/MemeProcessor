package bartalos86.controllers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import bartalos86.VideoProcessor;
import bartalos86.models.VideoItem;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class DownloadStreamController {

    @FXML
    ImageView imageView;
    @FXML
    TextField urlField;
    @FXML
    TextField splitterPathField;
    @FXML
    TextField frameSkipField;
    @FXML
    TextField saveNameField;

    @FXML
    Button selectSplitterPathBtn;

    @FXML
    ListView videoList;

    @FXML
    Label statusLabel;



    @FXML
    void videoListKeyPressed(KeyEvent event){
        if(event.getCode().toString().equals("DELETE")){
           Object selectedItem =  videoList.getSelectionModel().getSelectedItem();
           if(selectedItem != null)
               videoList.getItems().remove(selectedItem);
        }
    }

    @FXML
    void chooseSplitter(javafx.event.ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File choosenFile = chooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
        splitterPathField.setText(choosenFile.getAbsolutePath());
    }

    @FXML
    public void addItemToList(ActionEvent event) {

        int frameSkip = Integer.parseInt(frameSkipField.getText());
        VideoItem item = new VideoItem(urlField.getText(), splitterPathField.getText(), frameSkip, saveNameField.getText());
        videoList.getItems().add(item);


    }

    @FXML
    public void startExtraction(ActionEvent event) throws IOException, InterruptedException {
        ObservableList<VideoItem> videoItems = videoList.getItems();

        downloadVideos(videoItems);
        processDownloadedVideos();
    }

    private void processDownloadedVideos() throws IOException {
        File downloads = new File("temp");

        File[] files = downloads.listFiles();
        processDownloadedVideosRecursive(files, 0);
    }

    private void processDownloadedVideosRecursive(File[] files, int index) throws IOException {
        if (index == files.length || index > files.length)
            return;

        File videoFile = files[index];

        System.out.println(videoFile.getName());
        String name = videoFile.getName();


        int id = Integer.parseInt(name.substring(0, name.indexOf('.')));
        VideoItem theItem = null;
        for (Object videoI : videoList.getItems()) {
            VideoItem videoItem = ((VideoItem) videoI);
            if (videoItem.getId() == id) {
                theItem = videoItem;
                System.out.println("Video found with id: " + id);
                break;
            }else{
                System.out.println("Video not found with id: " + id);
            }

        }

        Task<Void> videoTask = createVideoprocessorTask(videoFile, theItem.getSplitterPath(), theItem.getFrameSkip(), theItem.getExtractFolder());
        videoTask.setOnSucceeded((event) -> {

                System.out.println("Suceeded");
                VideoItem item = (VideoItem) videoList.getItems().get(index);
                item.setStatus("✅ Finished - ");
                //processDownloadedVideosRecursive(files, index + 1);

        });

       videoTask.setOnFailed((event)-> {
           System.out.println("Failed");
           try {
               System.out.println("Index " + index + " Total video files: " + files.length);
               VideoItem item = (VideoItem) videoList.getItems().get(index);
               item.setStatus(" ❌ Failed - ");


               processDownloadedVideosRecursive(files, index + 1);
           } catch (IOException e) {
               e.printStackTrace();
           }

       });

        Thread thread = new Thread(videoTask);
        thread.setDaemon(true);
        thread.start();



    }


    private Task<Void> createVideoprocessorTask(File file, String splitterPath, int frameskip, String saveName) throws IOException {
        Task<Void> videoTask;
        if (frameskip > 0) {
            videoTask = new VideoProcessor(imageView).processVideoAsTask(file.getPath(),
                    "/Projektek/MemeCollection",
                    splitterPath,
                    frameskip,
                    saveName);
        } else {
            videoTask = new VideoProcessor(imageView).processVideoAsTask(file.getPath(),
                    "/Projektek/MemeCollection",
                    splitterPath,
                    saveName);
        }

         statusLabel.textProperty().bind(videoTask.messageProperty());


        return videoTask;

    }

    private void downloadVideos(ObservableList<VideoItem> videos) throws IOException, InterruptedException {
        File tempdir = new File("temp");
        if (!tempdir.exists())
            Files.createDirectory(tempdir.toPath());
        else
            cleanTemp();

        downloadVideoSync(videos);

        //downloadVideosRecursive(videos, 0);
    }

    private void cleanTemp() {
        File folder = new File("temp");

        for (File file : folder.listFiles()) {
            file.delete();
        }
    }

    private void downloadVideoSync(ObservableList<VideoItem> videos) throws IOException {

        for (VideoItem video: videos) {
            Runtime runtime = Runtime.getRuntime();
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd temp && youtube-dl","-o", String.valueOf(video.getId()), video.getUrl());

            Process proc = builder.start();

            BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                System.out.println(line);
            }
        }

    }

    private void downloadVideosRecursive(ObservableList<VideoItem> videos, int index) throws IOException, InterruptedException {

        if (index == videos.size() || index > videos.size())
            return;

        VideoItem video = videos.get(index);


        Runtime runtime = Runtime.getRuntime();
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd temp && youtube-dl","-o", String.valueOf(video.getId()), video.getUrl());

        Process proc = builder.start();

        BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }

        proc.onExit().thenRun(() -> {
            System.out.println("Done");
            try {
                downloadVideosRecursive(videos, index + 1);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).wait();
    }
}
