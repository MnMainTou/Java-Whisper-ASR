<template>
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
          v-for="(word, index) in words"
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
</template>

<script>
  import { ref, nextTick  } from 'vue';

  export default {
    name: 'WordMenu',
    props: {
      words: {
        type: Array,
        required: true
      },
      currentIndex: {
        type: Number,
        default: 0
      }
    },
    emits: ['word-selected'],
    setup(props, { emit }) {
      const menuRef = ref(null);

      const handleSelect = (index) => {
        emit('word-selected', parseInt(index));
      };

      // 添加菜单滚动到顶部方法
      const scrollMenuToTop = () => {
        // 使用 nextTick 确保 DOM 已经更新
        nextTick(() => {
          const menu = menuRef.value.$el;
          if (menu) {
            menu.scrollTop = 0;
          }
        });
      };

      // 保持激活项可见的样式
      const scrollMenuToActiveItem = () => {
        nextTick(() => {
          const menu = menuRef.value.$el;
          const activeItem = menu.querySelector('.el-menu-item.is-active');

          if (activeItem) {
            // 使用现代滚动API
            activeItem.scrollIntoView({
              behavior: 'smooth',
              block: 'center', // 垂直居中
              inline: 'nearest'
            });

            // 兼容性备用方案
            if (typeof activeItem.scrollIntoViewIfNeeded === 'function') {
              activeItem.scrollIntoViewIfNeeded(true);
            }
          }
        });
      };

      return {
        menuRef,
        handleSelect,
        scrollMenuToTop,
        scrollMenuToActiveItem,
      };
    }
  };
</script>

<style scoped>
  /* ========== 左侧菜单样式 ========== */
  .sidebar {
    width: 380px;
    flex-shrink: 0;
    height: calc(100vh - 180px); /* 根据容器高度计算 */
    position: relative;
    overflow: hidden; /* 隐藏原生滚动条 */
  }
  .sidebar-layout {
    display: flex;
    height: 100%;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    overflow: hidden;
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
</style>
