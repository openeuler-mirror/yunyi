<template>
  <div class="app-container rdssupervisorycontrolDeatil">
    <div class="pageHeader">
      <div style="width: 30%;">
        <el-page-header @back="goBack"></el-page-header>
      </div>

      <div>
        <b>监控时间范围：</b>
        <!-- <el-radio-group v-model="data.params.datetime" size="mini" @change="eventDatetimeChange">
          <el-radio-button v-for="(item, index) in datetimes" :label="item.value" :key="item.value" :index="index">{{
            item.label }}</el-radio-button>
        </el-radio-group> -->
        <el-date-picker v-model="createSecondDateTimes" type="datetimerange" range-separator="至"
          start-placeholder="开始时间" end-placeholder="结束时间" @change="eventDatetimeChange">
        </el-date-picker>
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
          <node-monitor-dashboard :ref="item.elementId" :echart="item"
            :chartWidth="chartWidth"></node-monitor-dashboard>
        </div>
      </div>
    </el-card>

    <el-card class="uhry-card-wrapper" shadow="never" v-for="(item, index) in echartsclusterData" :key="index">
      <div slot="header">
        <span class="uhry-card-title">{{ item.name }}</span>
      </div>
      <div class="centerbox" v-loading="loading">
        <div v-for="(item, index) in item.echarts" :key="item.id">
          <node-monitor-dashboard :ref="item.elementId" :echart="item"
            :chartWidth="chartWidth"></node-monitor-dashboard>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import nodeMonitorDashboard from './components/nodeMonitorDashboard'
import { getServiceNodes } from '@/api/console/rdsmonitor'
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
      echarts: [],
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
      echartsTitles: ['连接数', '内存使用量(M)', '内存总量(M)', 'key总数', '实际占用内存总量(M)', '最大可用内存量(M)', '网络IO每秒入流量', '网络IO每秒出流量', '当前程序CPU使用率', '当前系统CPU使用率'],
      createSecondDateTimes: [new Date(new Date().getTime() - 10 * 60 * 1000), new Date()]
    }
  },
  mounted() {
    for (let i = 0; i < this.echartsTitles.length; i++) {
      this.echarts.push({
        elementId: i+1,
        title: this.echartsTitles[i],
        data: [],
        categorys: []
      })
    }
    this.getNodeList()
    this.actionInitChartWidth()
  },
  beforeDestroy() {
    this.timer && clearInterval(this.timer)
  },
  methods: {
    actionInitChartWidth: function () {
      this.chartWidth = parseInt((window.innerWidth - 232 - 120) / 4)
    },
    async getNodeList() {
      this.timer && clearInterval(this.timer) // 首先进入清除定时器
      this.loading = true
      let beginCreateSecond = Math.floor(this.createSecondDateTimes[0].getTime() / 1000)
      let endCreateSecond = Math.floor(this.createSecondDateTimes[1].getTime() / 1000)
      let data = await getServiceNodes({
        serviceId: this.serviceId,
        //pastSecond: this.data.params.datetime
        beginCreateSecond: beginCreateSecond,
        endCreateSecond: endCreateSecond,
        pastSecond: endCreateSecond - beginCreateSecond
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
        let data = [[], [], [], [], [], [], [], [], [], []];
        item.stats.forEach((k, v) => {
          time.push(this.actionFormatTimestamp(k.createSecond))
          data[0].push(k.currentConnections)
          data[1].push(k.memoryUsed / 1024 / 1024)
          data[2].push((k.memoryUsed + k.memoryFree) / 1024 / 1024)
          data[3].push(k.currentKeys)
          data[4].push(k.memoryTotal / 1024 / 1024)
          data[5].push(k.memoryAvailable / 1024 / 1024)
          data[6].push(k.inputPerSecond)
          data[7].push(k.outputPerSecond)
          data[8].push(k.cpuProcessLoad)
          data[9].push(k.cpuSystemLoad)
        })
        for (let i = 0; i < this.echartsTitles.length; i++) {
          this.echarts[i].data = data[i];
          this.echarts[i].categorys = time;
        }
      })
    },
    clusterEcharts(data) {
      this.echartsclusterData = []
      this.isshow = false
      this.data.server.workNodeNum = data.nodes.length
      data.nodes.forEach((item, index) => {
        let time = []
        let data = [[], [], [], [], [], [], [], [], [], []];
        item.stats.forEach((k, v) => {
          time.push(this.actionFormatTimestamp(k.createSecond))
          data[0].push(k.currentConnections)
          data[1].push(k.memoryUsed / 1024 / 1024)
          data[2].push((k.memoryUsed + k.memoryFree) / 1024 / 1024)
          data[3].push(k.currentKeys)
          data[4].push(k.memoryTotal / 1024 / 1024)
          data[5].push(k.memoryAvailable / 1024 / 1024)
          data[6].push(k.inputPerSecond)
          data[7].push(k.outputPerSecond)
          data[8].push(k.cpuProcessLoad)
          data[9].push(k.cpuSystemLoad)
        })
        let thisEcharts = [];
        for (let i = 0; i < this.echartsTitles.length; i++) {
          thisEcharts.push({ elementId: this.uuid(), title: this.echartsTitles[i], categorys: time, data: data[i] })
        }
        this.echartsclusterData.push({
          name: item.instance,
          echarts: thisEcharts
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
    flex-wrap: wrap;
    width: 100%;

    >div {
      width: 25%;
      margin-top: 10px;
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
