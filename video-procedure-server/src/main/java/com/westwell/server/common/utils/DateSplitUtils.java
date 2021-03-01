package com.westwell.server.common.utils;

import com.westwell.api.common.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Description: 时间切片工具
 * @Auther: xuxiaojun
 * @Date: 2020-03-19
 */
public class DateSplitUtils {

    public enum IntervalType {
        DAY,
        HOUR,
        MINUTE,
        SECOND,
        ;
    }

    /**
     * 时间切割
     * @param startTime    被切割的开始时间
     * @param endTime      被切割的结束时间
     * @param intervalType
     * @param interval     >0
     * @return
     */
    public static List<DateSplit> splitDate(Date startTime, Date endTime, IntervalType intervalType, int interval) {
        if (interval < 0) {
            return null;
        }
        if (endTime.getTime() <= startTime.getTime()) {
            return null;
        }

        if (intervalType == IntervalType.DAY) {
            return splitByDay(startTime, endTime, interval);
        }
        if (intervalType == IntervalType.HOUR) {
            return splitByHour(startTime, endTime, interval);
        }
        if (intervalType == IntervalType.MINUTE) {
            return splitByMinute(startTime, endTime, interval);
        }
        if (intervalType == IntervalType.SECOND) {
            return splitBySecond(startTime, endTime, interval);
        }
        return null;
    }

    /**
     * 按照小时切割时间区间
     */
    public static List<DateSplit> splitByHour(Date startTime, Date endTime, int intervalHours) {
        if (endTime.getTime() <= startTime.getTime()) {
            return null;
        }

        List<DateSplit> dateSplits = new ArrayList<>(256);

        DateSplit param = new DateSplit();
        param.setStartDateTime(startTime);
        param.setEndDateTime(endTime);
        param.setEndDateTime(addHours(startTime, intervalHours));
        while (true) {
            param.setStartDateTime(startTime);
            Date tempEndTime = addHours(startTime, intervalHours);
            if (tempEndTime.getTime() >= endTime.getTime()) {
                tempEndTime = endTime;
            }
            param.setEndDateTime(tempEndTime);

            dateSplits.add(new DateSplit(param.getStartDateTime(), param.getEndDateTime()));

            startTime = addHours(startTime, intervalHours);
            if (startTime.getTime() >= endTime.getTime()) {
                break;
            }
            if (param.getEndDateTime().getTime() >= endTime.getTime()) {
                break;
            }
        }
        return dateSplits;
    }

    /**
     * 按照秒切割时间区间
     */
    public static List<DateSplit> splitBySecond(Date startTime, Date endTime, int intervalSeconds) {
        if (endTime.getTime() <= startTime.getTime()) {
            return null;
        }
        List<DateSplit> dateSplits = new ArrayList<>(256);

        DateSplit param = new DateSplit();
        param.setStartDateTime(startTime);
        param.setEndDateTime(endTime);
        param.setEndDateTime(addSeconds(startTime, intervalSeconds));
        while (true) {
            param.setStartDateTime(startTime);
            Date tempEndTime = addSeconds(startTime, intervalSeconds);
            if (tempEndTime.getTime() >= endTime.getTime()) {
                tempEndTime = endTime;
            }
            param.setEndDateTime(tempEndTime);

            dateSplits.add(new DateSplit(param.getStartDateTime(), param.getEndDateTime()));

            startTime = addSeconds(startTime, intervalSeconds);
            if (startTime.getTime() >= endTime.getTime()) {
                break;
            }
            if (param.getEndDateTime().getTime() >= endTime.getTime()) {
                break;
            }
        }
        return dateSplits;
    }

    /**
     * 按照天切割时间区间
     */
    public static List<DateSplit> splitByDay(Date startTime, Date endTime, int intervalDays) {
        if (endTime.getTime() <= startTime.getTime()) {
            return null;
        }
        List<DateSplit> dateSplits = new ArrayList<>(256);

        DateSplit param = new DateSplit();
        param.setStartDateTime(startTime);
        param.setEndDateTime(endTime);
        param.setEndDateTime(addDays(startTime, intervalDays));
        while (true) {
            param.setStartDateTime(startTime);
            Date tempEndTime = addDays(startTime, intervalDays);
            if (tempEndTime.getTime() >= endTime.getTime()) {
                tempEndTime = endTime;
            }
            param.setEndDateTime(tempEndTime);

            dateSplits.add(new DateSplit(param.getStartDateTime(), param.getEndDateTime()));

            startTime = addDays(startTime, intervalDays);
            if (startTime.getTime() >= endTime.getTime()) {
                break;
            }
            if (param.getEndDateTime().getTime() >= endTime.getTime()) {
                break;
            }
        }
        return dateSplits;
    }

    /**
     * 按照分钟切割时间区间
     *
     * @param startTime
     * @param endTime
     * @param intervalMinutes
     * @return
     */
    public static List<DateSplit> splitByMinute(Date startTime, Date endTime, int intervalMinutes) {
        if (endTime.getTime() <= startTime.getTime()) {
            return null;
        }
        List<DateSplit> dateSplits = new ArrayList<>(256);

        DateSplit param = new DateSplit();
        param.setStartDateTime(startTime);
        param.setEndDateTime(endTime);
        param.setEndDateTime(addMinute(startTime, intervalMinutes));
        while (true) {
            param.setStartDateTime(startTime);
            Date tempEndTime = addMinute(startTime, intervalMinutes);
            if (tempEndTime.getTime() >= endTime.getTime()) {
                tempEndTime = endTime;
            }
            param.setEndDateTime(tempEndTime);

            dateSplits.add(new DateSplit(param.getStartDateTime(), param.getEndDateTime()));

            startTime = addMinute(startTime, intervalMinutes);
            if (startTime.getTime() >= endTime.getTime()) {
                break;
            }
            if (param.getEndDateTime().getTime() >= endTime.getTime()) {
                break;
            }
        }
        return dateSplits;
    }



    private static Date addDays(Date date, int days) {
        return add(date, Calendar.DAY_OF_MONTH, days);
    }

    private static Date addHours(Date date, int hours) {
        return add(date, Calendar.HOUR_OF_DAY, hours);
    }

    private static Date addMinute(Date date, int minute) {
        return add(date, Calendar.MINUTE, minute);
    }
    private static Date addSeconds(Date date, int second) {
        return add(date, Calendar.SECOND, second);
    }

    private static Date add(final Date date, final int calendarField, final int amount) {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    private static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateSplit {
        private Date startDateTime;
        private Date endDateTime;

        public String getStartDateTimeStr() {
            return formatDateTime(startDateTime);
        }

        public String getEndDateTimeStr() {
            return formatDateTime(endDateTime);
        }
    }


    public static void main(String[] args) {
        Date yesterdayFirstTime = DateUtils.stringToDate("2021-02-23 0:29:40");
        Date yesterdayEndTime = DateUtils.stringToDate("2021-02-23 10:59:40");
        List<DateSplitUtils.DateSplit> dateSplits = DateSplitUtils.splitDate(yesterdayFirstTime, yesterdayEndTime, DateSplitUtils.IntervalType.HOUR,1);

        for (DateSplitUtils.DateSplit dateSplit : dateSplits) {
            System.out.println("切割后的时间区间: " + dateSplit.getStartDateTimeStr() + " --->  " +  dateSplit.getEndDateTimeStr());

        }
    }

}
