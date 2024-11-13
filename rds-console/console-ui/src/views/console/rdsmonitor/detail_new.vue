<template>
  <div class="app-container rdssupervisorycontrolDeatil">
    <div class="pageHeader">
      <div style="width: 30%;">
        <el-page-header @back="goBack"></el-page-header>
      </div>

      <div>
        <b>监控时间范围：</b>
        <el-date-picker v-model="createSecondDateTimes" type="datetimerange" range-separator="至" format="yyyy-MM-dd HH:mm"
          start-placeholder="开始时间" end-placeholder="结束时间" @change="eventDatetimeChange" :picker-options="pickerOptions">
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
              <span v-if="data.server.statusName === 'start-part'">部分启动</span>
            </el-col>
            <el-col :span="4" class="uhry-layout-algin-r"><span class="uhry-font-blue">节点数量</span></el-col>
            <el-col :span="8">
              <span v-if="data.server.workNodeNum > 0">节点{{ data.server.workNodeNum }}个</span>
              <span v-if="data.server.sentinelNodeNum > 0">{{ data.server.workNodeNum !== 0 ? ';':'' }}哨兵节点{{ data.server.sentinelNodeNum }}个</span>
              <span v-if="data.server.workNodeNum === 0 && data.server.sentinelNodeNum === 0">0个</span>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>

    <el-card class="uhry-card-wrapper" shadow="never" v-for="(item, index) in echartsclusterData" :key="index">
      <div slot="header">
        <span class="uhry-card-title">{{ item.name }}</span>
      </div>
      <div class="centerbox" v-loading="loading">
          <node-monitor-dashboard :ref="item2.elementId" v-for="item2 in item.echarts" :key="item2.id" :echart="item2" :chartWidth="chartWidth"></node-monitor-dashboard>
      </div>
    </el-card>
  </div>
</template>

<script>
import nodeMonitorDashboard from './components/nodeMonitorDashboard'
import { listServiceNodeStat } from '@/api/console/rdsmonitor'
import { getConfigKey } from '@/api/system/config'
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
      echartsclusterData: [],
      data: {
        server: {
          name: "",
          modeName: "",
          statusName: "stop",
          workNodeNum: 1,
          sentinelNodeNum: 0
        }
      },
      echartsTitles: [
        { label: '连接数', keys: 'currentConnections' },
        { label: '内存使用量(M)', keys: 'memoryUsed', format: 'M' },
        { label: '内存总量(M)', keys: ['memoryUsed', 'memoryFree'], format: 'M' },
        { label: 'key总数', keys: 'currentKeys' },
        { label: '实际占用内存总量(M)', keys: 'memoryTotal', format: 'M' },
        { label: '最大可用内存量(M)', keys: 'memoryAvailable', format: 'M' },
        { label: '网络IO每秒入流量', keys: 'inputPerSecond' },
        { label: '网络IO每秒出流量', keys: 'outputPerSecond' },
        { label: '当前程序CPU使用率', keys: 'cpuProcessLoad' },
        { label: '当前系统CPU使用率', keys: 'cpuSystemLoad' },
      ],
      createSecondDateTimes: [new Date(new Date().getTime() - 10 * 60 * 1000), new Date()],
      maxIntervalDay: 30,//最大时间跨度：30天
      selectDate: '',
      pickerOptions: {
        onPick: ({ maxDate, minDate }) => {
          this.selectDate = minDate.getTime()
          if (maxDate) {
            this.selectDate = ''
          }
        },
        disabledDate: (time) => {
          const limitDay = 1000 * 60 * 60 * 24 * this.maxIntervalDay
          if (this.selectDate) {
            return (
              time.getTime() < this.selectDate - limitDay ||
              time.getTime() > this.selectDate + limitDay ||
              time.getTime() > Date.now()
            )
          } else {
            return time.getTime() > Date.now()
          }
        }
      },
      refreshIntervalSeconds: 60
    }
  },
  created() {
    this.getMonitorRefreshSeconds()
  },
  mounted() {
    this.actionInitChartWidth()
    this.getNodeList()
  },
  beforeDestroy() {
    this.timer && clearInterval(this.timer)
  },
  methods: {
    getMonitorRefreshSeconds() {//系统配置的自动刷新时间
      getConfigKey('monitorChartAutoRefreshSeconds').then((data) => {
        if (data.code === 200 && data.data) {
          this.refreshIntervalSeconds = parseInt(data.data)
        }
      })
    },
    actionInitChartWidth: function () {
      this.chartWidth = parseInt((window.innerWidth - 232 - 120) / 3)
    },
    async getNodeList() {
      this.timer && clearInterval(this.timer) // 首先进入清除定时器
      this.loading = true
      let beginCreateSecond = Math.floor(this.createSecondDateTimes[0].getTime() / 1000)
      let endCreateSecond = Math.floor(this.createSecondDateTimes[1].getTime() / 1000)
      let data = await listServiceNodeStat({
        serviceId: this.serviceId,
        beginCreateSecond: beginCreateSecond,
        endCreateSecond: endCreateSecond
      })
      this.loading = false
      if (data.code === 200) {
        this.data.server.name = data.data.name
        this.data.server.modeName = data.data.deployMode
        this.data.server.workNodeNum = data.data.nodes.length
        this.data.server.statusName = data.data.status
        this.setVal()
        this.formateDataEcharts(data.data)
      }
    },
    formateDataEcharts(data) {
      this.clusterEcharts(data)
    },
    clusterEcharts(data) {
      this.echartsclusterData = []
      data.nodes.forEach(item => {
        let time = []
        let localEchartData = this.echartsTitles.map(x => [])
        item.stats.forEach(k => {
          time.push(k.time)
          this.echartsTitles.forEach((obj, index) => localEchartData[index].push(this.calValue(k, obj.keys, obj.format)))
        })
        let thisEcharts = [];
        for (let i = 0; i < this.echartsTitles.length; i++) {
          thisEcharts.push({ elementId: this.uuid(), title: this.echartsTitles[i].label, categorys: time, data: localEchartData[i] })
        }
        this.echartsclusterData.push({
          name: item.instance,
          echarts: thisEcharts
        })
      })
    },
    setVal() {
      this.timer = setInterval(() => this.getNodeList(), this.refreshIntervalSeconds * 1000)
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
    goBack() {
      this.$router.go(-1)
    },
    eventDatetimeChange() {
      this.$nextTick(() => {
        this.getNodeList()
      })
    },
    calValue(data, keys, format) {
      let v = 0
      if (keys instanceof Array) {
        keys.forEach(item => v += data[item])
        console.info(keys)
      } else {
        v = data[keys]
      }
      return format ? (v / 1024 / 1024) : v
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
      width: 33%;
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
