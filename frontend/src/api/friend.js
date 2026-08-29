import request from './request'

export const friendApi = {
    getFriendList(){
        return request.get('/friendlist')
    },
    getFriQuestList(){
        return request.get('/friquestlist')
    },
    agreeQuest(infoId){
        return request.get('/agreequest',{params:{infoId:infoId}})
    },
    rejectQuest(infoId){
        return request.get('/rejectquest',{params:{infoId:infoId}})
    },
    searchUser(name){
        return request.get('/searchuser',{params:{name:name}})
    },
    sendQuest(data){
        return request.get('/sendquest',{params: data})
    },
    deleteFriend(id){
        return request.get('/deletefriend',{params: { id:id }})
    }
}
