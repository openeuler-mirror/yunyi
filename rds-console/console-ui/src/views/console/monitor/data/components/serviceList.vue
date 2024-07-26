<template>
  <div>
    <el-dialog title="服务列表" :visible.sync="visible" :before-close="handleClose">
      <div class="body">

        <div class="body_content">
          <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="68px">
            <el-form-item label="服务名称" prop="serviceName">
              <el-input v-model="queryParams.serviceName" placeholder="请输入服务名称" clearable
                @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item label="部署模式" prop="deployMode">
              <el-select v-model="queryParams.deployMode" placeholder="请选择部署模式" clearable>
                <el-option v-for="dict in dict.type.cnsl_deploy_mode" :key="dict.value" :label="dict.label"
                  :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            </el-form-item>
          </el-form>
          <el-table ref="table" v-loading="loading" :highlight-current-row="true" :data="serviceData"
            @selection-change="handleCurrentChange" @row-click="handleRowClick">
            <el-table-column type="selection" align="center"></el-table-column>
            <el-table-column label="服务名称" align="center" prop="serviceName" />
            <el-table-column label="版本" align="center" prop="versionDesc" width="120" />
            <el-table-column label="配置模版" align="center" prop="groupName" width="100" />
            <el-table-column label="部署模式" align="center" prop="deployMode" width="80">
              <template slot-scope="scope">
                <dict-tag :options="dict.type.cnsl_deploy_mode" :value="scope.row.deployMode" />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleClose" size="small">取消</el-button>
        <el-button @click="save" type="primary" size="small">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { listServiceWithNodes } from "@/api/console/rdsservice";
export default {
  dicts: ['cnsl_deploy_env', 'cnsl_node_type', 'cnsl_node_status', 'cnsl_deploy_mode'],
  data() {
    return {
      loading: false,
      currentRow: {},
      visible: true,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serviceName: null,
        deployMode: null
      },
      serviceData: [
      ]
    }
  },
  mounted() {
    this.getServiceData()
  },
  methods: {
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getServiceData();
    },
    async getServiceData() {
      this.loading = true;
      let data = await listServiceWithNodes(this.queryParams)
      this.loading = false;
      if (data.code === 200) {
        this.serviceData = data.rows;
      }

    },
    handleRowClick(row) {
      let table = this.$refs.table;
      if (table.selection.length >= 1 && table.selection[0].id !== row.id) {
        table.clearSelection(row);
        table.toggleRowSelection(row);
      } else {
        table.toggleRowSelection(row);
      }
    },
    handleCurrentChange(val) {
      this.currentRow = val
      this.serviceData.forEach(item => {
        if (val[val.length - 1] === item) {
          this.$refs.table.toggleRowSelection(item, true)
        } else {
          this.$refs.table.toggleRowSelection(item, false)
        }
      })
    },
    async save() {
      if (this.currentRow.length > 0) {
        this.$emit('chooseService', this.currentRow)
      } else {
        this.$message.error('请选择服务')
      }

    },
    handleClose() {
      this.$emit('close')
    }
  }
}

</script>
<style lang="scss" scoped>
</style>
