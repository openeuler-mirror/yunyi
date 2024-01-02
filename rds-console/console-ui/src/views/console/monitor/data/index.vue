<template>
  <div class="app-container">

    <div class="content">
      <div class="content-left" :style="{ width: width + 'px' }">
        <div class="content-left-body">
          <div class="content-left-body-header">
            <span>当前连接</span>
            <span>
              {{ currentConnetValue.serviceName }}
            </span>
            <span>
              <el-button icon="el-icon-time" type="primary" size="mini" @click="serviceShow = true">选择</el-button>
            </span>
            <span >
              <el-select size="mini" v-model="selectDB" @change="selectdbChange">
                <el-option v-for="(item, index) in SelectDBList" :label="item.key" :value="item.value" :key="index">
                </el-option>
              </el-select>
            </span>

          </div>
          <div class="content-left-body-footer" >
            <KeyList @ClickSingleKey="ClickSingleKey" ref="KeyList" :serviceID="serviceID" :selectDB="selectDB" />
          </div>
        </div>
      </div>
      <XHandle @widthChange="widthChange" />
      <div class="content-right">
        <div v-if="!currentConnetValue.serviceName" class="noData">
          暂无数据项内容
        </div>
        <total v-if="totalShow && currentConnetValue.serviceName" />
        <keyType v-if="keySingleDateShow" :keyItem="keyItem" :serviceID="serviceID" :selectDB="selectDB"
          @StringSave="StringSave" @handleDel="handleDel" />
      </div>
    </div>
    <div class="components">
      <serviceList v-if="serviceShow" @close="Logclose" @chooseService="chooseService" />
      <AddConnet v-if="AddConnetShow" @close="Connetclose" />
    </div>
  </div>
</template>
<script>
import { rdsgetDB, rdssetKey, rdsgetKey, rdsxadd, rdsgetType, rdsdelKeys, rdshset, rdshgetAll, rdslpush, rdslrange, rdssadd, rdssmembers, rdszadd, rdszsetList, rdsstreamList } from "@/api/console/data/index.js"
import serviceList from "./components/serviceList.vue"
import AddConnet from "./components/addConnet.vue"
import XHandle from "@/components/Xhandle/index.vue"
import total from "./components/total.vue"
import AddKeyType from "./components/addKeyType.vue"
import KeyList from "./components/keyList.vue"
import keyType from "./components/keyType"
export default {
  components: { serviceList, AddConnet, XHandle, total, AddKeyType, KeyList, keyType },
  data() {
    return {
      width: 380,
      serviceShow: false,
      AddConnetShow: false,
      keySingleDateShow: false,
      totalShow: false,
      serviceListShow: false,
      currentConnetValue: {},
      totallist: 0,
      selectDB: null,  //某个DB的value
      keyItem: {
        serviceName: '',
        result: '',
        keySize: '',
        ttl: null,
        keyName: '',
        value: '',
        hashArr: [],
        ListArr: [],
        SetArr: [],
        ZsetArr: [],
        StreamArr: []
      },
      serviceID: null, //某个服务的ID
      SelectDBList: []
    }
  },
  mounted(){
    if( sessionStorage.getItem('serciveObj')){
      let service = JSON.parse( sessionStorage.getItem('serciveObj'))
      this.getSelectDB(service)
      this.currentConnetValue = service[0]
    }
  },
  methods: {
    chooseService(service) {  //选择某个服务
      sessionStorage.setItem('serciveObj',JSON.stringify(service))
      this.getSelectDB(service)
      this.currentConnetValue = service[0]
      this.serviceShow = false
      // this.totalShow = true
      this.keySingleDateShow = false
    },
    async getSelectDB(service) { //某个服务下的DBList数据
      let data = await rdsgetDB({ serviceId: service[0].serviceId })
      if (data.code === 200) {
        this.serviceID = data.data.serviceId
        this.SelectDBList = data.data.dbList
        this.selectDB = data.data.dbList.length ? data.data.dbList[0].value : '';
        setTimeout(() => {
          this.$refs.KeyList.initList()
        }, 500);
      }
    },
    selectdbChange() {   //切换每个DB获取该DB的key列表
      this.$refs.KeyList.initList()
    },




    async ClickSingleKey(val) { //点击每个key的事件
      let data = await rdsgetType({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: val
      })
      if (data.code === 200) {
        this.getKey(data.data.result, val, data.data.ttl)
      }
    },
    getKey(result, val, ttl) {  //获取key类型的信息
      switch (result) {
        case 'string':
          this.StringKeyDetail(val)
          break;
        case 'hash':
          this.HashKeyDetail(val, ttl)
          break;
        case 'list':
          this.ListKeyDetail(val, ttl)
          break;
        case 'set':
          this.SetKeyDetail(val, ttl)
          break;
        case 'zset':
          this.ZsetKeyDetail(val, ttl)
          break;
        case 'stream':
          this.StreamKeyDetail(val, ttl)
          break;
        default:
          break;
      }

    },
    async StreamKeyDetail(val, ttl) {
      let params = {
        serviceId: this.serviceID,
        db: this.selectDB,
        key: val
      }
      let data = await rdsstreamList(params)
      if (data.code === 200) {
        this.keyItem.keyName = val
        this.keyItem.ttl = ttl
        this.keyItem.result = 'stream'
        this.keyItem.serviceName = this.currentConnetValue.serviceName
        data.data.forEach((item, index) => {
          item.isWrite = false
        })
        this.keyItem.StreamArr = data.data
        this.totalShow = false
        this.keySingleDateShow = true
      }
    },
    async StringKeyDetail(val) {  //string详情信息
      let params = {
        serviceId: this.serviceID,
        db: this.selectDB,
        key: val
      }
      let data = await rdsgetKey(params)
      if (data.code === 200) {
        this.keyItem.keySize = data.data.size
        this.keyItem.result = data.data.type
        this.keyItem.ttl = data.data.ttl
        this.keyItem.keyName = val
        this.keyItem.value = data.data.value
        this.keyItem.serviceName = this.currentConnetValue.serviceName
        this.totalShow = false
        this.keySingleDateShow = true
      }
    },
    async HashKeyDetail(val, ttl) {//hash详情信息
      let data = await rdshgetAll({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: val
      })
      if (data.code === 200) {
        this.keyItem.result = 'hash'
        this.keyItem.keyName = val
        this.keyItem.ttl = ttl
        this.keyItem.serviceName = this.currentConnetValue.serviceName
        data.data.forEach((item, index) => {
          item.isWrite = false
        })
        this.keyItem.hashArr = data.data
        this.totalShow = false
        this.keySingleDateShow = true
      }
    },
    async ListKeyDetail(val, ttl) {   //list详情
      let data = await rdslrange({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: val,
        start: 0,
        end: 100
      })
      if (data.code === 200) {
        this.keyItem.result = 'list'
        this.keyItem.keyName = val
        this.keyItem.ttl = ttl
        this.keyItem.serviceName = this.currentConnetValue.serviceName
        data.data.forEach((item, index) => {
          item.isWrite = false
        })
        this.keyItem.ListArr = data.data
        this.totalShow = false
        this.keySingleDateShow = true
      }
    },
    async SetKeyDetail(val, ttl) {
      let data = await rdssmembers({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: val,
      })
      if (data.code === 200) {
        this.keyItem.result = 'set'
        this.keyItem.keyName = val
        this.keyItem.ttl = ttl
        this.keyItem.serviceName = this.currentConnetValue.serviceName
        data.data.forEach((item, index) => {
          item.isWrite = false
        })
        this.keyItem.SetArr = data.data
        this.totalShow = false
        this.keySingleDateShow = true
      }
    },
    async ZsetKeyDetail(val, ttl) {
      let data = await rdszsetList({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: val,
      })
      this.keyItem.result = 'zset'
      this.keyItem.keyName = val
      this.keyItem.ttl = ttl
      this.keyItem.serviceName = this.currentConnetValue.serviceName
      data.data.forEach((item, index) => {
        item.isWrite = false
      })
      this.keyItem.ZsetArr = data.data
      this.totalShow = false
      this.keySingleDateShow = true
    },
    async StringSave(data) {  //string保存
      let result = await rdssetKey(data)
      if (result.code === 200) {
        this.$notify.success('保存成功')
        this.$refs.KeyList.initList()
      }
    },
    handleDel(data) {//stringb删除
      this.$confirm('是否删除该key, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        let result = await rdsdelKeys(data)
        if (result.code === 200) {
          this.$notify.success('删除成功')
          this.$refs.KeyList.initList()
          this.keySingleDateShow = false
        }

      })

    },

 
    Logclose() {
      this.serviceShow = false
    },
    Connetclose() {
      this.AddConnetShow = false
    },
    widthChange(movement) {
      this.width -= movement;
      if (this.width < 370) {
        this.width = 370;
      }
      if (this.width > 1000) {
        this.width = 1000;
      }
    },

  },
  created() {

  }
}
</script>

<style lang="scss" scoped>
.content {
  width: 100%;
  height: 100%;
  display: flex;

  .content-left {
    width: 200px;
    // height: 100vh;
    // overflow: scroll;

    .content-left-header {
      width: 160px
    }

    .content-left-body {
      margin-top: 10px;

      .content-left-body-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0px 5px;
        font-size: 15px;
        color: black;
        widows: 100%;
        >span{
          width: 24%;
        }
      }

      .content-left-body-footer {
        margin-top: 20px;
        width: 100%;

        .opa-div {
          display: flex;
          justify-content: space-between;
          padding: 0px 10px
        }
      }
    }
  }

  .content-right {
    flex: 1;
    overflow: scroll;
    background: #f5f7fb;
    .noData{
      position: absolute;
      top:40%;
      left:60%;
    }
  }
}
</style>
