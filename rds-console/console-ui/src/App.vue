<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
  metaInfo() {
    return {
      title: this.$store.state.settings.dynamicTitle && this.$store.state.settings.title,
      titleTemplate: title => {
        return title ? `${title} - ${process.env.VUE_APP_TITLE}` : process.env.VUE_APP_TITLE
      }
    }
  }
}
</script>
<style lang="scss">
.el-form--inline .el-form-item {
  border: 1px solid #DCDFE6;
  padding-left: 14px
}
.el-form--inline .el-form-item:last-child{
  border: none;
}

.el-input--small .el-input__inner {
  border: none
}

.el-range-editor--small.el-input__inner {
  border: none
}
.top-right-btn{
  .el-row{
  display: none;
}
}

label {
  font-weight: 400;
}

.uhry-card-wrapper {

  .uhry-card-head {
    padding-bottom: 10px;

    .uhry-card-head-title {
      font-size: 16px;
      font-weight: bold;
    }
  }
}

.uhry-font-red {
  color: #F56C6C;
}

.uhry-font-green {
  color: #67C23A;
}

.uhry-font-yellow {
  color: #E6A23C;
}

.uhry-font-blue {
  color: #409EFF;
}

.uhry-font-minor {
  color: #909399;
}</style>
