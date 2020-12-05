package bartalos86.controllers;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.Format;
import com.github.kiulian.downloader.model.formats.VideoFormat;
import com.github.kiulian.downloader.model.quality.VideoQuality;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

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
    public void startExtraction(ActionEvent event) throws IOException, InterruptedException, YoutubeException {
        ObservableList<VideoItem> videoItems = videoList.getItems();

        downloadVideos(videoItems);
        processDownloadedVideos();
    }

    private void processDownloadedVideos() throws IOException {
        File downloads = new File("temp");

        File[] files = downloads.listFiles();
        if(files != null)
        processDownloadedVideosRecursive(files, 0);
    }

    @FXML
    private void readItemsFromFile() throws IOException {
        File list = new File("download-list.txt");
        if(!list.exists())
            return;

       BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(list.getPath())));
        String line;

        while((line = reader.readLine()) != null){
            String[] params = line.split(";");
            VideoItem itemToAdd = new VideoItem(params[0],params[1],Integer.parseInt(params[2]),params[3]);
            videoList.getItems().add(itemToAdd);
        }



    }

    private void processDownloadedVideosRecursive(File[] files, int index) throws IOException {
        if (index == files.length || index > files.length)
            return;

        File videoFile = files[index];

        System.out.println(videoFile.getName());
        String name = videoFile.getName();


        String id = name.substring(0, name.indexOf('.'));
        VideoItem theItem = null;
        for (Object videoI : videoList.getItems()) {
            VideoItem videoItem = ((VideoItem) videoI);
            if (videoItem.getId().equals(id)) {
                theItem = videoItem;
                System.out.println("Video found with id: " + id);
                break;
            }else{
                System.out.println("Video not found with id: " + id);
            }

        }

        Task<Void> videoTask = createVideoprocessorTask(videoFile, theItem.getSplitterPath(), theItem.getFrameSkip(), theItem.getExtractFolder());

        VideoItem finalTheItem = theItem;
        videoTask.setOnSucceeded((event) -> {

                System.out.println("Suceeded");
                VideoItem item = (VideoItem) videoList.getItems().get(videoList.getItems().indexOf(finalTheItem));
                item.setStatus("✅ Finished - ");
            try {
                processDownloadedVideosRecursive(files, index + 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });


       videoTask.setOnFailed((event)-> {
           System.out.println("Failed");
           try {
               System.out.println("Index " + index + " Total video files: " + files.length);
               VideoItem item = (VideoItem) videoList.getItems().get(videoList.getItems().indexOf(finalTheItem));
               item.setStatus(" ❌ Failed - ");
               System.out.println(event.getSource().exceptionProperty().toString());

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

    private void downloadVideos(ObservableList<VideoItem> videos) throws IOException, InterruptedException, YoutubeException {
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

    private void downloadVideo(String videoID) throws YoutubeException, IOException {

        System.out.println(videoID);
        YoutubeDownloader downloader = new YoutubeDownloader();
        YoutubeVideo video = downloader.getVideo(videoID);
        List<VideoFormat> formats = video.videoFormats();
        VideoFormat desiredFormat = null;
        for (VideoFormat format:
             formats) {
            if(format.videoQuality().equals(VideoQuality.hd720)){
                desiredFormat = format;
                break;
            }
        }

        video.download(desiredFormat,new File("temp"),videoID);

    }

    private void downloadVideoSync(ObservableList<VideoItem> videos) throws IOException, InterruptedException, YoutubeException {

        System.out.println("Video count: "+videos.size());
        for (VideoItem video: videos) {

            downloadVideo(video.getId());
        }

    }

}
