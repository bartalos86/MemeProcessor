package bartalos86.models;

public class VideoItem {
    private String url;
    private String splitterPath;
    private int frameSkip;
    private String extractFolder;
    private String id;
    private String status = "";


    public VideoItem(String url, String splitterPath, int rate, String extractFolder) {
        this.url = url;
        this.splitterPath = splitterPath;
        this.frameSkip = rate;
        this.extractFolder = extractFolder;
        id = extractId();

    }

    private String extractId(){
        int lastIndex = url.contains("&") ? url.indexOf("&") : url.length();
        return url.substring(url.lastIndexOf("v=")+2,lastIndex);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSplitterPath() {
        return splitterPath;
    }

    public void setSplitterPath(String splitterPath) {
        this.splitterPath = splitterPath;
    }

    public int getFrameSkip() {
        return frameSkip;
    }

    public void setFrameSkip(int frameSkip) {
        this.frameSkip = frameSkip;
    }

    public String getExtractFolder() {
        return extractFolder;
    }

    public void setExtractFolder(String extractFolder) {
        this.extractFolder = extractFolder;
    }

    public String getId() {
        return id;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
      return status + extractFolder;
    }
}
