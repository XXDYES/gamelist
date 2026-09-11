import request from "./request"

export const messageApi = {
    addShareMsg(data){
        return request.get("/addsharemsg", { params: data })
    },
    getMessage(){
        return request.get("/getmessage")
    },
    addCmtMessage(data){
        return request.get('/addcmtmessage',{params:data})
    },
    comfirmMessage(infoId){
        return request.get('/comfirmmessage',{params:{infoId:infoId}})
    }
}
