<template>
  <div class="viewer">
    <JsonViewer
      ref="codeViewer"
      :value="code"
      :expand-depth="expandDepth"
      theme="code-viewer"
      :preview-mode="previewMode"
    />
    <el-tooltip v-model="visible" :content="copyText" placement="top" :manual="manual">
      <i v-if="copyable" ref="copyBtn" class="el-icon-document-copy copy-btn" />
    </el-tooltip>
  </div>
</template>

<script>
import JsonViewer from 'vue-json-viewer/ssr'
import 'vue-json-viewer/style.css'
import Clipboard from 'clipboard'

export default {
  components: {
    JsonViewer
  },
  props: {
    code: [Array, Object],
    copyable: {
      type: Boolean,
      default: true
    },
    expandDepth: {
      type: Number,
      default: 5
    },
    previewMode: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      copyText: '复制',
      visible: false,
      manual: false
    }
  },
  mounted() {
    if (!this.copyable) return
    const clipBoard = new Clipboard(this.$refs.copyBtn, {
      container: this.$refs.codeViewer.$el,
      text: () => {
        return JSON.stringify(this.code, null, 2)
      }
    })
    clipBoard.on('success', (e) => {
      this.copyText = '复制成功!'
      this.visible = true
      this.manual = true
      setTimeout(() => {
        this.visible = false
        this.manual = false
        this.copyText = '复制'
      }, 1000)
    })

    clipBoard.on('error', (e) => {
      this.copyText = '复制失败'
      this.visible = true
      this.manual = true
      setTimeout(() => {
        this.visible = false
        this.manual = false
        this.copyText = '复制'
      }, 1000)
    })
  }
}

</script>

<style lang='scss'>
.viewer {
  position: relative;
  width: 100%;
  .copy-btn {
    position: absolute;
    right: 18px;
    top: 12px;
    font-size: 14px;
    transition: color .2s;
    cursor: pointer;
    &:hover {
      color: #1890FF;
    }
  }
  .code-viewer {
    font-family: Consolas,Menlo,Courier,monospace;
    width: 100%;
    height: 100%;
    font-size: 14px;
    .jv-code {
      background-color: #FBFBFB;
      padding: 8px;
      border-radius: 4px;
      .jv-key {
        color: #008080;
      }
      .jv-item {
        color: #d14;
        margin-left: 0.5em;
      }
    }
  }
}
</style>
