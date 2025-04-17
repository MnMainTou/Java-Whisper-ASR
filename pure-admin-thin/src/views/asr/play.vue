<template>
  <AudioPlayer
    :audio-url="audioFile"
    :btn-size="25"
    :progress-width="6"
  />
</template>

<script>
import AudioPlayer from './components/AudioPlayer.vue';

export default {
  components: {
    AudioPlayer
  },
  data() {
    return {
      audioFile: 'unit1/teenage_1.wav'
    }
  }
}
</script>
