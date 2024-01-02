<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="服务名称" prop="serviceName">
        <el-input
          v-model="queryParams.serviceName"
          placeholder="请输入服务名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8"  v-if="deployEnv=='host'">
      <el-col :span="1.5">
        <el-button type="primary"  icon="el-icon-plus" size="mini" @click="handleNew" v-hasPermi="['console:sentinelservice:add']">新增</el-button>
        </el-col>
        <el-col :span="1.5">
        <el-button type="danger"  icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['console:sentinelservice:remove']">删除</el-button>
        </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="rdsserviceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="服务名称" align="center" prop="serviceName" />
      <el-table-column label="版本" align="center" prop="versionDesc" width="120" />
      <el-table-column label="认证方式" align="center" prop="secureMode" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.cnsl_rds_secure_mode" :value="scope.row.secureMode"/>
        </template>
      </el-table-column>
      <el-table-column label="服务引用" align="center" prop="workerServices" width="100">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleViewWorkers(scope.row)" v-if="scope.row.workerServices"
                     v-hasPermi="['console:sentinelservice:query']">主从服务({{scope.row.workerServices}})</el-button>
          <span v-if="!scope.row.workerServices">无</span>
        </template>
      </el-table-column>
      <el-table-column label="节点[状态]" align="left" prop="nodes" width="230" >
        <template slot-scope="scope">
          <div v-for="(node, index) in scope.row.nodes">
          {{getNodeNameDesc(node)}} <span :style="getNodeStatusColor(node)">{{getNodeStatusLabel(node)}}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220" >
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-if="!scope.row.manualAdmin"
                     v-hasPermi="['console:sentinelservice:query']">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-if="scope.row.manualAdmin"
            v-hasPermi="['console:sentinelservice:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handlePasswordUpdate(scope.row)" v-if="!scope.row.manualAdmin && scope.row.secureMode == 2"
                     v-hasPermi="['console:sentinelservice:edit']">修改密码</el-button>
          <el-button size="mini" type="text" icon="el-icon-caret-right" @click="handleStart(scope.row)" v-if="scope.row.manualAdmin"
            v-hasPermi="['console:sentinelservice:edit']">启动</el-button>
          <el-button size="mini" type="text" icon="el-icon-video-pause" @click="handleStop(scope.row)" v-if="scope.row.manualAdmin"
            v-hasPermi="['console:sentinelservice:edit']">停止</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh-right" @click="handleRestart(scope.row)" v-if="scope.row.manualAdmin"
            v-hasPermi="['console:sentinelservice:edit']">重启</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete"@click="handleDelete(scope.row)" v-if="scope.row.manualAdmin"
            v-hasPermi="['console:sentinelservice:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <service-edit ref="sentinelService" deployMode="sentinel"  @transfer="handleServiceEditConfirm"> </service-edit>
    <service-password-edit ref="servicePasswordEdit" @transfer="handleServiceEditConfirm"> </service-password-edit>
    <service-view ref="sentinelServiceView" deployMode="sentinel"> </service-view>

    <el-dialog :title="worker.title" :visible.sync="worker.open" width="800px" append-to-body>
      <el-form :model="worker.queryParams" ref="queryForm" size="small" :inline="true" v-show="true" label-width="68px">
        <el-form-item label="服务名称" prop="serviceName">
          <el-input
            v-model="worker.queryParams.serviceName"
            placeholder="请输入服务名称"
            clearable
            @keyup.enter.native="handleWorkerQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleWorkerQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetWorkerQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="worker.loading" :data="worker.serviceList">
        <el-table-column label="服务名称" align="center" prop="serviceName" />
        <el-table-column label="版本" align="center" prop="versionDesc" width="120" />
        <el-table-column label="认证方式" align="center" prop="secureMode" width="100">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.cnsl_rds_secure_mode" :value="scope.row.secureMode"/>
          </template>
        </el-table-column>
        <el-table-column label="节点[状态]" align="left" prop="nodes" width="230" >
          <template slot-scope="scope">
            <div v-for="(node, index) in scope.row.nodes">
              {{getNodeNameDesc(node)}} <span :style="getNodeStatusColor(node)">{{getNodeStatusLabel(node)}}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="worker.total>0" :total="worker.total" :page.sync="worker.queryParams.pageNum" :limit.sync="worker.queryParams.pageSize"
                  @pagination="getWorkerList"/>
    </el-dialog>

  </div>
</template>

<script>
import { listServiceWithNodes, delService, startService, stopService, restartService } from "@/api/console/rdsservice";
import serviceEdit from "../rdsservice/edit/serviceEdit";
import servicePasswordEdit from "../rdsservice/edit/servicePasswordEdit";
import serviceView from "../rdsservice/view/serviceView";
import { getAppConfigKey } from '@/api/system/config'

export default {
  name: "SentinelService",
  dicts: [ 'cnsl_node_type', 'cnsl_node_status', 'cnsl_deploy_mode', 'cnsl_rds_secure_mode'],
  components: { serviceEdit, servicePasswordEdit, serviceView },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      //部署模式（从后台取得配置）
      deployEnv: null,
      // 总条数
      total: 0,
      // RDS服务表格数据
      rdsserviceList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serviceName: null,
        deployMode: 'sentinel'
      },
      /** 查询主从服务列表数据 */
      worker: {
        open: false,
        title: '主从服务列表-使用哨兵服务 ',
        loading:true,
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          serviceName: null,
          sentinelServiceId: null,
          deployMode: 'sentinel_worker'
        },
        serviceList: [],
        total: 0,
      }

    };
  },
  created() {
    this.getList();

    getAppConfigKey('console.deployEnv').then((res) => {
      this.deployEnv = res.data;
    });
  },
  methods: {
    /** 查询RDS服务列表 */
    getList() {
      this.loading = true;
      listServiceWithNodes(this.queryParams).then(response => {
        this.rdsserviceList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.serviceId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    handleNew(command) {
      this.$refs.sentinelService.show(null);
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.$refs.sentinelServiceView.show(row);
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$refs.sentinelService.show(row);
    },
    /** 修改密码按钮操作 */
    handlePasswordUpdate(row) {
      this.$refs.servicePasswordEdit.show(row);
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const serviceIds = row.serviceId || this.ids;

      this.$confirm('是否确认删除选中的哨兵服务?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        .then(async () => {
          this.loading = true;
          let response = await delService(serviceIds);
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

    },
    handleStart(row) {
      const serviceId = row.serviceId;
      this.loading = true;
      startService(serviceId).then(response => {
        this.$modal.msgSuccess("启动成功");
        this.getList();
      });
    },
    handleStop(row) {
      const serviceId = row.serviceId;
      this.loading = true;
      stopService(serviceId).then(response => {
        this.$modal.msgSuccess("停止成功");
        this.getList();
      });
    },
    handleRestart(row) {
      const serviceId = row.serviceId;
      this.loading = true;
      restartService(serviceId).then(response => {
        this.$modal.msgSuccess("重启成功");
        this.getList();
      });
    },

    handleServiceEditConfirm(serivceData) {
      this.getList();
    },

    getNodeNameDesc(node) {
      let typeLabel;
      this.dict.type.cnsl_node_type.forEach(dict => {
        if(dict.value === node.nodeType) {
          typeLabel = dict.label;
        }
      });
      return typeLabel + '-' + node.nodeName;
    },
    getNodeStatusLabel(node) {
      let statusLabel;
      this.dict.type.cnsl_node_status.forEach(dict => {
        if(dict.value === node.nodeStatus) {
          statusLabel = dict.label;
        }
      });
      return '[' + statusLabel + ']';
    },
    getNodeStatusColor(node) {
      if(node.nodeStatus === 'start')
        return  'color:green;';
      else if(node.nodeStatus === 'starting')
        return  'color:darkgray;';
      else if(node.nodeStatus === 'stop')
        return  'color:red;';
      else if(node.nodeStatus === 'stopping')
        return  'color:coral;';
      else
        return  'color:black;';
    },
    getWorkerList()  {
      this.worker.loading = true;
      listServiceWithNodes(this.worker.queryParams).then(response => {
        this.worker.serviceList = response.rows;
        this.worker.total = response.total;
        this.worker.loading = false;
      });
    },
    /** 查看使用的主从服务列表 */
    handleViewWorkers(row) {
      this.worker.open = true;
      this.worker.queryParams.sentinelServiceId = row.serviceId;
      this.handleWorkerQuery();
    },
    /** 主从服务列表搜索按钮操作 */
    handleWorkerQuery() {
      this.worker.queryParams.pageNum = 1;
      this.getWorkerList();
    },
    /** 主从服务列表重置按钮操作 */
    resetWorkerQuery() {
      this.resetForm("queryWorkerForm");
      this.handleWorkerQuery();
    }
  }
};
</script>
