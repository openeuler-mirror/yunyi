<template>
  <div class="app-container">
    <!-- 添加或修改RDS服务对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1000px" append-to-body>
      <el-form ref="form" :model="form" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="服务名称" prop="serviceName">
              <span>{{form.serviceName}}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="节点认证" prop="secureMode">
              <span v-if="form.secureMode==2">已开启密码认证</span><span v-if="form.secureMode==0">未开启密码认证</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="软件版本" prop="versionDesc">
              <span>{{form.versionDesc }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置模版" prop="groupName">
              <span>{{form.groupName }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="form.remark">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              {{form.remark}}
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <single-node-list ref="singleNodeList" :serviceId="form.serviceId" :viewOnly="true" v-if="deployMode === 'single'"></single-node-list>
      <sentinel-node-list ref="sentinelNodeList" :serviceId="form.serviceId" :viewOnly="true" v-if="deployMode === 'sentinel' || deployMode === 'sentinel_worker'"></sentinel-node-list>
      <sentinel-worker-node-list ref="sentinelWorkerNodeList" :serviceId="form.serviceId" :viewOnly="true" v-if="deployMode === 'sentinel_worker'"></sentinel-worker-node-list>
      <cluster-node-list ref="clusterNodeList" :serviceId="form.serviceId" :viewOnly="true" v-if="deployMode === 'cluster'"></cluster-node-list>
      <scalable-node-list ref="scalableNodeList" :serviceId="form.serviceId" :viewOnly="true" v-if="deployMode === 'scalable'"></scalable-node-list>

      <div slot="footer" class="dialog-footer">
        <el-button @click="onCancel">关 闭</el-button>
      </div>

    </el-dialog>

  </div>
</template>

<script>
import { getServiceWithNodes } from "@/api/console/rdsservice";
import singleNodeList from "../nodelist/singleNodeList.vue";
import sentinelNodeList from "../nodelist/sentinelNodeList.vue";
import sentinelWorkerNodeList from "../nodelist/sentinelWorkerNodeList.vue";
import clusterNodeList from "../nodelist/clusterNodeList.vue";
import scalableNodeList from "../nodelist/scalableNodeList.vue";
import util from "@/utils/aesutils";

export default {
  name: "ServiceEdit",
  components: { singleNodeList, clusterNodeList, sentinelNodeList, sentinelWorkerNodeList, scalableNodeList },
  props: {
    deployMode: {// single|sentinel|sentinel_worker|cluster|scalable
      type: [String]
    },
  },
  dicts: ['cnsl_deploy_mode'],
  data() {
    return {
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {},
      // 工作节点列表数据
      nodes: []
    };
  },
  created() { },
  methods: {
    show(serv) {
      this.reset(false);
      this.open = true;
      this.getServiceInfo(serv.serviceId); //获取Service数据和关联的Nodes数据
      if(serv.sentinelServiceId) {
        //在哨兵主从模式时，sentinelServiceId是其对应哨兵的Id。
        //在哨兵主从模式时，需要读取对应哨兵服务的信息并展示。
        getServiceWithNodes(serv.sentinelServiceId).then(response => {
          let passwd = util.decrypt(response.data.service.password);
          response.data.service.password = new String(passwd);

          this.$nextTick(() => {
            this.$refs.sentinelNodeList.initNodeList(response.data.nodes, []);
          });
        });
      }

      this.title = this.getSubTitle() + '-查看详情';
    },
    getSubTitle() {
      const t = this.deployMode;
      switch(t) {
        case 'single': return '单节点服务';
        case 'sentinel': return '哨兵服务';
        case 'sentinel_worker': return '哨兵主从服务';
        case 'cluster': return '集群模式服务';
        case 'scalable': return '可伸缩集群模式服务';
        default : return '未知模式服务！！！';
      }
    },
    getNodeList() {
      if(this.deployMode === 'single')
        return this.$refs.singleNodeList;
      else if(this.deployMode === 'cluster')
        return this.$refs.clusterNodeList;
      else if(this.deployMode === 'sentinel')
        return this.$refs.sentinelNodeList;
      else if(this.deployMode === 'sentinel_worker')
        return this.$refs.sentinelWorkerNodeList;
      else if(this.deployMode === 'scalable')
        return this.$refs.scalableNodeList;
      else
        return null;
    },
    getServiceInfo(serviceId) { //获得服务信息，并加载相关信息
      getServiceWithNodes(serviceId).then(response => {
        let passwd = util.decrypt(response.data.service.password);
        response.data.service.password = new String(passwd);

        this.form = response.data.service;
        this.nodes = response.data.nodes;
        this.$nextTick(() => {
          this.getNodeList().initNodeList(this.nodes, []);
        });

      });
    },
    // 表单重置
    reset() {
      this.form = {
        serviceId: null,
        serviceName: null,
        deployEnv: null,
        deployMode: this.deployMode,
        secureMode: 0,
        versionId: this.defaultVersionId,
        groupId: null,
        password: '',
        sentinelPassworded: false,
        remark: null
      };

      this.nodes = [];
      //this.resetForm("form");
    },

    // 取消按钮
    onCancel() {
      this.open = false;
      this.reset();
    },

  }
};
</script>
