module MemeProcessor {

    requires uk.co.caprica.vlcj;
    requires org.bytedeco.javacpp;
    requires org.bytedeco.openblas;
    requires org.bytedeco.javacv;
    requires org.bytedeco.javacpp.windows.x86_64;
    requires org.bytedeco.openblas.windows.x86_64;
    requires org.bytedeco.opencv.windows.x86_64;
   // requires org.bytedeco.opencv.windows.x86;
    requires org.bytedeco.ffmpeg;
    requires org.bytedeco.ffmpeg.windows.x86_64;
   // requires org.bytedeco.ffmpeg.windows.x86;
    requires org.bytedeco.videoinput;
    requires org.bytedeco.artoolkitplus;
    requires org.bytedeco.artoolkitplus.windows.x86_64;
   // requires org.bytedeco.artoolkitplus.windows.x86;
    requires org.bytedeco.leptonica;
    requires org.bytedeco.libdc1394;
   // requires org.bytedeco.javacpp.windows.x86;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;
    //requires org.bytedeco.opencv;
    requires static javafx.swt;
    requires static org.bytedeco.opencv;
    requires org.bytedeco.javacv.platform;
    requires uk.co.caprica.vlcj.javafx;


    exports sample.controllers;


    opens sample;
    opens sample.controllers;


}