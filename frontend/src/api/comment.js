import request from "./request";

export const commentApi = {
    getComment(){
        return request.get('/selectcomment')
    },
    addComment(data){
        return request.post('/addcomment',data)
    },
    deleteComment(id){
        return request.get('/deletecomment',{params:{cmtId:id}})
    }
}