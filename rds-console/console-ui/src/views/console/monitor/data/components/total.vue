<template>
  <div class="total">
    <el-row :gutter="12">
      <el-col :span="8">
        <el-card shadow="always">
          <div slot="header" class="clearfix">
            <span> <i class="el-icon-s-operation"></i>服务</span>
            <p class="server-status-tag-p">
              <el-tag class='server-status-container' type="info" size="big">
                {{ 'redis_version' }}:
                <span class="server-status-text">3210</span>
              </el-tag>
            </p>
            <p class="server-status-tag-p">
              <el-tag class='server-status-container' type="info" size="big">
                {{ 'OS' }}:
                <span class="server-status-text">windows</span>
              </el-tag>
            </p>
            <p class="server-status-tag-p">
              <el-tag class='server-status-container' type="info" size="big">
                {{ 'Process ID' }}:
                <span class="server-status-text">8620</span>
              </el-tag>
            </p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="always">
          <div slot="header" class="clearfix">
            <span><i class="el-icon-s-platform"></i> 内存</span>
            <p class="server-status-tag-p">
              <el-tag class='server-status-container' type="info" size="big">
                {{ 'Used Memory' }}:
                <span class="server-status-text">676.54K</span>
              </el-tag>
            </p>
            <p class="server-status-tag-p">
              <el-tag class='server-status-container' type="info" size="big">
                {{ 'Used Memory Peak' }}:
                <span class="server-status-text">676.54K</span>
              </el-tag>
            </p>
            <p class="server-status-tag-p">
              <el-tag class='server-status-container' type="info" size="big">
                {{ 'Used Memory Lua' }}:
                <span class="server-status-text">37K</span>
              </el-tag>
            </p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="always">
          <div slot="header" class="clearfix">
            <span><i class="el-icon-s-data"></i> 统计</span>
            <p class="server-status-tag-p">
              <el-tag class='server-status-container' type="info" size="big">
                {{ 'Connected Clients' }}:
                <span class="server-status-text">1</span>
              </el-tag>
            </p>
            <p class="server-status-tag-p">
              <el-tag class='server-status-container' type="info" size="big">
                {{ 'Total Connentions' }}:
                <span class="server-status-text">10</span>
              </el-tag>
            </p>
            <p class="server-status-tag-p">
              <el-tag class='server-status-container' type="info" size="big">
                {{ 'Total Commands' }}:
                <span class="server-status-text">4</span>
              </el-tag>
            </p>

          </div>
        </el-card>
      </el-col>
    </el-row>
    <div class="footer">
      <el-card shadow="always">
        <div slot="header" class="clearfix">
          <span> <i class="el-icon-s-claim"></i>关键统计信息</span>
          <el-table border style="margin-top:20px" :data="keyList">
            <el-table-column label="DB" align="center" prop="DB"></el-table-column>
            <el-table-column label="Keys" align="center" prop="Keys"></el-table-column>
            <el-table-column label="Expires" align="center" prop="Expires"></el-table-column>
            <el-table-column label="Avg TTL" align="center" prop="TTL"></el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>
<script>
export default{
  data(){
    return {
      keyList:[
        {
          DB:'Db0',
          Keys:'15',
          Expires:'0',
          TTL:'0'
        },
        {
          DB:'Db0',
          Keys:'15',
          Expires:'0',
          TTL:'0'
        },
        {
          DB:'Db0',
          Keys:'15',
          Expires:'0',
          TTL:'0'
        }
      ]
    }
  }
}
</script>
<style lang="scss" scoped>
.total {
  padding:0px 20px;
  .footer {
    margin-top: 20px
  }

  .server-status-tag-p {
    height: 32px;
  }

  .server-status-container {
    width: 100%;
    overflow-x: hidden;
    text-overflow: ellipsis;
  }

  .server-status-text {
    color: #43b50b;
  }
}
</style>