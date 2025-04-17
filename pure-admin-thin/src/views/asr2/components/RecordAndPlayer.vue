<template>
  <div class="audio-recorder-player">
    <canvas
      v-show="isRecording"
      ref="canvas"
      class="waveform-canvas"
      @click="toggleRecording"
    ></canvas>

    <div class="button-group" v-show="!isRecording">
      <el-button
        circle
        :icon="isWordAudioPlaying ? VideoPause : Headset"
        type="warning"
        @click="toggleWordAudio"
        :disabled="!wordAudioUrl"
      />
      <el-button circle :icon="Microphone" type="primary" @click="toggleRecording" />
      <el-button circle :icon="isPlaying ? VideoPause : VideoPlay" type="success" @click="togglePlay" :disabled="!audioBlob" />
    </div>
  </div>
</template>

<script setup>
  import { ref, onMounted, onUnmounted,watch  } from 'vue'
  import { ElButton } from 'element-plus'
  import { Microphone, VideoPause, VideoPlay, Headset } from '@element-plus/icons-vue'
  import Recorder from 'js-audio-recorder'

  const props = defineProps({
    index: String,
    uploadUrl: String,
    onUploadSuccess: Function,
    wordAudioUrl: String,
    sentence: String
  })

  let recorder = null
  const isRecording = ref(false)
  const isPlaying = ref(false)
  const isWordAudioPlaying = ref(false)
  const audioBlob = ref(null)
  let audioUrl = ''
  let audio = null
  let wordAudio = null
  let canvasCtx = null
  const canvas = ref(null)
  let animationId = null
  let audioCurrentTime = 0
  let wordAudioCurrentTime = 0

  watch(
    () => props.sentence,
    (newIndex, oldIndex) => {
      if (newIndex !== oldIndex) {
        wordAudio = null
        audio = null
      }
    }
  )

  const toggleRecording = async () => {
    if (isRecording.value) {
      await stopRecording()
    } else {
      await startRecording()
    }
  }

  const startRecording = async () => {
    recorder = new Recorder({ sampleBits: 16, sampleRate: 16000, numChannels: 1 })
    await recorder.start()
    isRecording.value = true
    drawWaveform()
  }

  const stopRecording = async () => {
    if (!recorder) return
    await recorder.stop()
    isRecording.value = false
    audioBlob.value = recorder.getWAVBlob()
    audioUrl = URL.createObjectURL(audioBlob.value)
    uploadAudio()
    cancelAnimationFrame(animationId)
    recorder.destroy()
    recorder = null
  }

  const togglePlay = () => {
    if (!audioUrl) return

    if (!audio) {
      audio = new Audio(audioUrl)
    }

    if (isPlaying.value) {
      audioCurrentTime = audio.currentTime
      audio.pause()
      isPlaying.value = false
    } else {
      audio.currentTime = audioCurrentTime
      audio.play()
      isPlaying.value = true
      audio.onended = () => {
        isPlaying.value = false
        audioCurrentTime = 0
      }
    }
  }

  const toggleWordAudio = () => {
    if (!props.wordAudioUrl) return

    if(!wordAudio) {
      wordAudio = new Audio(props.wordAudioUrl)
    }

    if (isWordAudioPlaying.value) {
      wordAudioCurrentTime = wordAudio.currentTime
      wordAudio.pause()
      isWordAudioPlaying.value = false
    } else {
      wordAudio.currentTime = wordAudioCurrentTime
      wordAudio.play()
      isWordAudioPlaying.value = true
      wordAudio.onended = () => {
        isWordAudioPlaying.value = false
        wordAudioCurrentTime = 0
      }
    }
  }

  const drawWaveform = () => {
    const draw = () => {
      if (!recorder) return
      const dataArray = recorder.getRecordAnalyseData()
      if (!canvasCtx || !dataArray.length) return

      canvasCtx.clearRect(0, 0, canvas.value.width, canvas.value.height)
      canvasCtx.beginPath()
      const sliceWidth = canvas.value.width / dataArray.length
      let x = 0
      dataArray.forEach((v) => {
        const y = (v / 256.0) * canvas.value.height
        canvasCtx.lineTo(x, y)
        x += sliceWidth
      })
      canvasCtx.stroke()
      animationId = requestAnimationFrame(draw)
    }
    draw()
  }

  const uploadAudio = () => {
    const formData = new FormData()
    formData.append('file', audioBlob.value, `recording-${props.index}.wav`)
    formData.append('sentence', props.sentence)
    fetch(props.uploadUrl, {
    // fetch('/api/test', {
      method: 'POST',
      body: formData
    })
      .then(response => response.json())
      .then(data => {
        if (props.onUploadSuccess) {
          props.onUploadSuccess(data, props.index)
        }
      })
      .catch(err => console.error('Upload error:', err))
  }

  onMounted(() => {
    const canvasEl = canvas.value
    canvasEl.width = 300
    canvasEl.height = 40
    canvasCtx = canvasEl.getContext('2d')
    canvasCtx.strokeStyle = '#409EFF'
    canvasCtx.lineWidth = 1.5
  })

  onUnmounted(() => {
    cancelAnimationFrame(animationId)
    if (recorder && isRecording.value) {
      recorder.stop()
      recorder.destroy()
      recorder = null
    }
    if (audio) {
      audio.pause()
      audio = null
    }
    if (wordAudio) {
      wordAudio.pause()
      wordAudio = null
    }
  })
</script>

<style scoped>
  .audio-recorder-player {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: fit-content;
    padding: 0;
    background-color: transparent;
    border: none;
    margin: 0;
  }
  .waveform-canvas {
    width: 300px;
    height: 40px;
    background-color: transparent;
    border-radius: 4px;
    margin-bottom: 12px;
    cursor: pointer;
  }
  .button-group {
    display: flex;
    gap: 12px;
  }
  .button-group .el-button {
    width: 40px;
    height: 40px;
    padding: 0;
    font-size: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  }
</style>
