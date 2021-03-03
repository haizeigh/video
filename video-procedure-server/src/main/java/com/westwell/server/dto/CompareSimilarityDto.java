package com.westwell.server.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CompareSimilarityDto {

    private String picColleKey;

    private Double similarity;

    private boolean overSimilarity;
}
