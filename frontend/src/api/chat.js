import request from "./request";

export const chatApi = {
    getChatHistory(){
        return request.get('/gethistory')
    },
    chatMsg(msg){
        return request.get('/chatmsg',{params:{msg:msg}})
    }
}