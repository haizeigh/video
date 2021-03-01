package com.westwell.api.common.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
public class ImgTransitionUtil {
    /** resize the image in byte stream(format: [in]GIF, JPG; [out]JPG)
     *  @param in - the binary stream of the original picture in GIF or JPG
     *  @param maxDim - the bigger one between height and width after the picture is resized
     *  @return the binary stream of the resized picture in JPG
     */
    /*这个方法是重新根据原图片宽高比例按传入的宽度进行调整，
    in原图片转为的byte数组，maxDim需要转的宽度*/
    public static byte[] resizeImage(byte[] in,int maxDim)
    {
        try
        {
            Image inImage=Toolkit.getDefaultToolkit().createImage(in);
            ImageIcon inImageIcon = new ImageIcon(in);

            int imh = inImageIcon.getIconHeight();
            int imw = inImageIcon.getIconWidth();
            double scale;
            if( imh <= maxDim && imw <= maxDim )
                scale = 1;
            else if( imh > imw )
                scale = (double) maxDim / (double) imh;
            else
                scale = (double) maxDim / (double) imw;

            int scaledW = (int) (scale * imw);
            int scaledH = (int) (scale * imh);

            Image img = inImage.getScaledInstance(scaledW, scaledH, Image.SCALE_FAST);
           /* AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
            img = ato.filter(bufImg, null);*/
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean flag = ImageIO.write(toBufferedImage(img) , "jpg", out);
            //byte[] b = out.toByteArray();
            /*JimiRasterImage raster = Jimi.createRasterImage(img.getSource());
            // --java.io.ByteArrayOutputStream
            Jimi.putImage("image/jpeg", raster, outStream);
            outStream.flush();
            outStream.close();*/
            return out.toByteArray();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
    /*结合上面那个图片放大缩小方法使用的*/
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }
        image = new ImageIcon(image).getImage();
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
        }

        if (bimage == null) {
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        Graphics g = bimage.createGraphics();

        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }
    /*该方法跟其名字在一样，将文件转为byte数组*/
    public static byte [] fileToByte(File img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte [] bytes = null;
        try {
            BufferedImage bi;
            bi = ImageIO.read(img);
            ImageIO.write(bi, "jpg", baos);
            bytes = baos.toByteArray();

        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            baos.close();
        }
        return bytes;
    }

    /*同理将byte数组转为file文件*/
    public static void byteToFile(byte[] bytes, String imagePath)throws Exception{
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage bi1 =ImageIO.read(bais);
        try {
//            File w2 = new File("W:\\img\\00000000003.jpg");//可以是jpg,png,gif格式
            File w2 = new File(imagePath);//可以是jpg,png,gif格式
            if (!w2.exists()){
                w2.mkdirs();
            }

            ImageIO.write(bi1, "jpg", w2);//不管输出什么格式图片，此处不需改动
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            bais.close();
        }
    }


    public static byte[] base64ToByte(String image) throws IOException {

        /*String imgBase64 = fileImg.replaceAll("data:image/png;base64,","");
        BASE64Decoder d = new BASE64Decoder();
        byte[] data = d.decodeBuffer(imgBase64);*/

        // 通过base64来转化图片
        image = image.replaceAll("data:image/jpeg;base64,", "");
        // Base64解码
        BASE64Decoder d = new BASE64Decoder();
        byte[] data = d.decodeBuffer(image);

        return data;
    }

    public static void base64ToFile(String image, String path) throws Exception {
        byte[] bytes = base64ToByte(image);
        byteToFile(bytes, path);

    }

    public static String bytesToBase64(byte[] bytes) throws IOException {

        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        // 返回Base64编码过的字节数组字符串
        return encoder.encode(bytes);

    }

    public static String imagePathToBase64(String imagePath) throws Exception {

        byte[] bytes = fileToByte(new File(imagePath));
        return bytesToBase64(bytes);

    }

    public static String imageFileToBase64(File image) throws Exception {

        byte[] bytes = fileToByte(image);
        return bytesToBase64(bytes);

    }


    public static void main(String[] args) throws Exception {
        String image = "/home/westwell/java/file2/pic-00025.jpeg";

        byte[] bytes = fileToByte(new File(image));

        String toBase64 = bytesToBase64(bytes);
        System.out.println(toBase64);

        byte[] base64ToByte = base64ToByte(toBase64);

        String image2 = "/home/westwell/java/file2/pic-000252.jpeg";
        byteToFile(base64ToByte, image2);


    }

}


