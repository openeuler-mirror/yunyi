<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
      <el-form-item label="服务名称"  prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入服务名称" clearable/>
      </el-form-item>
      <el-form-item label="服务状态" prop="status">
        <el-select v-model="queryParams.status" clearable>
          <el-option label="运行" value="start"></el-option>
          <el-option label="部分运行" value="start-part"></el-option>
          <el-option label="停止" value="stop"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" style="width: 100%;margin-bottom: 20px;" row-key="name"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
      <el-table-column label="名称" prop="name">
        <template slot-scope="scoped">
          <el-button v-if="!scoped.row.nodeId" size="mini" type="text" @click="gotoDeatil(scoped.row)">{{scoped.row.name}}</el-button>
          <span v-else>{{scoped.row.name}}</span>
        </template>
       </el-table-column>
      <el-table-column label="模型/类型" prop="typeName" align="center">
        <template slot-scope="scoped">
          <span v-if="!scoped.row.nodeId">
            <span v-if="scoped.row.deployMode === 'single'">单点模式</span>
            <span v-if="scoped.row.deployMode === 'sentinel'">哨兵模式</span>
            <span v-if="scoped.row.deployMode === 'sentinel_worker'">哨兵主从</span>
            <span v-if="scoped.row.deployMode === 'cluster'">集群模式</span>
            <span v-if="scoped.row.deployMode === 'center'">中心服务</span>
            <span v-if="scoped.row.deployMode === 'scalable'">可伸缩集群</span>
          </span>
          <span v-else>
            <span></span>
            {{scoped.row.nodeType}}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="内存(已用/总量）" align="center">
        <template slot-scope="scoped">
          <span>
            {{ ((scoped.row.memoryUsed/1024)/1024).toFixed(0) + 'MB'}} /
            {{ ((scoped.row.memoryTotal/1024)/1024).toFixed(0) + 'MB'}}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="当前连接数" prop="currentConnections" align="center"></el-table-column>
      <el-table-column label="key总数" prop="currentKeys" align="center"></el-table-column>
      <el-table-column label="状态" prop="status" align="center">
        <template slot-scope="scoped">
        <el-tag type="success" v-if="scoped.row.status === 'start'">运行</el-tag>
        <el-tag type="warning" v-if="scoped.row.status === 'start-part'">部分运行</el-tag>
        <el-tag type="danger" v-if="scoped.row.status === 'stop'">停止</el-tag>
        </template>

      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import {listServiceStat, getServiceNodes} from '@/api/console/rdsmonitor'
export default {
  name: "rdsSupervisor",
  data() {
    return {
      queryParams: {
        serviceName: null,
        serviceStatus: null
      },
      tableData: [],
    }
  },
  created() {
    this.getList()
  },
  methods: {
    toReadableSize(size){
      let SIZE_KB  = 1024
      let SIZE_MB = 1048576
      let SIZE_GB = 1073741824
      let SIZE_TB = 1099511627776

    },
    async getList(){
      let data = await listServiceStat(this.queryParams)
      if(data.code === 200 ){
        this.tableData = data.data
      }
    },
    handleQuery() {
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    gotoDeatil(row){
      const detailId = row.serviceId;
      //this.$router.push("/monitor/rdssupervisorycontrol/detail/rdssupervisorycontrol/" + detailId);
      this.$router.push("/monitor/rdssupervisorycontrol/detail/rdssupervisorycontrolnew/" + detailId);//优化版
    }

  }
}
</script>
