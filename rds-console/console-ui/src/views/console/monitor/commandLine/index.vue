<template>
  <div class="commandLine app-container">
    <div class="command" v-loading="loading">
      <div class="select">
        <span>当前连接:{{ currentConnetValue }}</span>
        <el-button type="primary" icon="el-icon-time" size="small" @click="serviceShow = true">服务列表</el-button>
        <serviceList v-if="serviceShow" @close="Logclose" @chooseService="chooseService" />
      </div>
      <div class="commad_header">
        <div v-for="(item, index) in dataList" :key="index">
          <div>{{ item.name }}</div>
          <div>{{ item.key }}</div>
        </div>
      </div>
      <div class="commad_footer">
        <el-input @keyup.enter.native="handkeyCode" v-model="inputVal" placeholder="输入Redis命令后 按Enter键执行" />
      </div>
    </div>
  </div>
</template>

<script>
import { listServiceWithNodes } from "@/api/console/rdsservice";
import { rdsexecCommand, getRdsServiceList } from "@/api/console/data/index.js";
import serviceList from "../data/components/serviceList.vue";
import states from "./redis.js";
export default {
  components: {
    serviceList,
  },
  name: "commandLine",
  data() {
    return {
      loading: false,
      list: [],
      options: [],
      inputVal: "",
      serviceShow: false,
      dataList: [],
      currentConnetValue: '',
      serviceData: [],
      serviceId: "",
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serviceName: null,
        deployMode: null,
        deployEnv: null,
      },
    };
  },
  created() {


  },
  mounted() {
    if (sessionStorage.getItem('commandObj')) {
      let val = JSON.parse(sessionStorage.getItem('commandObj'))
      this.getVal(val)
    }
    this.list = states.map((item) => {
      return { value: item, label: item };
    });
  },
  methods: {
    async handkeyCode() {
      if(!this.inputVal){
        return
      }
      if (!this.loading) {
        let data = await rdsexecCommand({serviceId: this.serviceId,command: this.inputVal});
        if (data.code === 200) {
          this.$message.success("交互成功");
          this.dataList.push({
            key: data.data,
            name: this.inputVal,
          });
          this.inputVal = "";
        }
      }

    },
    Logclose() {
      this.serviceShow = false;
    },
    chooseService(row) {
      sessionStorage.setItem('commandObj', JSON.stringify(row))
      this.getVal(row)
    },
    getVal(row) {
      this.currentConnetValue = row[0].serviceName
      this.serviceId = row[0].serviceId;
      this.serviceShow = false;
      this.dataList = [];
      this.inputVal = "";
    },


  }
}
</script>

<style lang="scss" scoped>
.command {
  width: 90%;

  position: relative;

  .select {
    width: 400px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0px 5px;
    font-size: 15px;
    color: black;

  }

  .commad_header {
    height: 400px;
    width: 100%;
    background: #e5e8f0;
    position: absolute;
    left: 50px;
    top: 70px;
    overflow: scroll;
    color: #635f5f;
    border-radius: 15px;

    div {
      padding: 5px 10px;
    }
  }

  .commad_footer {
    width: 100%;
    height: 40px;
    // background:#eee ;
    position: absolute;
    left: 45px;
    top: 474px;
    border-radius: 15px;

    .el-select {
      width: 100%;
    }
  }

  .t-container {
    width: 75% !important;
    height: 85px !important;
    z-index: 100;
    left: 270px !important;
    top: 544px !important;
  }
}
</style>