package com.metal.web.utils.asr;

import com.metal.web.domain.asr.entity.WordResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * handle whisper get sentence asr to words, but words could be split the char
 */
public class WhisperScoreCalUtils {

    public static List<WordResult> calculateWordScoresOptimized(String sentence, List<Map<String, Float>> tokens) {
        // 保留字母、空格、单引号，并转化为小写
        sentence = sentence.replaceAll("[^a-zA-Z0-9\\s']", "").toLowerCase();
        String[] words = sentence.split(" ");
        int currentTokenIndex = 1; // 跳过_BEG_（索引0）
        List<WordResult> wordResultList = new ArrayList<>(words.length);
        // 预处理过滤无效单词
        List<String> validWords = Arrays.stream(words)
                .filter(word -> word.indexOf('_') == -1)
                .collect(Collectors.toList());

        for (String word : validWords) {
            int wordLength = word.length();
            boolean found = false;

            // 从currentTokenIndex开始寻找匹配
            for (int start = currentTokenIndex; start < tokens.size(); start++) {
                StringBuilder sb = new StringBuilder();
                float sum = 0;
                int count = 0;

                for (int end = start; end < tokens.size(); end++) {
                    String token = tokens.get(end).keySet().iterator().next().replaceFirst(" ", "");
                    if (token.indexOf('_') != -1) break; // 遇到无效token终止

                    sb.append(token);
                    sum += tokens.get(end).values().iterator().next();
                    count++;

                    // 提前终止条件：超过单词长度或已完全匹配
                    if (sb.length() > wordLength) break;
                    if (sb.toString().toLowerCase().equals(word)) {
                        WordResult wordResult = new WordResult(
                                word,
                                word,
                                sum / count,
                                null);
                        wordResultList.add(wordResult);
                        currentTokenIndex = end + 1; // 移动到下一个token
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }

            if (!found) {
//                throw new RuntimeException("未找到匹配的token序列，单词: " + word);
            }
        }
        return wordResultList;
    }

    public static void main(String[] args) {
        String sentence = "A short flight from Cusco,takes you from the Andes into the Amazon rainforest.";
        List<Map<String, Float>> tokens = new ArrayList<>();
        tokens.add(Collections.singletonMap("_BEG_", 0.957518f));
        tokens.add(Collections.singletonMap(" A", 0.97342396f));
        tokens.add(Collections.singletonMap("short", 0.99600255f));
        tokens.add(Collections.singletonMap("flight", 0.9951841f));
        tokens.add(Collections.singletonMap("from", 0.9963256f));
        tokens.add(Collections.singletonMap("C", 0.53120184f));
        tokens.add(Collections.singletonMap("us", 0.6215427f));
        tokens.add(Collections.singletonMap("co", 0.9916002f));
        tokens.add(Collections.singletonMap("takes", 0.944948f));
        tokens.add(Collections.singletonMap("you", 0.9986467f));
        tokens.add(Collections.singletonMap("from", 0.9955847f));
        tokens.add(Collections.singletonMap("the", 0.989655f));
        tokens.add(Collections.singletonMap("And", 0.8728354f));
        tokens.add(Collections.singletonMap("es", 0.95619255f));
        tokens.add(Collections.singletonMap("into", 0.94687486f));
        tokens.add(Collections.singletonMap("the", 0.987654f));
        tokens.add(Collections.singletonMap("Amazon", 0.978923f));
        tokens.add(Collections.singletonMap("rain", 0.944567f));
        tokens.add(Collections.singletonMap("forest", 0.934567f));

        List<WordResult> scores = calculateWordScoresOptimized(sentence, tokens);
        System.out.println("优化后分数: " + scores);
    }
}