<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="服务名称" prop="serviceName">
        <el-input
          v-model="queryParams.serviceName"
          placeholder="请输入服务名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
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
      <!-- <el-form-item label="部署模式" prop="deployMode">
        <el-select v-model="queryParams.deployMode" placeholder="请选择部署模式" clearable>
          <el-option
            v-for="dict in dict.type.cnsl_deploy_mode"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item> -->
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="table-operator">
      <right-toolbar :search="false" @queryTable="getList" />
    </el-row>

    <el-table
      :data="tableData" style="width: 100%;margin-bottom: 20px;" row-key="name"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      :header-cell-style="{background: '#f3f5f6',}"
    >
      <el-table-column label="名称" prop="name">
        <template slot-scope="scope">
          <el-button v-if="!scope.row.nodeId" size="mini" type="text" @click="openDetail(scope.row)">{{ scope.row.name }}</el-button>
          <span v-else>{{ scope.row.name }}</span>
          <!-- <el-button v-else size="mini" type="text" @click="handleOpenNode(scope.row)">{{ scope.row.name }}</el-button> -->
        </template>
      </el-table-column>
      <!-- <el-table-column label="模型/类型" prop="typeName" align="center">
        <template slot-scope="scope">
          <dict-tag v-if="!scope.row.nodeId" :options="dict.type.cnsl_deploy_mode" :value="scope.row.deployMode" />
          <span v-if="scope.row.nodeId != null">
            {{ scope.row.nodeType }}
          </span>
        </template>
      </el-table-column> -->
      <el-table-column label="内存(已用/总量）" align="center">
        <template slot-scope="scope">
          <span>
            {{ ((scope.row.memoryUsed/1024)/1024).toFixed(0) + 'MB' }} /
            {{ ((scope.row.memoryTotal/1024)/1024).toFixed(0) + 'MB' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="当前连接数" prop="currentConnections" align="center" />
      <el-table-column label="key总数" prop="currentKeys" align="center" />
      <el-table-column label="状态" prop="status" align="center">
        <template slot-scope="scoped">
          <el-tag type="success" v-if="scoped.row.status === 'start'">运行</el-tag>
          <el-tag type="warning" v-if="scoped.row.status === 'start-part'">部分运行</el-tag>
          <el-tag type="danger" v-if="scoped.row.status === 'stop'">停止</el-tag>
        </template>

      </el-table-column>
      <!-- <el-table-column label="状态" prop="status" align="center">
        <template slot-scope="scope">
          <dict-tag v-if="scope.row.nodeId" :options="dict.type.cnsl_node_status" :value="scope.row.status" />
          <dict-tag v-else :options="dict.type.cnsl_rds_service_status" :value="scope.row.status" />
        </template>
      </el-table-column> -->
    </el-table>
  </div>
</template>

<script>
import { listServiceStat } from '@/api/console/rdsmonitor'
export default {
  // dicts: ['cnsl_deploy_mode', 'cnsl_rds_service_status', 'cnsl_node_status'],
  data() {
    return {
      queryParams: {
        serviceName: null,
        deployMode: null
      },
      tableData: []
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      listServiceStat(this.queryParams).then(res => {
        this.tableData = res.data
      })
    },
    handleQuery() {
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    openDetail(row) {
      const detailId = row.serviceId
      this.$router.push("/monitor/rdssupervisorycontrol/detail/rdssupervisorycontrol/" + detailId)
      // this.$router.push('/console/serviceMonitoring/serverDetail/' + row.serviceId)
    },
    handleOpenNode(row) {
      this.$router.push('/console/serviceMonitoring/nodeDetail/' + row.nodeId)
    }
  }
}
</script>
