<script setup>
  import { ref, computed } from 'vue'
  import 'element-plus/es/components/tag/style/css'
  import 'element-plus/es/components/button/style/css'

  const sentenceList = ref([
    {
      id: 1,
      text: 'She is a typical teenager who loves pop music and social media.',
      result: null
    },
    {
      id: 2,
      text: 'Many teenage students enjoy playing basketball after school.',
      result: null
    },
    {
      id: 3,
      text: 'The sun rises in the east and sets in the west.',
      result: null
    }
  ])

  function mockFetchResult(id) {
    const mockMap = {
      1: {
        standardSentence: 'She is a typical teenager who loves pop music and social media.',
        asrResultSentence: 'She is a typical teenager who likes pop songs and social apps.',
        totalScore: 83.42,
        wordResultList: [
          { word: 'she', standardWord: 'she', compare: 'MATCH' },
          { word: 'is', standardWord: 'is', compare: 'MATCH' },
          { word: 'a', standardWord: 'a', compare: 'MATCH' },
          { word: 'typical', standardWord: 'typical', compare: 'MATCH' },
          { word: 'teenager', standardWord: 'teenager', compare: 'MATCH' },
          { word: 'who', standardWord: 'who', compare: 'MATCH' },
          { word: 'likes', standardWord: 'loves', compare: 'PARTIAL_MATCH' },
          { word: 'pop', standardWord: 'pop', compare: 'MATCH' },
          { word: 'songs', standardWord: 'music', compare: 'MIS_MATCHED' },
          { word: 'and', standardWord: 'and', compare: 'MATCH' },
          { word: 'social', standardWord: 'social', compare: 'MATCH' },
          { word: 'apps', standardWord: 'media', compare: 'MIS_MATCHED' }
        ]
      },
      2: {
        standardSentence: 'Many teenage students enjoy playing basketball after school.',
        asrResultSentence: 'Many teenage students enjoy playing basketball after school.',
        totalScore: 96.58,
        wordResultList: [
          { word: 'many', standardWord: 'many', compare: 'MATCH' },
          { word: 'teenage', standardWord: 'teenage', compare: 'MATCH' },
          { word: 'students', standardWord: 'students', compare: 'MATCH' },
          { word: 'enjoy', standardWord: 'enjoy', compare: 'MATCH' },
          { word: 'playing', standardWord: 'playing', compare: 'MATCH' },
          { word: 'basketball', standardWord: 'basketball', compare: 'MATCH' },
          { word: 'after', standardWord: 'after', compare: 'MATCH' },
          { word: 'school', standardWord: 'school', compare: 'MATCH' }
        ]
      }
    }

    const match = sentenceList.value.find(s => s.id === id)
    if (match) match.result = mockMap[id] || null
  }

  function getStyledClass(compare) {
    switch (compare) {
      case 'MATCH': return 'text-green-700'
      case 'PARTIAL_MATCH': return 'text-yellow-700'
      case 'MIS_MATCHED':
      case 'MISSED': return 'text-red-700'
      default: return 'text-gray-600'
    }
  }

  function getScoreTag(score) {
    if (score >= 85) return { label: '很棒', type: 'success', icon: '🌟' }
    else if (score >= 60) return { label: '一般', type: 'warning', icon: '⚠️' }
    else return { label: '糟糕', type: 'danger', icon: '❌' }
  }

  function renderStandardWords(sentence) {
    if (!sentence.result || !sentence.result.wordResultList) {
      return sentence.text.split(' ').map(word => ({ text: word, compare: 'UNKNOWN' }))
    }
    const wordMap = new Map()
    sentence.result.wordResultList.forEach(w => wordMap.set(w.standardWord.toLowerCase(), w.compare))
    return sentence.text.split(' ').map(word => ({
      text: word,
      compare: wordMap.get(word.toLowerCase()) || 'UNKNOWN'
    }))
  }
</script>

<template>
  <div class="p-6 bg-gray-50 min-h-screen font-serif space-y-8">
    <div v-for="sentence in sentenceList" :key="sentence.id" class="bg-white shadow border rounded-lg p-4">
      <div class="flex justify-between items-center mb-2">
        <h2 class="text-lg font-semibold text-gray-800">📘 句子 {{ sentence.id }}</h2>
        <el-button size="small" type="primary" @click="mockFetchResult(sentence.id)">
          模拟识别
        </el-button>
      </div>
      <p class="text-base leading-relaxed">
        <template v-for="(word, index) in renderStandardWords(sentence)" :key="index">
          <span :class="['mx-1', getStyledClass(word.compare)]">{{ word.text }}</span>
        </template>
      </p>
      <div v-if="sentence.result" class="mt-3 flex items-center gap-3 text-gray-700">
        得分：
        <span class="text-blue-700 font-semibold">{{ sentence.result.totalScore.toFixed(2) }}</span>
        <el-tag :type="getScoreTag(sentence.result.totalScore).type" effect="light">
          {{ getScoreTag(sentence.result.totalScore).icon }} {{ getScoreTag(sentence.result.totalScore).label }}
        </el-tag>
      </div>
    </div>
  </div>
</template>
