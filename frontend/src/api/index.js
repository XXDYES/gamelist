import request from './request'
import { userApi } from './user'
import { gameApi } from './games'
import { friendApi } from './friend'
import { commentApi } from './comment'
import { chatApi } from './chat'
export {request}

export {userApi,gameApi,friendApi,commentApi,chatApi}

export default {
  user: userApi,
  game: gameApi,
  friend:friendApi
}