<template>
    <div class="video-wrapper" v-show="pageReady">
        <video 
                :src="bgVideo"
                preload="auto"
                @canplay="onVideoReady" @error="onVideoReady"
                class="bg-video"
                autoplay
                loop
                muted
                playsinline
            ></video>
        <div class="overlay"></div>
        <!-- <div class="deco deco-1"></div>
        <div class="deco deco-2"></div> -->
        <div class="bg">
            <div class="loginbox">
                <div class="brand">
                    <p class="welcome">CREATE ACCOUNT</p>
                    <h2 class="title">注册界面</h2>
                    <p class="subtitle">创建一个新账号，即刻开始</p>
                </div>
                <el-form style="width: 100%;" :model="data.user" :rules="data.rules">
                    <el-form-item prop="username">
                        <el-input size="large" placeholder="请输入账号" :prefix-icon="User" v-model="data.user.username"></el-input>
                    </el-form-item>
                    <el-form-item prop="password">  
                        <el-input size="large" placeholder="请输入密码" :prefix-icon="Lock" v-model="data.user.password" show-password></el-input>
                    </el-form-item>
                    <el-form-item  prop="repassword">
                        <el-input size="large" placeholder="请再次输入密码" :prefix-icon="Lock" v-model="data.user.repassword" show-password></el-input>       
                    </el-form-item>
                    <el-form-item style="display: flex;">                    
                        <el-button class="login-btn" style="width: 100%;margin: auto;" :loading="loading" @click="registerin">注 册</el-button>                                        
                    </el-form-item>
                    <div class="footer-links">
                        <span>已经有账号？请<router-link to="/login" style="text-decoration: none;"><span class="link">登录</span></router-link></span>
                    </div>
                </el-form>
            </div>
        </div>  
    </div>
    
</template>
<script setup>
import bgVideo from '@/assets/register_bg.webm' 
import { User, Search,Lock } from '@element-plus/icons-vue'
import { reactive ,ref,onMounted } from 'vue'
import axios from 'axios'
import router from '@/router'
import { ElMessage,ElMessageBox} from 'element-plus'
import { userApi } from '@/api'
const pageReady = ref(false)
const onVideoReady = () => {
    pageReady.value = true
}
onMounted(() => {
    setTimeout(onVideoReady, 3000)
})
const data = reactive({
    user:{username: '',password: '',repassword:""},
    rules:{username:[{required: true,message:"请填写账号",trigger:"blur"}],
        password:[{required: true,message:"请填写密码",trigger:"blur"}],
        repassword:[{required: true,message:"请再次填写相同密码",trigger:"blur"}]}
})
const loading = ref(false)
const registerin=()=>{
   loading.value = true
   userApi.register(data.user).then(res=>{
    if (res.data.code=="200"){
        alert("注册成功"); 
        router.push("/login")      
    }else{ElMessage(res.data.msg)}
}).catch(error => {
        console.error('注册请求异常：', error) // 控制台打印便于调试
        ElMessage('注册网络异常')
      })
      .finally(() => { loading.value = false })
}
</script>

<style scoped>
    :global(html),
    :global(body) {
    margin: 0;        /* 清除默认外边距 */
    padding: 0;       /* 清除默认内边距 */
    height: 100%;     /* 让 html/body 占满视口高度 */
    }
    
    .video-wrapper {
    position: relative;
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    background-color: #31519c;
    }

    /* 视频背景：填满容器，盖在底层 */
    .bg-video {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;  /* 类似 background-size: cover 的效果 */
      filter: brightness(1.12) contrast(1.08) saturate(1.08);
    z-index: 0;         /* 底层 */
    }
    .overlay {
      position: absolute;
      inset: 0;
      z-index: 1;
      pointer-events: none;
      background: linear-gradient(180deg, rgba(2,6,23,0) 0%, rgba(2,6,23,0) 45%, rgba(2,6,23,.10) 100%);
    }
    .deco {
      position: absolute;
      border-radius: 50%;
      filter: blur(28px);
      pointer-events: none;
      z-index: 1;
    }
    .deco-1 { width: 320px; height: 320px; background: rgba(56,189,248,.26); top: 8%; left: 6%; }
    .deco-2 { width: 280px; height: 280px; background: rgba(168,85,247,.24); bottom: 10%; right: 8%; }
    .bg{
      position: relative;
      z-index: 2;
      height: 100vh;
      width: 100vw;
      overflow: hidden;
      align-items: center;
      display: flex;
      justify-content: center;
    }
    .loginbox{
      display: flex;
      flex-direction: column;
      align-items: stretch;
      background: rgba(255,255,255,.12);
      backdrop-filter: blur(24px) saturate(140%);
      -webkit-backdrop-filter: blur(24px) saturate(140%);
      border: 1px solid rgba(255,255,255,.16);
      width: 420px;
      max-width: 92vw;
      border-radius: 20px;
      box-shadow: 0 12px 48px rgba(0,0,0,.38);
      padding: 36px 34px 28px;
    }
    .brand { text-align: center; margin-bottom: 22px; }
    .welcome { margin: 0 0 6px; color: rgba(255,255,255,.55); font-size: 13px; letter-spacing: 2px; }
    .title {
      margin: 0 0 8px;
      font-size: 30px; font-weight: 700;
      background: linear-gradient(135deg, #7dd3fc, #a78bfa);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
    }
    .subtitle { margin: 0; color: rgba(255,255,255,.45); font-size: 13px; }
    .loginbox :deep(.el-input__wrapper) {
      background: rgba(255,255,255,.07);
      box-shadow: 0 0 0 1px rgba(255,255,255,.14) inset;
      border-radius: 10px;
      padding: 6px 12px;
    }
    .loginbox :deep(.el-input__wrapper.is-focus) {
      box-shadow: 0 0 0 1px rgba(125,211,252,.75) inset, 0 0 14px rgba(99,102,241,.28);
      background: rgba(255,255,255,.1);
    }
    .loginbox :deep(.el-input__inner) { color: #fff; }
    .loginbox :deep(.el-input__inner::placeholder) { color: rgba(255,255,255,.45); }
    .loginbox :deep(.el-input__prefix) { color: rgba(255,255,255,.6); }
    .login-btn.el-button {
      height: 46px; font-size: 16px; letter-spacing: 6px; border-radius: 12px;
      border: none; color: #fff;
      background: linear-gradient(135deg, #38bdf8, #6366f1, #a855f7);
      box-shadow: 0 8px 20px rgba(99,102,241,.35);
      transition: transform .15s ease, box-shadow .15s ease, filter .15s ease;
    }
    .login-btn.el-button:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 28px rgba(99,102,241,.5);
      filter: brightness(1.06);
    }
    .login-btn.el-button:active { transform: translateY(0); }
    .footer-links {
      display: flex; justify-content: space-between; align-items: center;
      color: rgba(255,255,255,.55); font-size: 14px; margin-top: 4px;
    }
    .link { color: #7dd3fc; cursor: pointer; transition: color .15s; }
    .link:hover { color: #a78bfa; text-decoration: underline; }
    @media (max-width: 768px) {
      .loginbox { width: 92vw; padding: 28px 22px 22px; }
    }
</style>