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


    private String task_no;

    private String camera_no;

    private String student_id;

    private String student_name;

    private long start_time;

    private long end_time;

    private List<String> locations;
}
