<template>
  <div class="app-container home">
    <div class="home-top">
      <div class="top-le">
        <div>
          <span>欢迎使用{{config.name}}</span><br/>
		  <span style="font-size: 16px;"> &nbsp;&nbsp;&nbsp;版本：{{config.version}}</span>
        </div>
        <div class="le-content">
          TongRDS是分布式内存数据缓存中间件，用于高性能内存数据共享与应用支持。基于我公司对于高性能与高可靠业务的充分理解，
          结合内存数据库技术方面的多年研究开发经验，推出的新一代内存数据缓存产品。
        </div>
      </div>
      <div class="top-ri">
        <img src="../assets/logo/homenav.jpg" alt="">
      </div>
    </div>
    <div class="home-bot" v-if="false">
      <div>
        <img src="../assets/logo/26.png" alt="">
        <span>服务配置信息</span>
      </div>
      <div style="justify-content: flex-end;" class="bot_content">
        <div class="con_le">
            <div class="le_tit_con">
              控制台服务地址：
            </div>
            <div class="le_tit_con">
              {{config.servHost}}
            </div>
        </div>
        <div class="con_ri">
            <div class="le_tit_con">
              所被管理的节点会通过该"主机名/IP"来和控制台进行通信
            </div>
        </div>
      </div>
      <div class="line"></div>
      <div style="justify-content: flex-end;" class="bot_content">
        <div class="con_le">
            <div class="le_tit_con">
              控制台服务端口：
            </div>
            <div class="le_tit_con">
              {{config.servPort}}
            </div>
        </div>
        <div  class="con_ri">
            <div class="le_tit_con">
              提供各节点的控制信息和监控信息上传
            </div>
        </div>
      </div>
      <div class="line"></div>
      <div style="justify-content: flex-end;" class="bot_content">
        <div  class="con_le">
            <div class="le_tit_con">
              中心节点服务端口：
            </div>
            <div class="le_tit_con">
              {{config.centerPort}}
            </div>
        </div>
        <div class="con_ri">
            <div class="le_tit_con">
              提供各节点获取授权和获取配置所用端口
            </div>
        </div>
      </div>
    </div>
    <div style="margin-top: 50px;" class="home-bot">
      <div>
        <img src="../assets/logo/homeicon.png" alt="">
        <span>管理控制台主要特性介绍</span>
      </div>
      <div class="bot_content">
        <div class="con_le">
          <img src="../assets/logo/u80.jpg" alt="">
          <div class="con_tit_con">
            <div class="le_tit">多样化的数据处理能力</div>
            <div class="le_tit_con">
              <div>国内常见业务需求，支持Integer、Long、IP4、IP6、MAC等数据类型，提供更高效的处理能力、并有效节省内存的使用量；
                优化了Key/Value键值模型，提供了特有的索引支持能力；支持复合条件的数据查询和定位能力；
              </div>
            </div>
          </div>
        </div>
        <div class="con_ri">
          <img src="../assets/logo/u79.jpg" alt="" style="width:90px;height:90px;margin-top:20px;">
          <div class="con_tit_con">
            <div class="le_tit">高效稳定</div>
            <div class="le_tit_con">
              <div>采用了高效的数据池处理，提供了可以伸缩的多线程并行处理能力，
                保证了系统稳定和高效。</div>
            </div>
          </div>
        </div>
      </div>
      <div class="line"></div>
      <div class="bot_content">
        <div class="con_le">
          <img src="../assets/logo/u78.jpg" alt="" style="margin-top:0 !important;">
          <div class="con_tit_con">
            <div class="le_tit">部署灵活</div>
            <div class="le_tit_con">
              <div>采用分布式系统设计方法，支持群集环境的高效线性扩展能力。
                集群节点间采用的端对端的数据复制技术，保证了数据的可靠和安全，避免单点故障的发生。
              </div>
            </div>
          </div>
        </div>
        <div class="con_ri">
          <img src="../assets/logo/u87.jpg" alt="" >
          <div class="con_tit_con">
            <div class="le_tit">安全性高</div>
            <div class="le_tit_con">
              <div>数据使用、共享及传输上，全面支持国密级加密算法；
                支持连接加密；支持数据使用过程中的鉴权控制。</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-dialog :visible.sync="passwordDialog" title="密码修改" :show-close="false" :close-on-press-escape="false" :close-on-click-modal="false"	>
      <div>
        <h3 style="color:#ff4949">  {{ message }}</h3>
        <resetPwd :user="user" :passwordDialog="passwordDialog" @close='close'/>
      </div>
    </el-dialog>




  </div>
</template>

<script>

import { getisEditPassword } from '@/api/login'
import { getAppConfigKey } from '@/api/system/config'
import resetPwd from "./system/user/profile/resetPwd.vue";
import { getUserProfile } from "@/api/system/user";
export default {
  name: "index",
  data() {
    return {
      config:{},
      passwordDialog:false,
      message:"",
      user:{}
    };
  },
  components:{
    resetPwd
  },
  mounted(){
    this.isShowEditPassword()  //是否是集成或非集成
    this.getisPassword()      //是否修改密码
   
  },
  methods: {
    close(){
      this.passwordDialog = false
    },
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data;
      });
    },
    async isShowEditPassword(){
      let data = await getAppConfigKey('console.embedding')
      sessionStorage.setItem('checkoutVison',JSON.stringify(data.data))
    },
    gotoEditPassword() {
      this.$router.push({
        path:'/user/profile',
        query:{
          istag:'resetPwd'
        }
      })
    },
    async getisPassword() {
      this.getUser()
      let data = await getisEditPassword()
      if (data.code === 200) {
        if (data.data!='none') {
          this.message = data.msg
          this.passwordDialog = true
        }
      }

    }
  },
  created() {
  
    this.config = {
      name : null,
      version : null
    };

    getAppConfigKey('console.name').then((res) => {
      this.config.name = res.data;
      getAppConfigKey('console.version').then((res) => {
        this.config.version = res.data;
      });
    });
  }
};
</script>
<style scoped lang="scss">
.home {
  color:#333;
  .home-top{
    width: 94%;
    margin:20px 0 0 33px;
    background-color: #fff;
    border:1px solid #fff;
    padding:14px 0 0 14px;
    height: 260px;
    .top-le{
      display: inline-block;
      width: 54%;
      img{
        display: inline-block;
        width: 30px;
        height: 30px;
      }
      span{
        font-size: 24px;
        font-weight: 700;
        vertical-align: top;
        margin-left: 10px;
      }
      .le-content{
        line-height: 2;
        text-indent:2em;
        margin-top: 50px;
      }
    }
    .top-ri{
      display: inline-block;
      width: 38%;
      float: right;
      img{
        display: inline-block;
        width: 55%;
      }
    }
  }
  .home-bot{
    width: 94%;
    margin:0 0 0 33px;
    background-color: fff;
    border:1px solid fff;
    padding-left:14px;
    img{
      display: inline-block;
      width: 30px;
      height: 30px;
    }
    span{
      font-size: 24px;
      font-weight: 700;
      vertical-align: top;
      margin-left: 10px;
    }
    .bot_content{
      display: flex;
      margin-top: 20px;
      .con_le{
        display: flex;
        width: 47%;
        img{
          display: inline-block;
          width: 100px;
          height: 100px;
          margin-right: 10px;
         margin-top: 10px;
        }
        .le_tit{
          font-size: 18px;
          font-weight: 700;
        }
        .le_tit_con{
          line-height: 1.5;
        }
      }
      .con_ri{
        display: flex;
        width: 47%;
        margin-left: 30px;
        img{
          display: inline-block;
          width: 100px;
          height: 100px;
          margin-right: 10px;
        }
        .le_tit{
          font-size: 18px;
          font-weight: 700;
        }
        .le_tit_con{
          line-height: 1.5;
        }
      }
    }
    .line{
      width: 98%;
      height: 1px;
      margin:20px auto;
      background-color: #d7d7d7;
    }
  }
}
</style>

