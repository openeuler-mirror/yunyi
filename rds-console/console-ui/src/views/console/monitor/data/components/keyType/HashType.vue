<template>
  <div class="StringType">
    <div class="StringTypeBox">
      <el-row>
        <el-col :span="7">
          <el-input size="small" v-model="keyItem.keyName" disabled><template slot="prepend">Hash</template></el-input>
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
    <el-dialog :visible.sync="addRowShow" title="添加新行">
      <el-form :model="formInline" class="demo-form-inline">
        <el-form-item label="Field">
          <el-input v-model="formInline.field"></el-input>
        </el-form-item>
        <el-form-item label="Value">
          <el-select v-model="keyType" placeholder="请选择" size="mini" @change="changeValue">
            <el-option v-for="item in typeoptions" :key="item.value" :label="item.text" :value="item.value">
            </el-option>
          </el-select>
          <div style="margin-top:10px">
            <el-input type="textarea" v-model="formInline.value" v-if="keyType === 'ViewerText'" />
            <el-input type="textarea" v-model="StringTypeList.ViewerHex" v-if="keyType === 'ViewerHex'" />
            <el-input type="textarea" v-model="StringTypeList.ViewerJson" v-if="keyType === 'ViewerJson'" />
            <el-input type="textarea" v-model="StringTypeList.ViewerBinary" v-if="keyType === 'ViewerBinary'" />
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addRowShow = false">取 消</el-button>
        <el-button type="primary" @click="savekey">确 定</el-button>
      </span>
    </el-dialog>
    <div class="StringTypeContent">
      <el-button type="primary" size="small" @click="addRow">添加新行</el-button>
      <el-table :data="keyItem.hashArr">
        <el-table-column label="ID" type="index" align="center"></el-table-column>
        <el-table-column label="key" prop="key" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.key }}</span>
          </template>
        </el-table-column>
        <el-table-column label="value" prop="value" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.value }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="editRow(scope.row)">编辑</el-button>
            <el-button type="text" size="mini" @click="delTable(scope.$index, scope.row, keyItem.hashArr)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>



  </div>
</template>
<script>
import { rdshset, rdshdel, rdshgetAll } from "@/api/console/data/index.js"
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
      formInline: {
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName,
        field: '',
        value: '',
      },
      addRowShow: false,
      search: '',
      keyType: 'ViewerText',
      StringTypeList: {
        ViewerHex: '',
        ViewerJson: '',
        ViewerBinary: ''
      },
      typeoptions: [
        { value: 'ViewerText', text: 'Text' },
        { value: 'ViewerHex', text: 'Hex' },
        { value: 'ViewerJson', text: 'Json' },
        { value: 'ViewerBinary', text: 'Binary' },
      ],
    }
  },
  created() {

  },
  methods: {
    fresh(){
      this.HashKeyDetail()
    },
    
    changeValue(val) {
      console.log(val)
      switch (val) {
       
        case 'ViewerText':
          this.ViewerText(this.formInline.value)
          break;
        case 'ViewerHex':
          this.ViewerHex()
          break;
        case 'ViewerJson':
          this.ViewerJson()
          break;
        case 'ViewerBinary':
          this.ViewerBinary()
          break;

        default:
          break;
      }
    },
    ViewerText(value) {  //文本

    },
    ViewerHex() {//十六进制
      console.log(this.formInline.value)
      this.StringTypeList.ViewerHex = this.stringToHex(this.formInline.value)
    },
    ViewerJson() {//json格式
      // this.StringTypeList.ViewerJson = JSON.parse(this.keyItem.value)
    },
    ViewerBinary() {//二进制
      this.StringTypeList.ViewerBinary = this.strToBinary(this.formInline.value)
    },
    stringToHex(str) {
      var val = "";
      for (var i = 0; i < str.length; i++) {
        if (val == "")
          val = str.charCodeAt(i).toString(16);
        else
          val += "" + str.charCodeAt(i).toString(16);
      }
      return val;
    },
    strToBinary(str) {
      var result = [];
      var list = str.split("");
      for (var i = 0; i < list.length; i++) {
        if (i != 0) {
          result.push(" ");
        }
        var item = list[i];
        var binaryStr = item.charCodeAt().toString(2);
        result.push(binaryStr);
      }
      return result.join("");
    },

    addRow() {
      if (this.keyItem.hashArr.length === 0) {
        this.keyItem.hashArr.push({
          key: 'New field',
          value: 'New value',
        })
      } else {
        this.addRowShow = true
      }

    },
    editRow(row) {
      this.formInline.field = row.key
      this.formInline.value = row.value
      this.addRowShow = true
    },
    async savekey() {
      let data = await rdshset(this.formInline)
      if (data.code === 200) {
        this.$notify.success('保存成功')
        this.addRowShow = false
        this.HashKeyDetail()
      }
      //todo 保存key接口
    },
    delTable(index, row, list) {
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        let data = await rdshdel({
          serviceId: this.serviceID,
          db: this.selectDB,
          key: this.keyItem.keyName,
          field: row.key,
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
    async HashKeyDetail() {//hash详情信息
      let data = await rdshgetAll({
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName
      })
      if (data.code === 200) {
        this.keyItem.hashArr = data.data
      }
    },
    eventClearTtl() {

    },
    eventUpdateTtl() {

    },
  }
}

</script>
<style lang="scss">
.StringType {
  padding: 10px 10px;

  .StringTypeBox {
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