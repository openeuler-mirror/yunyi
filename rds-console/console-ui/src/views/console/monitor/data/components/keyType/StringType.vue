<template>
  <div class="StringType">
    <div class="StringTypeBox">
      <el-row>
        <el-col :span="7">
          <el-input   size="small" v-model="keyItem.keyName" disabled><template slot="prepend">String</template></el-input>
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
      <div>
        <el-select v-model="keyType" placeholder="请选择" size="mini" @change="changeValue">
          <el-option v-for="item in typeoptions" :key="item.value" :label="item.text" :value="item.value">
          </el-option>
        </el-select>
        <span style="color:#1890FF;font-size:14px;margin-left:10px">Size:{{ keyItem.keySize }}</span>
      </div>
      <div style="margin-top:10px">
        <el-input type="textarea" v-model="keyItem.value" v-if="keyType === 'ViewerText'" />
        <el-input type="textarea" v-model="StringTypeList.ViewerHex" v-if="keyType === 'ViewerHex'" />
        <el-input type="textarea" v-model="StringTypeList.ViewerJson" v-if="keyType === 'ViewerJson'" />
        <el-input type="textarea" v-model="StringTypeList.ViewerBinary" v-if="keyType === 'ViewerBinary'" />
      </div>

      <el-divider></el-divider>
      <el-button size="small" type="primary" @click="saveString">保存</el-button>
    </div>



  </div>
</template>
<script>
import {rdsExpireTime,rdsgetKey} from "@/api/console/data/index.js"

export default {
  components: {

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
      StringTypeList: {
        ViewerHex: '',
        ViewerJson: '',
        ViewerBinary: ''
      },
      keyType: 'ViewerText',
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
      this.getDetail()
    },
    async getDetail(){
      let params = {
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName
      }
      let data = await rdsgetKey(params)
      if (data.code === 200) {
        this.keyItem.keySize = data.data.size
        this.keyItem.ttl = data.data.ttl
        this.keyItem.keyName = val
        this.keyItem.value = data.data.value
      }
    },
    handleDeleKeys() {
      let data = {
        keys: [this.keyItem.keyName],
        db: this.selectDB,
        serviceId: this.serviceID,
      }
      this.$emit('handleDel', data)
    },
    changeValue(val) {
      switch (val) {
        case 'ViewerText':
          this.ViewerText(this.keyItem.value)
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
      this.StringTypeList.ViewerHex = this.stringToHex(this.keyItem.value)
    },
    ViewerJson() {//json格式
      // this.StringTypeList.ViewerJson = JSON.parse(this.keyItem.value)
    },
    ViewerBinary() {//二进制
      this.StringTypeList.ViewerBinary = this.strToBinary(this.keyItem.value)
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
    saveString() {
      let data = {
        serviceId: this.serviceID,
        db: this.selectDB,
        key: this.keyItem.keyName,
        value: this.keyItem.value,
        expireTime: this.keyItem.ttl
      }
      this.$emit('StringSave', data)
    }
  }
}

</script>
<style lang="scss">
.StringType {
  padding: 10px 10px;

  .jsoneditor-vue {
    height: 400px !important;
  }

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

    .el-textarea__inner {
      height: 300px;
    }
  }
}
</style>