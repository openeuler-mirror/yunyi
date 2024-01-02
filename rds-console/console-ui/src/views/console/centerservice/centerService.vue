<template>
    <!-- 添加或修改RDS服务对话框 -->
    <el-dialog title="修改中心服务" :visible.sync="open" width="920px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="服务名称" prop="serviceName">
              <el-input v-model="form.serviceName" placeholder="请输入服务名称" :disabled="disableSettings" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署环境" prop="deployEnv">
              <dict-tag :options="dict.type.cnsl_deploy_env" :value="deployEnv"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row  v-if="deployEnv=='host'">
          <el-col :span="12">
            <el-form-item label="软件版本" prop="versionId">
              <el-select v-model="form.versionId" placeholder="请选择版本" @change="onVersionChange"  :disabled="disableSettings"  >
                <el-option
                  v-for="item in versionOptions"
                  :key="item.versionId"
                  :label="item.versionDesc"
                  :value="item.versionId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置模版" prop="groupId">
              <el-select v-model="form.groupId" placeholder="请选择配置模版" :disabled="disableSettings" >
                <el-option
                  v-for="item in groupOptions"
                  :key="item.groupId"
                  :label="item.groupName"
                  :value="item.groupId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="deployEnv=='k8s'">
          <el-col :span="12">
            <el-form-item label="服务地址" prop="hostAddress">
              <el-input v-model="form.hostAddress" placeholder="请输入服务地址"  :disabled="disableSettings" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="管理端口" prop="adminPort">
              <el-input v-model.number="form.adminPort" type="number" maxlength="5" placeholder="请输入管理端口"  :disabled="disableSettings" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-button size="small" type="primary" @click="handleTestConnection" :loading="false">测试连接</el-button>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

</template>

<script>
import {existsServiceName} from "@/api/console/rdsservice";
import {updateService, getService, testAdminConnection, testAdminConnectionNew} from "@/api/console/centerservice";
import { getAppConfigKey } from '@/api/system/config'
import { listVersionByStatus } from "@/api/console/rdsversion";
import { validServiceName } from '@/utils/validate';

export default {
  name: "CenterServiceEdit",
  dicts: ['cnsl_deploy_env', 'cnsl_node_type', 'cnsl_node_status', 'cnsl_rds_secure_mode', 'cnsl_deploy_mode'],
  props: {
    disableSettings: {
      type: [Boolean]
    }
  },
  data() {
    const validExistsServiceName = (rule, value, callback) => {
      if(value.trim() != this.formOrg.serviceName) {
        existsServiceName(value).then((res) => {
          if (res.data) {
            callback(new Error('服务名不能和其它服务的名字相同!'));
          } else {
            callback();
          }
        })
      }
      else {
        callback();
      }
    };

    return {
      // 是否显示弹出层
      open: false,
      // 下属节点的数量
      nodeCount: 0,
      // 所有启用的版本列表
      versionOptions: [],
      // 模版组列表（选择版本对应的）
      groupOptions: [],
      // 默认版本ID
      defaultVersionId: null,
      // 默认模版ID
      defaultGroupId: null,
      //部署模式（从后台取得配置）
      deployEnv: null,
      // 表单参数
      form: {},
      // 保留获取数据的原始值
      formOrg: {},
      // 表单校验
      rules: {
        serviceName: [
          { required: true, message: "服务名称不能为空", trigger: "blur" },
          { required: true, validator: validServiceName, trigger: 'blur' },
          { required: true, validator: validExistsServiceName, trigger: 'blur' },
        ],
        hostAddress: [
          { required: true, message: "服务地址不能为空", trigger: "blur" }
        ],
        adminPort: [
          { required: true, type: 'number', message: '管理端口不能为空', trigger: "blur" }
        ],
        versionId: [
          { required: true, message: "版本不能为空", trigger: "change" }
        ],
        groupId: [
          { required: true, message: "配置模版不能为空", trigger: "change" }
        ],
      }
    };
  },
  created() {
    this.getOptions();

    getAppConfigKey('console.deployEnv').then((res) => {
      this.deployEnv = res.data;
    });
  },
  methods: {
    show(serviceId) {
      this.$nextTick(() => {
        if(serviceId != null) {
          this.reset();
          getService().then(response => {
            response.data.passwordConfirm=''
            this.form = response.data;
            this.formOrg = JSON.parse(JSON.stringify(response.data));
            this.form.passwordConfirm = response.data.password;
            this.getGroupOptions(this.form.versionId);
            if(!this.form.adminPort) this.form.adminPort = 8086;
            this.open = true;
          });
          //弹出窗口
          this.visible = true;
        }
      });
    },
    getOptions() {
      //get all avalible version options
      listVersionByStatus('1').then(response => {
          this.versionOptions = response.data;
          let defaultVersion = this.versionOptions.find(v => v.defaultVersion);
          if(defaultVersion) {
            this.defaultVersionId = defaultVersion.versionId;
          }
        });
    },
    onVersionChange(versionId) {
      let selVersion = this.versionOptions.find(v => v.versionId == versionId);
      if(selVersion.defaultGroupId) {
         this.form.groupId = selVersion.defaultGroupId;
      }
      else {
        this.form.groupId = null;
      }
      this.getGroupOptions(versionId);
    },
    onSecureModeChange(val) {
      this.$refs['form'].clearValidate(['password', 'passwordConfirm']);
    },
    getGroupOptions(versionId) {

    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        serviceId: null,
        serviceName: null,
        hostAddress: null,
        adminPort: null,
        deployMode: 'single',
        secureMode: 0,
        versionId: this.defaultVersionId,
        groupId: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      };
      this.resetForm("form");
      if(this.defaultVersionId) {
        this.onVersionChange(this.defaultVersionId);
      }

    },

    handleTestConnection() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.$confirm('将测试中心服务的管理端口是否正常，请确认？', '提示', { confirmButtonText: '测试', cancelButtonText: '取消', type: 'info' })
            .then(async () => {
              this.loading = true;
              let response = await testAdminConnectionNew(this.form);
              this.loading = false;
              if (response.code < 400) {
                this.$modal.msgSuccess(response.msg);
              }
            })
            .catch(() => {
              this.loading = false;
            });
        }
      });
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          updateService(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.$emit('transfer', this.form);
          });
        }
      });
    }
  }
};
</script>
