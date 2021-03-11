package com.westwell.server.common.utils;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
public class ImageUtil {

    public static void drawRect ( int x, int y, int width, int height, String infile, String outfile) throws IOException  {
        BufferedImage image = ImageIO.read(new File(infile));
        Graphics g = image.getGraphics();
        g.setColor(Color.green);
        Graphics2D g2D = (Graphics2D)g;
        g2D.setStroke(new BasicStroke(4.5f)); //此处2.5f即是画线宽度
        g2D.drawRect(x, y, width, height);

        FileOutputStream out = new FileOutputStream(outfile);//输出图片的地址
        ImageIO.write(image, "jpeg", out);
    }


     public static void main(String[] args) throws IOException {
//    InputStream in = new FileInputStream("d:\\test.jpg");//图片路径
    BufferedImage image = ImageIO.read(new File("/home/westwell/java/file/identify/2021-03-09/1-117/113000-113030/pic/wellcare:117:1:1614051000000:01"));
    Graphics g = image.getGraphics();
         g.setColor(Color.green);
         Graphics2D g2D = (Graphics2D)g;
         g2D.setStroke(new BasicStroke(4.5f)); //此处2.5f即是画线宽度
         g2D.drawRect(2263, 557, 308, 367);
//    g.setPaintMode();
//    g.setColor(Color.RED);//画笔颜色
//    g.fillRect(2263 , 557, 308 + 5, 367 + 5 );
//         2263_557_308_367
//    g.drawRect(2263, 557, 308, 367);//矩形框(原点x坐标，原点y坐标，矩形的长，矩形的宽)
    //g.dispose();
    FileOutputStream out = new FileOutputStream("/home/westwell/java/file/identify/2021-03-09/1-117/113000-113030/pic/wellcare:117:1:1614051000000:001");//输出图片的地址
    ImageIO.write(image, "jpeg", out);
  }
}


