<template>
  <div>
    <el-dialog title="新建连接" :visible.sync="visible" :before-close="handleClose" width="700px">
      <div>
        <el-form label-width="80px" label-position="left">
          <el-form-item label="地址">
            <el-input size="small" placeholder="127.0.0.1" />
          </el-form-item>
          <el-form-item label="端口">
            <el-input size="small" placeholder="6379" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input size="small" placeholder="Auth" />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input size="small" placeholder="ACL in Redis >=6.0" />
          </el-form-item>
          <el-form-item label="连接名称">
            <el-input size="small" />
          </el-form-item>
          <el-form-item label="分隔符">
            <el-input size="small" placeholder=":" />
          </el-form-item>
          <el-form-item label="操作">
            <el-checkbox v-model="sshOptionsShow">SSH</el-checkbox>
            <el-checkbox v-model="sslOptionsShow">SSL</el-checkbox>
            <el-checkbox v-model="sentinelOptionsShow">
              Sentinel
              <el-popover trigger="hover">
                <i slot="reference" class="el-icon-question"></i>
                多个哨兵任选其一即可，地址、端口、密码请填写哨兵配置，Redis节点密码为哨兵监听的Master节点密码
              </el-popover>
            </el-checkbox>
            <el-checkbox v-model="clusterOptionsShow">
              Cluster
              <el-popover trigger="hover">
                <i slot="reference" class="el-icon-question"></i>
                选择集群中任一节点配置填入即可，会自动识别其它节点
              </el-popover>
            </el-checkbox>
            <el-checkbox v-model="connectionReadOnlyOptionsShow">
              Readonly
              <el-popover trigger="hover">
                <i slot="reference" class="el-icon-question"></i>
                只读模式，禁止新增、编辑和删除
              </el-popover>
            </el-checkbox>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleClose" size="small">取消</el-button>
        <el-button @click="connetOK" type="primary" size="small">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
export default {
  data() {
    return {
      visible: true,
      sshOptionsShow:false,
      sslOptionsShow:false,
      sentinelOptionsShow:false,
      clusterOptionsShow:false,
      connectionReadOnlyOptionsShow:false
    }
  },
  methods: {
    handleClose() {
      this.$emit('close')
    },
    connetOK() {
      this.$emit('close')
    }
  }
}
</script>