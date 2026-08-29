import request from './request'
import { userApi } from './user'
import { gameApi } from './games'
import { friendApi } from './friend'

export {request}

export {userApi,gameApi,friendApi}

export default {
  user: userApi,
  game: gameApi,
  friend:friendApi
}