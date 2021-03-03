package com.westwell.server.common.utils;

public class LocationUtil {

    /**
     *  IoU（交并比）计算
     * @param box1  (x1,y1)(x2,y2) | x y w(宽) h(高)
     * @param box2
     * @param x1y1x2y2 true 为 x1y1x2y2 false 为 xywh
     * @return IoU矩阵重叠率
     */
    public static double iou(double[] box1, double[] box2, boolean x1y1x2y2){
        if (x1y1x2y2){
            box1[2] = box1[2]-box1[0];//x2-x1
            box1[3] = box1[3]-box1[1];//y2-y1
            box2[2] = box2[2]-box2[0];
            box2[3] = box2[3]-box2[1];
        }
        double leftColumnMax = Math.max(box1[0], box2[0]);
        double rightColumnMin = Math.min(box1[0]+box1[2],box2[0]+box2[2]);
        double upRowMax = Math.max(box1[1], box2[1]);
        double downRowMin = Math.min(box1[1]+box1[3],box2[1]+ box2[3]);

        if (leftColumnMax>=rightColumnMin || downRowMin<=upRowMax){
            return 0;
        }
        double s1 = box1[2]*box1[3];
        double s2 = box2[2]*box2[3];
        double sCross = (downRowMin-upRowMax)*(rightColumnMin-leftColumnMax);
        return sCross/(s1+s2-sCross+1e-16);
    }

    public static double[] parseLocation(String location){

        String[] locationSplit = location.split("_");
        double[] locationArray = new double[locationSplit.length];
        for (int i = 0; i < locationSplit.length; i++) {
            locationArray[i] = Double.parseDouble(locationSplit[i]);
        }
        return locationArray;
    }

    public static double calculateLocationIOU(String location1, String location2){
        double[] doubles1 = parseLocation(location1);
        double[] doubles2 = parseLocation(location2);
        return iou(doubles1, doubles2, false);

    }



    public static void main(String[] args) {
        double[] box2 = { 3, 3, 5, 6};
        double[] box1 = { 4, 1, 6, 4};
//        double[] box1 = { 3, 3, 2, 3};
//        double[] box2 = { 4, 1, 2, 3};
        double v = iou(box1, box2,true);
        System.out.println(v);
    }

}
