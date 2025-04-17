package com.metal.web.utils.asr;

import com.metal.web.domain.asr.entity.ASRResultEntity;
import com.metal.web.domain.asr.entity.WordResult;
import com.metal.web.enums.EnumWordCompare;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ASRUtils2 {

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

    public static ASRResultEntity evaluate(String sentence, List<WordResult> userWords) {
        ASRResultEntity asrResult = new ASRResultEntity();
        List<WordResult> wordAlignmentList = new ArrayList<>();
        List<WordResult> standardWords = sentenceToWordResultList(sentence);
        int m = standardWords.size();
        int n = userWords.size();

        // 预计算匹配得分
        float[][] matchScores = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                WordResult std = standardWords.get(i);
                WordResult usr = userWords.get(j);
                if (std.word().equals(usr.word())) {
                    matchScores[i][j] = usr.confident() >= std.confident() ?
                            100.0f : (usr.confident() / std.confident()) * 100.0f;
                } else {
                    // 不匹配时计算相似度
                    float similarity = calculateSimilarity(std.word(), usr.word());
                    matchScores[i][j] = similarity - 5.0f; // 减去一个惩罚值，或者直接使用 similarity
                }
            }
        }

        // 初始化 DP 表
        float[][] dp = new float[m + 1][n + 1];
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i * -10; // 漏读惩罚
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j * -5; // 多读惩罚
        }
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                dp[i][j] = Math.max(
                        dp[i - 1][j - 1] + matchScores[i - 1][j - 1], // 对齐当前单词
                        Math.max(dp[i - 1][j] - 10, dp[i][j - 1] - 5) // 漏词或多词
                );
            }
        }

        // 填充 DP 表
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                float match = dp[i - 1][j - 1] + matchScores[i - 1][j - 1]; // 匹配或错误
                float delete = dp[i - 1][j] - 10; // 漏读
                float insert = dp[i][j - 1] - 5; // 多读
                dp[i][j] = Math.max(match, Math.max(delete, insert));
            }
        }

        // 回溯路径
        int i = m, j = n;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + matchScores[i - 1][j - 1]) {
                WordResult std = standardWords.get(i - 1);
                WordResult usr = userWords.get(j - 1);
                EnumWordCompare compare;
                float score = matchScores[i - 1][j - 1];

                if (std.word().equals(usr.word())) {
                    compare = EnumWordCompare.MATCH; // 完全匹配
                } else if (score > 50.0f) { // 假设 50 是部分匹配的阈值
                    compare = EnumWordCompare.PARTIAL_MATCH; // 定义一个新枚举值表示部分匹配
                } else {
                    compare = EnumWordCompare.MIS_MATCHED; // 不匹配
                    score = -5.0f; // 不匹配的默认得分
                }
                wordAlignmentList.add(new WordResult(usr.word(), std.word(), score, compare));
                i--;
                j--;
            } else if (i > 0 && dp[i][j] == dp[i - 1][j] - 10) {
                WordResult std = standardWords.get(i - 1);
                wordAlignmentList.add(new WordResult(null, std.word(), MISSED_CONFIDENT, EnumWordCompare.MISSED));
                i--;
            } else if (j > 0 && dp[i][j] == dp[i][j - 1] - 5) {
                WordResult usr = userWords.get(j - 1);
                wordAlignmentList.add(new WordResult(usr.word(), null, EXTRA_CONFIDENT, EnumWordCompare.EXTRA));
                j--;
            }
        }

        // 反转对齐列表并设置结果
        Collections.reverse(wordAlignmentList);
        asrResult.setTotalScore(Math.max(0.0f, dp[m][n] / m));
        asrResult.setWordResultList(wordAlignmentList);
        asrResult.setStandardSentence(sentence);
        return asrResult;
    }

    /**
     * 返回一个 0 到 100 的得分，值越大表示单词越相似
     * @param word1
     * @param word2
     * @return
     */
    private static float calculateSimilarity(String word1, String word2) {
        int distance = levenshteinDistance(word1, word2); // 计算编辑距离
        int maxLen = Math.max(word1.length(), word2.length()); // 取两个单词长度的最大值
        float similarity = 1.0f - (float) distance / maxLen; // 转换为相似度（0到1之间）
        return similarity * 100.0f; // 转换为0到100的得分
    }

    // 计算 Levenshtein 距离
    private static int levenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        // 初始化第一行和第一列
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        // 填充 DP 表
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), // 插入或删除
                        dp[i - 1][j - 1] + cost // 替换
                );
            }
        }
        return dp[m][n];
    }
}
