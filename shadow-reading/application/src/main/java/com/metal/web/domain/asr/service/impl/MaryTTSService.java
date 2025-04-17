package com.metal.web.domain.asr.service.impl;

import com.metal.web.domain.asr.entity.WordOfIPAEntity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import marytts.LocalMaryInterface;
import marytts.datatypes.MaryDataType;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.*;

/**
 * @program: shadow-reading
 * @description: mary TTS init and operation
 * @author: metal
 * @create: 2025-01-15 09:52
 **/
@Service
@Slf4j
public class MaryTTSService {

    private static final String MARYTTS_VOICE = "cmu-slt-hsmm";

    /**
     * init maryTTS
     */
    private LocalMaryInterface maryTTS;

    /**
     * samapa phonetic symbol conversion ipa
     */
    private final Map<String, String> samapaToIpaMap = new HashMap<>();

    /**
     * phonetic symbol is long vowel
     */
    private final Map<String, Boolean> longVowels = new HashMap<>();

    /**
     * setting maryTTS and samapaToIpaMap
     */
    @PostConstruct
    public void init() throws MaryConfigurationException {

        //init maryTTS
        maryTTS = new LocalMaryInterface();
        maryTTS.setVoice(MARYTTS_VOICE); // 选择一个支持音标的语音

        // setting samapa2Ipa
        // 初始化映射表，这里只列出了一部分常见的SAMPA和对应的IPA
        // 同时记录哪些是长元音
        samapaToIpaMap.put("A", "\u0251");  // ɑ
        longVowels.put("A", true);
        samapaToIpaMap.put("O", "\u0254");  // ɔ
        longVowels.put("O", true);
        samapaToIpaMap.put("u", "\u0075");  // u
        longVowels.put("u", true);
        samapaToIpaMap.put("i", "\u0069");  // i
        longVowels.put("i", true);

        samapaToIpaMap.put("{", "\u0259");  // ə
        samapaToIpaMap.put("V", "\u028C");  // ʌ
        samapaToIpaMap.put("E", "\u025B");  // ɛ
        samapaToIpaMap.put("I", "\u026A");  // ɪ
        samapaToIpaMap.put("U", "\u028A");  // ʊ

        samapaToIpaMap.put("@", "\u0259");  // ə
        samapaToIpaMap.put("r=", "\u0279"); // ɹ (近似)

        // 双元音
        samapaToIpaMap.put("aU", "\u0061\u028A"); // aʊ
        samapaToIpaMap.put("OI", "\u0254\u026A"); // ɔɪ
        samapaToIpaMap.put("@U", "\u0259\u028A"); // əʊ
        samapaToIpaMap.put("EI", "\u025B\u0069"); // eɪ
        samapaToIpaMap.put("AI", "\u0061\u0069"); // aɪ

        // 辅音
        samapaToIpaMap.put("p", "\u0070");
        samapaToIpaMap.put("t", "\u0074");
        samapaToIpaMap.put("k", "\u006B");
        samapaToIpaMap.put("b", "\u0062");
        samapaToIpaMap.put("d", "\u0064");
        samapaToIpaMap.put("g", "\u0067");

        samapaToIpaMap.put("tS", "\u0074\u0283"); // tʃ
        samapaToIpaMap.put("dZ", "\u0064\u0292"); // dʒ

        samapaToIpaMap.put("f", "\u0066");
        samapaToIpaMap.put("v", "\u0076");
        samapaToIpaMap.put("T", "\u00FE"); // θ
        samapaToIpaMap.put("D", "\u00F0"); // ð
        samapaToIpaMap.put("s", "\u0073");
        samapaToIpaMap.put("z", "\u007A");
        samapaToIpaMap.put("S", "\u01A9"); // ʃ
        samapaToIpaMap.put("Z", "\u01B6"); // ʒ
        samapaToIpaMap.put("h", "\u0068");

        samapaToIpaMap.put("l", "\u006C");
        samapaToIpaMap.put("m", "\u006D");
        samapaToIpaMap.put("n", "\u006E");
        samapaToIpaMap.put("N", "\u014B"); // ŋ
        samapaToIpaMap.put("r", "\u0279"); // ɹ (近似)
        samapaToIpaMap.put("w", "\u0077");
        samapaToIpaMap.put("j", "\u006A");
    }

    /**
     * analyse sentence get every word and phoneme
     * @param sentence
     * @return
     */
    public List<WordOfIPAEntity> analyseSentence(String sentence) throws Exception {
        maryTTS.setInputType(MaryDataType.TEXT.name());
        maryTTS.setOutputType(MaryDataType.PHONEMES.name());
        // 生成 XML 格式的音素数据
        Document xmlOutput = maryTTS.generateXML(sentence);
        List<WordOfIPAEntity> wordOfIPAEntityList =extractPhonemesFromXMLDocument(xmlOutput);
        return wordOfIPAEntityList;
    }

    /**
     * get maryTTS xml result
     * @param document
     * @return
     * @throws Exception
     */
    public List<WordOfIPAEntity> extractPhonemesFromXMLDocument(Document document) throws Exception {
        List<WordOfIPAEntity> wordOfIPAEntityList = new ArrayList<>();
        NodeList tElements = document.getElementsByTagNameNS("http://mary.dfki.de/2002/MaryXML", "t");
        // 遍历所有的<t>元素
        for (int i = 0; i < tElements.getLength(); i++) {
            Element tElement = (Element) tElements.item(i);
            // 获取ph属性值
            String phValue = tElement.getAttribute("ph");
            // 获取<t>元素的文本内容
            String word = tElement.getTextContent();
            wordOfIPAEntityList.add(new WordOfIPAEntity(word, this.convertSamPaToIpa(phValue)));
        }
        return wordOfIPAEntityList;
    }

    /**
     * convert maryTTS sampa to ipa
     * @param samPa
     * @return
     */
    public String convertSamPaToIpa(String samPa) {
        StringBuilder ipaBuilder = new StringBuilder();
        int length = samPa.length();
        for (int i = 0; i < length; ) {
            boolean found = false;
            // 尝试匹配两个字符的组合
            if (i + 1 < length) {
                String twoChars = samPa.substring(i, i + 2);
                if (samapaToIpaMap.containsKey(twoChars)) {
                    String ipaSymbol = samapaToIpaMap.get(twoChars);
                    // 检查是否是长元音
                    if (longVowels.containsKey(twoChars)) {
                        ipaSymbol += "\u02D0"; // 长音符号
                    }
                    ipaBuilder.append(ipaSymbol);
                    i += 2;
                    found = true;
                }
            }

            // 如果没有找到两个字符的组合，则尝试匹配单个字符
            if (!found) {
                String oneChar = samPa.substring(i, i + 1);
                if (samapaToIpaMap.containsKey(oneChar)) {
                    String ipaSymbol = samapaToIpaMap.get(oneChar);
                    // 检查是否是长元音
                    if (longVowels.containsKey(oneChar)) {
                        ipaSymbol += "\u02D0"; // 长音符号
                    }
                    ipaBuilder.append(ipaSymbol);
                } else {
                    // 如果找不到匹配项，保留原字符
                    if(StringUtils.isNotBlank(oneChar) && !oneChar.equals("-")) { //ignore bank and - char
                        ipaBuilder.append(oneChar);
                    }
                }
                i++;
            }
        }
        return ipaBuilder.toString();
    }
}
