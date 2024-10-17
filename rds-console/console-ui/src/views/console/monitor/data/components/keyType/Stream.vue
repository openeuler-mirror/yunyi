<template>
  <div class="ListType">
    <div class="ListTypeBox">
      <el-row>
        <el-col :span="7">
          <el-input size="small" v-model="keyItem.keyName" disabled><template slot="prepend">Stream</template></el-input>
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
      <el-table :data="keyItem.StreamArr">
        <el-table-column label="NO" align="center" type="index"></el-table-column>
        <el-table-column label="ID" align="center" prop="id"></el-table-column>
        <el-table-column label="fields" prop="fields" align="center">
          <template slot-scope="scope">
            <el-input v-model="scope.row.fields" size="small" v-if="scope.row.isWrite" />
            <span v-else>{{ scope.row.fields }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <!-- <el-button v-if="!scope.row.isWrite" type="text" size="mini" @click="scope.row.isWrite = true">编辑
            </el-button> -->
            <el-button :disabled="scope.row.fields?false:true" v-if="scope.row.isWrite" type="text" size="mini" @click="savekey(scope.row)">保存</el-button>
            <el-button type="text" size="mini" @click="delTable(scope.$index, scope.row, keyItem.StreamArr)">删除
            </el-button>
            <el-button  type="text" size="mini" @click="look(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog :visible.sync="lookDialog" title="详情">
        <el-form>
            <el-form-item label="ID">
              <el-input v-model="lookObj.id" disabled/>
            </el-form-item>
            <el-form-item label="Value">
              <el-select v-model="current" >
                <el-option label="Json" value="Json"></el-option>
                <el-option label="Text" value="Text"></el-option>
              </el-select>
              <p></p>
              <CodeViewer class="params-viewer" :code="lookObj.fields" v-if="current === 'Json'"/>
              <el-input  disabled v-model="lookObj.fields" type="textarea"  v-if="current === 'Text'"></el-input>
            </el-form-item>
        </el-form>

      </el-dialog>
    </div>



  </div>
</template>
<script>
import CodeViewer from '@/components/CodeViewer'
import { rdsxadd, rdstreamdel,rdsExpireTime,rdsstreamList } from "@/api/console/data/index.js"
export default {
  components: {
    CodeViewer
  },
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
      lookObj:{},
      current:'Json',
      lookDialog:false
    }
  },
  created() {

  },
  methods: {
    fresh(){
      this.getDetail()
    },
    look(row){
      this.lookDialog = true
      this.lookObj = row
    },
    
    addRow() {
      if (this.keyItem.StreamArr.length === 0) {
        this.keyItem.StreamArr.push({
          fields: 'New member',
          isWrite: true
        })
      } else {
        this.keyItem.StreamArr.push({
          fields: '',
          isWrite: true
        })
      }

    },
    async getDetail(){
      let params = {
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName
      }
      let data = await rdsstreamList(params)
      if (data.code === 200) {
        this.keyItem.StreamArr = data.data
      }
      
    },
    async savekey(row) {
      let data = await rdsxadd({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName,
        id:row.id,
        value: row.fields
      })
      if (data.code === 200) {
        row.isWrite = false
        this.$notify.success('保存成功')
        this.getDetail()
      }
      //todo 保存key接口
    },
    delTable(index, row, list) {
      if(!row.id){
        list.splice(index,1)
        return 
      }
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        let data = await rdstreamdel({
          serviceId: this.serviceID,
          db: this.selectDB,
          key: this.keyItem.keyName,
          id:row.id
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
      let params ={
        db:this.selectDB,
        expireTime:-1,
        key:this.keyItem.keyName,
        serviceName:this.keyItem.serviceName,
        serviceId:this.serviceID
      }
      let data = await rdsExpireTime(params)
      if(data.code === 200){
        this.$message.success('操作成功')
      }
    },

    async eventUpdateTtl() {
      let params ={
        db:this.selectDB,
        expireTime:this.keyItem.ttl,
        key:this.keyItem.keyName,
        serviceName:this.keyItem.serviceName,
        serviceId:this.serviceID
      }
      let data = await rdsExpireTime(params)
      if(data.code === 200){
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
    .el-input__inner{
      border:1px solid #ccc;
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