<template>
  <div class="app-container">
    <!-- 添加或修改RDS服务对话框 -->
    <el-dialog title="修改服务密码" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="服务名称" prop="serviceName">
              <span>{{form.serviceName}}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署模式" prop="deployMode">
              <dict-tag :options="dict.type.cnsl_deploy_mode" :value="form.deployMode"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="软件版本" prop="versionDesc">
              <span>{{form.versionDesc }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置模版" prop="groupName">
              <span>{{form.groupName }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="工作节点认证" prop="secureMode">
              <span v-if="form.secureMode==2">已开启密码认证</span><span v-if="form.secureMode==0">未开启密码认证</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" show-password placeholder="请输入密码"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码确认" prop="passwordConfirm">
              <el-input v-model="form.passwordConfirm" show-password placeholder="请再次录入相同密码"/>
            </el-form-item>
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
        <el-button type="primary" :disabled="loading"  @click="onConfirmForm">确 定</el-button>
        <el-button :disabled="loading" @click="onCancel">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { getService, updateServicePassword } from "@/api/console/rdsservice";
import { validatePassword } from '@/utils/validate';
import util from '@/utils/aesutils.js';

export default {
  name: "ServicePasswordEdit",
  dicts: ['cnsl_deploy_mode', 'cnsl_rds_secure_mode'],
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.form.password != value) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };

    return {
      // 是否显示弹出层
      open: false,
      // 加载标志
      loading: false,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        password: [
          { required: true, message: "密码不能为空", trigger: "blur" },
        ],
        passwordConfirm: [
          { required: true, validator: equalToPassword, trigger: 'blur' }
        ],
      },
    };
  },
  created() { },
  methods: {
    show(serv) {
      this.reset();
      this.open = true;
      getService(serv.serviceId).then(response => {
        //需要在this.form对象被赋值前添加passwordConfirm，否则会出现绑定紊乱的情况
        let passwd = util.decrypt(response.data.password);
        response.data.password = new String(passwd);
        response.data.passwordConfirm = new String(passwd);
        this.form = response.data;
      });
    },

    // 表单重置
    reset() {
      this.form = {
        serviceId: null,
        serviceName: null,
        deployEnv: 'host',
        deployMode: this.deployMode,
        secureMode: 0,
        versionId: this.defaultVersionId,
        groupId: null,
        password: '',
        passwordConfirm: '',
        remark: null
      };

      this.resetForm("form");
    },

    /** Service提交按钮 */
    onConfirmForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {

          let postData = JSON.parse(JSON.stringify(this.form));
          postData.password = util.encrypt(this.form.password);
          postData.passwordConfirm = undefined;

          this.loading = true;
          updateServicePassword(postData).then(response => {
            this.$modal.msgSuccess("密码修改成功");
            this.open = false;
            this.$emit('transfer', this.form);
            this.loading = false;
          });

        }
      });
    },

    // 取消按钮
    onCancel() {
      this.open = false;
      this.reset();
    },
  }
};
</script>
