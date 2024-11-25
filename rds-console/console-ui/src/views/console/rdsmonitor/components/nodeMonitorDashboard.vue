<template>
  <div class="uhry-wrapper">
    <div class="uhry-chart-wrapper">
      <div
        class="uhry-chart"
        :id="echart.elementId"
        :style="{ width: chartStyle, height: '200px' }"
      ></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
export default {
  components: {},
  props: {
    echart: Object,
    chartWidth: Number
  },
  computed: {
    categorys() {
      return this.echart.categorys
    }
  },
  watch: {
    categorys(current, old) {
      this.eventRefreshEcharts()
    }
  },
  data() {
    return {
      chart: undefined,
      chartStyle: ''
    }
  },
  methods: {
    //刷新图表
    eventRefreshEcharts: function () {
      if (this.chart === undefined) {
        let dom = document.getElementById(this.echart.elementId)
        this.chart = echarts.init(dom)
      }
      let option = {

        color: ['#409EFF'],
        grid: {
          right: 10,
          top: 50,
          bottom: 20,
          containLabel: true
        },
        tooltip: {
          trigger: 'axis'
        },
        title: {
          text: this.echart.title,
          textStyle: {
            color: '#909399',
            fontSize: 14
          }
        },
        xAxis: {
          type: 'category',
          data: this.echart.categorys,
          axisLine: {
            lineStyle: {
              color: '#909399'
            }
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            lineStyle: {
              color: '#909399'
            }
          }
        },
        series: [
          {
            data: this.echart.data,
            type: 'line',
            smooth: true
          }
        ]
      }
      this.chart.clear()
      this.chart.setOption(option)
    }
  },
  created() {
    this.chartStyle = this.chartWidth + 'px'
  },
  mounted() {
    //刷新图表
    this.eventRefreshEcharts()
  }
}
</script>
<style lang="scss" scoped>
.uhry-wrapper {
  .uhry-chart-wrapper {
    padding: 10px;
    border: solid 1px rgb(102, 177, 255);
    border-radius: 5px;

    .uhry-chart {
      height: 350px;
    }
  }
}
</style>
