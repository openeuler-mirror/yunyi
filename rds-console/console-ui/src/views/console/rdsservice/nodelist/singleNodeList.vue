<template>
  <div class="app-container">

    <el-table v-loading="loading" :data="nodeList" :row-style="onNodeListRowStyle" @selection-change="handleSelectionChange">
      <el-table-column label="节点名称" align="center" prop="nodeName" />
      <el-table-column label="管理器" align="center" prop="managerName" />
      <el-table-column label="节点地址" align="center" prop="hostAddress" />
      <el-table-column label="服务端口" width="80" align="center" prop="servicePort" />
      <el-table-column label="Redis端口" width="85" align="center" prop="redisPort" />
      <el-table-column label="节点状态" width="80" align="center" prop="nodeStatus">
        <template v-slot="scope">
          <dict-tag :options="dict.type.cnsl_node_status" :value="scope.row.nodeStatus" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="60" v-if="viewOnly">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)"
                     v-hasPermi="['console:rdsservice:query']">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <node-view ref="nodeView" nodeType="worker" deployMode="cluster"></node-view>
  </div>
</template>

<script>
import { getNode, listNode, delNode, startNode, stopNode, restartNode } from "@/api/console/rdsnode";
import nodeView from "../view/nodeView";
import {getDiffProps} from '@/utils/validate';

export default {
  name: "SingleNodeList",
  components: { nodeView },
  props: {
    serviceId: {
      type: [Number]
    },
    viewOnly: {  //只读查看列表
      type: [Boolean],
      default: false
    }
  },
  dicts: ['cnsl_deploy_env', 'cnsl_node_type', 'cnsl_node_status', 'cnsl_rds_secure_mode', 'cnsl_deploy_mode'],
  data() {

    return {
      // 识别Node变更属性时，需要跳过的属性
      nodeSkipProps: ['searchValue', 'createBy', 'createTime', 'updateBy', 'updateTime', 'params', '__ob__'],
      // 工作节点列表加载
      loading: false,
      // 选中数组
      selectedRows: [],
      // 没有列表选中项
      noneSelected: true,
      // 当前编辑和变更的node列表，保存时以此列表中的信息为准
      nodeList: [],
      // 原node列表，更新时从数据库取出时的原始值，用于对比变更
      nodeListOrg: [],
      //nodeIndex的下一个值
      nextIndex: 0,
    };
  },
  methods: {
    setLoading(isLoading) {
      this.loading = isLoading;
    },
    initNodeList(nodeList, nodeListOrg) { //对nodeList进行加载处理
      this.nodeList = nodeList;
      this.nodeListOrg = nodeListOrg;
      //为每一个node加入 index
      this.nodeList.forEach((node, idx) => {
        node.index = idx;
      });
      this.nextIndex = this.nodeList.length;
    },
    getNodeList() {
      return this.nodeList;
    },
    validate() { //验证
      if(this.nodeList != null && this.nodeList.length != 1) {
        this.$alert('服务中需要具有一个节点信息！', '系统提示', {type: 'warning'} );
        return false;
      }

      return true; //验证通过
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

    /** 启动、停止、重启操作 */
    handleStart(row) {
      const nodeId = row.nodeId;
      row.nodeStatus = "starting";
      startNode(nodeId).then(response => {
        row.nodeStatus = 'start';
        this.$modal.msgSuccess("启动成功");
      });
    },
    handleStop(row) {
      const nodeId = row.nodeId;
      row.nodeStatus = "stopping";
      stopNode(nodeId).then(response => {
        row.nodeStatus = 'stop';
        this.$modal.msgSuccess("停止成功");
        this.getList();
      });
    },
    handleRestart(row) {
      const nodeId = row.nodeId;
      row.nodeStatus = "starting";
      restartNode(nodeId).then(response => {
        row.nodeStatus = 'start';
        this.$modal.msgSuccess("重启成功");
        this.getList();
      });
    },

    /** 删除按钮操作 */
    handleDelete() {
      if(this.selectedRows.length == 0) {
        this.$modal.msgError("请先选择要删除节点信息");
        return;
      }

      const selectedIndexs = this.selectedRows.map(item => item.index);
      const nodeNames = this.selectedRows.map(item => item.nodeName);
      //this.$modal.confirm('是否确认删除节点名为"' + nodeNames + '"的节点？')

      this.nodeList = this.nodeList.filter((val) => {
        return selectedIndexs.indexOf(val.index) == -1;
      });
    },
    onNodeListRowStyle({ row, rowIndex }) {
      if(!row.nodeId)
        return {backgroundColor: '#e8f4ff'}; //新建行的样式
      else if(row.changedProps && row.changedProps.length > 0)
        return {backgroundColor: 'oldlace'}; //修改行的样式
      else
        return {};
    },
  }
};
</script>
