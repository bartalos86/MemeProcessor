module MemeProcessor {
    requires org.bytedeco.javacpp;
    requires org.bytedeco.openblas;
    requires org.bytedeco.javacv;
    requires org.bytedeco.javacpp.windows.x86_64;
    requires org.bytedeco.openblas.windows.x86_64;
    requires org.bytedeco.opencv.windows.x86_64;
    requires org.bytedeco.ffmpeg;
    requires org.bytedeco.ffmpeg.windows.x86_64;
    requires org.bytedeco.videoinput;
    requires org.bytedeco.artoolkitplus;
    requires org.bytedeco.artoolkitplus.windows.x86_64;
    requires org.bytedeco.leptonica;
    requires org.bytedeco.libdc1394;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.controls;
    requires java.desktop;
    requires static org.bytedeco.opencv;
    requires org.bytedeco.javacv.platform;
    requires org.slf4j;
    requires org.bytedeco.tesseract;
    requires org.bytedeco.tesseract.windows.x86_64;
    requires tess4j;
    requires javafx.graphics;
    requires java.youtube.downloader;
    requires java.sql;
    requires fastjson;

    exports bartalos86.controllers;

    opens bartalos86;
    opens bartalos86.controllers;
}