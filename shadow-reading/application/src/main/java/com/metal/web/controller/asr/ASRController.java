package com.metal.web.controller.asr;

import com.metal.web.domain.asr.entity.ASRResultEntity;
import com.metal.web.domain.asr.entity.WordOfIPAEntity;
import com.metal.web.domain.asr.service.impl.MaryTTSService;
import com.metal.web.domain.asr.service.impl.WhisperModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @program: shadow-reading
 * @description: asr api
 * @author: metal
 * @create: 2025-01-14 11:25
 **/
@RestController
@RequestMapping("/asr")
@Tag(name = "asr api")
public class ASRController {

    @Resource
    private WhisperModelService whisperModelService;

    @Resource
    private MaryTTSService maryTTSService;


    @Operation(summary = "wav file to text", parameters = {
                @Parameter(name = "file", description = "wav file", required = true),
                @Parameter(name = "sentence", description = "reading sentence", required = true)})
    @PostMapping(value = "/asrResult", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ASRResultEntity asrResult(MultipartFile file, String sentence) throws Exception {
        return whisperModelService.asrResult(file, sentence);
    }

    @PostMapping("/analyseSentence")
    @Operation(summary = "sentence analyse every word phoneme")
    public List<WordOfIPAEntity> analyseSentence(String sentence) throws Exception {
        return maryTTSService.analyseSentence(sentence);
    }

}
