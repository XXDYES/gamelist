import request from './request'
import { userApi } from './user'
import { gameApi } from './games'

export {request}

export {userApi,gameApi}

export default {
  user: userApi,
  game: gameApi,
}