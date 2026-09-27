import axios from "axios";
import router from "@/router";
import { ElMessage } from "element-plus";
import { API_BASE } from "./base";

const request = axios.create({
    baseURL: API_BASE,
    timeout: 40000,
    withCredentials: true,
})
// 请求拦截器：自动携带Token
request.interceptors.request.use(
    (config)=>{
        const token = localStorage.getItem("userToken")
        if (token){
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error)=>Promise.reject(error)
)
// 响应拦截器：统一处理响应
request.interceptors.response.use(
    (response)=>{
       return response
    },
    (error)=>{
        if(error.response?.status === 401){
            localStorage.removeItem("userToken")
            ElMessage.error("登录已过期，请重新登录")
            router.push("/login")
        }else{
            ElMessage.error(error.message || '网络异常')
        }
        return Promise.reject(error)
    }
)
export default request
