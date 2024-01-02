<template>
  <div class="ListType">
    <div class="ListTypeBox">
      <el-row>
        <el-col :span="7">
          <el-input size="small" v-model="keyItem.keyName" disabled><template slot="prepend">Set</template></el-input>
        </el-col>
        <el-col :span="7">
          <el-input size="small" v-model="keyItem.ttl">
            <template slot="prepend">TTL</template>
            <span class="uh-input-icons" slot="suffix">
              <i class="el-icon-close uh-input-icon" @click="eventClearTtl"></i>
              <i class="el-icon-check uh-input-icon" @click="eventUpdateTtl"></i>
            </span>
          </el-input>
        </el-col>
        <el-col :span="7">
          <el-button type="danger" size="mini" icon="el-icon-delete" @click="handleDeleKeys"></el-button>
          <el-button type="success" size="mini" icon="el-icon-refresh" @click="fresh"></el-button>
        </el-col>
      </el-row>
    </div>
    <div class="StringTypeContent">
      <el-button type="primary" size="small" @click="addRow">添加新行</el-button>
      <el-table :data="keyItem.SetArr">
        <el-table-column label="ID" type="index" align="center"></el-table-column>
        <el-table-column label="value" prop="value" align="center">
          <template slot-scope="scope">
            <el-input v-model="scope.row.value" size="small" v-if="scope.row.isWrite" />
            <span v-else>{{ scope.row.value }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button v-if="!scope.row.isWrite" type="text" size="mini" @click="edit(scope.row)">编辑
            </el-button>
            <el-button v-if="scope.row.isWrite" type="text" size="mini" @click="savekey(scope.$index, scope.row, keyItem.SetArr)">保存</el-button>
            <el-button type="text" size="mini" @click="delTable(scope.$index, scope.row, keyItem.SetArr)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>



  </div>
</template>
<script>
import { rdssrem, rdssadd, rdsExpireTime ,rdssmembers} from "@/api/console/data/index.js"
export default {
  components: {},
  props: {
    keyItem: {
      type: Object
    },
    selectDB: {
      type: Number
    },
    serviceID: {
      type: Number
    }
  },
  data() {
    return {
      initVal:''
    }
  },
  created() {

  },
  methods: {
    addRow() {
      if (this.keyItem.SetArr.length === 0) {
        this.keyItem.SetArr.push({
          value: 'New member',
          isWrite: true
        })
      } else {
        this.keyItem.SetArr.push({
          value: '',
          isWrite: true
        })
      }

    },
    edit(row){
      row.isWrite = true
      this.initVal = row.value
    },
    fresh(){
      this.getDetail()
    },
    async getDetail(){
      let data = await rdssmembers({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName,
      })
      if (data.code === 200) {
        this.keyItem.SetArr = data.data
      }
    },
    async savekey(index, row, list) {
      let data = await rdssrem({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName,
        member: this.initVal
      })
      if (data.code === 200) {
        row.isWrite = false
        list.splice(index, 1);
        rdssadd({
          serviceId: this.serviceID,
          db: this.selectDB,
          key: this.keyItem.keyName,
          member: row.value
        }).then(res => {
          if (res.code === 200) {
            this.$notify.success('保存成功')
            this.getDetail()
          }
        })
      }
    },
    async del(row, index,list) {
      let data = await rdssrem({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName,
        member: row.value
      })
      if (data.code === 200) {
        list.splice(index, 1);
        this.$notify.success('删除成功')
      }
    },
    delTable(index, row, list) {
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.del(row, index,list)
      })

      //todo 删除key接口
    },
    handleDeleKeys() {
      let data = {
        keys: [this.keyItem.keyName],
        db: this.selectDB,
        serviceId: this.serviceID,
      }
      this.$emit('handleDel', data)
    },
    async eventClearTtl() {
      let params = {
        db: this.selectDB,
        expireTime: -1,
        key: this.keyItem.keyName,
        serviceName: this.keyItem.serviceName,
        serviceId: this.serviceID
      }
      let data = await rdsExpireTime(params)
      if (data.code === 200) {
        this.$message.success('操作成功')
      }
    },

    async eventUpdateTtl() {
      let params = {
        db: this.selectDB,
        expireTime: this.keyItem.ttl,
        key: this.keyItem.keyName,
        serviceName: this.keyItem.serviceName,
        serviceId: this.serviceID
      }
      let data = await rdsExpireTime(params)
      if (data.code === 200) {
        this.$message.success('操作成功')
      }
    },
  }
}

</script>
<style lang="scss">
.ListType {
  padding: 10px 10px;

  .ListTypeBox {
    padding: 0px 10px;

    .el-input__inner {
      border: 1px solid #ccc;
    }

    .el-col {
      padding: 0px 5px;

      .uh-input-icons {
        font-size: 18px;
        line-height: 32px;

        .uh-input-icon {
          display: inline-block;
          margin-right: 5px;

          &:hover {
            color: #909399;
          }
        }
      }
    }
  }

  .StringTypeContent {
    margin-top: 20px;
    padding: 0px 15px;


  }
}
</style>