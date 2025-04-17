package com.metal.web.domain.asr.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @program: shadow-reading
 * @description: asr result entity
 * @author: metal
 * @create: 2025-01-16 10:13
 **/
@Data
@Schema(description = "asr result")
public class ASRResultEntity {

    @Schema(description = "asr wav file text result")
    private String asrResultSentence;

    @Schema(description = "standard sentence")
    private String standardSentence;

    @Schema(description = "word compare result")
    private List<WordResult> wordResultList;

    @Schema(description = "calculate user reading sentence total score")
    private float totalScore;

}
