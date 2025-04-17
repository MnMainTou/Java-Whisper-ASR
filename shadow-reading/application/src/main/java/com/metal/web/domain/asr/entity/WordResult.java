package com.metal.web.domain.asr.entity;

import com.metal.web.enums.EnumWordCompare;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 *
 * @param word
 * @param confident
 * @param compare
 */
@Schema(description = "sentence analyse to word confident score")
public record WordResult(
        @Schema(description = "asr to word")
        String word,
        @Schema(description = "sentence to word")
        String standardWord,
        @Schema(description = "asr to word confident")
        float confident,
        @Schema(description = "word compare standardWord result")
        EnumWordCompare compare ) {
}
