package com.metal.web.utils.asr;

import cn.hutool.core.collection.CollectionUtil;
import com.metal.web.domain.asr.entity.ASRResultEntity;
import com.metal.web.domain.asr.entity.WordResult;
import com.metal.web.enums.EnumWordCompare;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: shadow-reading
 * @description: asr utils
 * @author: metal
 * @create: 2025-01-16 11:12
 **/
@Slf4j
public class ASRUtils {

    private static final float DEFAULT_CONFIDENT = 1f;
    private static final float MIS_MATCHED_CONFIDENT = -10f;
    private static final float MISSED_CONFIDENT = -10f;
    private static final float EXTRA_CONFIDENT = -5f;

    public static List<WordResult> sentenceToWordResultList(String sentence) {
        List<WordResult> wordResultList = new ArrayList<>();
        if(StringUtils.isEmpty(sentence)) {
            return wordResultList;
        }
        // 保留字母、空格、单引号，并转化为小写
        sentence = sentence.replaceAll("[^a-zA-Z0-9\\s']", "").toLowerCase();
        // sentence split word list
        String[] words = sentence.split(" ");
        for (String word : words) {
            if(StringUtils.isNotEmpty(sentence)) {
                wordResultList.add(new WordResult(word, word, DEFAULT_CONFIDENT, EnumWordCompare.MATCH));
            }
        }
        return wordResultList;
    }

    /**
     * evaluate sentence and user word result
     * @param sentence
     * @param userWords
     * @return
     */
    public static ASRResultEntity evaluate(String sentence, List<WordResult> userWords) {
        ASRResultEntity asrResult = new ASRResultEntity();
        List<WordResult> wordAlignmentList = new ArrayList<>(); //compare word result list
        List<WordResult> standardWords = sentenceToWordResultList(sentence);

        int m = standardWords.size();
        int n = userWords.size();
        float[][] dp = new float[m + 1][n + 1];

        // 初始化 DP 表
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i * -10; // 漏读惩罚
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j * -5; // 多读惩罚
        }
        // 填充 DP 表
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                var std = standardWords.get(i - 1);
                var usr = userWords.get(j - 1);

                // 匹配单词情况
                float matchScore = 0;
                if (std.word().equals(usr.word())) {
                    if (usr.confident() >= std.confident()) {
                        matchScore = 100.0f; // 满分
                    } else {
                        matchScore = (usr.confident() / std.confident()) * 100.0f; // 按比例评分
                    }
                } else {
                    matchScore = -5; // 错误发音惩罚
                }

                // 状态转移：匹配、漏读、多读
                dp[i][j] = Math.max(dp[i - 1][j - 1] + matchScore, // 匹配或错误发音
                        Math.max(dp[i - 1][j] - 10, // 漏读
                                dp[i][j - 1] - 5)); // 多读
            }
        }

        // 回溯找出最佳路径
        int i = m, j = n;
        while (i > 0 && j > 0) {
            var std = standardWords.get(i - 1);
            var usr = userWords.get(j - 1);
            if (dp[i][j] == dp[i - 1][j - 1] + (std.word().equals(usr.word())
                    ? (usr.confident() >= std.confident()
                    ? 100.0f
                    : (usr.confident() / std.confident()) * 100.0f)
                    : -5f)) {
                if (std.word().equals(usr.word())) { // 匹配成功
                    float score = usr.confident() >= std.confident() ? 100.0f : (usr.confident() / std.confident()) * 100.0f;
                    wordAlignmentList.add(new WordResult(usr.word(),std.word(),score, EnumWordCompare.MATCH));
                } else { // 匹配失败：错误发音
                    wordAlignmentList.add(new WordResult(usr.word(), std.word(),MIS_MATCHED_CONFIDENT, EnumWordCompare.MIS_MATCHED));
                }
                i--;
                j--;
            } else if (dp[i][j] == dp[i - 1][j] - 10) { // 漏读
                wordAlignmentList.add(new WordResult(null, std.word(),MISSED_CONFIDENT,EnumWordCompare.MISSED));
                i--;
            } else { // 多读
                wordAlignmentList.add(new WordResult(usr.word(), null,EXTRA_CONFIDENT, EnumWordCompare.EXTRA));
                j--;
            }
        }
        while (i > 0) {
            log.info("Missed:  {}",standardWords.get(--i).word());
        }
        while (j > 0) {
            log.info("Extra:  {}",userWords.get(--j).word());
        }

        // 归一化分数 四舍五入保留2位
        asrResult.setTotalScore(Math.max(0.0f, dp[m][n] / m));
        asrResult.setWordResultList(CollectionUtil.reverse(wordAlignmentList));
        asrResult.setStandardSentence(sentence);
        return asrResult;
    }

}
