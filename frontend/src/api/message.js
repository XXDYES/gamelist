import request from "./request"

export const messageApi = {
    addShareMsg(data){
        return request.get("/addsharemsg", { params: data })
    },
    getMessage(){
        return request.get("/getmessage")
    }
}
