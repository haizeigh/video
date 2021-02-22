package com.westwell.server.common.utils;

public class FfmpegUtil {

    //    视频截图
    public static Process cutPics(String commands) throws Exception {
/*
        File file = new File(picsPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        List<String> commands = new java.util.ArrayList<String>();
        commands.add(DataConfig.FFMPEG_PATH);
//        commands.add("ffmpeg");

        commands.add("-i");
        commands.add(videoPath);

        commands.add("-t");
        commands.add("5");

        commands.add("-r");
        commands.add("10");

        commands.add("-q:v");
        commands.add("2");

        commands.add("-f");
        commands.add("image2");

        commands.add("-y");

        commands.add(picsPath + "/%09d.jpeg");
        StringBuffer commandsBuffer = new StringBuffer();
        for (int i = 0; i < commands.size(); i++)
            commandsBuffer.append(commands.get(i) + " ");
        System.out.println("command is : " + commandsBuffer);*/
        Process proc = null;
        try {
//            这里需要执行"sh", "-c" 不然造成 No such file or directory
            proc = Runtime.getRuntime().exec(new String[]{"sh", "-c", commands});
//            读取流信息, 防止因为信息太多导致被填满，最终导致子进程阻塞住
            PrintStream errorStream = new PrintStream(proc.getErrorStream());
            PrintStream inputStream = new PrintStream(proc.getInputStream());
            errorStream.start();
            inputStream.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return proc;
    }

//    海康带宽不足的问题 method DESCRIBE failed: 453 Not Enough Bandwidth

    public static void main(String[] args) throws Exception {

//        FfmpegUtil ff = new FfmpegUtil();
        Process process = cutPics("rtsp://admin:xijingkeji123@10.66.64.57:554/Streaming/tracks/101?starttime=20210201t000000z&endtime=20210201t000005z" + "/home/westwell/java/file2");
        int exit = 0;
        if ((exit = process.waitFor()) == 0) {
            System.out.println("---执行结果：---" + (exit == 0 ? "【成功】" : "【失败】"));
        }


    }


    static class PrintStream extends Thread {
        java.io.InputStream __is = null;

        public PrintStream(java.io.InputStream is) {
            __is = is;
        }

        @Override
        public void run() {
            try {
                while (this != null) {
                    int _ch = __is.read();
                    if (_ch != -1) {
                        System.out.print((char) _ch);
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
