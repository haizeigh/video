package com.westwell.server.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RouterGapResultDto {

    private Date start;

    private Date end;

    private Boolean result;
}
