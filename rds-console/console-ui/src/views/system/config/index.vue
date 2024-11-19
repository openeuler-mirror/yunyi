<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="68px">
      <el-form-item :label="$t('system.config.configName')" prop="configName">
        <el-input
          v-model="queryParams.configName"
          :placeholder="$t('system.config.placeholder.configName')"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('system.config.configKey')" prop="configKey">
        <el-input
          v-model="queryParams.configKey"
          :placeholder="$t('system.config.placeholder.configKey')"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('system.config.configType')" prop="configType">
        <el-select v-model="queryParams.configType" :placeholder="$t('system.config.placeholder.configType')" clearable>
          <el-option
            v-for="dict in dict.type.sys_yes_no"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('system.config.dateRange')">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          :start-placeholder="$t('system.config.placeholder.startDate')"
          :end-placeholder="$t('system.config.placeholder.endDate')"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">{{ $t('search') }}</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">{{ $t('reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:config:add']"
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >{{ $t('add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:config:edit']"
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
        >{{ $t('edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:config:remove']"
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
        >{{ $t('delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:config:export']"
          type="warning"
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >{{ $t('export') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:config:remove']"
          type="danger"
          icon="el-icon-refresh"
          size="mini"
          @click="handleRefreshCache"
        >{{ $t('refreshCache') }}</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('system.config.configId')" align="center" prop="configId" />
      <el-table-column :label="$t('system.config.configName')" align="center" prop="configName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('system.config.configKey')" align="center" prop="configKey" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('system.config.configValue')" align="center" prop="configValue" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('system.config.configType')" align="center" prop="configType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_yes_no" :value="scope.row.configType" />
        </template>
      </el-table-column>
      <el-table-column :label="$t('system.config.remark')" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('system.config.dateRange')" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('actions')" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['system:config:edit']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >{{ $t('edit') }}</el-button>
          <el-button
            v-hasPermi="['system:config:remove']"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >{{ $t('delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改参数配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('system.config.configName')" prop="configName">
          <el-input v-model="form.configName" :placeholder="$t('system.config.placeholder.configName')" />
        </el-form-item>
        <el-form-item :label="$t('system.config.configKey')" prop="configKey">
          <el-input v-model="form.configKey" :placeholder="$t('system.config.placeholder.configKey')" />
        </el-form-item>
        <el-form-item :label="$t('system.config.configValue')" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :placeholder="$t('system.config.placeholder.configValue')" />
        </el-form-item>
        <el-form-item :label="$t('system.config.configType')" prop="configType">
          <el-radio-group v-model="form.configType">
            <el-radio
              v-for="dict in dict.type.sys_yes_no"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('system.config.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :placeholder="$t('system.config.placeholder.remark')" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('dialog.confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('dialog.cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listConfig, getConfig, delConfig, addConfig, updateConfig, refreshCache } from '@/api/system/config'

export default {
  name: 'Config',
  dicts: ['sys_yes_no'],
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
      // 总条数
      total: 0,
      // 参数表格数据
      configList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        configName: undefined,
        configKey: undefined,
        configType: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        configName: [
          { required: true, message: this.$t('system.rules.configName'), trigger: 'blur' }
        ],
        configKey: [
          { required: true, message: this.$t('system.rules.configKey'), trigger: 'blur' }
        ],
        configValue: [
          { required: true, message: this.$t('system.rules.configValue'), trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询参数列表 */
    getList() {
      this.loading = true
      listConfig(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.configList = response.rows
        this.total = response.total
        this.loading = false
      }
      )
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        configId: undefined,
        configName: undefined,
        configKey: undefined,
        configValue: undefined,
        configType: 'Y',
        remark: undefined
      }
      this.resetForm('form')
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = this.$t('system.config.addParameters')
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const configId = row.configId || this.ids
      getConfig(configId).then(response => {
        this.form = response.data
        this.open = true
        this.title = this.$t('system.config.modifyParameters')
      })
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.configId !== undefined) {
            updateConfig(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('dialog.editSuccess'))
              this.open = false
              this.getList()
            })
          } else {
            addConfig(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('dialog.addSuccess'))
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const configIds = row.configId || this.ids
      this.$modal.confirm(this.$t('dialog.confirmDelete', { msg: configIds })).then(function() {
        return delConfig(configIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess(this.$t('dialog.deleteSuccess'))
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/config/export', {
        ...this.queryParams
      }, `config_${new Date().getTime()}.xlsx`)
    },
    /** 刷新缓存按钮操作 */
    handleRefreshCache() {
      refreshCache().then(() => {
        this.$modal.msgSuccess(this.$t('dialog.refreshSuccess'))
      })
    }
  }
}
</script>
