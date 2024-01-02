<template>
  <el-dialog title="新建key" :visible.sync="visible" :before-close="handleClose" width="600px">
    <div class="addKey">
      <el-form :model="formKey" label-width="100px" :rules="rules" ref="ruleForm" size="small">
        <el-form-item label="键名" prop="newKeyName">
          <el-input size="small" v-model="formKey.newKeyName" />
        </el-form-item>
        <el-form-item label="类型" prop="value">
          <el-select size="small" v-model="formKey.value">
            <el-option v-for="(type, showType) in keyList" :key="type" :label="showType" :value="type">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose" size="small">取消</el-button>
      <el-button @click="submitForm('ruleForm')" type="primary" size="small">保存</el-button>
    </div>
  </el-dialog>
</template>
<script>
export default {
  data() {
    return {
      rules: {
        newKeyName: [
          { required: true, message: '请输入key名称', trigger: 'blur' },
        ],
        value: [
          { required: true, message: '请选择类型', trigger: 'change' }
        ]
      },
      visible: true,
      formKey: {
        newKeyName: '',
        value: ''
      },
      keyList: { String: 'string', Hash: 'hash', List: 'list', Set: 'set', Zset: 'zset', Stream: 'stream' }
    }
  },
  methods: {
    handleClose() {
      this.$emit('KeysClose')
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.$emit('KeysTypeSave', this.formKey)
        } else {
          console.log('error submit!!');
          return false;
        }
      });
    },
  }
}
</script>
<style lang="scss" >
.addKey{
  .el-input--small .el-input__inner {
    border: 1px solid #ccc !important;
  }
}
</style>