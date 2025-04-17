<template>
  <div class="audio-player">
    <div
      class="progress-container"
      :style="{ width: progressSize + 'px', height: progressSize + 'px' }"
    >
      <el-progress
        class="progress-ring"
        type="circle"
        :percentage="progress"
        :stroke-width="progressWidth"
        :color="progressColor"
        :show-text="false"
        :width="progressSize"
      />
      <el-button
        class="play-btn"
        :style="{
          backgroundColor: progressColor,
          width: btnSize + 'px',
          height: btnSize + 'px'
        }"
        @click="togglePlayback"
      >
        <transition name="fade" mode="out-in">
          <!-- 暂停图标 -->
          <svg
            v-if="isPlaying"
            key="pause"
            class="audio-icon"
            :style="{ width: iconSize, height: iconSize }"
            viewBox="0 0 24 24"
          >
            <rect x="6" y="4" width="4" height="16" fill="currentColor"/>
            <rect x="14" y="4" width="4" height="16" fill="currentColor"/>
          </svg>

          <!-- 播放图标 -->
          <svg
            v-else
            key="play"
            class="audio-icon"
            :style="{ width: iconSize, height: iconSize }"
            viewBox="0 0 24 24"
          >
            <path d="M8 5v14l11-7z" fill="currentColor"/>
          </svg>
        </transition>
      </el-button>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue';
import { ElButton, ElProgress } from 'element-plus';

export default {
  name: "AudioPlayer",
  components: {
    ElButton,
    ElProgress
  },
  props: {
    audioUrl: {
      type: String,
      default: '',
      required: true
    },
    btnSize: {
      type: Number,
      default: 25
    },
    colors: {
      type: Object,
      default: () => ({
        play: '#FF8888',    // 柔和的珊瑚红
        pause: '#88C888'    // 低饱和度的叶绿色
      })
    },
    progressWidth: {
      type: Number,
      default: 6
    }
  },
  setup(props) {
    const audio = ref(null);
    const isPlaying = ref(false);
    const progress = ref(0);

    // 计算属性
    const progressSize = computed(() => props.btnSize * 1.8);
    const iconSize = computed(() => props.btnSize * 0.5 + 'px'); // 调整为按钮尺寸的50%
    const progressColor = computed(() =>
      isPlaying.value ? props.colors.play : props.colors.pause
    );

    // 初始化音频
    const initAudio = () => {
      if (audio.value) {
        audio.value.pause();
        audio.value.remove();
      }

      audio.value = new Audio();
      const source = document.createElement('source');
      source.src = props.audioUrl;
      audio.value.appendChild(source);

      // 事件监听
      audio.value.addEventListener('timeupdate', updateProgress);
      audio.value.addEventListener('ended', resetState);
    };

    // 更新进度
    const updateProgress = () => {
      if (audio.value.duration > 0) {
        progress.value = (audio.value.currentTime / audio.value.duration) * 100;
      }
    };

    // 重置状态
    const resetState = () => {
      isPlaying.value = false;
      progress.value = 0;
    };

    // 切换播放状态
    const togglePlayback = () => {
      if(!props.audioUrl) return;
      isPlaying.value ? audio.value.pause() : audio.value.play();
      isPlaying.value = !isPlaying.value;
    };

    // 监听音频URL变化
    watch(() => props.audioUrl, initAudio);

    onMounted(initAudio);

    return {
      isPlaying,
      progress,
      progressSize,
      progressColor,
      iconSize,
      togglePlayback
    };
  }
};
</script>

<style scoped>
.audio-player {
  display: inline-flex;
}

.progress-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  border: none;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);  /* 更柔和的投影 */
}

.play-btn:hover {
  transform: translate(-50%, -50%) scale(1.1);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.25);
}

.play-btn:active {
  transform: translate(-50%, -50%) scale(0.95);
}

/* 新增图标样式 */
.audio-icon {
  display: block;
  color: white; /* 控制图标颜色 */
  transition: transform 0.2s ease;
}

/* 优化过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease-in-out, transform 0.15s ease-in-out;
}

.fade-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.fade-leave-to {
  opacity: 0;
  transform: scale(1.2);
}

/* 按钮悬停效果优化 */
.play-btn:hover .audio-icon {
  transform: scale(1.1);
}

.play-btn:active .audio-icon {
  transform: scale(0.9);
}

/* 优化图标对齐 */
.play-btn svg {
  display: block;
  margin: auto;
}

/* 深度样式覆盖 */
:deep(.el-progress-circle__track) {
  stroke: rgba(255, 255, 255, 0.6);  /* 更柔和的轨道颜色 */
}

:deep(.el-progress-circle__path) {
  stroke-linecap: round;
}
</style>
