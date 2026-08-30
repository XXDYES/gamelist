import { defineStore } from "pinia";
import { ref } from "vue";
import { userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
    const username = ref('未登录')
    const id = ref(null)

    async function fetchUserInfo() {
        const res = await userApi.getUserInfo()
        if (res.data.code === '200') {
            username.value = res.data.data.username
            id.value = res.data.data.id
        }
    }
    function clear() {
        username.value = '未登录'
        id.value = null
    }
    return {username,id,fetchUserInfo,clear}
})
