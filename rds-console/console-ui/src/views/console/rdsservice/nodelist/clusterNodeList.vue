<template>
  <div class="app-container">

    <el-card v-for="(shard, index) in shardList" :key="index">
      <div slot="header">
        <span>分片{{shard.index}} (Slot: {{shard.slot}})</span>
      </div>


      <el-table v-loading="loading" :data="shard.nodes" :row-style="onNodeListRowStyle" @selection-change="(selection) => { handleSelectionChange(shard, selection) }">
        <!-- <el-table-column label="节点ID" align="center" prop="nodeId" /> -->
        <el-table-column label="节点名称" align="center" prop="nodeName" />
        <el-table-column label="管理器" align="center" prop="managerName" />
        <el-table-column label="节点地址" align="center" prop="hostAddress" />
        <el-table-column label="服务端口" width="80" align="center" prop="servicePort" />
        <el-table-column label="Redis端口" width="85" align="center" prop="redisPort" />
        <el-table-column label="主/从" width="85" align="center" prop="masterNode">
          <template v-slot="scope">
            <span v-if="(viewOnly == true && (scope.row.nodeStatus != 'stop' && scope.row.nodeStatus != 'none')) || viewOnly == false ">
              <span style="color:#1890FF;" v-if="scope.row.masterNode">主</span><span style="color:darkgray;" v-else>从</span>
            </span>
          </template>
        </el-table-column>

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
    </el-card>

    <node-view ref="nodeView" nodeType="worker" deployMode="cluster"></node-view>
  </div>
</template>

<script>
import { getNode, listNode, delNode, startNode, stopNode, restartNode } from "@/api/console/rdsnode";
import nodeView from "../view/nodeView";
import {getDiffProps} from '@/utils/validate';

export default {
  name: "ClusterNodeList",
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
      //集群插最大值
      slotMaxIndex: 16383,
      // 分片数据，以及每个分片中的Node列表（会和nodeList中同步引用相同的一组nodes)
      shardList: [],
      // 当前编辑和变更的node列表，保存时以此列表中的信息为准
      nodeList: [],
      // 原node列表，更新时从数据库取出时的原始值，用于对比变更
      nodeListOrg: [],
      //shard 初始化大小（创建时为 1， 修改时为读取对应service中shard的数量。
      shardInitCount: 1,
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
      this.shardList = [];

      //得到最大shard index, 为每个node.index赋值
      let shardIndex = 0;
      this.nodeList.forEach((node, idx) => {
        node.index = idx;
        if(node.shard > shardIndex) shardIndex = node.shard;
      });

      this.nextIndex = this.nodeList.length;
      this.shardInitCount = shardIndex + 1;
      this.buildShardList(this.shardInitCount);

      //nodes 加入到不同的shard.nodes数组中
      this.nodeList.forEach((node, idx) => {
        this.shardList[node.shard].nodes.push(node);
        if(this.shardList[node.shard].slot != node.slot) { //以节点中的slot为准
          this.shardList[node.shard].slot = node.slot;
        }
      });
    },
    getNodeList() { //获取当前nodeList数据
      return this.nodeList;
    },
    validate() { //验证
      let valid = true;
      this.shardList.forEach((shard) => {
        let shardName = '分片' + shard.index + '(Slot: '+ shard.slot +')';
        if(shard.nodes.length < 2) {
          this.$alert(shardName + ' 至少应该具有两个或两个以上节点！', '系统提示', {type: 'warning'} );
          valid = false;
          return false;
        }
        else if(shard.nodes.findIndex(node => {return (node.masterNode == true)}) < 0) {
          this.$alert(shardName + ' 未指定主节点！', '系统提示', {type: 'warning'} );
          valid = false;
          return false;
        }
      });

      return valid; //验证通过
    },


    //计算集群部署模式下每个分片所管理的插槽范围
    buildShardList(shardCount) {
      this.shardList = [];

      //计算每个分片所需管理的平均槽数
      let perShardSlot = Math.floor((this.slotMaxIndex+1) / shardCount);

      //给出每个shard的slot和index, 并push到shardList
      for(var i = 0 ; i < shardCount ; i++ ) {
        let slotString = this.getShardSlot(i, shardCount, perShardSlot);
        this.shardList.push({
          slot: slotString,
          index: i,
          nodes: [],
          noneSelected: true, //列表中没有被选中的标识
          selectedRows: [],
        });
      }
    },
    onIncreaseShard() {
      let shardCount = this.shardList.length + 1;  //原shard长度加1
      //计算每个分片所需管理的平均槽数
      let perShardSlot = Math.floor((this.slotMaxIndex+1) / shardCount);

      //给出每个shard的slot和index, 并push到shardList
      for(var i = 0 ; i < shardCount ; i++ ) {
        let slotString = this.getShardSlot(i, shardCount, perShardSlot);
        if(i === (shardCount-1) ) {
          this.shardList.push({
            slot: slotString,
            index: i,
            nodes: [],
            noneSelected: true,
            selectedRows: [],
          });
        }
        else {
          this.shardList[i].slot = slotString; //更新slot
          this.shardList[i].nodes.forEach(n => { n.slot = slotString });
        }
      }
    },
    onDecreaseShard() {
      let shardCount = this.shardList.length;
      let lastShard = this.shardList[shardCount - 1];
      if(this.shardInitCount == shardCount) {
        this.$alert('目前分片数量已是最小值，无法再减少！', "系统提示", {type: 'warning'});
        return;
      }
      else if(lastShard.nodes.length > 0) {
        this.$alert("最后一分片中还存在节点，清空节点后才可减少分片", "系统提示", {type: 'warning'});
        return;
      }

      shardCount --;
      this.shardList.pop(); //数组减一
      //计算每个分片所需管理的平均槽数
      let perShardSlot = Math.floor((this.slotMaxIndex+1) / shardCount);

      for(var i = 0 ; i < shardCount ; i++ ) { //更新shard和shard.nodes中的slot值
        let slotString = this.getShardSlot(i, shardCount, perShardSlot);
        this.shardList[i].slot = slotString;
        this.shardList[i].nodes.forEach(n => { n.slot = slotString });
      }
    },
    /**
     * @param {Object} index 获得当前shard的索引，
     * @param {Object} shardCount shard的总数
     * @param {Object} perShardSlot 每个shard中slot的数量
     */
    getShardSlot(index, shardCount, perShardSlot) {
      let startSlot = index * perShardSlot;
      let endSlot = (index === shardCount-1) ? this.slotMaxIndex : ((index+1)*perShardSlot - 1);
      return startSlot + '-' + endSlot;
    },

    /** 多选框选中数据 */
    handleSelectionChange(shard, selection) {
      shard.selectedRows = selection;
      shard.noneSelected = !selection.length
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
