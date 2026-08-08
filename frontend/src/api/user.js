import request from './request'

export const userApi = {
  // 登录
  login(data) {
    return request.post('/login', data)
  },
  
  // 注册
  register(data) {
    return request.post('/register', data)
  },
  
  // 获取用户信息
  getUserInfo() {
    return request.get('/user/info')
  }
}