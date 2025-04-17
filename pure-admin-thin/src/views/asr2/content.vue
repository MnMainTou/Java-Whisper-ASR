<template>
  <div class="container">
    <h1 class="custom-title"></h1>
    <div class="content-wrapper">
      <WordMenu
        ref="WordMenu"
        :words="data"
        :currentIndex="currentIndex"
        @word-selected="handleWordSelected"
      />

      <WordCard
        :currentWord="currentWord"
        :currentIndex="currentIndex"
        :words="data"
        :isRecording="isRecording"
        :hasRecorded="hasRecorded"
        :audioPathPre="audioPathPre"
        @prev-card="prevCard"
        @next-card="nextCard"
        @toggle-recording="toggleRecording"
        @play-recording="playRecording"
      />
    </div>
  </div>
</template>

<script>
  import JsAudioRecorder from 'js-audio-recorder';
  import { VideoPlay, ArrowLeft, ArrowRight, Headset, Microphone, VideoPause } from '@element-plus/icons-vue';
  import WordMenu from './components/WordMenu.vue';
  import WordCard from './components/WordCard.vue';


  export default {
    name: "content",
    components: {
      VideoPlay,
      ArrowLeft,
      ArrowRight,
      Headset,
      Microphone,
      VideoPause,
      WordMenu,
      WordCard,
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
        audioRecorder: null,
        data: [],
        currentIndex: 0,
        audioPathPre: '',
      }
    },
    computed: {
      currentWord() {
        // 添加多层安全保护
        if (
          /*// 确保数据已加载
          this.data?.words &&
          // 确认是有效数组
          Array.isArray(this.data.words) &&
          // 验证索引范围
          this.currentIndex >= 0 &&
          this.currentIndex < this.data.words.length*/

          // 确认是有效数组
          Array.isArray(this.data) &&
          // 验证索引范围
          this.currentIndex >= 0 &&
          this.currentIndex < this.data.length
        ) {
          // return this.data.words[this.currentIndex]
          return this.data[this.currentIndex]
        }

        // 返回安全默认值
        return {
          word: '加载中...',
          phonetic: '',
          translate: '',
          examples: ''
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
          this.$refs.WordMenu.scrollMenuToTop();
        }
      },
      async loadData() {
        try {
          let json = this.$route.query.json;
          let fileName = this.$route.query.fileName;
          this.audioPathPre = json+"/";
          const response = await fetch(json+'/'+fileName+'.json')
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
      handleWordSelected(index) {
        this.currentIndex = index;
        this.$refs.WordMenu.scrollMenuToActiveItem();
      },
      prevCard() {
        if (this.currentIndex > 0) {
          this.currentIndex--;
          this.$refs.WordMenu.scrollMenuToActiveItem();
        }
      },
      nextCard() {
        if (this.currentIndex < this.data.length - 1) {
          this.currentIndex++;
          this.$refs.WordMenu.scrollMenuToActiveItem();
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
            sampleBits: 16,      // 采样位数
            sampleRate: 16000,    // 采样率
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

</style>
