<template>
  <div class="ListType">
    <div class="ListTypeBox">
      <el-row>
        <el-col :span="7">
          <el-input size="small" v-model="keyItem.keyName" disabled><template slot="prepend">List</template></el-input>
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
    <el-dialog :visible.sync="addRowShow" :title="title">
      <el-form :model="formInline" class="demo-form-inline">
        <el-form-item label="value">
          <el-input v-model="formInline.value"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addRowShow = false">取 消</el-button>
        <el-button type="primary" @click="savekey" :disabled="formInline.value?false:true">确 定</el-button>
      </span>
    </el-dialog>
    <div class="StringTypeContent">
      <el-button type="primary" size="small" @click="addRow(1)">添加新行</el-button>
      <el-table :data="keyItem.ListArr">
        <el-table-column label="ID" type="index" align="center"></el-table-column>
        <el-table-column label="value" prop="value" align="center">
          <template slot-scope="scope">
            <el-input v-model="scope.row.value" size="small" v-if="scope.row.isWrite" />
            <span v-else>{{ scope.row.value }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="addRow(2, scope.row,scope.$index)">编辑
            </el-button>
            <el-button type="text" size="mini" @click="delTable(scope.$index, scope.row, keyItem.ListArr)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>



  </div>
</template>
<script>
import { rdslpush, rdslset, rdslrem, rdsExpireTime, rdslrange } from "@/api/console/data/index.js"
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
      title: '添加新行',
      formInline: {
        value: ''
      },
      addRowShow: false,
      index:0
    }
  },
  created() {

  },
  methods: {
    fresh() {
      this.getDetail()
    },
    async getDetail() {
      let data = await rdslrange({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName,
        start: 0,
        end: 100
      })
      if (data.code === 200) {
        this.keyItem.ListArr = data.data
      }
    },
    addRow(type, row,index) {
      if (type === 1) {
        this.title = '添加新行'
        this.addRowShow = true
        this.formInline = {}
      } else {
        this.title = '编辑新行'
        this.index  = index
        this.formInline = JSON.parse(JSON.stringify(row)) 
        this.addRowShow = true
      }


    },
    async savekey() {
      if (this.title === '添加新行') {
        let data = await rdslpush({
          serviceId: this.serviceID,
          db: this.selectDB,
          key: this.keyItem.keyName,
          value: this.formInline.value
        })
        if (data.code === 200) {
          this.addRowShow = false
          this.$notify.success('添加成功')
          this.getDetail()
          
        }
      } else if (this.title === '编辑新行') {
        let data = await rdslset({
          serviceId: this.serviceID,
          db: this.selectDB,
          key: this.keyItem.keyName,
          value: this.formInline.value,
          index:this.index
        })
        if (data.code === 200) {
          this.addRowShow = false
          this.$notify.success('编辑成功')
          this.getDetail()
        }
      }

      //todo 保存key接口
    },
    delTable(index, row, list) {
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        let data = await rdslrem({
          serviceId: this.serviceID,
          db: this.selectDB,
          key: this.keyItem.keyName,
          value: row.value
        })
        if (data.code === 200) {
          list.splice(index, 1);
          this.$notify.success('删除成功')
        }
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