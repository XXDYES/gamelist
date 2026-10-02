<template>
    <el-drawer v-model="drawerVisible" :modal="false" modal-penetrable :show-close="false" 
    class="chat-drawer" @open="onDrawerOpen()">
        <template #header>
            <el-avatar :size="50" style="flex: none;margin-right: 10px;" :src="nailongava" fit="cover" />
            <span style="font-size: 25px;line-height: 1;">游戏高手：主机龙</span>
            <el-tooltip class="box-item" effect="dark" placement="bottom" :disabled="!drawerVisible">
                    <template #content>
                        <div>AI助手：主机龙，使用deepseek-v4-flash模型</div>
                        <div>人设：喜欢单机游戏，讨厌二次元抽卡手游</div>
                        <div>每天使用额度:100W TOKEN</div>
                        <div>流式输出，首字延迟约1-3s</div>
                        <div>上下文记忆：20轮对话，历史24H过期</div>
                        <div>内容为AI生成，不代表作者观点(原神NB😤)</div>
                    </template>
                    <span style="display: inline-flex; align-items: center;margin-left: 10px;">
                        <el-icon :size="18">
                            <InfoFilled />
                        </el-icon>
                    </span>
                </el-tooltip>
            <el-button style="margin-left: auto;padding: 5px;font-size: 20px;" :icon="Close" type="danger"
                @click="drawerVisible = false"></el-button>
        </template>
        <!-- 助手靠左、用户靠右，间距由 .msg-list 的 gap 控制 -->
        <div class="msg-list" ref="msgListRef">
            <template v-for="(chat, i) in chatHistory" :key="i">
                <div v-if="chat.role == 'assistant'" class="msg-row">
                    <el-avatar :size="40" class="msg-avatar" :src="nailongava" fit="cover" />
                    <div class="bubble bubble-bot">{{ chat.content }}</div>
                </div>
                <div v-else-if="chat.role == 'user'" class="msg-row is-user">
                    <div class="bubble bubble-user">{{ chat.content }}</div>
                    <el-avatar :size="40" class="friend-avatar">{{ userStore.username.charAt(0) }}</el-avatar>
                </div>
            </template>
            <div v-if="sending && !answer" class="msg-row">
                <el-avatar :size="40" class="msg-avatar" :src="nailongava" fit="cover" />
                <div class="bubble bubble-bot typing" role="status" aria-label="正在思考"><i></i><i></i><i></i></div>
            </div>
            <div v-if="answer" class="msg-row">
                <el-avatar :size="40" class="msg-avatar" :src="nailongava" fit="cover" />
                <div class="bubble bubble-bot">{{ answer }}</div>
            </div>
        </div>
        <template #footer>
            <div style="text-align: left;padding: 2px 15px;display: flex;gap: 7px;align-items: center;">
                <div>常见问题：</div>
                <span class="question" @click="sendMsg('你是？')">你是？</span>
                <span class="question" @click="sendMsg('原神好玩吗？')">原神好玩吗?</span>
                <span class="question" @click="sendMsg('根据我的游戏库，给我推荐一款游戏')">推荐一款游戏</span>
            </div>
            <div class="drawer-footer">
                <textarea v-model="msg" placeholder="聊聊游戏那些事" class="msgarea" :rows="4" 
                @keydown.enter="sendMsg(msg)"></textarea>
                <div class="send-row">
                    <div style="margin-right: 10px;color: gray;">DeepSeek-Flash</div>
                    <button class="chat-send" type="button" :disabled="sending" @click="sendMsg(msg)">
                        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor"
                            stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M12 19V6" />
                            <path d="M5.5 12.5 12 6l6.5 6.5" />
                        </svg>
                    </button>
                </div>
            </div>
        </template>
    </el-drawer>
</template>
<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import nailongava from '@/assets/naiwa_thinking.png'
import { Close,InfoFilled } from '@element-plus/icons-vue';
import { API_BASE } from '@/api/base'
import { useUserStore } from '@/store/user'
import { chatApi } from '@/api';
import { ElMessage } from 'element-plus';

const userStore = useUserStore()
const BASE = API_BASE   // EventSource 不能复用 axios 实例，但共用同一个基地址常量
const props = defineProps({ visible: { type: Boolean, default: false } })
const emits = defineEmits(["update:visible"])
const msg = ref("")
const chatHistory = ref([])
const clientId = ref('')
const answer = ref('')
const sending = ref(false)
const msgListRef = ref(null)
let scrollFrame = null
let autoFollow = true
let bodyBound = false
let es = null

const drawerVisible = computed({
    get: () => props.visible,
    set: (val) => emits('update:visible', val)
})
const disconnect = () => {
    if (es) {
        es.close()
        es = null
    }
}
const connect = () => {
    disconnect()
    const url = `${BASE}/chatconnect?clientId=${encodeURIComponent(clientId.value)}`
    es = new EventSource(url)
    es.onmessage = (e) => { 
        if (e.data == 'DONE' || e.data == '"DONE"') {
            sending.value = false
            if (answer.value) {
                chatHistory.value.push({ role: 'assistant', content: answer.value })
            }
            answer.value = ''
            scrollToBottomThrottled()
            return
        }
        let chunk
        try { chunk = JSON.parse(e.data) } catch (err) { chunk = e.data }
        answer.value += chunk
        scrollToBottomThrottled()
    }
}
const bodyEl = () => msgListRef.value?.closest('.el-drawer__body')
const onBodyScroll = () => {
    const el = bodyEl()
    if (!el) return
    autoFollow = el.scrollHeight - el.scrollTop - el.clientHeight < 120
}
const bindBodyScroll = async () => {
    await nextTick()
    const el = bodyEl()
    if (el && !bodyBound) {
        bodyBound = true
        el.addEventListener('scroll', onBodyScroll, { passive: true })
    }
}
const scrollToBottom = async (force = false) => {
    const el = bodyEl()
    if (!el) return
    const stick = force || autoFollow
    await nextTick()
    if (stick) el.scrollTop = el.scrollHeight
}
const scrollToBottomThrottled = () => {
    if (scrollFrame) return
    scrollFrame = requestAnimationFrame(() => {
        scrollFrame = null
        scrollToBottom()
    })
}
const onDrawerOpen = () => {
    bindBodyScroll()
    getHistory()
}
const sendMsg = async (text) => {
    if (!text || !text.trim()) return ElMessage.warning('先输入内容')
    if (sending.value) return
    sending.value = true
    answer.value = ''
    chatHistory.value.push({role: 'user', content: text })
    try {
        const res = await chatApi.chatMsg(text)
        if (res.data.code == '200') {
            msg.value = ''
            scrollToBottom(true)
        } else {
            ElMessage.error(res.data.msg || "发送消息失败")
            sending.value = false
        }
    } catch (e) {
        ElMessage.error("发送消息失败")
        sending.value = false
    }
}
const getHistory = async() => {
    const res = await chatApi.getChatHistory()
    if(res.data.code == '200'){
        chatHistory.value = res.data.data
        scrollToBottom(true)
    }else{ElMessage.error("获取对话历史失败")}
}
onMounted(async () => {
    try {
        await userStore.fetchUserInfo()
        clientId.value = String(userStore.id ?? '')
    } catch (e) {
    }
    connect()   // 进页面自动连
})
onBeforeUnmount(disconnect)
</script>
<style>
.chat-drawer {
    --chat-panel: #f4f6f9;
    --chat-head: #e9eef4;
    --chat-line: #dde4ec;
    --chat-card: #ffffff;
    --chat-text: #303133;
    --chat-muted: #a3aab3;
    --chat-accent-bg: #e7f1fb;
    --chat-accent: #3d7fb8;

    background-color: var(--chat-panel);
    border-left: 1px solid var(--chat-line);
    box-shadow: -10px 0 30px rgba(31, 45, 61, .18);
}

.chat-drawer .el-drawer__header {
    margin-bottom: 8px;
    padding: 8px 10px;
    background-color: var(--chat-head);
    border-bottom: 1px solid var(--chat-line);
    color: var(--chat-text);
    margin-bottom: 0;
}

.chat-drawer .el-drawer__body {
    background-color: var(--chat-panel);
    color: var(--chat-text);
    padding: 20px 10px;
}

.chat-drawer .el-drawer__footer {
    background-color: var(--chat-panel);
    color: var(--chat-text);
    text-align: left;
}

/* ---------- 消息区：助手靠左（白卡片），用户靠右（蓝底白字） ---------- */
.chat-drawer .msg-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
    text-align: left;
}

.chat-drawer .msg-row {
    display: flex;
    align-items: flex-start;
    gap: 10px;
}

.chat-drawer .msg-row.is-user {
    justify-content: flex-end;
}

.chat-drawer .msg-avatar {
    flex: none;
}

.chat-drawer .bubble {
    max-width: 78%;
    padding: 10px 14px;
    border-radius: 14px;
    font-size: 15px;
    line-height: 1.7;
    text-align: justify;
    white-space: pre-wrap;
    word-break: break-word;
    overflow-wrap: anywhere;
}

/* 助手气泡：白底卡片，左上角收成小圆角，看起来"贴"着头像 */
.chat-drawer .bubble-bot {
    background-color: var(--chat-card);
    border: 1px solid var(--chat-line);
    box-shadow: 0 1px 2px rgba(31, 45, 61, .05);
    border-top-left-radius: 4px;
}

/* 用户气泡：强调蓝底 + 白字，右上角收角 */
.chat-drawer .bubble-user {
    background-color: var(--chat-accent);
    border-top-right-radius: 4px;
    color: #fff;
}

.chat-drawer .typing {
    display: flex;
    align-items: center;
    gap: 5px;
}

.chat-drawer .typing i {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: currentColor;
    opacity: .2;
    animation: typing-breath 1.6s infinite ease-in-out;
}

.chat-drawer .typing i:nth-child(2) {
    animation-delay: .25s;
}

.chat-drawer .typing i:nth-child(3) {
    animation-delay: .5s;
}

@keyframes typing-breath {

    0%,
    100% {
        opacity: .2;
    }

    50% {
        opacity: .85;
    }
}

@media (prefers-reduced-motion: reduce) {
    .chat-drawer .typing i {
        animation: none;
        opacity: .5;
    }
}

/* 输入卡片：白底 + 细边 + 一点投影，从浅灰底里浮出一层 */
.chat-drawer .drawer-footer {
    background-color: var(--chat-card);
    border: 1.5px solid var(--chat-line);
    box-shadow: 0 1px 2px rgba(31, 45, 61, .06);
    display: flex;
    flex-direction: column;
    gap: 5px;
    border-radius: 15px;
    padding: 15px 15px 5px 15px;
}

.chat-drawer .drawer-footer .msgarea {
    border: none;
    background-color: transparent;
    color: var(--chat-text);
    font-size: 15px;
    line-height: 1.2;
    text-align: left;
    resize: none;
    outline: none;
    display: block;
    scrollbar-gutter: stable;
}

.chat-drawer .drawer-footer .msgarea::placeholder {
    color: var(--chat-muted);
}

.chat-drawer .drawer-footer .msgarea::-webkit-scrollbar {
    width: 10px;
}

.chat-drawer .drawer-footer .msgarea::-webkit-scrollbar-track {
    background-color: transparent;
}

.chat-drawer .drawer-footer .msgarea::-webkit-scrollbar-thumb {
    background-color: rgba(144, 160, 176, .55);
    background-clip: padding-box;
    border: 3px solid transparent;
    border-radius: 10px;
}

.chat-drawer .drawer-footer .msgarea::-webkit-scrollbar-thumb:hover {
    background-color: rgba(93, 112, 130, .8);
}

.chat-drawer .drawer-footer .msgarea::-webkit-scrollbar-corner {
    background-color: transparent;
}

.chat-drawer .drawer-footer .send-row {
    display: flex;
    justify-content: flex-end;
    align-items: center;
}

.chat-drawer .drawer-footer .chat-send {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex: none;
    width: 36px;
    height: 36px;
    padding: 0;
    border: none;
    border-radius: 50%;
    background-color: #e4e4e4;
    color: #2f2f2f;
    cursor: pointer;
    transition: background-color .15s ease, transform .15s ease, opacity .15s ease;
}

.chat-drawer .drawer-footer .chat-send:not(:disabled):hover {
    background-color: #f2f2f2;
    transform: translateY(-1px);
}

.chat-drawer .drawer-footer .chat-send:not(:disabled):active {
    transform: translateY(0) scale(.96);
}

.chat-drawer .drawer-footer .chat-send:disabled {
    opacity: .35;
    cursor: default;
    transform: none;
}

.chat-drawer .question {
    background-color: var(--chat-accent-bg);
    color: var(--chat-accent);
    padding: 6px;
    border-radius: 10px;
    cursor: pointer;
    transition: background-color .15s ease;
}

.chat-drawer .question:hover {
    background-color: #d6e8f9;
}
</style>
