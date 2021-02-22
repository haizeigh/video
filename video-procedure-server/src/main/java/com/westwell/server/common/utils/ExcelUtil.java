package com.westwell.server.common.utils;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

public class ExcelUtil {

    public static void main(String[] args) {
        // 定义一个数据格式化对象
        XSSFWorkbook wb = null;
        try {
            //excel模板路径
            File cfgFile = ResourceUtils.getFile("/home/westwell/java/data/class4/name.xlsx");
            InputStream in = new FileInputStream(cfgFile);
            //读取excel模板
            wb = new XSSFWorkbook(in);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> map = new HashedMap();

//        /获取sheet表格，及读取单元格内容
        XSSFSheet sheet = null;
        try{
            sheet = wb.getSheetAt(0);

            XSSFCell cell = sheet.getRow(0).getCell(0);
//            System.out.println(cell.getStringCellValue());

            Iterator<Row> rowIterator = sheet.rowIterator();
            rowIterator.next();

            while (rowIterator.hasNext()){
                Row next = rowIterator.next();
                int num = (int) next.getCell(1).getNumericCellValue();
                String name = next.getCell(3).getStringCellValue();
//                System.out.println(numericCellValue);
                map.put(num+"", name);
            }
            //先将获取的单元格设置为String类型，下面使用getStringCellValue获取单元格内容
            //如果不设置为String类型，如果单元格是数字，则报如下异常
            //java.lang.IllegalStateException: Cannot get a STRING value from a NUMERIC cell
//            sheet.getRow(2).getCell(2).setCellType(CellType.STRING);
            //读取单元格内容
//            String cellValue = sheet.getRow(2).getCell(2).getStringCellValue();

            //添加一行
//            XSSFRow row = sheet.createRow(1); //第2行开始写数据
//            row.setHeight((short)400); //设置行高
            //向单元格写数据
//            row.createCell(1).setCellValue("名称");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        String targetPath = "/home/westwell/java/data/class4/pic2";
        File targetPathFile = new File(targetPath);
        if (!targetPathFile.exists()){
            targetPathFile.mkdirs();
        }

        String sourcePath = "/home/westwell/java/data/class4/pic";
        File sourcePathFile = new File(sourcePath);
        File[] files = sourcePathFile.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            String[] split = fileName.split("_");
//            System.out.println(split[1]);

//            System.out.println(map.get(split[1]));
            StrBuilder strBuilder = new StrBuilder();
            strBuilder
                    .append(split[0])
                    .append("_")
                    .append(map.get(split[1]))
                    .append("_")
                    .append(split[2]);
            System.out.println(strBuilder.toString());
            file.renameTo(new File(targetPath + "/" + strBuilder.toString()));
        }





    }
}
