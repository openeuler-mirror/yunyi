<template>
  <div class="key_list">
    <AddKeyType v-if="AddKeyTypeShow" @KeysClose="KeysClose" @KeysTypeSave="KeysTypeSave" />
    <div class="key_list_div">
      <span>
        <el-input type="text" size="mini" v-model="searchMatchKey" placeholder="录入关键词后回车搜索" @keyup.enter.native="initList"
          clearable>
          <el-tooltip slot="append" effect="dark" content="精确匹配" placement="bottom-start">
            <el-checkbox v-model="keyPatternPrecise" size="mini"></el-checkbox>
          </el-tooltip>
        </el-input>
      </span>
      <span>
        <el-button size="mini" icon="el-icon-plus" :disabled="serviceID?false:true" @click="AddKeyTypeShow = true" type="primary">
        </el-button>
        <el-button size="mini" icon="el-icon-delete" type="danger" :disabled="multipleSelection.length?false:true"
        @click="delkeys"
        >
        </el-button>
        <el-button size="mini" icon="el-icon-refresh" type="success" @click="referch"  :disabled="serviceID?false:true">
        </el-button>
      </span>

    </div>

    <div class="key_data">
      <el-table :data="keyList" @row-click="changeSingleKey" @selection-change="selectionVal" highlight-current-row :height="Theight">
        <el-table-column type="selection" align="center"></el-table-column>
        <el-table-column label="keys" prop="keys"></el-table-column>
      </el-table>
      <!-- <ul>
        <li :class="{ active: isActive === index }" v-for="(item, index) in keyList" :key="index"
          @click="changeSingleKey(item, index)">{{ item }}</li>
      </ul> -->
      <div class="uhry-align-center uhry-panel-footer" v-if="serviceID">
        <el-button class="uhry-button-full" style="width: 100%;" type="primary" size="mini" @click="nextList"
          :disabled="loadMoreKeyDisable === true">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { rdskeyList, rdsxadd, rdssetKey, rdshset, rdslpush, rdssadd, rdszadd,rdsdelKeys } from "@/api/console/data/index.js"
import AddKeyType from "../components/addKeyType.vue"
export default {
  components: {
    AddKeyType
  },
  props: {
    selectDB: {
      type: Number
    },
    serviceID: {
      type: Number
    }
  },
  data() {
    return {
      Theight:'470',
      multipleSelection:[],
      AddKeyTypeShow: false,
      searchMatchKey: "",
      keyPatternPrecise: false,
      // isActive: null,
      keyList: [],
      stringCursor: 0,
      cursor: '0',
      endpoint: undefined,
      loadMoreKeyDisable: false
    }
  },
  created(){
    let leftH = document.getElementsByClassName('app-main')[0].clientHeight;
    this.Theight =  leftH  - 57 - 100
     
  },
  methods: {
    KeysClose() {
      this.AddKeyTypeShow = false
    },
    KeysTypeSave(params) {   //在每个DB下不同类型新增该DB的key事件
      switch (params.value) {
        case 'string':
          this.addString(params)
          break;
        case 'hash':
          this.addHash(params)
          break;
        case 'list':
          this.addList(params)
          break;
        case 'set':
          this.addSet(params)
          break;
        case 'zset':
          this.addZset(params)
          break;
        case 'stream':
          this.addStream(params)
          break;
        default:
          break;
      }
    },
    async addString(params) {   //string类型的新增key

      let data = await rdssetKey({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: params.newKeyName,
        value: params.value
      })

      if (data.code === 200) {
        this.AddKeyTypeShow = false
        this.$notify.success('保存成功')
        this.initList()
        this.$emit('ClickSingleKey', params.newKeyName)
      }
    },
    async addHash(params) {     //hash类型的新增key

      let data = await rdshset({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: params.newKeyName,
        field: 'New field',
        value: 'New value'
      })

      if (data.code === 200) {
        this.AddKeyTypeShow = false
        this.$notify.success('保存成功')
        this.initList()
        this.$emit('ClickSingleKey', params.newKeyName)
      }
    },
    async addList(params) {
      let data = await rdslpush({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: params.newKeyName,
        value: 'New member'
      })
      if (data.code === 200) {
        this.AddKeyTypeShow = false
        this.$notify.success('保存成功')
        this.initList()
        this.$emit('ClickSingleKey', params.newKeyName)
      }
    },
    async addSet(params) {
      let data = await rdssadd({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: params.newKeyName,
        member: 'New member'
      })
      if (data.code === 200) {
        this.AddKeyTypeShow = false
        this.$notify.success('保存成功')
        this.initList()
        this.$emit('ClickSingleKey', params.newKeyName)
      }
    },
    async addZset(params) {
      let data = await rdszadd({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: params.newKeyName,
        score: 0,
        member: 'New member'
      })
      if (data.code === 200) {
        this.AddKeyTypeShow = false
        this.$notify.success('保存成功')
        this.initList()
        this.$emit('ClickSingleKey', params.newKeyName)
      }
    },
    async addStream(params) {
      let data = await rdsxadd({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: params.newKeyName,
        value: ''
      })
      if (data.code === 200) {
        this.AddKeyTypeShow = false
        this.$notify.success('保存成功')
        this.initList()
        this.$emit('ClickSingleKey', params.newKeyName)
      }
    },
    async nextList() {
      let data = await rdskeyList({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.searchMatchKey,
        cursor: this.stringCursor,
        endpoint: this.endpoint,
        keyPatternPrecise: this.keyPatternPrecise
      })
      if (data.code == 200) {
        if (data.data.scanResult !== null) {
          if (data.data.isFirstPage) {
            let arr = []
            this.isActive = null;
            data.data.scanResult.result.forEach((k, i) => {
              arr.push({
                keys: k
              })
            })
            this.keyList = arr
          }else{
            this.actionUpdateKeys(data.data.scanResult.result)
          }
          this.stringCursor = data.data.scanResult.cursor
          if (this.stringCursor === '0') {
            this.loadMoreKeyDisable = true
          } else {
            this.loadMoreKeyDisable = false
          }
        }
        this.endpoint = data.data.endpoint
      }
    },
    actionUpdateKeys(keys) {
        let map = {}
        this.keyList.map((item, index) => {
          map[item] = index
        })
        keys.map((item, index) => {
          if (map[item] === undefined) {
            let arr = {
              keys:item
            }
            this.keyList.push(arr)
          }
        })
    },
    async initList() {
      let data = await rdskeyList({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.searchMatchKey,
        cursor: this.cursor,
        keyPatternPrecise: this.keyPatternPrecise
      })
      if (data.code === 200) {
        let arr = []
        // this.isActive = null;
        data.data.scanResult.result.forEach((k, i) => {
          arr.push({
            keys: k
          })
        })
        this.keyList = arr
        this.stringCursor = data.data.scanResult.cursor
        if (this.stringCursor === '0') {
          this.loadMoreKeyDisable = true
        } else {
          this.loadMoreKeyDisable = false
        }
        this.endpoint = data.data.endpoint
      }
    },
    referch(){
      this.initList()
    },  
    delkeys(){
      this.$confirm('是否删除所选择的key, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        let arr  =[]
        this.multipleSelection.forEach((item,index)=>{
          arr.push(item.keys)
        })
        let params = {
          db: this.selectDB,
          serviceId: this.serviceID,
          keys:arr
        }
        let result = await rdsdelKeys(params)
        if (result.code === 200) {
          this.$notify.success('删除成功')
          this.initList()
        }

      })
    },
    selectionVal(selection){
      this.multipleSelection = selection
      console.log(selection)
    },
    changeSingleKey(val) {
      this.$emit('ClickSingleKey', val.keys)
    }
  }
}
</script>

<style lang="scss" >
.key_list {
  padding: 10px 10px;

  .key_list_div {
    display: flex;
    justify-content: space-between;
    width: 100%;

    span:nth-child(2) {
      width: 50%;
    }
  }


  .key_data {
    width: 100%;

    ul {
      list-style: none;
      padding: 0;
      // color: gray;

      li {
        height: 35px;
        line-height: 35px;
        padding: 0 10px;
        cursor: pointer;
        transition: all .5s;

        &.active {
          background: #6e91c9;
          color: white;
          border-radius: 5px;
        }
      }
    }
  }
}
</style>