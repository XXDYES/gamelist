import request from './request'
import { userApi } from './user'
import { gameApi } from './games'
import { friendApi } from './friend'
import { commentApi } from './comment'

export {request}

export {userApi,gameApi,friendApi,commentApi}

export default {
  user: userApi,
  game: gameApi,
  friend:friendApi
}