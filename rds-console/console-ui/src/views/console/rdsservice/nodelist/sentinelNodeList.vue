<template>
  <div class="app-container">

    <el-card>
      <div slot="header">
        <span>哨兵节点列表</span>
      </div>

      <el-table v-loading="loading" :data="nodeList" :row-style="onNodeListRowStyle" @selection-change="handleSentinelSelectionChange">
        <el-table-column type="selection" width="55" align="center" v-if="viewOnly==false"/>
        <el-table-column label="节点名称" align="center" prop="nodeName" />
        <el-table-column label="管理器" align="center" prop="managerName" />
        <el-table-column label="节点地址" align="center" prop="hostAddress" />
        <el-table-column label="服务端口" width="80" align="center" prop="servicePort" />
        <el-table-column label="节点状态" width="80" align="center" prop="nodeStatus">
          <template v-slot="scope">
            <dict-tag :options="dict.type.cnsl_node_status" :value="scope.row.nodeStatus" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="60" v-if="viewOnly">
          <template v-slot="scope">
            <el-button size="mini" type="text" icon="el-icon-view" @click="onSentinelView(scope.row)"
                       v-hasPermi="['console:rdsservice:query']">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <node-view ref="sentinelNodeView" nodeType="sentinel" deployMode="sentinel"></node-view>
  </div>
</template>

<script>
import { getNode, listNode, delNode, startNode, stopNode, restartNode } from "@/api/console/rdsnode";
import nodeView from "../view/nodeView";
import {getDiffProps} from '@/utils/validate';

export default {
  name: "SentinelNodeList",
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
      // 当前编辑和变更的node列表，保存时以此列表中的信息为准
      nodeList: [],
      // 原node列表，更新时从数据库取出时的原始值，用于对比变更
      nodeListOrg: [],
      //nodeIndex的下一个值
      nextIndex: 0,

      sentinelsNoneSelected: true, // 哨兵节点列表中没有被选中的标识
      sentinelsSelectedRows: [], // 哨兵节点列表中选中的行
    };
  },
  methods: {
    setLoading(isLoading) {
      this.loading = isLoading;
    },
    initNodeList(nodeList, nodeListOrg) { //对nodeList进行加载处理
      this.nodeList = nodeList;
      this.nodeListOrg = nodeListOrg;
      this.nextIndex = this.nodeList.length;
    },
    getNodeList() { //获取当前nodeList数据
      return this.nodeList;
    },
    validate() { //验证
      if(this.nodeList.length < 1) {
        this.$alert('哨兵节点至少应该具有一个或更多节点！', '系统提示', {type: 'warning'} );
        return false;
      }
      return true; //验证通过
    },
    /** 多选框选中数据 */
    handleSentinelSelectionChange(selection) {
      this.sentinelsNoneSelected = selection;
      this.sentinelsSelectedRows = !selection.length
    },
    /** 根据变更或者新建状态，显示不同的行颜色 */
    onNodeListRowStyle({ row, rowIndex }) {
      if(!row.nodeId)
        return {backgroundColor: '#e8f4ff'}; //新建行的样式
      else if(row.changedProps && row.changedProps.length > 0)
        return {backgroundColor: 'oldlace'}; //修改行的样式
      else
        return {};
    },
    /** 查看按钮操作 */
    onSentinelView(row) {
      this.$refs.sentinelNodeView.showView(row);
    },
    /** 启动、停止、重启操作 */
    onStart(row) {
      const nodeId = row.nodeId;
      row.nodeStatus = "starting";
      startNode(nodeId).then(response => {
        row.nodeStatus = 'start';
        this.$modal.msgSuccess("启动成功");
      });
    },
    onStop(row) {
      const nodeId = row.nodeId;
      row.nodeStatus = "stopping";
      stopNode(nodeId).then(response => {
        row.nodeStatus = 'stop';
        this.$modal.msgSuccess("停止成功");
        this.getList();
      });
    },
    onRestart(row) {
      const nodeId = row.nodeId;
      row.nodeStatus = "starting";
      restartNode(nodeId).then(response => {
        row.nodeStatus = 'start';
        this.$modal.msgSuccess("重启成功");
        this.getList();
      });
    },

  }
};
</script>
