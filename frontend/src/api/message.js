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
    },
    rejectShare(infoId){
        return request.get('/rejectshare',{ params: {infoId:infoId} })
    },
    acceptShare(infoId){
        return request.get('/acceptshare',{params:{infoId:infoId}})
    },
    applyShare(toId,gameId){
        return request.get('/applyshare',{params:{toId:toId,gameId:gameId}})
    },
    acceptApply(infoId){
        return request.get('/acceptapply',{params:{infoId:infoId}})
    },
    rejectApply(infoId){
        return request.get('/rejectapply',{params:{infoId:infoId}})
    }
}
