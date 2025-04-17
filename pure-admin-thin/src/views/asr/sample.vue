<template>
  <div class="container">
    <h1 class="custom-title"><!--{{ data.title }}--></h1>
    <div class="content-wrapper">
      <!-- 左侧菜单 -->
      <div class="sidebar">
        <div class="sidebar-layout">
          <el-menu
            ref="menuRef"
            :default-active="String(currentIndex)"
            @select="handleSelect"
            class="custom-menu"
            active-text-color="#4299e1"
          >
            <el-menu-item
              v-for="(word, index) in data.words"
              :key="index"
              :index="String(index)"
            >
              <div class="menu-item">
                <span class="word">{{ word.word }}</span>
              </div>
            </el-menu-item>
          </el-menu>
        </div>
      </div>

      <!-- 右侧卡片 -->
      <div class="main-content">
        <el-card class="custom-card">
          <!-- 卡片头部 -->
          <template #header>
            <div class="centered-header">
              <div class="header-content">
                <h2 class="word-title">{{ currentWord.word }}</h2>
              </div>
            </div>
          </template>

          <!-- 卡片主体 -->
          <div class="card-body">
            <!-- 导航按钮 -->
            <div
              class="nav-button left"
              :class="{ disabled: currentIndex === 0 }"
              @click="prevCard"
            >
              <el-icon><ArrowLeft /></el-icon>
            </div>

            <div class="scrollable-content">
              <div class="centered-content">
              <!-- 修改后的翻译内容区域 -->
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
                      @click="playAudio(currentWord.video)"
                    >
                      <el-icon><Headset /></el-icon>
                    </el-button>
                    <el-button
                      type="danger"
                      circle
                      :loading="isRecording"
                      @click="toggleRecording"
                    >
                      <el-icon v-if="!isRecording"><Microphone /></el-icon>
                    </el-button>
                    <el-button
                      type="success"
                      circle
                      :disabled="!hasRecorded"
                      @click="playRecording"
                    >
                      <el-icon><VideoPlay /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- 例句折叠面板 -->
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
                          @click="playAudio(example.video)"
                        >
                          <el-icon><Headset /></el-icon>
                        </el-button>
                        <el-button
                          type="danger"
                          circle
                          :class="{ 'stop-button': isRecording }"
                          @click="toggleRecording"
                        >
                          <el-icon v-if="!isRecording"><Microphone /></el-icon>
                          <el-icon v-else><VideoPause /></el-icon> <!-- 添加暂停图标 -->
                        </el-button>
                        <el-button
                          type="success"
                          circle
                          :disabled="!hasRecorded"
                          @click="playRecording"
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
              :class="{ disabled: data.words && currentIndex === data.words.length - 1 }"
              @click="nextCard"
            >
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import JsAudioRecorder from 'js-audio-recorder'
import { VideoPlay, ArrowLeft, ArrowRight, Headset, Microphone, VideoPause  } from '@element-plus/icons-vue'

export default {
  name:"sample",
  components: {
    VideoPlay,
    ArrowLeft,
    ArrowRight,
    Headset,
    Microphone,
    VideoPause,
  },
  data() {
    return {
      treeProps: {
        children: 'children',
        label: 'label'
      },
      isRecording: false,
      hasRecorded: false,
      mediaRecorder: null,
      audioChunks: [],
      audioRecorder: null,
      data: {},
      currentIndex: 0
    }
  },
  computed: {
    currentWord() {
      // 添加多层安全保护
      if (
        // 确保数据已加载
        this.data?.words &&
        // 确认是有效数组
        Array.isArray(this.data.words) &&
        // 验证索引范围
        this.currentIndex >= 0 &&
        this.currentIndex < this.data.words.length
      ) {
        return this.data.words[this.currentIndex]
      }

      // 返回安全默认值
      return {
        word: '加载中...',
        pronounce: '',
        translate: '',
        video: '',
        example: []
      }
    }
  },
  async created() {
    await this.loadData()
  },
  methods: {
    // 新增树节点点击处理
    async handleTreeClick(data) {
      if (data.path) {
        await this.loadData(data.path)
        this.currentIndex = 0
        this.scrollMenuToTop()
      }
    },
    // 添加菜单滚动到顶部方法
    scrollMenuToTop() {
      this.$nextTick(() => {
        const menu = this.$refs.menuRef.$el
        if (menu) {
          menu.scrollTop = 0
        }
      })
    },
    async loadData() {
      try {
        const response = await fetch('unit1/unit1.json')
        if (!response.ok) {
          throw new Error('数据加载失败')
        }
        this.data = await response.json()
      } catch (error) {
        console.error('加载数据时出错:', error)
        this.$message.error('数据加载失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    handleSelect(index) {
      this.currentIndex = parseInt(index)
      this.scrollMenuToActiveItem()
    },
    prevCard() {
      if (this.currentIndex > 0) {
        this.currentIndex--;
        this.scrollMenuToActiveItem();
      }
    },
    nextCard() {
      if (this.currentIndex < this.data.words.length - 1) {
        this.currentIndex++;
        this.scrollMenuToActiveItem();
      }
    },
    scrollMenuToActiveItem() {
      this.$nextTick(() => {
        const menu = this.$refs.menuRef.$el
        const activeItem = menu.querySelector('.el-menu-item.is-active')

        if (activeItem) {
          // 使用现代滚动API
          activeItem.scrollIntoView({
            behavior: 'smooth',
            block: 'center',  // 垂直居中
            inline: 'nearest'
          })

          // 兼容性备用方案
          if (typeof activeItem.scrollIntoViewIfNeeded === 'function') {
            activeItem.scrollIntoViewIfNeeded(true)
          }
        }
      })
    },
    async playAudio(url) {
      try {
        // 1. 获取WAV文件
        const response = await fetch(url)
        const arrayBuffer = await response.arrayBuffer()

        // 2. 转换为Blob
        const blob = new Blob([arrayBuffer], { type: 'audio/wav' })

        // 3. 创建录音器实例
        const recorder = new JsAudioRecorder({
          sampleBits: 16,     // 必须与文件参数一致
          sampleRate: 16000,  // 必须与文件参数一致
          numChannels: 1
        })

        // 4. 播放音频
        await recorder.play(blob)
        console.log('play success')

      } catch (error) {
        console.error('播放失败:', error)
        this.$message.error('音频播放失败')
      }
    },
    async toggleRecording() {
      if (this.isRecording) {
        this.stopRecording()
      } else {
        await this.startRecording()
      }
    },
    async startRecording() {
      try {
        // 初始化录音器
        this.audioRecorder = new JsAudioRecorder({
          sampleBits: 16,     // 采样位数
          sampleRate: 16000,  // 采样率
          numChannels: 1      // 声道数
        })

        // 开始录音
        await this.audioRecorder.start()
        this.isRecording = true
        this.hasRecorded = false

        // 添加可视化支持（可选）
        this.initAudioVisualization()

      } catch (error) {
        console.error('录音启动失败:', error)
        this.$message.error('需要麦克风权限才能录音')
      }
    },

    stopRecording() {
      if (this.audioRecorder) {
        // 停止录音
        this.audioRecorder.stop()

        // 获取WAV格式音频
        this.audioRecorder.getWAVBlob(blob => {
          this.recordedAudio = URL.createObjectURL(blob)
          this.hasRecorded = true
        })

        // 销毁可视化（可选）
        this.destroyAudioVisualization()

        this.isRecording = false
      }
    },

    playRecording() {
      if (this.recordedAudio) {
        // 使用库自带的播放方法
        this.audioRecorder.play(this.recordedAudio)
      }
    },

    // 音频可视化（可选）
    initAudioVisualization() {
      const canvas = this.$refs.audioCanvas
      if (!canvas) return

      this.audioRecorder.createAnalyser()
      const ctx = canvas.getContext('2d')

      const draw = () => {
        if (!this.isRecording) return

        const array = new Uint8Array(this.audioRecorder.analyser.frequencyBinCount)
        this.audioRecorder.analyser.getByteFrequencyData(array)

        // 绘制波形
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        // ...添加绘制逻辑

        requestAnimationFrame(draw)
      }
      draw()
    },
    destroyAudioVisualization() {
      const canvas = this.$refs.audioCanvas
      if (canvas) {
        canvas.getContext('2d').clearRect(0, 0, canvas.width, canvas.height)
      }
    }
  }
}
</script>

<style scoped>
/* ========== 基础布局 ========== */
.container {
  min-height: 80vh;
  /*padding: 2rem;*/
  display: flex;
  flex-direction: column;
}
.custom-title {
  text-align: center;
  font-size: 24px;
  color: #333;
  font-weight: bold;
  margin-bottom: 20px;
}
.content-wrapper {
  flex: 1;
  display: flex;
  gap: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* ========== 左侧菜单样式 ========== */
.sidebar {
  width: 380px;
  flex-shrink: 0;
  height: calc(100vh - 180px); /* 根据容器高度计算 */
  position: relative;
  overflow: hidden; /* 隐藏原生滚动条 */
  /*padding: 8px 0; !* 增加上下间距 *!*/
}
.sidebar-layout {
  display: flex;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* ========== 课程树样式 ========== */
.course-tree {
  flex: 0 0 160px;
  border-right: 1px solid #ebeef5;
  padding: 8px 0;
  background: #f8fafc;

  :deep(.el-tree-node) {
    padding: 4px 8px;
  }

  :deep(.el-tree-node__content) {
    height: 36px;
    border-radius: 4px;
    margin: 0 8px;
    transition: all 0.2s;

    &:hover {
      background: rgba(66, 153, 225, 0.08);
    }
  }

  :deep(.el-tree-node.is-current) {
    > .el-tree-node__content {
      background: #ebf8ff;
      position: relative;

      &::after {
        content: "";
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        height: 60%;
        width: 3px;
        background-color: #4299e1;
        border-radius: 2px;
      }
    }
  }
}

.tree-node {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #606266;

  .el-icon {
    margin-right: 6px;
    font-size: 14px;
    color: #909399;
  }
}

.count-badge {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
  background: rgba(144, 147, 153, 0.08);
  border-radius: 10px;
  padding: 2px 8px;
}
/* 自定义滚动条样式 */
.custom-menu {
  flex: 1;
  scroll-behavior: smooth;
  height: 100%;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  background: white;
  overflow-y: auto; /* 启用垂直滚动 */
  scrollbar-width: thin; /* Firefox */
  scrollbar-color: #cbd5e0 transparent; /* Firefox */

  /* 隐藏原生滚动条轨道 */
  &::-webkit-scrollbar {
    width: 6px;
    background-color: transparent;
  }

  /* 滚动条滑块 */
  &::-webkit-scrollbar-thumb {
    background-color: #cbd5e0;
    border-radius: 3px;
    transition: background-color 0.3s;
  }

  /* 悬停时滑块颜色 */
  &:hover::-webkit-scrollbar-thumb {
    background-color: #a0aec0;
  }

  /* 滚动条轨道 */
  &::-webkit-scrollbar-track {
    background: transparent;
    margin: 8px 0;
  }
}

.menu-item {
  padding: 0.5rem 0;

  .word {
    font-weight: 600;
    color: #2d3748;
  }
}

/* 保持激活项可见的样式 */
.el-menu-item.is-active {
  scroll-margin: 8px; /* 防止激活项被裁剪 */
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


/* 激活状态指示条 */
:deep(.el-menu-item.is-active) {
  position: relative;
  overflow: hidden;

  &::after {
    content: "";
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    height: 60%;
    width: 3px;
    background-color: #4299e1;
    border-radius: 2px;
    transition: all 0.3s ease;
  }
}

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
  color: #2d3748;
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

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;

    .sidebar {
      height: 300px; /* 移动端固定高度 */
      width: 100%;

      .custom-menu {
        scrollbar-width: none; /* Firefox */
        &::-webkit-scrollbar {
          display: none; /* 移动端隐藏滚动条 */
        }
      }
    }
  }

  .card-body {
    padding: 0 20px;
    min-height: 300px;
  }

  .nav-button {
    width: 40px;
    height: 40px;

    &.left { left: 1%; }
    &.right { right: 1%; }

    .el-icon {
      font-size: 18px;
    }
  }

  .translate-box {
    padding: 1rem;

    .pronunciation {
      font-size: 1rem;
    }

    .translation {
      font-size: 0.95rem;
    }
  }

  .audio-control-group {
    flex-direction: row;
    justify-content: space-around;
  }
}

/* ========== 暗黑模式适配 ========== */
@media (prefers-color-scheme: dark) {
  :deep(.custom-menu) {
    --el-menu-active-color: #63b3ed;
    --el-menu-active-bg-color: rgba(66, 153, 225, 0.15);

    .el-menu-item.is-active::after {
      background-color: #63b3ed;
    }
  }

  .custom-card {
    background: rgba(0, 0, 0, 0.1);

    :deep(.el-card__header) {
      background: rgba(0, 0, 0, 0.2);
    }
  }
}

/* 添加录音状态样式 */
.stop-button {
  background-color: #ff4d4d !important;
  border-color: #ff4d4d !important;
  animation: pulse 1.5s infinite;
}

</style>
