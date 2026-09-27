<template>
    <div class="sse-page">
        <h2>SSE 对话测试</h2>

        <div class="bar">
            <span class="label">clientId（= 当前用户 ID）</span>
            <el-input v-model="clientId" style="width: 120px;" />
            <el-button type="primary" @click="connect" :disabled="esState === 'OPEN'">连接</el-button>
            <el-button type="danger" @click="disconnect" :disabled="esState !== 'OPEN'">断开</el-button>
        </div>

        <div class="bar">
            <el-input v-model="msg" placeholder="输入要问的内容，例如：用三句话介绍原神"
                style="width: 460px;" @keyup.enter="send" />
            <el-button type="success" @click="send" :disabled="!connected">发送</el-button>
            <el-button @click="clearAll">清空</el-button>
        </div>

        <div class="meta">
            <span>EventSource：{{ esState }}</span>
            <span>当前用户：{{ userStore.username }}（id={{ userStore.id }}）</span>
            <span>收到增量：{{ deltaCount }} 个 / {{ answer.length }} 字</span>
        </div>

        <!-- 模型回答：增量逐字追加 -->
        <div class="answer">
            <div v-if="!answer" class="placeholder">（模型的回答会逐字出现在这里）</div>
            <span v-else>{{ answer }}<span class="cursor">▌</span></span>
        </div>

        <!-- 原始事件日志，默认收起 -->
        <details class="raw" open>
            <summary>原始事件日志（{{ logs.length }} 条）</summary>
            <div class="console">
                <div v-for="(l, i) in logs" :key="i" :class="['line', l.level]">{{ l.text }}</div>
            </div>
        </details>
    </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/store/user'
import { API_BASE } from '@/api/base'

const BASE = API_BASE   // EventSource 不能复用 axios 实例，但共用同一个基地址常量
const userStore = useUserStore()

const clientId = ref('')
const msg = ref('')
const answer = ref('')
const deltaCount = ref(0)
const esState = ref('未连接')
const logs = ref([])

const connected = computed(() => esState.value === 'OPEN')
let es = null

const add = (text, level = '') => {
    logs.value.push({ text: `[${new Date().toLocaleTimeString()}] ${text}`, level })
}

/** 连接：clientId 用当前用户 id，和 /chatmsg 推送时用的 key 必须一致 */
const connect = () => {
    disconnect()
    if (!clientId.value) {
        ElMessage.warning('还没拿到当前用户 id')
        return
    }
    const url = `${BASE}/chatconnect?clientId=${encodeURIComponent(clientId.value)}`
    esState.value = '连接中…'
    add('连接：' + url)

    es = new EventSource(url)
    es.onopen = () => {
        esState.value = 'OPEN'
        add('onopen —— 连接已建立')
    }
    // 握手帧是具名事件 connect
    es.addEventListener('connect', e => add(`握手帧：${e.data}`))
    // 正文增量没有 event 名，走默认的 message
    es.onmessage = e => {
        if (e.data === '[DONE]') {
            add('收到结束标记 [DONE]')
            return
        }
        // 后端把增量转义成 JSON 字符串发过来（否则换行会破坏 SSE 分帧），这里还原；
        // catch 是过渡期兜底：后端还没重启时收到的是裸文本
        let chunk
        try { chunk = JSON.parse(e.data) } catch (err) { chunk = e.data }
        answer.value += chunk
        deltaCount.value++
    }
    es.onerror = () => {
        const st = es ? es.readyState : -1
        esState.value = `ERROR (readyState=${st})`
        add(`onerror —— readyState=${st}（0=连接中 1=已打开 2=已关闭）`, 'err')
    }
}

const disconnect = () => {
    if (es) {
        es.close()
        es = null
        if (esState.value === 'OPEN') add('EventSource.close()')
        esState.value = '已关闭'
    }
}

/** 发送消息：普通 HTTP 请求触发后端调模型，正文稍后由 SSE 推回来 */
const send = async () => {
    const text = msg.value.trim()
    if (!text) return ElMessage.warning('先输入内容')
    if (!connected.value) return ElMessage.warning('SSE 还没连上')

    answer.value = ''
    deltaCount.value = 0
    add(`发送 /chatmsg：${text}`)
    try {
        const res = await request.get('/chatmsg', { params: { msg: text } })
        add(`/chatmsg 返回 code=${res.data.code}（空响应体，正文走 SSE）`)
    } catch (e) {
        add('发送失败：' + e.message, 'err')
        ElMessage.error('发送失败')
    }
}

const clearAll = () => {
    answer.value = ''
    deltaCount.value = 0
    logs.value = []
}

onMounted(async () => {
    try {
        await userStore.fetchUserInfo()
        clientId.value = String(userStore.id ?? '')
        add(`当前用户：${userStore.username}  id=${clientId.value}`)
    } catch (e) {
        add('获取当前用户信息失败：' + e.message, 'err')
    }
    // connect()   // 进页面自动连
})

onBeforeUnmount(() => disconnect())
</script>

<style scoped>
.sse-page {
    min-height: 100vh;
    padding: 24px 32px;
    background: #1e2228;
    color: #e6e6e6;
    font-family: "Consolas", "Menlo", monospace;
}

.sse-page h2 {
    margin: 0 0 18px;
    font-size: 20px;
    font-weight: 600;
}

.bar {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
    flex-wrap: wrap;
}

.label {
    font-size: 13px;
    color: #9aa4b2;
}

.meta {
    display: flex;
    gap: 24px;
    margin: 12px 0;
    font-size: 13px;
    color: #9aa4b2;
}

.answer {
    min-height: 180px;
    max-height: 40vh;
    overflow-y: auto;
    padding: 16px 18px;
    background: #14171c;
    border: 1px solid #2e343d;
    border-radius: 8px;
    font-size: 15px;
    line-height: 1.9;
    text-align: left;
    /* white-space: pre-wrap; */
    word-break: break-word;
}

.answer .placeholder {
    color: #6b7280;
    font-size: 13px;
}

.cursor {
    color: #7ee787;
    animation: blink 1s step-start infinite;
}

@keyframes blink {
    50% {
        opacity: 0;
    }
}

.raw {
    margin-top: 14px;
    font-size: 13px;
    color: #9aa4b2;
}

.raw summary {
    cursor: pointer;
    padding: 4px 0;
}

.console {
    height: 220px;
    overflow-y: auto;
    padding: 10px 14px;
    background: #0d1013;
    border: 1px solid #2e343d;
    border-radius: 6px;
    font-size: 12px;
    line-height: 1.7;
}

.console .line {
    white-space: pre-wrap;
    word-break: break-all;
}

.console .line.err {
    color: #ff7b72;
}
</style>
