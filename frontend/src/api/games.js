import request from './request'

export const gameApi = {
  // 获取游戏列表
  getGameList(userId) {
    return request.get('/getlist', { params: { userId } })
  }
}