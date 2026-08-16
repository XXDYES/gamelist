import request from './request'

export const gameApi = {
  // 获取游戏列表
  getGameList(userId) {
    return request.get('/getlist', { params: { userId } })
  },
  getAiResponse(gameName){
    return request.get('/ai',{params: {gameName}})
  },
  insertGame(game){
    return request.post('/addgame',game)
  },
  setPlayed(data){
    return request.post('/setplayed',data)
  },
  deleteGame(gameId){
    return request.get('/deletegame',{params:{gameId}})
  }
}