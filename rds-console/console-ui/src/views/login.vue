<template>
  <div class="login">
    <div v-if="this.data.status">
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
    
    <h3 class="title"><img src="../assets/logo/logo-cn-270x54.png" alt=""> 管理控制台</h3>
    <el-form-item prop="username">
      <el-input
        v-model="loginForm.username"
        type="text"
        auto-complete="off"
        placeholder="账号"
      >
        <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
      </el-input>
    </el-form-item>
    <el-form-item prop="password">
      <el-input
        v-model="loginForm.password"
        type="password"
        auto-complete="off"
        placeholder="密码"
        @keyup.enter.native="handleLogin"
      >
        <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
      </el-input>
    </el-form-item>
    <el-form-item prop="code" v-if="captchaEnabled">
      <el-input
        v-model="loginForm.code"
        auto-complete="off"
        placeholder="验证码"
        style="width: 63%"
        @keyup.enter.native="handleLogin"
      >
        <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
      </el-input>
      <div class="login-code">
        <img :src="codeUrl" @click="getCode" class="login-code-img"/>
      </div>
    </el-form-item>
    <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
    <el-form-item style="width:100%;">
      <el-button
        :loading="loading"
        size="medium"
       
        style="width:100%;"
        @click.native.prevent="handleLogin"
      >
        <span v-if="!loading">登 录</span>
        <span v-else>登 录 中...</span>
      </el-button>
      <div style="float: right;" v-if="register">
        <router-link class="link-type" :to="'/register'">立即注册</router-link>
      </div>
    </el-form-item>
  </el-form>
    </div>
    <div v-else>
      <!--安装引导流程-开始-->
      <el-dialog width="1000px" class="uhry-dialog uhry-dialog-guide uhry-dialog-start" :visible.sync="dialog.guideStart.status"
        :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
        <el-row class="uhry-align-center uhry-block-title">
          <el-col :span="24">
            <span>欢迎使用{{consoleConfig.name}}</span>
          </el-col>
        </el-row>
        <el-row class="uhry-align-center uhry-block-version">
          <el-col :span="24"> 控制台版本：{{consoleConfig.version}} </el-col>
        </el-row>
        <div slot="footer" class="uhry-dialog-footer">
          <el-button class="uhry-btn-start-confirm" type="primary" @click="eventGuideStartConfirm">开始安装配置</el-button>
        </div>
      </el-dialog>
      <!--安装引导流程-服务器配置-->
      <el-dialog width="1000px" class="uhry-dialog uhry-dialog-guide uhry-dialog-host" title="" :visible.sync="dialog.guideHost.status"
        :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
        <div slot="title" class="uhry-dialog-head">
          <el-row>
            <el-col :span="22" class="uhry-dialog-title uhry-align-center">管理控制台安装配置 — 第一步 服务地址配置</el-col>
          </el-row>
        </div>
        <el-form ref="formGuideHost" :model="consoleConfig" label-width="170px" :rules="data.rules">
          <el-form-item label="控制台服务地址：" prop="servHost">
            <el-input v-model="consoleConfig.servHost" placeholder="请输入主机地址"></el-input>
            <p class="uhry-element-remark">
              被管理的节点会通过该"主机名/IP"来和控制台进行通信，一般设置本机的IP地址即可，注意要保证被管理的节点可以通过该地址访问到该主机。
            </p>
          </el-form-item>
          <el-form-item label="控制台服务端口：" prop="servPort">
            <el-input v-model="consoleConfig.servPort" placeholder="请输入控制台服务端口"></el-input>
            <p class="uhry-element-remark">提供各节点的控制信息和监控信息上传</p>
          </el-form-item>
          <el-form-item label="中心节点服务端口：" prop="centerPort">
            <el-input v-model="consoleConfig.centerPort" placeholder="请输入中心节点服务端口"></el-input>
            <p class="uhry-element-remark">
              提供各节点获取授权和获取配置所用端口
            </p>
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button type="primary" @click="eventGuideHostConfirm">下一步</el-button>
        </div>
      </el-dialog>
      <!--安装引导流程-License上传-->
      <el-dialog width="1000px" class="uhry-dialog uhry-dialog-guide uhry-dialog-license" :visible.sync="dialog.guideLicense.status"
        :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
        <div slot="title" class="uhry-dialog-head">
          <el-row>
            <el-col :span="22" class="uhry-dialog-title uhry-align-center">管理控制台安装配置 — 第二步 授权信息上传</el-col>
          </el-row>
        </div>
        <div v-if="data.licenseInfo.expireDate">
          <el-row class="uhry-block-license-info">
            <el-col :span="24">
              <div class="uhry-block-head">
                <h3 class="uhry-block-title">授权信息</h3>
              </div>
              <div class="uhry-block-main">
                <p><span>基本信息：</span>{{ data.licenseInfo.product }}</p>
                <p><span>证书类型：</span>{{ data.licenseInfo.licenseTypeDesc }}</p>
                <p><span>授权用户：</span>{{ data.licenseInfo.userName }}</p>
                <p><span>有效期至：</span>{{ data.licenseInfo.expireDate }}</p>
                <p><span>可用最大内存：</span>{{ data.licenseInfo.totalMemory }}</p>
              </div>
            </el-col>
          </el-row>
        </div>
        <div v-else>
          <el-alert title="您当前还未上传文件,请选择文件上传！" type="warning" show-icon :closable="false">
          </el-alert>
        </div>
        <el-form :model="data.formGuide">
          <el-form-item label="">
            <el-upload style="margin-top: 30px" class="uhry-block-upload" ref="licenseUpload"
              :action="licenseUpload.url" :on-change="handleChange" :file-list="data.licenseInfo.files"
              :on-success="handleFileSuccess" :on-error="handleFileError">
              <el-button type="primary" slot="trigger" size="small" class="uhry-btn-choice-file">上传授权文件</el-button>
              <!-- <el-button size="small" type="primary" @click="eventUploadLicense">上传</el-button> -->
            </el-upload>
          </el-form-item>
        </el-form>
        <div style="position: absolute; bottom: 10px; right: 10px" slot="footer">
          <el-button type="info" @click="eventGuideLicensePrev">上一步</el-button>
          <el-button type="primary" @click="eventGuideLicenseConfirm">下一步</el-button>
        </div>
      </el-dialog>
      <!--安装引导流程-修改密码-->
      <el-dialog width="1000px" class="uhry-dialog uhry-dialog-guide uhry-dialog-account" :visible.sync="dialog.guideAccount.status"
        :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false">
        <div slot="title" class="uhry-dialog-head">
          <el-row>
            <el-col :span="22" class="uhry-dialog-title uhry-align-center">管理控制台安装配置 — 第三步 设置管理用户密码</el-col>
          </el-row>
        </div>
        <el-form ref="formGuideAccount" :model="data.formGuide" label-width="120px" :rules="data.rules">
          <el-form-item label="管理员用户名：">
            <el-input v-model="data.formGuide.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="密码：" prop="password">
            <el-input type="password" v-model="data.formGuide.password" placeholder="请填写密码" :show-password="true">
            </el-input>
          </el-form-item>
          <el-form-item label="确认密码：" prop="confirmPassword">
            <el-input type="password" v-model="data.formGuide.confirmPassword" placeholder="请填写确认密码"
              :show-password="true"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button type="info" @click="eventGuideAccountPrev">上一步</el-button>
          <el-button type="primary" @click="eventGuideAccountConfirm" :loading="load.installConfigSubmit.status">完成
          </el-button>
        </div>
      </el-dialog>
    </div>
   
    <!--  底部  -->
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import {getConfig} from "@/api/console/home";
import {getCenterLic1Install} from "@/api/login";
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    const validateHost = (rule, value, callback) => {
      if (value.length === 0) {
        callback(new Error("请输入主机地址"));
      } else if (/[^\.\dA-Za-z_-]{1,}/.test(value)) {
        callback(
          new Error(
            "主机地址仅限输入阿拉伯数字／大小写英文字母／下划线／中划线以及."
          )
        );
      } else if (/[\.\dA-Za-z_-]{3,30}/.test(value) === false) {
        callback(new Error("主机地址仅限长度为3到30个字符"));
      } else {
        callback();
      }
    };
    const validatePassword = (rule, value, callback) => {
      if (value.length === 0) {
        callback(new Error("请输入密码"));
      } else if (value.length < 8 || value.length > 15) {
        callback(new Error("密码仅限长度为8到15位"));
      } else if (
        this.data.formGuide.confirmPassword !== undefined &&
        this.data.formGuide.password !== this.data.formGuide.confirmPassword
      ) {
        callback(new Error("两次输入密码不一致"))
      } else {
        callback();
      }
    };
    const validateConfirmPassword = (rule, value, callback) => {
      if (value.length === 0) {
        callback(new Error("请输入确认密码"));
      } else if (value.length < 8 || value.length > 15) {
        callback(new Error("密码仅限长度为8到15位"));
      } else {
        this.$refs.formGuideAccount.validateField("password");
        callback();
      }
    };
    
    
    return {
      consoleConfig:{},
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      load: {
        //安装配置提交
        installConfigSubmit: {
          status: false,
        },
      },
      licenseUpload: {
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/warrant/importCenterLicInstall",
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关
      captchaEnabled: true,
      // 注册开关
      register: false,
      redirect: undefined,
      dialog: {
        guideStart: {
          status: false,
        },
        guideHost: {
          status: false,
        },
        guideLicense: {
          status: false,
        },
        guideAccount: {
          status: false,
        },
      },
      data: {
        status: true,
        //安装配置表单
        formGuide: {
          username: "admin",
          servHost: undefined,
          password: undefined,
          confirmPassword: undefined,
          license: undefined,
        },
        formGuidePwd: {
          userName: "admin",
          password: "",
          confirmPassword: "",
        },
        //授权信息
        licenseInfo: {
          product: '',
          licenseTypeDesc: '',
          userName: '',
          expireDate: '',
          totalMemory: '',
          files: [],
        },
        //表单校验规则
        rules: {
          servHost: [
            { required: true, message: "请填写主机名称", trigger: "blur" },
            { validator: validateHost, trigger: "blur" },
          ],
          servPort: [
            { required: true, message: "请填写控制台端口", trigger: "blur" },
            { validator: validateHost, trigger: "blur" },
          ],
          centerPort: [
            { required: true, message: "请填写中心节点端口", trigger: "blur" },
            { validator: validateHost, trigger: "blur" },
          ],
          password: [
            { required: true, message: "请填写密码", trigger: "blur" },
            { validator: validatePassword, trigger: "blur" },
          ],
          confirmPassword: [
            { required: true, message: "请填写确认密码", trigger: "blur" },
            { validator: validateConfirmPassword, trigger: "blur" },
          ],
        },
      },
    };
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.getCode();
          this.getCookie();
    // getConfig().then((res) => {
		// this.consoleConfig = res.data;
    //     if (this.consoleConfig.initialized) {
    //       this.data.status = true;
         
    //     } else {
		//   this.data.status = false;
		//   this.consoleConfig.servHost = null; //控台服务地址清空
		//   this.dialog.guideStart.status = true;
    //     }
    //   });
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled;
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.loginForm.uuid = res.uuid;
        }
      });
    },
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            sessionStorage.removeItem('serciveObj')
            sessionStorage.removeItem('commandObj')
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
            if (this.captchaEnabled) {
              this.getCode();
            }
          });
        }
      });
    },
    eventGuideStartConfirm() {
      this.dialog.guideStart.status = false;
      this.dialog.guideHost.status = true;
    },
    eventGuideHostConfirm() {
      let validSuccessNum = 0;
      let validFailNum = 0;
      let fields = ["servHost", "servPort", "centerPort"];
      this.$refs.formGuideHost.validateField(fields, (errorMessage) => {
        errorMessage.length === 0 ? validSuccessNum++ : validFailNum++;
        //所有字段全部校验通过则提交数据
        if (
          validFailNum + validSuccessNum === fields.length &&
          validFailNum === 0
        ) {
          this.dialog.guideHost.status = false;
          this.dialog.guideLicense.status = true;
        }
      });
    },
    eventUploadLicense() {
      this.$refs.licenseUpload.submit();
    },
     // 文件上传成功处理
     handleFileSuccess(response, file) {
      this.data.licenseInfo.files = ''
      if (response.code !== 200) {
        file.status = 'error'
        this.$message({
          type: "error",
          message: response.msg,
        });
      } else {
        this.msgSuccess("上传成功！");
        getCenterLic1Install().then((res) => {
          this.data.licenseInfo.product = res.data.product;
          this.data.licenseInfo.licenseTypeDesc = res.data.licenseTypeDesc;
          this.data.licenseInfo.userName = res.data.userName;
          this.data.licenseInfo.expireDate = res.data.expireDate;
          this.data.licenseInfo.totalMemory = res.data.totalMemory;
        });
      }
    },
    handleChange(file, fileList) {
      this.data.licenseInfo.files = fileList.slice(-1);
    },
     handleFileError(response) {
      this.$message.error("上传失败, 请重试");
    },
    eventGuideLicensePrev() {
      this.dialog.guideLicense.status = false;
      this.dialog.guideHost.status = true;
    },
    eventGuideLicenseConfirm() {
      if (
        this.data.licenseInfo.expireDate === undefined ||
        this.data.licenseInfo.expireDate.length === 0
      ) {
        this.$message.error("请上传License授权文件");
      }
      else {
        this.dialog.guideLicense.status = false;
        this.dialog.guideAccount.status = true;
      }
    },
    eventGuideAccountPrev() {
      this.dialog.guideAccount.status = false;
      this.dialog.guideLicense.status = true;
    },
    eventGuideAccountConfirm(){

    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
  display: flex;
  align-items: center;
justify-content: center;
  img{
    width: 120px;
  }
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  border-top:4px solid #DF2525;
  .el-input {
    height: 38px;
    input {
      height: 38px;
    }
  }
  button{
    background: #DF2525;
    color:white
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 38px;
}
</style>
