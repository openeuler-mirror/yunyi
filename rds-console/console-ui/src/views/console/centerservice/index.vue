<template>
  <div class="app-container">
    <el-row>
      <el-col :span="24" class="card-box">
        <el-card>
          <div slot="header">
            <span>中心服务信息</span>
          </div>
          <div class="el-table el-table--enable-row-hover el-table--medium">
            <table cellspacing="0" style="width: 100%;">
              <tbody>
              <tr>
                <td class="el-table__cell is-leaf">
                  <div class="cell">服务名称</div>
                </td>
                <td class="el-table__cell is-leaf">
                  <div class="cell">{{ serviceInfo.serviceName }}</div>
                </td>
                <td class="el-table__cell is-leaf">
                  <div class="cell">更新时间</div>
                </td>
                <td class="el-table__cell is-leaf">
                  <div class="cell">{{ parseTime(serviceInfo.updateTime) }} </div>
                </td>
              </tr>
              <tr>
                <td class="el-table__cell is-leaf">
                  <div class="cell">版本</div>
                </td>
                <td class="el-table__cell is-leaf">
                  <div class="cell">{{ serviceInfo.versionDesc }}</div>
                </td>
                <td class="el-table__cell is-leaf">
                  <div class="cell">模版组</div>
                </td>
                <td class="el-table__cell is-leaf">
                  <div class="cell">{{ serviceInfo.groupName }}</div>
                </td>
              </tr>
              <tr v-if="deployEnv=='k8s'">
                <td class="el-table__cell is-leaf">
                  <div class="cell">服务地址</div>
                </td>
                <td class="el-table__cell is-leaf">
                  <div class="cell">{{ serviceInfo.hostAddress }}</div>
                </td>
                <td class="el-table__cell is-leaf">
                  <div class="cell">管理端口</div>
                </td>
                <td class="el-table__cell is-leaf">
                  <div class="cell">{{ serviceInfo.adminPort }}</div>
                </td>
              </tr>
              <tr>
                <td class="el-table__cell is-leaf" colspan="4">
                  <div class="cell" style="text-align-last:right;">
                    <el-button icon="el-icon-connection" size="mini" @click="handleTestConnection"
                               v-hasPermi="['console:centerservice:edit']" :disabled="loading">测试连接</el-button>

                    <el-button type="primary"  icon="el-icon-refresh-right" size="mini" @click="handleConfigUpdate"
                               v-if="deployEnv=='host'"
                               v-hasPermi="['console:centerservice:edit']" :disabled="loading">更新各服务配置</el-button>

                    <el-button type="success" icon="el-icon-edit" size="mini" @click="handleServiceUpdate"
                               v-hasPermi="['console:centerservice:edit']" :disabled="loading"> 修   改 </el-button>

                    <el-button type="danger" icon="el-icon-s-release" size="mini" @click="handleResetConfig"
                               v-hasPermi="['console:centerservice:edit']" :disabled="loading">清除重置</el-button>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :search="false" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="centerNodeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <!-- <el-table-column label="节点ID" align="center" prop="nodeId" /> -->
      <el-table-column label="节点名称" align="center" prop="nodeName" />
      <!--
      <el-table-column label="节点类型" align="center" prop="nodeType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.cnsl_node_type" :value="scope.row.nodeType" />
        </template>
      </el-table-column>
      -->
      <el-table-column label="管理器" align="center" prop="managerName" />
      <el-table-column label="节点地址" align="center" prop="hostAddress" />
      <el-table-column label="服务端口" align="center" prop="servicePort" />
      <el-table-column label="管理端口" align="center" prop="adminPort" />
      <el-table-column label="节点状态" align="center" prop="nodeStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.cnsl_node_status" :value="scope.row.nodeStatus" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="210">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-if="deployEnv=='k8s'"
                     v-hasPermi="['console:centerservice:query']">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <center-service ref="centerService" :disableSettings="centerNodeList.length > 0" :serviceId="centerServiceId" @transfer="onServiceConfirm"></center-service>
    <node-view ref="nodeView" nodeType="center"  deployMode="cluster"></node-view>
  </div>
</template>

<script>
import { clearCenterConfig, testAdminConnection, resendCenterConfig, getService } from "@/api/console/centerservice";
import { delNode, listNode, addNode, updateNode,  startNode, stopNode, restartNode } from "@/api/console/rdsnode";
import { getAppConfigKey,getConfigKey } from '@/api/system/config'
import {getDiffProps} from '@/utils/validate';
import centerService from "./centerService";
import nodeView from "../rdsservice/view/nodeView";

export default {
  name: "CenterService",
  dicts: ['cnsl_node_type', 'cnsl_deploy_env', 'cnsl_node_status'],
  components: {centerService, nodeView},
  data() {
    return {
      // 识别Node变更属性时，需要跳过的属性
      nodeSkipProps: ['searchValue', 'createBy', 'createTime', 'updateBy', 'updateTime', 'params', '__ob__'],
      // 中心服务固定的ID值
      centerServiceId: 1,
      // 遮罩层
      loading: false,
      // 选中数组
      selectedRows: [],
      // 没有列表选中项
      noneSelected: true,
      // 中心节点列表数据
      centerNodeList: [],
      // 中心服务信息
      serviceInfo: {},
      // 更新时获取的Node数据
      updateNodeData: {},
      //nodeIndex的下一个值
      nextIndex: 0,
      //部署模式（从后台取得配置）
      deployEnv: null,

      timer: null,
      refreshIntervalSeconds: 60
    };
  },
  created() {
    getAppConfigKey('console.deployEnv').then((res) => {
      this.deployEnv = res.data;
    });
    //获取配置监控数据刷新时间
    getConfigKey('k8sCenterNodeAutoRefreshSeconds').then((data) => {
      if (data.code === 200 && data.data) {
        this.refreshIntervalSeconds = parseInt(data.data)
      }
    })
    this.getServiceInfo();
  },
  beforeDestroy() {
    console.log('清空定时器 beforeDestroy')
    this.timer && clearInterval(this.timer)
  },
  methods: {
    //////////////////////////////////////
    // 服务相关操作
    //////////////////////////////////////
    getServiceInfo() {
      getService().then(response => {
        this.serviceInfo = response.data;
        this.getList();

      });
    },
    handleServiceUpdate() {
      this.$refs.centerService.show(this.centerServiceId);
    },
    handleConfigUpdate() {
      this.$confirm('当中心的地址或端口发生变更后，需要对每一个服务节点更新中心节点地址配置，此过程可能会耗用较长时间，请确认是否现在更新？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        .then(async () => {
          this.loading = true;
          let response = await resendCenterConfig();
          if (response.code < 400) {
            this.$modal.msgSuccess('中心节点地址配置更新完成！');
            this.getList();
          }
        })
        .catch(() => {
          this.loading = false;
        });
    },
    handleResetConfig() {
      this.$confirm('当前要重置中心服务的配置，将删除所有中心节点，所有的动态RDS服务信息也将被删除，是否继续？', '重要提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        .then(async () => {
          this.loading = true;
          let response = await clearCenterConfig();
          if (response.code < 400) {
            this.$modal.msgSuccess('中心服务配置，重置完成！');
            this.getServiceInfo();
            this.getList();
          }
        })
        .catch(() => {
          this.loading = false;
        });
    },
    handleTestConnection() {
      this.$confirm('将测试中心服务的管理端口是否正常，请确认？', '提示', { confirmButtonText: '测试', cancelButtonText: '取消', type: 'info' })
        .then(async () => {
          this.loading = true;
          let response = await testAdminConnection();
          this.loading = false;
          if (response.code < 400) {
            this.$modal.msgSuccess(response.msg);
          }
        })
        .catch(() => {
          this.loading = false;
        });
    },
    onServiceConfirm(serivceData) {
      this.getServiceInfo();
      this.getList();
    },

    //////////////////////////////////////
    // 中心节点相关操作
    //////////////////////////////////////
    /** 查询中心节点列表 */
    getList() {
      this.loading = true;
      this.timer && clearInterval(this.timer) // 首先进入清除定时器
      listNode({serviceId: 1, }).then(response => {
        this.centerNodeList = response.data;
        this.centerNodeList.forEach((node, idx) => {
          node.index = idx;
        });
        this.nextIndex = this.centerNodeList.length;
        if( this.deployEnv == 'k8s'){  //如果部署环境是k8s启用,按配置自动刷新
          this.timer = setInterval(() => this.getList(), this.refreshIntervalSeconds * 1000)
        }
        this.loading = false;
      });
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.selectedRows = selection;
      this.noneSelected = !selection.length
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.$refs.nodeView.showView(row);
    },

    /** 提交按钮 */
    onNodeConfirm(nodeData) {
      if(nodeData.newAdded) {
        this.loading = true;
        addNode(nodeData).then(response => {
          this.getList();
          this.$modal.msgSuccess("节点'" + nodeData.nodeName + "'完成添加!");
        });
      }
      else {
        this.loading = true;
        nodeData.changedProps = getDiffProps(this.updateNodeData, nodeData, this.nodeSkipProps);
        updateNode(nodeData).then(response => {
          this.getList();
          this.$modal.msgSuccess("节点'" + nodeData.nodeName + "'完成修改!");
        });
      }
    },

    /** 修改按钮操作 */
    handleStart(row) {
      const nodeId = row.nodeId;
      this.loading = true;
      startNode(nodeId).then(response => {
        this.$modal.msgSuccess("启动成功");
        this.getList();
      });
    },
    handleStop(row) {
      const nodeId = row.nodeId;
      this.loading = true;
      stopNode(nodeId).then(response => {
        this.$modal.msgSuccess("停止成功");
        this.getList();
      });
    },
    handleRestart(row) {
      const nodeId = row.nodeId;
      this.loading = true;
      restartNode(nodeId).then(response => {
        this.$modal.msgSuccess("重启成功");
        this.getList();
      });
    },

    /** 删除按钮操作 */
    handleDelete() {
      const nodeIds = this.selectedRows.map(item => item.nodeId);
      const nodeNames = this.selectedRows.map(item => item.nodeName);
      this.$confirm('是否确认删除节点名为"' + nodeNames + '"的中心节点？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        .then(async () => {
          this.loading = true;
          let response = await delNode(nodeIds);
          if (response.code < 400) {
            this.$modal.msgSuccess("删除成功");
            this.getList();
          }
          else {
            this.$modal.msgError("删除时出现异常", response.msg);
          }
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });

      // this.$confirm('是否确认删除节点名为"' + nodeNames + '"的中心节点？').then(function () {
      //   console.log("deleting...", nodeIds);
      //   this.loading = true;
      //   return delNode(nodeIds);
      // }).then(() => {
      //   this.getList();
      //   this.$modal.msgSuccess("删除成功");
      // }).catch(() => {
      //   this.loading = false;
      // });
    }

  }
};
</script>
