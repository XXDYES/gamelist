import request from "./request";

export const commentApi = {
    getComment(){
        return request.get('/selectcomment')
    },
    getFriComment(friId){
        return request.get('/getfricomment',{params:{friId:friId}})
    },
    addComment(data){
        return request.post('/addcomment',data)
    },
    deleteComment(id){
        return request.get('/deletecomment',{params:{cmtId:id}})
    }
}