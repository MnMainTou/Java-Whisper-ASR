<template>
  <div class="recorder-container">
    <!-- 波形显示 -->
    <canvas
      ref="canvas"
      class="waveform"
      @click="stopRecording"
      :style="{ display: isRecording ? '' : 'none' }"
    ></canvas>
    <!-- 控制按钮 -->
    <div class="controls" :style="{ display: isRecording ? 'none' : 'flex' }">
      <el-button
        class="microphone-button"
        :icon="Microphone"
        circle
        @click="toggleRecording"
      />
      <!--<AudioPlayer
        :audio-url="audioUrl"
        :btn-size="25"
        :progress-width="6"
      />-->
      <div>{{audioUrl.value}}</div>
      <el-button
        type="success"
        :icon="VideoPlay"
        circle
        :disabled="!audioUrl"
        @click="playAudio"
        title="播放录音"
      />
    </div>
    <!-- 隐藏的音频播放器 -->
    <audio ref="audioPlayer" :src="audioUrl" controls class="mt-4" :style="{display: 'none'}"></audio>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElButton } from 'element-plus'
import { Microphone, VideoPause, VideoPlay } from '@element-plus/icons-vue';
import JsAudioRecorder from 'js-audio-recorder'
import AudioPlayer from './components/AudioPlayer.vue';


// 录音实例配置
const recorder = new JsAudioRecorder({
  sampleBits: 16,     // 采样位数
  sampleRate: 16000,  // 采样率
  numChannels: 1      // 单声道
})

// 响应式数据
const isRecording = ref(false)
const audioUrl = ref("")
const audioBlob = ref(null)
const canvas = ref(null)
const audioPlayer = ref(null)
let animationFrame = null

// 初始化Canvas
onMounted(() => {
  const ctx = canvas.value.getContext('2d')
  ctx.strokeStyle = '#409EFF'  // 改为使用线条颜色
  ctx.lineWidth = 2            // 设置线条粗细
})

// 录音控制
const toggleRecording = () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

// 开始录音
const startRecording = () => {
  recorder.start().then(() => {
    isRecording.value = true
    drawWaveform()
  }).catch(err => {
    console.error('录音启动失败:', err)
  })
}

// 停止录音
const stopRecording = async () => {
  recorder.stop()
  isRecording.value = false
  cancelAnimationFrame(animationFrame)

  // 获取录音数据
  audioBlob.value = recorder.getWAVBlob()
  audioUrl.value = URL.createObjectURL(audioBlob.value)

  // 上传服务器
  uploadRecording(audioBlob.value)
}

// 绘制波形
const drawWaveform = () => {
  if (!isRecording.value) return

  const ctx = canvas.value.getContext('2d')
  const dataArray = recorder.getAnalyseData()
  const width = canvas.value.width
  const height = canvas.value.height

  ctx.clearRect(0, 0, width, height)
  ctx.beginPath()

  // 创建平滑路径
  dataArray.forEach((value, i) => {
    // 将数据转换为Y坐标（上下对称）
    const y = (value / 255) * (height / 2) + height / 4

    // 计算X坐标（横向拉伸）
    const x = (i / dataArray.length) * width * 2

    // 使用二次贝塞尔曲线创建平滑效果
    if (i === 0) {
      ctx.moveTo(x, y)
    } else {
      const prevX = ( (i-1) / dataArray.length ) * width * 2
      const prevY = (dataArray[i-1] / 255) * (height / 2) + height / 4
      const cx = (prevX + x) / 2
      const cy = (prevY + y) / 2
      ctx.quadraticCurveTo(prevX, prevY, cx, cy)
    }
  })

  ctx.stroke()
  animationFrame = requestAnimationFrame(drawWaveform)
}

// 播放录音
const playAudio = () => {
  // audioPlayer.value.play()
  recorder.play();
}

// 下载录音
const downloadAudio = () => {
  const link = document.createElement('a')
  link.href = audioUrl.value
  link.download = `recording_${Date.now()}.wav`
  link.click()
}

// 上传服务器（需根据实际API实现）
const uploadRecording = async (blob) => {
  const formData = new FormData()
  formData.append('audio', blob, 'recording.wav')

  try {
    // 示例：使用axios上传
    // await axios.post('/api/upload', formData)
    console.log('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
  }
}
</script>

<style scoped>
.recorder-container {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
  text-align: center;
  position: relative;
}

.waveform {
  width: 100%;
  height: 120px;
  background: linear-gradient(to bottom, #f8f9fa, #e9ecef);/* 添加渐变背景增强视觉效果 */
  border-radius: 4px;
  margin-bottom: 20px;
  position: absolute;
  top: 0;
  left: 0;
}

.controls {
  display: flex;
  justify-content: center;
  gap: 15px;
  z-index: 10;
  position: relative;
}

.microphone-button {
  /*margin-top: 7px;*/
}

audio {
  width: 100%;
  margin-top: 20px;
}
</style>
