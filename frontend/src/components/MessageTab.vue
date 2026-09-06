<template>
    <el-popover class="box-item" placement="bottom" trigger="click" :width="560" popper-class="message-popover">
        <template #reference>
            <div style="display: inline-flex; align-items: center; cursor: pointer;">
                <el-icon :size="20" style="color: white;">
                    <Message />
                </el-icon>
                <el-badge :value="messageList.length" :offset="[-19, 2]" :max="10" :show-zero="false">
                    <span style="margin: 0 25px 0 5px;color: #fff;">消息</span>                   
                </el-badge>
            </div>
        </template>
        <div class="msg-title">📨 消息列表</div>
        <div v-if="messageList.length" class="msg-list">
            <div v-for="msg in messageList" :key="msg.id" class="msg-item">
                <!-- type=2：分享游戏，带同意/拒绝 -->
                <div v-if="msg.type == 2" class="msg-card">
                    <div class="msg-card-head">
                        <el-avatar :size="40" class="friend-avatar">{{ msg.userName?.charAt(0) || '?' }}</el-avatar>
                        <div class="msg-info">
                            <div class="msg-name-row">
                                <span class="msg-name">{{ msg.userName }}</span>
                            </div>
                            <div class="msg-content">
                                向你推荐：<span class="msg-game">🎮 {{ msg.gameName }}</span>
                            </div>
                        </div>
                        <div class="msg-actions">
                            <el-button round class="friend-btn-main">同意</el-button>
                            <el-button round class="friend-btn-del">拒绝</el-button>
                        </div>
                    </div>
                    <div class="msg-footer">
                        <div v-if="msg.message" class="msg-note">💬 留言：{{ msg.message || '无' }}</div>
                        <span class="msg-time">{{ msg.createAt }}</span>
                    </div>
                </div>
                <!-- type=1：文本通知 -->
                <div v-else class="msg-card msg-card-text">
                    <el-avatar :size="40" class="friend-avatar">{{ msg.userName?.charAt(0) || '?' }}</el-avatar>
                    <div class="msg-info">
                        <div class="msg-name-row">
                            <span class="msg-name">{{ msg.userName }}</span>
                        </div>
                        <div class="msg-content">{{ msg.message }}</div>
                        <div class="msg-footer">
                            <span class="msg-time">{{ msg.createAt }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div v-else class="msg-empty">暂无消息</div>
    </el-popover>
</template>
<script setup>
import { messageApi } from '@/api/message';
import { Message } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { onBeforeUnmount, onMounted, ref } from 'vue';
const messageList = ref([])
const getMessage = async () => {
    const res = await messageApi.getMessage()
    if (res.data.code === '200') {
        messageList.value = res.data.data
    } else { ElMessage.error('获取消息列表失败') }
}
let pollTimer = null
const startPoll = () => {
    pollTimer = setInterval(() => {
        getMessage()
    },170000)
}
const stopPoll = () => {
    if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
    }
}
onMounted(() => {
    getMessage()
    startPoll()
})
onBeforeUnmount(() => {
    stopPoll()
})
</script>
<style>
/* ===== 消息弹层（效仿 FriendTab 深色视觉） ===== */
.message-popover {
    --el-popover-bg-color: rgba(64, 65, 69, 0.95);
    --el-popover-border-color: rgba(255, 255, 255, 0.15);
    height: 400px;
    overflow: hidden;
    padding: 8px 10px;
    transform: translateX(-20px);
    color: #fff;
    display: flex;
    flex-direction: column;
}

.message-popover .msg-title {
    padding: 4px 6px 10px;
    font-size: 15px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.9);
    border-bottom: 1px solid rgba(255, 255, 255, 0.12);
    margin-bottom: 8px;
}

.message-popover .msg-list {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    overflow-x: hidden;
    scrollbar-width: thin;                        /* Firefox 细滚动条 */
    scrollbar-color: rgba(255, 255, 255, 0.25) transparent;
}

.message-popover .msg-list::-webkit-scrollbar {
    width: 6px;
}

.message-popover .msg-list::-webkit-scrollbar-track {
    background: transparent;
}

.message-popover .msg-list::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.25);
    border-radius: 3px;
    transition: background 0.2s ease;
}

.message-popover .msg-list::-webkit-scrollbar-thumb:hover {
    background: rgba(255, 255, 255, 0.45);
}

.message-popover .msg-item {
    margin: 0 5px 10px 5px;
    transition: all 0.3s ease;
}

.message-popover .msg-card {
    padding: 10px 12px;
    background-color: rgba(255, 255, 255, 0.06);
    border-radius: 12px;
    transition: all 0.3s ease;
}

.message-popover .msg-card:hover {
    background-color: rgba(255, 255, 255, 0.12);
    transform: translateX(4px);
    box-shadow: 0 0 15px rgba(107, 137, 255, 0.25);
}

.message-popover .msg-card-head {
    display: flex;
    align-items: center;
    gap: 12px;
}

.message-popover .msg-card-text {
    display: flex;
    align-items: flex-start;
    gap: 12px;
}

.message-popover .msg-info {
    flex: 1;
    min-width: 0;
}

.message-popover .msg-name-row {
    display: flex;
    align-items: baseline;
}

.message-popover .msg-name {
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.message-popover .msg-time {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.45);
    white-space: nowrap;
    flex-shrink: 0;
}

.message-popover .msg-content {
    margin-top: 4px;
    font-size: 15px;
    color: rgba(255, 255, 255, 0.75);
    line-height: 1.5;
    word-break: break-word;
}

.message-popover .msg-game {
    color: #7ee8fa;
    font-weight: 600;
}

.message-popover .msg-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
}

.message-popover .msg-actions .el-button + .el-button {
    margin-left: 0;   /* 覆盖 Element Plus 相邻按钮默认 12px 间距 */
}

.message-popover .msg-note {
    font-size: 15px;               /* 留言字号 */
    color: rgba(255, 255, 255, 0.65);
    word-break: break-word;
    flex: 1;
    min-width: 0;
    margin-left: 52px;             /* 与"向你推荐"同一列（头像40+间距12） */
}

.message-popover .msg-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    margin-top: 8px;
    padding-right: 0;
}

.message-popover .msg-empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.4);
}
</style>
