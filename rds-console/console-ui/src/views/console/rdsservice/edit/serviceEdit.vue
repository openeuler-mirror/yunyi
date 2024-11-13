<template>
  <div class="app-container">
    <!-- 添加或修改RDS服务对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1000px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="服务名称" prop="serviceName">
              <el-input v-model="form.serviceName" placeholder="请输入服务名称" :disabled="form.serviceId != null" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="软件版本" prop="versionId">
              <el-select v-model="form.versionId" placeholder="请选择版本" @change="onVersionChange" :disabled="form.serviceId != null" >
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
              <el-select v-model="form.groupId" placeholder="请选择配置模版" :disabled="form.serviceId != null">
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
        <el-row>
          <el-col :span="24">
            <el-form-item label="工作节点认证" prop="secureMode">
              <el-checkbox v-model="form.secureMode" @change="onSecureModeChange"
              :false-label="0" :true-label="2">开启密码认证</el-checkbox>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" show-password placeholder="请输入密码" :disabled="form.secureMode != 2"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码确认" prop="passwordConfirm">
              <el-input v-model="form.passwordConfirm" show-password placeholder="请再次录入相同密码" :disabled="form.secureMode != 2"/>
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
        <el-row v-if="form.deployMode === 'sentinel_worker'">
          <el-col :span="24">
            <el-form-item label="哨兵服务" prop="sentinelServiceId" >
              <el-select v-model="form.sentinelServiceId" placeholder="请选择哨兵服务"
                         no-data-text="不存在哨兵服务，请先创建！" :disabled="form.serviceId != null" @change="onSentinelServiceChange">
                <el-option v-for="item in sentinelServices"
                  :key="item.serviceId"  :label="item.serviceName"
                  :value="item.serviceId"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>

      <sentinel-node-list ref="sentinelNodeViewList" :serviceId="form.sentinelServiceId" :viewOnly="true" v-if="deployMode === 'sentinel_worker'"></sentinel-node-list>

      <single-node-list ref="singleNodeList" :serviceId="form.serviceId" v-if="deployMode === 'single'"></single-node-list>
      <cluster-node-list ref="clusterNodeList" :serviceId="form.serviceId" v-if="deployMode === 'cluster'"></cluster-node-list>
      <sentinel-node-list ref="sentinelNodeList" :serviceId="form.serviceId" v-if="deployMode === 'sentinel'"></sentinel-node-list>
      <sentinel-worker-node-list ref="sentinelWorkerNodeList" :serviceId="form.serviceId" v-if="deployMode === 'sentinel_worker'"></sentinel-worker-node-list>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :disabled="loading"  @click="onConfirmForm">确 定</el-button>
        <el-button icon="el-icon-refresh" :disabled="loading" @click="onReloadForm" v-if="form.serviceId > 0">还 原</el-button>
        <el-button icon="el-icon-refresh" :disabled="loading" @click="onResendConfig" v-if="form.serviceId > 0">更新配置</el-button>
        <el-button :disabled="loading" @click="onCancel">取 消</el-button>
      </div>

    </el-dialog>
    <el-dialog title="确认提交" :visible.sync="saveConfirm.open" width="450px">
        <div>请确认是否要保存当前的信息</div>
        <div  v-if="saveConfirm.createNodes.length > 0">新建了节点：{{saveConfirm.createNodes}}; </div>
        <div  v-if="saveConfirm.deleteNodes.length > 0">删除了节点：{{saveConfirm.deleteNodes}}; </div>
        <div  v-if="saveConfirm.changeNodes.length > 0">修改变更了节点：{{saveConfirm.changeNodes}}; </div>
        <div v-if="saveConfirm.reloadNodes.length > 0">需要重启以生效变更的节点：{{saveConfirm.reloadNodes}};</div>
        <div v-if="saveConfirm.reloadNodes.length > 0 && deployMode === 'sentinel_worker'">使用的哨兵服务{{sentinelServiceName}}的各节点也需要重启后才能生效！</div>

        <div slot="footer" class="dialog-footer">
          <el-button v-if="saveConfirm.reloadNodes.length > 0"  @click="onSaveConfirmSubmit(false)">保存不重启</el-button>
          <el-button v-if="saveConfirm.reloadNodes.length === 0" type="primary" @click="onSaveConfirmSubmit(false)">保 存</el-button>
          <el-button v-if="saveConfirm.reloadNodes.length > 0" icon="el-icon-refresh" type="primary" @click="onSaveConfirmSubmit(true)">保存&重启</el-button>
          <el-button @click="onSaveConfirmCancel">取 消</el-button>
        </div>
    </el-dialog>

  </div>
</template>

<script>
import { getServiceWithNodes, updateServiceWithNodes, addServiceWithNodes, existsServiceName, resendService, listService } from "@/api/console/rdsservice";
import { listVersionByStatus } from "@/api/console/rdsversion";
import { validServiceName, validatePassword, getDiffProps} from '@/utils/validate';
import singleNodeList from "../nodelist/singleNodeList.vue";
import clusterNodeList from "../nodelist/clusterNodeList.vue";
import sentinelNodeList from "../nodelist/sentinelNodeList.vue";
import sentinelWorkerNodeList from "../nodelist/sentinelWorkerNodeList.vue";
import util from "@/utils/aesutils";

export default {
  name: "ServiceEdit",
  components: { singleNodeList, clusterNodeList, sentinelNodeList, sentinelWorkerNodeList },
  props: {
    deployMode: {// single|sentinel|sentinel_worker|cluster|scalable
      type: [String]
    },
  },
  dicts: ['cnsl_deploy_env', 'cnsl_node_type', 'cnsl_node_status', 'cnsl_rds_secure_mode', 'cnsl_deploy_mode'],
  data() {
    const validatePassword2 = (rule, value, callback) => {
      //启用密码时需做校验
      if (this.form.secureMode == 2) {
        validatePassword(rule, value, callback);
      } else { //未启用密码则不做校验
        callback();
      }
    };

    const equalToPassword = (rule, value, callback) => {
      if (this.form.password != value) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };

    return {
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 加载标志
      loading: false,
      // 所有启用的版本列表
      versionOptions: [],
      // 模版组列表（选择版本对应的）
      groupOptions: [],
      // 如果部署模式是 'sentinel_worker' 时，需要提供sentinelServices的选择框数据
      sentinelServices: [],
      // 识别Service变更属性时，需要跳过的属性
      serviceSkipProps: ['searchValue', 'createBy', 'createTime', 'updateBy', 'updateTime', 'params', '__ob__'],
      // 需要变更的属性
      nodeLoadProps: ['hostAddress', 'servicePort', 'redisPort'],
      // 默认版本ID
      defaultVersionId: null,
      // 默认模版ID
      defaultGroupId: null,
      //哨兵服务名称，内部回显使用
      sentinelServiceName: '',
      // 表单参数
      form: {},
      // 保留从服务端取来的原始数据
      formOrg: {},
      // 表单校验
      rules: {
        password: [
          { validator: validatePassword2, trigger: 'blur' }
        ],
        passwordConfirm: [
          { validator: equalToPassword, trigger: 'blur' }
        ],
        versionId: [
          { required: true, message: "版本不能为空", trigger: "change" }
        ],
        groupId: [
          { required: true, message: "配置模版不能为空", trigger: "change" }
        ],
        sentinelServiceId: [
          { required: true, message: "哨兵服务不能为空", trigger: "change" }
        ],
      },

      saveConfirm: {
        open: false,
        reloadNodes: [],
        deleteNodeIds: [],
        deleteNodes: [],
        changeNodes: [],
        createNodes: [],
      },

      //////////////////////
      // 工作节点列表相关变量
      //////////////////////
      // 工作节点列表数据
      nodes: [],
      // 保留原工作节点数据
      nodesOrg: [],

    };
  },
  created() {
    this.getOptions();
  },
  methods: {
    validExistsServiceName(rule, value, callback) {
      if(!this.form.serviceId) {
        existsServiceName(value).then((res) => {
          if (res.data) {
            callback(new Error('服务名不能和其它服务的名字相同'));
          } else {
            callback();
          }
        })
      }
      else {
        callback();
      }
    },
    show(serv) {
      if(serv) {
        this.rules.serviceName = [];
        this.reset(false);

        this.open = true;
        this.getServiceInfo(serv.serviceId); //获取Service数据和关联的Nodes数据
        if(this.deployMode === 'sentinel_worker' && serv.sentinelServiceId) {
          this.getSentinelNodeViewList(serv.sentinelServiceId); //获取哨兵节点列表
        }

        this.title = '修改' + this.getSubTitle();
      }
      else {
        this.rules.serviceName = [
          { required: true, message: "服务名称不能为空", trigger: "blur" },
          { validator: validServiceName, trigger: 'blur' },
          { validator: this.validExistsServiceName, trigger: 'blur' },
        ];

        this.reset();
        this.title = '添加' + this.getSubTitle();
        this.open = true;

        if(this.deployMode === 'sentinel_worker') {
          this.$refs.sentinelNodeViewList.initNodeList([], []);
        }

        this.$nextTick(() => {
          this.getNodeList().initNodeList(this.nodes, this.nodesOrg);
        });
      }

      this.getOptions();
    },
    getSubTitle() {
      const t = this.deployMode;
      switch(t) {
        case 'single': return '单节点服务';
        case 'sentinel': return '哨兵服务';
        case 'sentinel_worker': return '哨兵主从服务';
        case 'cluster': return '集群模式服务';
        case 'scalable': return '可伸缩集群模式服务';
        default : return '未知模式服务！！！';
      }
    },
    getOptions() {
      //get all available version options
      listVersionByStatus('1').then(response => {
        this.versionOptions = response.data;
        let defaultVersion = this.versionOptions.find(v => v.defaultVersion);
        if(defaultVersion) {
          this.defaultVersionId = defaultVersion.versionId;
        }
      });

      //加载 sentinel service 列表
      listService({deployMode: "sentinel"}).then(response => {
        this.sentinelServices = response.data;
      });
    },
    getNodeList() {
      if(this.deployMode === 'single')
        return this.$refs.singleNodeList;
      else if(this.deployMode === 'cluster')
        return this.$refs.clusterNodeList;
      else if(this.deployMode === 'sentinel')
        return this.$refs.sentinelNodeList;
      else if(this.deployMode === 'sentinel_worker')
        return this.$refs.sentinelWorkerNodeList;
      else
        return null;
    },
    getServiceInfo(serviceId) { //获得服务信息，并加载相关信息
      this.loading = true;
      //this.$refs.singleNodeList.setLoading();
      getServiceWithNodes(serviceId).then(response => {
        response.data.service.passwordConfirm = '';
        let passwd = util.decrypt(response.data.service.password);
        response.data.service.password = new String(passwd);

        this.form = response.data.service;
        this.formOrg = JSON.parse(JSON.stringify(response.data.service));
        if(this.form.secureMode == 2) {
          this.form.passwordConfirm = new String(this.form.password);
        }
        this.getGroupOptions(this.form.versionId);

        this.nodes = response.data.nodes;
        this.nodesOrg = JSON.parse(JSON.stringify(response.data.nodes));

        this.$nextTick(() => {
          this.getNodeList().initNodeList(this.nodes, this.nodesOrg);
        });


        this.loading = false;
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
    onSentinelServiceChange(serviceId) {
      if(serviceId) {
        this.getSentinelNodeViewList(serviceId);
      }
    },
    getSentinelNodeViewList(serviceId) {
      //加载sentinelNodeList
      getServiceWithNodes(serviceId).then(response => {
        this.$nextTick(() => {
          let passwd = util.decrypt(response.data.service.password);
          response.data.service.password = new String(passwd);
          this.$refs.sentinelNodeViewList.initNodeList(response.data.nodes, []);
        });
      });
    },
    getGroupOptions(versionId) {
    },

    // 表单重置
    reset(resetForm = true) {
      this.form = {
        serviceId: null,
        sentinelServiceId: null,
        serviceName: null,
        deployMode: this.deployMode,
        secureMode: 0,
        versionId: this.defaultVersionId,
        groupId: null,
        password: '',
        passwordConfirm: '',
        remark: null
      };

      this.sentinelServiceName = ''; //内部回显使用
      this.nodes = [];
      this.nodesOrg = [];

      if(this.defaultVersionId) {
        this.onVersionChange(this.defaultVersionId);
      }

      if(resetForm) {
        this.resetForm("form");
      }
    },
    resetSaveConfirm() {
      this.saveConfirm = {
        open: false,
        reloadNodes: [],
        deleteNodeIds: [],
        deleteNodes: [],
        changeNodes: [],
        createNodes: [],
      };
    },
    /** Service重置按钮 */
    onReloadForm() {
      this.$confirm('当前的修改内容将被重置还原, 请确认是否继续！')
      .then(() => {
        const serviceId = this.form.serviceId;
        this.show(serviceId);
      }).catch(() => {
        this.form.password = this.formOrg.password;
        this.form.passwordConfirm = this.formOrg.password;
      });
    },
    onResendConfig() {
      const serviceId = this.form.serviceId;
      this.$confirm('接下来将重新对服务各节点上的配置进行下发更新，请确认是否继续?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        .then(async () => {
          this.loading = true;
          let response = await resendService(serviceId);
          if (response.code < 400) {
            this.$modal.msgSuccess("更新成功");
            this.getList();
          }
          else {
            this.$modal.msgError("更新时出现异常", response.msg);
          }
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    /** Service提交按钮 */
    onConfirmForm() {
      this.resetSaveConfirm();
      this.$refs["form"].validate(valid => {
        if (valid && this.getNodeList().validate()) {
          //如果是不设置密码模式，清空密码值
          if(this.form.secureMode != 2) {
            this.form.password = '';
            this.form.passwordConfirm = '';
          }

          // 更新nodes
          this.nodes = this.getNodeList().getNodeList();

          ////进行校验判断，判断出修改、要重启和要删除的nodes信息(nodeName 和 serviceName 创建后不能被修改)
          if(this.form.serviceId) {

            //Service被改变的属性
            this.form.changedProps = getDiffProps(this.formOrg, this.form, this.serviceSkipProps);

            this.nodesOrg.forEach(nodeOrg => {
              let node = this.nodes.find(n => n.nodeId == nodeOrg.nodeId);
              if(node) {
                if(node.changedProps && node.changedProps.length > 0) {
                  this.saveConfirm.changeNodes.push(node.nodeName);
                  //查找是否有变更属性存在于this.nodeLoadProps中
                  let reloadIdx = node.changedProps.findIndex(val =>
                     this.nodeLoadProps.includes(val)
                  );

                  if(reloadIdx >= 0) {
                    this.saveConfirm.reloadNodes.push(node.nodeName);
                  }
                }
              }
              else {
                this.saveConfirm.deleteNodeIds.push(nodeOrg.nodeId);
                this.saveConfirm.deleteNodes.push(nodeOrg.nodeName);
              }
            });


            //设置sentinelServiceName 用于显示
            if(this.deployMode === 'sentinel_worker' && this.form.sentinelServiceId && this.saveConfirm.reloadNodes.length > 0) {
              let sentinelServ = this.sentinelServices.find(serv => {return serv.serviceId == this.form.sentinelServiceId });
              this.sentinelServiceName = '(' + sentinelServ.serviceName + ')';
            }

          }

          //加入新创建的节点名称列表
          this.nodes.forEach(node => {
            if(node.nodeId == null && node.nodeName != null) {
              this.saveConfirm.createNodes.push(node.nodeName);
            }
          });



          //打开确认对话框
          this.saveConfirm.open = true;
        }
      });
    },

    // 取消按钮
    onCancel() {
      this.open = false;
      this.reset();
    },

    onSaveConfirmCancel() {
      this.saveConfirm.open = false;
    },

    onSaveConfirmSubmit(reloadable) { //reloadable 表示是否重启必要的节点
      this.saveConfirm.open = false;
      let serviceData = JSON.parse(JSON.stringify(this.form));
      serviceData.password = util.encrypt(this.form.password);
      serviceData.passwordConfirm = undefined;

      let data = {
        service: serviceData,
        nodes: this.nodes,
        deleteNodeIds: this.saveConfirm.deleteNodeIds,
        reloadNodes: this.saveConfirm.reloadNodes,
        reloadable: reloadable,
      }

      this.loading = true;
      this.getNodeList().setLoading(true);
      if(this.form.serviceId) {
        updateServiceWithNodes(data).then(response => {
          this.$modal.msgSuccess("修改成功");
          this.open = false;
          this.$emit('transfer', this.form);
          this.loading = false;
          this.getNodeList().setLoading(false);
        });
      }
      else {
        addServiceWithNodes(data).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.loading = false;
          this.getNodeList().setLoading(false);
          this.$emit('transfer', this.form);
        });
      }

    },
  }
};
</script>
