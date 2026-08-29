<template>
    <el-popover class="box-item" placement="bottom" trigger="click" :width="300" popper-class="friend-popover"
        @after-leave="handlePopoverClose">
        <template #reference>
            <div style="display: inline-flex; align-items: center; cursor: pointer;margin-right: 27px;">
                <el-icon :size="20" style="color: white;">
                    <User />
                </el-icon>
                <el-badge :value="friQuestList.length" :offset="[8, 2]" :max="10" :show-zero="false">
                    <span style="margin-left: 5px; color: #fff;">好友</span>
                </el-badge>
            </div>
        </template>
        <el-tabs v-model="activeName" class="">
            <el-tab-pane label="好友列表" name="first">
                <div v-for="fri in friendList" :key="fri.id" class="friend-item">
                    <el-avatar :size="40" class="friend-avatar">{{ fri.friendName.charAt(0) }}</el-avatar>
                    <div class="friend-info">
                        <span class="friend-name">{{ fri.friendName }}</span>
                        <span class="friend-date">添加于:{{ fri.createAt }}</span>
                    </div>
                    <div class="friend-actions">
                        <el-button type="success" size="small" round class="friend-btn-main">主页</el-button>
                        <el-button type="danger" size="small" round class="friend-btn-del"
                            style="margin-left: 5px;" @click="deleteFriend(fri.id)">删除</el-button>
                    </div>
                </div>
            </el-tab-pane>
            <el-tab-pane label="好友申请" name="second">
                <div v-for="fq in friQuestList" :key="fq.id" class="friend-item">
                    <el-avatar :size="40" class="friend-avatar">{{ fq.fromName.charAt(0) }}</el-avatar>
                    <div class="friend-info">
                        <span class="friend-name">{{ fq.fromName }}</span>
                        <span class="friend-date">申请于:{{ fq.createAt }}</span>
                    </div>
                    <div class="friend-actions">
                        <el-button type="success" size="small" round class="friend-btn-main" @click="agreeQuest(fq.id)">
                            同意</el-button>
                        <el-button type="danger" size="small" round class="friend-btn-del" style="margin-left: 5px;"
                            @click="rejectQuest(fq.id)">拒绝</el-button>
                    </div>
                    <div style="color: #fff;margin-left: 15px;">留言：{{ fq.message }}</div>
                </div>
            </el-tab-pane>
            <el-tab-pane label="添加好友" name="third">
                <div style="display: flex;align-items: center;justify-content: center;"">
                    <el-input v-model="searchName" placeholder="请输入名称" class="search-friend-input"
                        style="width: 220px;" clearable @clear="searchResult = 0"/>
                    <el-button type="primary" circle :icon="Search" style="width: 32px;height: 32px;font-size: 20px;"
                        @click="searchUser(searchName)">
                    </el-button>
                </div>
                <div v-if="searchResult === 2" class="search-empty">
                    <el-icon :size="36" class="search-empty-icon">
                        <CircleCloseFilled />
                    </el-icon>
                    <div class="search-empty-title">该用户不存在</div>
                    <div class="search-empty-sub">请检查名称是否输入正确</div>
                </div>
                <div v-if="searchResult === 1" style="display: flex;flex-direction: column;align-items: center;
                background-color: rgba(255, 255, 255, 0.3);margin: 10px;border-radius: 15px;animation: userIn 0.3s ease;">
                    <el-avatar :size="60" class="friend-avatar" style="margin-top: 5px;">
                        <span style="font-size: 20px;">{{ searchName.charAt(0) }}</span>
                    </el-avatar>
                    <div class="friend-info">
                        <span class="friend-name" style="font-size: 20px;">{{ searchName }}</span>
                    </div>
                    <el-input v-model="message" placeholder="请输入留言(20字以内)" style="width: 200px;
                    margin: 10px;" class="message-input" maxlength="20" :rows="3" type="textarea"/>
                    <div style="padding: 10px;">
                        <el-button class="handle-button" @click="sendQuest(userId,message)">
                            提交申请
                        </el-button>
                    </div>
                </div>
            </el-tab-pane>
        </el-tabs>
    </el-popover>
</template>
<script setup>
import { friendApi } from '@/api';
import { User, Search, CircleCloseFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { onBeforeUnmount, onMounted, ref } from 'vue';
const activeName = ref('first')
const friendList = ref([])
const friQuestList = ref([])
const searchName = ref('')
const userId = ref(0)
const searchResult = ref(0)
const message = ref('')
const getFriendList = async () => {
    try {
        const res = await friendApi.getFriendList()
        if (res.data.code == '200') {
            friendList.value = res.data.data
            console.log("好友列表：", friendList.value)
        }
    } catch (e) { ElMessage.error("获取好友列表失败") }
}
const getFriQuestList = async () => {
    try {
        const res = await friendApi.getFriQuestList()
        if (res.data.code == '200') {
            friQuestList.value = res.data.data
            console.log("好友申请列表：", friQuestList.value)
        }
    } catch (e) { ElMessage.error("获取好友申请失败") }
}
const agreeQuest = async (infoId) => {
    try {
        const res = await friendApi.agreeQuest(infoId)
        if (res.data.code == '200') {
            ElMessage.success('已同意申请')
            getFriendList()
            getFriQuestList()
        } else {
            ElMessage.error(res.data.msg || '操作失败')
        }
    } catch (e) { ElMessage.error("系统异常") }
}
const rejectQuest = async (infoId) => {
    try {
        const res = await friendApi.rejectQuest(infoId)
        if (res.data.code == '200') {
            ElMessage.success('已拒绝申请')
            getFriQuestList()
        } else {
            ElMessage.error(res.data.msg || '操作失败')
        }
    } catch (e) { ElMessage.error("系统异常") }
}
const handlePopoverClose = () => {
    searchName.value = ''
    searchResult.value = 0
    userId.value = 0
}
const searchUser = async(name) =>{
    const res = await friendApi.searchUser(name)
    if (res.data.code === '200'){
        userId.value = res.data.data
        searchResult.value = 1
    }else{
        searchResult.value = 2
    }
}
const sendQuest = async(toId,msg) => {
    try{
        const res = await friendApi.sendQuest({toId: toId,msg: msg})
        if (res.data.code == '200'){
            ElMessage.success("请求发送成功")
            message.value = ''
            searchResult.value = 0
            searchName.value = ''
            userId.value = 0
        }else{
            ElMessage.error(res.data.msg || '发送失败')
        }
    }catch(e){ElMessage.error("发送失败")}
}
const deleteFriend = async(id) => {
    const res = await friendApi.deleteFriend(id)
    if (res.data.code === '200'){
        ElMessage.success("删除成功")
        getFriendList()
    }else{
            ElMessage.error(res.data.msg || '发送失败')
        }
}
let pollTimer = null
const startPolling = () => {
    pollTimer = setInterval(() => {
        getFriQuestList()
    }, 180000)
}
const stopPolling = () => {
    if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
    }
}
onMounted(() => {
    getFriendList()
    getFriQuestList()
    startPolling()
})
onBeforeUnmount(() => {
    stopPolling()
})
</script>
<style>
.friend-item {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    margin: 0 5px 10px 5px;
    padding: 10px 12px;
    background-color: rgba(255, 255, 255, 0.06);
    border-radius: 12px;
    transition: all 0.3s ease;
}

.friend-item:hover {
    background-color: rgba(255, 255, 255, 0.12);
    transform: translateX(4px);
    box-shadow: 0 0 15px rgba(107, 137, 255, 0.25);
}

.friend-avatar {
    --el-avatar-bg-color: transparent;
    background: linear-gradient(135deg, #6b89ff, #b06bff);
    color: #fff;
    font-weight: 600;
    flex-shrink: 0;
}

.friend-info {
    position: relative;
    flex: 1;
    min-width: 0;
}

.friend-name {
    display: block;
    color: #fff;
    font-size: 15px;
    font-weight: 600;
    line-height: 1.4;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.friend-date {
    position: absolute;
    top: calc(100% + 2px);
    left: 0;
    font-size: 10px;
    color: rgba(255, 255, 255, 0.45);
    white-space: nowrap;
}

.friend-actions {
    display: flex;
    flex-shrink: 0;
}

.friend-btn-main,
.friend-btn-del {
    border: none !important;
    color: #fff !important;
    transition: all 0.3s ease;
}

.friend-btn-main {
    background: linear-gradient(135deg, #38ef7d, #00b894) !important;
}

.friend-btn-main:hover {
    background: linear-gradient(135deg, #6ef7a8, #00d4aa) !important;
    box-shadow: 0 0 12px rgba(56, 239, 125, 0.5);
}

.friend-btn-del {
    background: linear-gradient(135deg, #ff6b6b, #e0245e) !important;
}

.friend-btn-del:hover {
    background: linear-gradient(135deg, #ff8a8a, #ff416c) !important;
    box-shadow: 0 0 12px rgba(255, 107, 107, 0.5);
}

.search-friend-input .el-input__wrapper {
    border-radius: 50px;
    margin-right: 5px;
}

.friend-popover {
    --el-popover-bg-color: rgba(64, 65, 69, 0.95);
    --el-popover-border-color: rgba(255, 255, 255, 0.15);
    height: 400px;
    overflow: hidden;
    padding: 5px;
    transform: translateX(-20px);
    color: #fff;
    display: flex;
    flex-direction: column;
}

.friend-popover .el-tabs {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
}

.friend-popover .el-tabs__content {
    flex: 1;
    overflow-y: auto;
    min-height: 0;
}

.friend-popover .el-tabs__nav-wrap::after {
    background-color: rgba(255, 255, 255, 0.12);
}

.friend-popover .el-tabs__item {
    color: rgba(255, 255, 255, 0.6);
    font-size: 14px;
}

.friend-popover .el-tabs__item:hover {
    color: #fff;
}

.friend-popover .el-tabs__item.is-active {
    color: #fff;
    font-weight: 600;
}

.friend-popover .el-tabs__active-bar {
    background: linear-gradient(90deg, #6b89ff, #b06bff, #38ef7d);
    height: 3px;
    border-radius: 3px;
}
.message-input .el-textarea__inner{
    border-radius: 8px !important;
    box-shadow: 0 0 0 1px #dcdfe6 inset !important;
    transition: all 0.3s ease;
    resize: none;
}
.handle-button{
    background: linear-gradient(135deg, #38ef7d 0%, #00b894 100%);
    border: 0;color: #fff;
    transition: all 0.3s ease;
}
.handle-button.el-button:hover{
    color: #fff !important;
    background: linear-gradient(135deg, #6ef7a8, #00d4aa) !important;
    transform: scale(1.05);
}
.search-empty{
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 30px 0;
    animation: searchEmptyIn 0.3s ease;
}
.search-empty-icon{
    color: rgba(255, 107, 107, 0.75);
}
.search-empty-title{
    font-size: 16px;
    color: rgba(255, 255, 255, 0.7);
}
.search-empty-sub{
    font-size: 12px;
    color: rgba(255, 255, 255, 0.35);
}
@keyframes searchEmptyIn{
    from{ opacity: 0; transform: translateY(-6px); }
    to{ opacity: 1; transform: translateY(0); }
}
@keyframes userIn{
    from{ opacity: 0; transform: translateY(6px); }
    to{ opacity: 1; transform: translateY(0); }
}
</style>
