<template>
  <div class="app-container rdssupervisorycontrolDeatil">
    <div class="pageHeader">
      <div style="width: 30%;">
        <el-page-header @back="goBack"></el-page-header>
      </div>

      <div>
        <b>监控时间范围：</b>
        <el-radio-group v-model="data.params.datetime" size="mini" @change="eventDatetimeChange">
          <el-radio-button v-for="(item, index) in datetimes" :label="item.value" :key="item.value" :index="index">{{
            item.label }}</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    <el-divider></el-divider>
    <el-card class="uhry-card-wrapper" shadow="never">
      <div slot="header">
        <span class="uhry-card-title">服务基本信息</span>
      </div>
      <div class="uhry-card-body">
        <div class="uhry-server-info">
          <el-row :gutter="5" class="uhry-card-row" style="background: #DCDFE6">
            <el-col :span="8" class="uhry-layout-algin-r"><span class="uhry-font-blue">服务名称</span></el-col>
            <el-col :span="4">{{ data.server.name }}</el-col>
            <el-col :span="4" class="uhry-layout-algin-r"><span class="uhry-font-blue">服务模式</span></el-col>
            <el-col :span="8">
              <span v-if="data.server.modeName === 'single'">单点模式</span>
              <span v-if="data.server.modeName === 'sentinel'">哨兵模式</span>
              <span v-if="data.server.modeName === 'cluster'">集群模式</span>
              <span v-if="data.server.modeName === 'center'">中心模式</span>
              <span v-if="data.server.modeName === 'scalable'">可伸缩集群模式</span>
            </el-col>
          </el-row>
          <el-row :gutter="5" class="uhry-card-row" style="background: #E4E7ED">
            <el-col :span="8" class="uhry-layout-algin-r"><span class="uhry-font-blue">服务状态</span></el-col>
            <el-col :span="4">
              <span v-if="data.server.statusName === 'stop'">停止</span>
              <span v-if="data.server.statusName === 'start'">运行</span>
            </el-col>
            <el-col :span="4" class="uhry-layout-algin-r"><span class="uhry-font-blue">节点数量</span></el-col>
            <el-col :span="8">
              <span v-if="data.server.workNodeNum > 0">节点{{ data.server.workNodeNum }}个；</span>
              <span v-if="data.server.sentinelNodeNum > 0">哨兵节点{{ data.server.sentinelNodeNum }}个</span>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
    <el-card class="uhry-card-wrapper" shadow="never" v-if="isshow">
      <div slot="header">
        <span class="uhry-card-title">服务监控</span>
      </div>
      <div class="centerbox">
        <div v-for="(item, index) in echarts" :key="item.id">
          <node-monitor-dashboard :ref="item.elementId" :echart="item" :chartWidth="chartWidth"></node-monitor-dashboard>
        </div>
      </div>
    </el-card>

    <el-card class="uhry-card-wrapper" shadow="never" v-for="(item, index) in echartsclusterData" :key="index">
      <div slot="header">
        <span class="uhry-card-title">{{ item.name }}</span>
      </div>
      <div class="centerbox" v-loading="loading">
        <div v-for="(item, index) in item.echarts" :key="item.id">
          <node-monitor-dashboard :ref="item.elementId" :echart="item" :chartWidth="chartWidth"></node-monitor-dashboard>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import nodeMonitorDashboard from './components/nodeMonitorDashboard'
import {getServiceNodes} from '@/api/console/rdsmonitor'
export default {
  components: {
    nodeMonitorDashboard
  },
  data() {
    return {
      timer: null,
      loading: false,
      serviceId: this.$route.params.rdssupervisorycontrolId,
      chartWidth: 250,
      isshow: true,
      echarts: [
        {
          elementId: 1,
          title: '内存使用（MB）-最大内存',
          data: [],
          categorys: []
        },
        {
          elementId: 2,
          title: '客户端连接数-最大可用',
          data: [],
          categorys: []
        },
        {
          elementId: 3,
          title: '请求数/秒',
          data: [],
          categorys: []
        },
        {
          elementId: 4,
          title: '当前key',
          data: [],
          categorys: []
        }
      ],
      echartsclusterData: [],
      data: {
        server: {
          name: "",
          modeName: "",
          statusName: "",
          workNodeNum: 1,
          sentinelNodeNum: 0
        },
        params: {
          datetime: 600
        }
      },
      datetimes: [
        {
          label: '10分钟',
          value: 600
        },
        {
          label: '30分钟',
          value: 1800
        },
        {
          label: '1小时',
          value: 3600
        },
        {
          label: '2小时',
          value: 7200
        },

      ],
    }
  },
  mounted() {
    this.getNodeList()
    this.actionInitChartWidth()
  },
  beforeDestroy() {
    console.log('清空定时器 beforeDestroy')
    this.timer && clearInterval(this.timer)
  },
  methods: {
    actionInitChartWidth: function () {
      this.chartWidth = parseInt((window.innerWidth - 232 - 120) / 4)
    },
    async getNodeList() {
      this.timer && clearInterval(this.timer) // 首先进入清除定时器
      this.loading = true
      let data = await getServiceNodes({
        serviceId: this.serviceId,
        pastSecond: this.data.params.datetime
      })
      this.loading = false
      if (data.code === 200) {
        this.data.server.name = data.data.name
        this.data.server.modeName = data.data.deployMode
        this.data.server.statusName = data.data.status
        this.setVal()
        this.formateDataEcharts(data.data)

      }
    },
    formateDataEcharts(data) {

      this.clusterEcharts(data)

    },
    centerEcharts(data) {
      data.nodes.forEach((item, index) => {
        let time = []
        let echartsdata = []
        let memoryAvailableData = []
        let throughputAverage60Data = []
        let currentkeyData = []
        item.stats.forEach((k, v) => {
          time.push(this.actionFormatTimestamp(k.createSecond))
          echartsdata.push(k.memoryTotal / 1024 / 1024)
          memoryAvailableData.push(k.memoryAvailable)
          throughputAverage60Data.push(k.throughputAverage60)
          currentkeyData.push(k.currentKeys)
          this.echarts[0].elementId = 1
          this.echarts[0].data = echartsdata
          this.echarts[0].categorys = time
          this.echarts[1].elementId = 2
          this.echarts[1].data = memoryAvailableData
          this.echarts[1].categorys = time
          this.echarts[2].elementId = 3
          this.echarts[2].data = throughputAverage60Data
          this.echarts[2].categorys = time
          this.echarts[3].elementId = 4
          this.echarts[3].data = currentkeyData
          this.echarts[3].categorys = time
        })
      })
    },
    clusterEcharts(data) {

      this.echartsclusterData = []
      this.isshow = false
      this.data.server.workNodeNum = data.nodes.length
      data.nodes.forEach((item, index) => {
        let time = []
        let memoryData = []
        let connectionData = []
        let processSecondData = []
        let currentKeyData = []
        item.stats.forEach((k, v) => {
          time.push(this.actionFormatTimestamp(k.createSecond))
          memoryData.push(k.memoryTotal / 1024 / 1024)
          connectionData.push(k.currentConnections)
          processSecondData.push(k.throughputAverage60)
          currentKeyData.push(k.currentKeys)
        })
        this.echartsclusterData.push({
          name: item.instance,
          echarts: [
            { elementId: this.uuid(), title: '内存使用（MB）-最大内存', categorys: time, data: memoryData },
            { elementId: this.uuid(), title: '客户端连接数-最大可用', categorys: time, data: connectionData },
            { elementId: this.uuid(), title: '请求数/秒', categorys: time, data: processSecondData },
            { elementId: this.uuid(), title: '当前key', categorys: time, data: currentKeyData }
          ]
        })

      })

      // this.echartsclusterData = data.nodes
    },
    setVal() {
      this.timer = setInterval(() => this.getNodeList(), 30000)
    },
    uuid() {
      var s = [];
      var hexDigits = "0123456789abcdef";
      for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
      }
      s[14] = "4";
      s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);
      s[8] = s[13] = s[18] = s[23] = "-";

      var uuid = s.join("");
      return uuid;
    },
    actionFormatTimestamp(timestamp) {
      let date = new Date(timestamp * 1000)
      return date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds()
    },
    goBack() {
      this.$router.go(-1)

    },
    eventDatetimeChange() {
      this.$nextTick(() => {
        this.getNodeList()
      })

    }
  }
}
</script>

<style lang="scss" scoped>
.rdssupervisorycontrolDeatil {
  .centerbox {
    display: flex;
    width: 100%;

    >div {
      width: 25%;
      padding: 0px 5px;
    }
  }

  .pageHeader {
    width: 100%;
    display: flex;
  }

  .uhry-card-wrapper {
    margin: 10px;
    border: 0;

    ::v-deep .el-card__header {
      padding: 14px 0 7px;
      border-bottom: solid 1px #409EFF;
    }

    .uhry-card-title {
      line-height: 40px;
      font-size: 20px;
      color: #409EFF;
    }

    ::v-deep .el-card__body {
      padding: 15px 0 20px 0;
    }

    .uhry-card-body {

      .uhry-server-info {
        line-height: 30px;
      }
    }
  }
}
</style>
