package com.metal.web.domain.asr.entity;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import java.io.File;

/**
 * @program: shadow-reading
 * @description: whisper model file path
 * @author: metal
 * @create: 2025-01-14 11:11
 **/
@ConfigurationProperties(prefix = "whisper")
@Validated
@Data
public class WhisperEntity {

    @NotEmpty(message = "启动命令需要添加参数：whisper.modelFile=whisper模型路径+文件名")
    private String modelFile;

}
