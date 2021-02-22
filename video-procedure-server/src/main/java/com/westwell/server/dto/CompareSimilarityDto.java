package com.westwell.server.dto;

import lombok.Data;

@Data
public class CompareSimilarityDto {

    private String picColleKey;

    private Double similarity;

    private boolean overSimilarity;
}
