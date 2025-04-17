package com.metal.web.domain.asr.service.impl;

import com.metal.sdk.exception.GlobalException;
import com.metal.web.domain.asr.entity.ASRResultEntity;
import com.metal.web.domain.asr.entity.WhisperEntity;
import com.metal.web.domain.asr.entity.WordResult;
import com.metal.web.utils.asr.ASRUtils2;
import com.metal.web.utils.asr.WhisperScoreCalUtils;
import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperJNI;
import io.github.givimad.whisperjni.WhisperSamplingStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
* @program: shadow-reading
* @description: whisper init and method
* @author: metal
* @create: 2025-01-14 10:54
**/
@Service
@Slf4j
public class WhisperModelService {

    @Resource
    private WhisperEntity whisperEntity;

    private WhisperJNI whisper;
    private WhisperContext ctx;

    private static final List MIS_ASR_WORD = List.of("[_BEG_]","[_TT_180]", ".", ",", "?");

    /**
     * load model to whisper
     */
    @PostConstruct
    private void init() throws IOException {
        Path modelPath = Path.of(whisperEntity.getModelFile());
        File modelFile = modelPath.toFile();
        if(!modelFile.exists() || !modelFile.isFile()) {
            throw new RuntimeException("Missing model file: " + modelPath.toAbsolutePath());
        }

        log.info("======begin init whisper model======");
        //setting whisper library dir
//        System.setProperty("io.github.givimad.whisperjni.libdir", whisperEntity.getWhisperLibDir());
        WhisperJNI.loadLibrary(log::info);
        WhisperJNI.setLibraryLogger(log::info);
        whisper = new WhisperJNI();
        ctx = whisper.init(modelPath);
        log.info("======finish init whisper model======");
    }

    /**
     * close whisper and content
     */
    @PreDestroy
    private void close() {
        if(ctx != null) {
            ctx.close();
        }
    }

    /**
     * asr wav file to text
     *
     * @param file     a 16 bit int 16000hz little endian wav file
     * @param sentence
     * @return asr text
     */
    public ASRResultEntity asrResult(MultipartFile file, String sentence) throws Exception {
        StringBuffer asrText = new StringBuffer();
        float[] samples = this.readJFKFileSamples(file);
        WhisperFullParams params = new WhisperFullParams(WhisperSamplingStrategy.GREEDY);
        params.nThreads = this.getThreadCount(); // 设置线程数
        params.singleSegment = true; //控制将整个音频识别为单个段落
        int result = whisper.full(ctx, params, samples, samples.length);
        if(result != 0) {
            throw new GlobalException("asr wav file fail, check file type is right!");
        }

        List<WordResult> wordResultList = new ArrayList<>();
        int numSegments = whisper.fullNSegments(ctx);
        for (int i = 0; i < numSegments; i++) {
            String segmentText = whisper.fullGetSegmentText(ctx, i).replaceFirst(" ", "");
            asrText.append(segmentText);
            var tokens = whisper.fullGetSegmentWordConfidence(ctx, i);
            wordResultList.addAll(WhisperScoreCalUtils.calculateWordScoresOptimized(segmentText, tokens));
        }
        ASRResultEntity entity = ASRUtils2.evaluate(sentence, wordResultList);
        entity.setAsrResultSentence(asrText.toString());
        return entity;
    }

    /**
     * wav file to float array
     * @param file a 16 bit int 16000hz little endian wav file
     * @return
     * @throws Exception
     */
    public float[] readJFKFileSamples(MultipartFile file) throws Exception {
        InputStream bufferedIn = new BufferedInputStream(file.getInputStream());
        // sample is a 16 bit int 16000hz little endian wav file
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
        // read all the available data to a little endian capture buffer
        ByteBuffer captureBuffer = ByteBuffer.allocate(audioInputStream.available());
        captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int read = audioInputStream.read(captureBuffer.array());
        if (read == -1) {
            throw new IOException("Empty file");
        }
        // obtain the 16 int audio samples, short type in java
        ShortBuffer shortBuffer = captureBuffer.asShortBuffer();
        // transform the samples to f32 samples
        float[] samples = new float[captureBuffer.capacity() / 2];
        int i = 0;
        while (shortBuffer.hasRemaining()) {
            samples[i++] = Float.max(-1f, Float.min(((float) shortBuffer.get()) / (float) Short.MAX_VALUE, 1f));
        }
        return samples;
    }

    /**
     * get jvm current thread
     * @return
     */
    public static int getThreadCount() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        return threadMXBean.getThreadCount();
    }
}
