package com.westwell.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskFinalResultDto {

//    String mapKey = "task_no,camera_no,student_id,label,start_time,end_time,locations";

    private String task_no;

    private String camera_no;

    private String student_id;

    private String label;

    private long start_time;

    private long end_time;

    private List<String> locations;
}
