package bartalos86;

public class ImageCropData {
    private int xOffset;
    private int width;
    private int height;
    private int yOffset;

    public ImageCropData(int xOffset, int width,  int height){
        this(xOffset,0,width,height);
    }

    public ImageCropData(int xOffset,int yOffset, int width,  int height) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
    }

    public int getxOffset() {
        return xOffset;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
