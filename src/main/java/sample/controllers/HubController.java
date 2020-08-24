package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.plaf.synth.SynthTextAreaUI;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

public class HubController {

    @FXML
    public void fromFileClicked(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/File.fxml"));
        Scene scene = new Scene(root,800,600);
        Stage stage = new Stage();
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setTitle("Extract from file");
        stage.setScene(scene);
        stage.show();

        Stage mainWindow = (Stage) ((Node)event.getSource()).getScene().getWindow();
        mainWindow.close();

    }

    @FXML
    public void fromStreamClicked(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("/views/Stream.fxml"));
        Scene scene = new Scene(root,800,600);
        Stage stage = new Stage();
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setTitle("Extract from stream");
        stage.setScene(scene);
        stage.show();

        Stage mainWindow = (Stage) ((Node)event.getSource()).getScene().getWindow();
        mainWindow.close();

    }

    @FXML
    public void fromRedditClicked(ActionEvent event) throws IOException {

        //https://www.reddit.com/r/memes/top.json?limit=900 - majd ezzel

        URL url = new URL("https://www.reddit.com/r/facepalm/");
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"))){
            String line;

            int count = 0;

            while((line = reader.readLine()) != null)
            while(line.contains("https://preview.redd.it/")){
                String link;
                int linkIndex = line.indexOf("https://preview.redd.it/");
                //System.out.println(linkIndex);

                 link = line.substring(linkIndex, linkIndex+150);
                 if(link.contains("width=1080")){
                    link =  link.replace("\\u0026","&").replace("amp;","");


                     String actualLink = link.substring(0,link.indexOf("\""));
                     System.out.println(actualLink);

                     URL linkUrl = new URL(actualLink);
                     File imageFile = new File("/home/bartalos86/Projects/MemeCollection/Reddit/meme-" + count +".png");
                     while (imageFile.exists()){
                         count++;
                         imageFile = new File("/home/bartalos86/Projects/MemeCollection/Reddit/meme-" + count +".png");
                     }

                     Files.createDirectories(imageFile.toPath());

                     BufferedImage meme = ImageIO.read(linkUrl);
                     if(meme != null){
                         ImageIO.write(meme,"png",imageFile);
                         count++;
                     }

                 }


                 line = line.substring(linkIndex+150);


            }

            System.out.println("Total: " + (count-1));

        }

    }
}
