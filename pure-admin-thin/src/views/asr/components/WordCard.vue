<template>
  <div class="main-content">
    <el-card class="custom-card">
      <template #header>
        <div class="centered-header">
          <div class="header-content">
            <h2 class="word-title">{{ currentWord.word }}</h2>
          </div>
        </div>
      </template>

      <div class="card-body">
        <div
          class="nav-button left"
          :class="{ disabled: currentIndex === 0 }"
          @click="$emit('prev-card')"
        >
          <el-icon><ArrowLeft /></el-icon>
        </div>

        <div class="scrollable-content">
          <div class="centered-content">
            <div class="translate-box">
              <div class="translation-content">
                <p class="pronunciation">{{ currentWord.pronounce }}</p>
                <p class="translation">{{ currentWord.translate }}</p>
              </div>
              <div class="audio-control-row">
                <div class="audio-control-group">
                  <el-button
                    type="primary"
                    circle
                    @click="$emit('play-audio', currentWord.video)"
                  >
                    <el-icon><Headset /></el-icon>
                  </el-button>
                  <el-button
                    type="danger"
                    circle
                    :loading="isRecording"
                    @click="$emit('toggle-recording')"
                  >
                    <el-icon v-if="!isRecording"><Microphone /></el-icon>
                  </el-button>
                  <el-button
                    type="success"
                    circle
                    :disabled="!hasRecorded"
                    @click="$emit('play-recording')"
                  >
                    <el-icon><VideoPlay /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>

            <el-collapse class="example-collapse">
              <el-collapse-item
                v-for="(example, exIndex) in currentWord.example"
                :key="exIndex"
                :title="`例句 ${exIndex + 1}`"
              >
                <div class="example-content">
                  <p>{{ example.sentence }}</p>
                  <p>{{ example.translate }}</p>
                  <div class="audio-control-row">
                    <div class="audio-control-group">
                      <el-button
                        type="primary"
                        circle
                        @click="$emit('play-audio', example.video)"
                      >
                        <el-icon><Headset /></el-icon>
                      </el-button>
                      <el-button
                        type="danger"
                        circle
                        :class="{ 'stop-button': isRecording }"
                        @click="$emit('toggle-recording')"
                      >
                        <el-icon v-if="!isRecording"><Microphone /></el-icon>
                        <el-icon v-else><VideoPause /></el-icon> </el-button>
                      <el-button
                        type="success"
                        circle
                        :disabled="!hasRecorded"
                        @click="$emit('play-recording')"
                      >
                        <el-icon><VideoPlay /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>

        <div
          class="nav-button right"
          :class="{ disabled: words && currentIndex === words.length - 1 }"
          @click="$emit('next-card')"
        >
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
  import { VideoPlay, ArrowLeft, ArrowRight, Headset, Microphone, VideoPause } from '@element-plus/icons-vue';

  export default {
    name: 'WordCard',
    components: {
      VideoPlay,
      ArrowLeft,
      ArrowRight,
      Headset,
      Microphone,
      VideoPause,
    },
    props: {
      currentWord: {
        type: Object,
        required: true
      },
      currentIndex: {
        type: Number,
        default: 0
      },
      words: {
        type: Array,
        default: () => []
      },
      isRecording: {
        type: Boolean,
        default: false
      },
      hasRecorded: {
        type: Boolean,
        default: false
      }
    },
    emits: ['prev-card', 'next-card', 'play-audio', 'toggle-recording', 'play-recording']
  };
</script>

<style scoped>
  /* ========== 右侧卡片样式 ========== */
  /* 新增滚动区域样式 */
  .scrollable-content {
    flex: 1;
    overflow-y: auto;
    padding: 0 20px;
    height: calc(100vh - 306px); /* 与左侧高度一致 */

    /* 滚动条样式与左侧统一 */
    scrollbar-width: thin;
    scrollbar-color: #cbd5e0 transparent;

  &::-webkit-scrollbar {
     width: 6px;
   }

  &::-webkit-scrollbar-thumb {
     background-color: #cbd5e0;
     border-radius: 3px;
   }

  &:hover::-webkit-scrollbar-thumb {
     background-color: #a0aec0;
   }
  }

  .main-content {
    flex: 1;
    min-width: 0;
  }

  .custom-card {
    height: 100%;
    border-radius: 0.75rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

  /* 卡片头部 */
  :deep(.el-card__header) {
    padding: 24px 20px !important;
    background: #f8fafc;
  }
  }

  .word-title {
    margin: 0;
    font-size: 1.8rem;
    /*color: #2d3748;*/
    line-height: 1.3;
    text-align: center;
  }

  /* ========== 内容区域样式 ========== */
  .card-body {
    position: relative;
    /*min-height: 400px;*/
    padding: 0 80px;
  }

  .translate-box {
    text-align: center;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    padding: 1.5rem;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);

  .pronunciation {
    font-size: 1.2rem;
    color: #4a5568;
    line-height: 1.5;
    word-break: break-word;
  }

  .translation {
    font-size: 1.1rem;
    color: #2d3748;
    line-height: 1.6;
    white-space: pre-line;
  }
  }

  /* ========== 音频控制组件 ========== */
  .audio-control-group {
    display: flex;
    gap: 1rem;
    justify-content: center;
    flex-wrap: wrap;

  /* 统一按钮尺寸 */
  .el-button {
    width: 40px;
    height: 40px;

  .el-icon {
    font-size: 1.2rem;
  }
  }
  }

  /* ========== 导航按钮组件 ========== */
  .nav-button {
    position: absolute;
    top: 50%;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: white;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(0, 0, 0, 0.05);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transform: translateY(-50%);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 20;
    animation: button-float 3s ease-in-out infinite;

  /* 方向定位 */
  &.left { left: 2%; transform-origin: left center; }
  &.right { right: 2%; transform-origin: right center; }

  /* 交互状态 */
  &:active:not(.disabled) {
     transform: translateY(-50%) scale(0.95);
     box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
   }

  .el-icon {
    color: #4299e1;
    transition: color 0.3s ease;
  }

  &:hover:not(.disabled) .el-icon {
     color: #3182ce;
   }

  /* 禁用状态 */
  &.disabled {
     opacity: 0.4;
     cursor: not-allowed;
     animation: none;
     background: #f8fafc;

  .el-icon {
    color: #cbd5e0 !important;
  }
  }
  }

  @keyframes button-float {
    0%, 100% { transform: translateY(-50%) scale(1); }
    50% { transform: translateY(-50%) scale(1.05); }
  }

  .example-content {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    font-size: 16px;
  }

  :deep(.example-collapse .el-collapse-item__header) {
    font-size: 16px !important;
    font-weight: 500;
    color: #2d3748;
    padding: 12px 0;
    text-align: center;
  }

  /* 添加录音状态样式 */
  .stop-button {
    background-color: #ff4d4d !important;
    border-color: #ff4d4d !important;
    animation: pulse 1.5s infinite;
  }
</style>
