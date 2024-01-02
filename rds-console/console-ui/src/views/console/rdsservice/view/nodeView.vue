<template>
  <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
    <el-form ref="form" :model="form"  label-width="100px">
      <el-form-item label="节点管理器" prop="managerId" v-if="form.managerId">
        <span>{{form.managerName}}</span>
      </el-form-item>
      <el-form-item label="节点名称" prop="nodeName">
        <span>{{form.nodeName}}</span>
      </el-form-item>
      <el-form-item label="节点地址" prop="hostAddress">
        <span>{{form.hostAddress}}</span>
      </el-form-item>
      <el-form-item label="服务端口" prop="servicePort">
        <span>{{form.servicePort}}</span>
      </el-form-item>
      <el-form-item label="Redis端口" prop="redisPort" v-if="nodeType==='worker'">
        <span>{{form.redisPort}}</span>
      </el-form-item>
      <el-form-item label="管理端口" prop="adminPort" v-if="nodeType==='center'">
        <span>{{form.adminPort}}</span>
      </el-form-item>
      <el-form-item label="主/从节点" prop="masterNode" v-if="nodeType==='worker' && (deployMode==='sentinel'||deployMode==='cluster') ">
        <span v-if="form.masterNode">主节点</span><span v-if="!form.masterNode">从节点</span>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <span>{{form.remark}}</span>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="onCancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {listSamePortNode,listSameNameNode} from "@/api/console/rdsnode"

export default {
  name: "NodeView",
  props: {
    nodeType: {// center|worker|sentinel|proxy
      type: [String]
    },
    deployMode: {// single|sentinel|cluster|cncluster
      type: [String]
    }
  },
  data() {
    return {
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {}
    };
  },
  created() {},
  methods: {
    // 表单重置
    reset() {
      this.form = {
        newAdded: true,// 是否为创建新增
        nodeId: null,
        serviceId: null,
        managerId: null,
        nodeType: this.nodeType,
        nodeName: null,
        hostAddress: null,
        masterNode: false,
        shard: 0,
        slot: null,
        servicePort: null,
        redisPort: null,
        adminPort: null,
        nodeStatus: 'none',
        remark: null
      };
      //this.resetForm("form");
    },
    getSubTitle() {
      const t = this.nodeType;
      switch(t) {
        case 'center': return '中心节点';
        case 'worker': return '工作节点';
        case 'sentinel': return '哨兵节点';
        case 'proxy': return '代理节点';
        default : return '';
      }
    },
    /** 显示节点信息 */
    showView(nodeData) {
      this.form = nodeData;
      this.open = true;
      this.title = this.getSubTitle() + "-详细信息";
    },
    // 取消按钮
    onCancel() {
      this.open = false;
    }
  }
};

</script>
