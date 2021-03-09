package com.westwell.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskSummaryResultDto {


    private String no;

    private String task_no;

    private String camera_no;

    private String student_id;

    private String student_name;

    private long start_time;

    private long end_time;

    private String video_day;

    private String create_time;

//    private List<String> locations;
}
