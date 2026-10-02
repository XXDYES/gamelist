<template>
    <el-collapse v-model="activeNames" accordion class="custom-collapse" @change="resetComment">
        <div v-if="games.length === 0" class="empty-tip">此处无数据，快添加吧</div>
        <el-collapse-item v-else v-for="game in games" :key="game.id" :name="String(game.id)">
            <template #title>
                <el-scrollbar class="itembox-scroll" @mousedown.capture="onBarMousedown" @click="onBarClick">
                    <div class="itembox-content">
                        <span style="font-size: 26px;">🎯{{ game.name }}</span>
                        <span class="game-tag">🏢:{{ game.company }}</span>
                        <span class="game-tag">🖥️:{{ game.platform }}</span>
                        <span class="game-tag">🕹️:{{ game.type }}</span>
                    </div>
                </el-scrollbar>
                <div @click.stop style="display: flex; align-items: center; flex: 0 0 auto; margin-left: auto;">
                    <el-rate v-model="game.rating" v-if="game.played != 0" :texts="['拉完了', '拉', 'NPC', '夯', '夯爆了']"
                        size="large" show-text style="pointer-events: auto !important;" @change="setRating(game)"
                        :disabled="readonly" />
                    <template v-if="!readonly">
                        <el-popover trigger="click" placement="left" :width="100" popper-class="status-popover">
                            <el-radio-group v-model="playStatus"
                                style="display: flex; flex-direction: column; align-items: flex-start; gap: 5px;">
                                <el-radio :value="0">🕜️待玩</el-radio>
                                <el-radio :value="1">✅️已玩</el-radio>
                                <el-radio :value="2">❌️弃坑</el-radio>
                            </el-radio-group>
                            <div style="display: flex; justify-content: center; margin: 10px 0;">
                                <el-button type="primary" @click="confirmStatus(game)">确认</el-button>
                            </div>
                            <template #reference>
                                <el-button type="success" :icon="Select" circle style="width: 40px; height: 40px; font-size: 20px;margin-left: 6px;
                                pointer-events: auto !important;" @click="playStatus = game.played"></el-button>
                            </template>
                        </el-popover>
                        <el-tooltip class="box-item" effect="dark" content="编辑信息" placement="top">
                            <el-button type="primary" :icon="Edit" circle style="width: 40px; height: 40px; font-size: 20px;margin-left: 6px;
                            pointer-events: auto !important;"
                                @click="editDialog = true; editTarget = game"></el-button>
                        </el-tooltip>
                        <el-tooltip class="box-item" effect="dark" content="分享信息" placement="top">
                            <el-button type="warning" :icon="Share" circle style="width: 40px; height: 40px; font-size: 20px;margin-left: 6px;
                            pointer-events: auto !important;" @click="shareDialog = true, editTarget = game"></el-button>
                        </el-tooltip>
                        <el-tooltip class="box-item" effect="dark" content="删除信息" placement="top">
                            <el-button type="danger" :icon="Delete" circle style="width: 40px; height: 40px; font-size: 20px;margin-left: 6px;
                            pointer-events: auto !important;"
                                @click="deleteDialog = true; deleteTarget = game"></el-button>
                        </el-tooltip>
                    </template>
                    <template v-if="readonly">
                        <el-tooltip class="box-item" effect="dark" content="请求共享" placement="top">
                            <el-button type="warning" :icon="Collection" circle style="width: 40px; height: 40px; font-size: 20px;margin-left: 6px;
                            pointer-events: auto !important;" @click="applyShare(game.id)"></el-button>
                        </el-tooltip>
                    </template>
                </div>
            </template>
            <div style="display: flex;gap: 10px;">
                <span class="game-tag">💰:{{ game.price }}</span>
                <span class="game-tag">MC评分:{{ game.mcRating }}</span>
                <span class="game-tag">📅发售日期:{{ game.releaseDate }}</span>
                <span class="game-tag">➕添加日期:{{ game.addDate }}</span>
            </div>
            <div class="divider-label">📖 游戏介绍</div>
            <div style="text-align: left;text-indent: 2em;">
                <div style="text-indent: 2em;">{{ game.info }}</div>
            </div>
            <div class="divider-label">💬 游戏评价（{{ (comments[game.id] || []).length }}）</div>
            <div style="display: flex;align-items: stretch;gap: 10px;padding: 10px;">
                <el-avatar :size="50" class="friend-avatar">
                    <span style="font-size: 20px;">{{ userStore.username.charAt(0) }}</span>
                </el-avatar>
                <el-input v-model="comment" placeholder="请输入评价(400字以内)" style="width: 80%;" :rows="2"
                    class="message-input" type="textarea" />
                <div style="display: flex;flex-direction: column;">
                    <el-rate v-model="cmtrating" style="height: 12px;" />
                    <el-button style="flex: 1;width: 70%;margin: 3px auto 0 auto;color: #fff;
                    background: linear-gradient(135deg, #38ef7d 0%, #00b894 100%);
                    border-radius: 15px;" @click="addComment(game)">
                        提交</el-button>
                </div>
            </div>
            <div v-if="(comments[game.id] || []).length">
                <div v-for="c in comments[game.id]" :key="c.id">
                    <div style="display: flex;padding: 10px;align-items: center;gap: 10px;
                    background-color: #f0f0f0 ; border-radius: 25px;margin-bottom: 10px;">
                        <el-avatar :size="50" class="friend-avatar"
                            style="background: linear-gradient(135deg, #82ff73 , #2cda18);">
                            <span style="font-size: 20px;">{{ c.username.charAt(0) }}</span>
                        </el-avatar>
                        <div style="display: flex;flex-direction: column;align-items: flex-start;">
                            <span style="font-size: 32px;line-height: 1;" class="gradient-text">{{ c.username }}</span>
                            <span style="font-size: 12px;line-height: 1.05;color: #9B9B9B ;white-space: nowrap;">{{
                                c.createAt
                                }}</span>
                        </div>
                        <el-rate v-model="c.rating" disabled></el-rate>
                        <span style="text-align: start;">{{ c.content }}</span>
                        <template v-if="!readonly || c.userId == userStore.id">
                            <el-button type="danger" :icon="Delete" circle style="width: 40px; height: 40px;
                            font-size: 20px;margin-left: auto;
                            pointer-events: auto !important;" @click="deleteComment(c.id)"></el-button>
                        </template>
                    </div>
                </div>
            </div>
            <div v-else>---暂无评价，快来抢沙发🥵---</div>
        </el-collapse-item>
    </el-collapse>
    <EditDialog v-model:visible="editDialog" :game="editTarget" @edit="emit('edit')" />
    <ShareDialog v-model:visible="shareDialog" :game="editTarget" />
    <el-dialog v-model="deleteDialog" class="delete-dialog" width="420" align-center append-to-body center>
        <template #header>
            <span class="delete-dialog-title">🗑️ 确认删除游戏</span>
        </template>
        <div class="delete-dialog-body">
            <div class="delete-dialog-icon">⚠️</div>
            <div class="delete-dialog-msg">
                <p class="delete-dialog-name">🎯 {{ deleteTarget?.name }}</p>
                <p class="delete-dialog-hint">删除后不可撤回，只能重新添加</p>
            </div>
        </div>
        <template #footer>
            <div class="dialog-footer">
                <el-button class="delete-cancel-btn" @click="deleteDialog = false">取消</el-button>
                <el-button class="delete-confirm-btn" @click="deleteGame(deleteTarget)">确认删除</el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { Select, Edit, Share, Delete, Collection } from '@element-plus/icons-vue'
import { commentApi, gameApi } from '@/api'
import { ElMessage } from 'element-plus'
import EditDialog from './EditDialog.vue'
import ShareDialog from './ShareDialog.vue'
import { useUserStore } from '@/store/user'
import { messageApi } from '@/api/message.js'
import { useRoute } from 'vue-router'
const props = defineProps({
    games: { type: Array, default: () => [] },
    comments: { type: Object, default: () => ({}) },
    readonly: { type: Boolean, default: false }
})
const emit = defineEmits(['change-status', 'delete', 'edit', 'addcmt'])
const userStore = useUserStore()
const route = useRoute()
const comment = ref('')
const cmtrating = ref(0)
const activeNames = ref('')
const playStatus = ref(0)
const deleteDialog = ref(false)
const deleteTarget = ref(null)
const editDialog = ref(false)
const shareDialog = ref(false)
const editTarget = ref(null)
const resetComment = () => {
    cmtrating.value = 0
    comment.value = ''
}
let barDragging = false
const onBarMousedown = (e) => {
    barDragging = !!e.target.closest?.('.el-scrollbar__bar')
}
const onBarClick = (e) => {
    if (barDragging) {
        barDragging = false
        e.stopPropagation()
    }
}
const confirmStatus = (game) => {
    emit('change-status', { game, played: playStatus.value })
}
const setRating = async (game) => {
    try {
        const res = await gameApi.setRating({ gameId: game.id, rating: game.rating })
        if (res.data.code == "200") {
            ElMessage.success("设置成功")
        } else { ElMessage.error(res.data.msg) }
    } catch (error) { ElMessage.error("设置失败") }
}
const deleteGame = async (game) => {
    try {
        const res = await gameApi.deleteGame(game.id)
        if (res.data.code == "200") {
            ElMessage.success("删除成功")
            emit('delete')
            deleteDialog.value = false
        } else { ElMessage.error(res.data.msg) }
    } catch (error) { ElMessage.error("删除失败") }
}
const addComment = async (game) => {
    try {
        const res = await commentApi.addComment({
            gameId: game.id, content: comment.value.trim(), rating: cmtrating.value
        })
        if (res.data.code === '200') {
            if(props.readonly){await messageApi.addCmtMessage({toId:route.params.id,gameId:game.id})}
            ElMessage.success('评论成功')
            comment.value = ''
            cmtrating.value = 0
            emit('addcmt')
        } else { ElMessage.error(res.data.msg) }
    } catch (e) { ElMessage.error('网络错误，添加评论失败') }
}
const deleteComment = async (id) => {
    try {
        const res = await commentApi.deleteComment(id)
        if (res.data.code === '200') {
            ElMessage.success("删除评论成功")
            emit("addcmt")
        } else { ElMessage.error("无权删除此评论") }
    } catch { ElMessage.error("网络异常，删除失败") }
}
const applyShare = async(gameId) => {
    try{
        const res = await messageApi.applyShare(route.params.id,gameId)
        if(res.data.code === '200'){
            ElMessage.success("申请成功")
        }else{ElMessage.error(res.data.msg)}
    }catch{ElMessage.error("网络异常")}
}
</script>

<style scoped>
.custom-collapse {
    --el-collapse-border-color: transparent;
    --el-collapse-header-bg-color: transparent;
    --el-collapse-content-bg-color: transparent;
    border: none;
}

.custom-collapse :deep(.el-collapse-item) {
    width: 98%;
    background-color: rgba(231, 231, 231, 0.8);
    border-radius: 30px;
    padding: 8px 20px;
    margin: 0 auto 10px;
    font-size: 20px;
    line-height: 1;
    transition: all 0.25s ease;
    box-sizing: border-box;
    overflow: hidden;
}

.custom-collapse :deep(.el-collapse-item:not(.is-active):hover) {
    transform: translateY(-2.5px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
    background-color: rgba(245, 245, 245, 0.9);
}

.custom-collapse :deep(.el-collapse-item__header) {
    padding: 0 !important;
    background: transparent !important;
    border: none !important;
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 20px;
    line-height: 1;
    min-height: auto;
    cursor: default !important;
    pointer-events: none !important;
}

.custom-collapse :deep(.el-collapse-item__title) {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
    /* 关键：title 自身可收缩，不允许内容把整行撑宽 */
    padding: 0;
    background: transparent;
}

.custom-collapse :deep(.el-collapse-item__title .itembox-scroll) {
    flex: 1;
    min-width: 0;
    height: 40px;
    /* header 是 pointer-events:none，需单独恢复滚动区的交互 */
    pointer-events: auto;
}

.custom-collapse :deep(.el-collapse-item__title .itembox-scroll .el-scrollbar__wrap) {
    pointer-events: auto;
    /* el-scrollbar 默认纵横都能滚，这里锁死纵向 */
    overflow-y: hidden;
}

.custom-collapse :deep(.el-collapse-item__title .itembox-scroll .el-scrollbar__view) {
    width: max-content;
}

.custom-collapse :deep(.el-collapse-item__title .itembox-content) {
    display: flex;
    align-items: center;
    height: 40px;
    gap: 10px;
    width: max-content;
}

.custom-collapse :deep(.el-scrollbar__bar.is-horizontal) {
    height: 6px;
    bottom: 0;
}

.custom-collapse :deep(.el-scrollbar__bar.is-horizontal .el-scrollbar__thumb) {
    background: rgba(0, 0, 0, 0.25);
    opacity: 1;
}

.custom-collapse :deep(.el-scrollbar__bar.is-horizontal .el-scrollbar__thumb:hover) {
    background: rgba(0, 0, 0, 0.4);
}

.custom-collapse :deep(.el-collapse-item__title .itembox-content > *) {
    flex-shrink: 0;
    /* 每个标签保持自然宽度，堆出溢出 */
    white-space: nowrap;
    /* 文字内部不换行 */
}

.custom-collapse :deep(.el-collapse-item__arrow) {
    margin-left: auto;
    font-size: 18px;
    color: #666;
    transition: transform 0.3s ease;
    flex-shrink: 0;
    pointer-events: auto !important;
    cursor: pointer !important;
}

.custom-collapse :deep(.el-collapse-item__header.is-active .el-collapse-item__arrow) {
    transform: rotate(90deg);
}

.custom-collapse :deep(.el-rate__text) {
    order: -1;
    margin-right: 10px;
}

.custom-collapse :deep(.el-rate) {
    --el-rate-void-color: #909399;
    --el-rate-disabled-void-color: #b7b8b9;
    /* background: rgba(217, 255, 0, 0.35); */
    /* border: 1px solid rgba(0, 0, 0, 0.1);
    border-radius: 999px; */
    padding: 0 4px;
}

/* 悬停放大 */
.custom-collapse :deep(.el-rate__item) {
    transition: transform 0.15s ease;
}

.custom-collapse :deep(.el-rate__item:hover) {
    transform: scale(1.18);
}

/* 禁用状态（评论展示星级）：去掉指针和悬停放大 */
.custom-collapse :deep(.el-rate.is-disabled .el-rate__item) {
    cursor: default;
    transform: none;
}

.custom-collapse :deep(.el-rate.is-disabled .el-rate__item:hover) {
    transform: none;
}

.custom-collapse :deep(.el-collapse-item__content) {
    padding: 8px 0 0 0;
    background: transparent;
    color: #333;
    font-size: 16px;
    line-height: 1.6;
}

.divider-label {
    display: flex;
    align-items: center;
    gap: 14px;
    font-weight: 600;
    color: #333;
    font-size: 20px;
    margin: 0;
}

.divider-label::after {
    content: '';
    flex: 1;
    height: 2.5px;
    background: rgba(0, 0, 0, 0.18);
    border-radius: 4px;
}

.game-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 5px 10px;
    border-radius: 14px;
    font-size: 18px;
    font-weight: 500;
    color: #4eafff;
    background: rgba(64, 158, 255, 0.12);
    white-space: nowrap;
}

.empty-tip {
    text-align: center;
    color: rgba(255, 255, 255, 1);
    font-size: 40px;
}
</style>

<style>
/* 状态切换弹层：放开组件库的最小宽度限制 */
.status-popover {
    min-width: 0 !important;
}
</style>

<style>
/* ===== 删除确认弹窗美化 ===== */
.delete-dialog {
    --el-dialog-bg-color: rgba(20, 24, 40, 0.92);
    backdrop-filter: blur(14px);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.55);
    /* 让顶部光带被圆角裁剪，贴住面板边缘 */
    overflow: hidden;
}

/* 顶部渐变光带 */
.delete-dialog::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #ff6b6b, #ff8e53, #ff5c8a);
    z-index: 1;
}

/* 渐变标题 */
.delete-dialog-title {
    background: linear-gradient(120deg, #ff6b6b, #ff8e53, #ff5c8a);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    font-weight: 700;
    font-size: 18px;
}

.delete-dialog .el-dialog__header {
    padding: 18px 20px 6px;
}

.delete-dialog .el-dialog__headerbtn .el-icon {
    color: rgba(255, 255, 255, 0.7);
}

/* 弹窗主体 */
.delete-dialog-body {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 14px 20px 6px;
    color: rgba(255, 255, 255, 0.9);
    text-align: center;
}

.delete-dialog-icon {
    font-size: 44px;
    line-height: 1;
    filter: drop-shadow(0 4px 12px rgba(255, 107, 107, 0.45));
}

.delete-dialog-msg p {
    margin: 4px 0;
}

.delete-dialog-name {
    font-size: 17px;
    font-weight: 600;
    color: #fff;
}

.delete-dialog-hint {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.55);
}

.delete-dialog .el-dialog__footer {
    padding: 8px 20px 20px;
}

/* 取消按钮 */
.delete-cancel-btn.el-button {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: rgba(255, 255, 255, 0.85);
    border-radius: 10px;
    transition: all 0.2s ease;
}

.delete-cancel-btn.el-button:hover {
    background: rgba(255, 255, 255, 0.16);
    border-color: rgba(255, 255, 255, 0.35);
    color: #fff;
}

/* 确认删除按钮 */
.delete-confirm-btn.el-button {
    border: none;
    border-radius: 10px;
    background: linear-gradient(135deg, #ff6b6b, #ff5c8a);
    color: #fff;
    box-shadow: 0 4px 14px rgba(255, 92, 138, 0.35);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.delete-confirm-btn.el-button:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 22px rgba(255, 92, 138, 0.55);
}

.gradient-text {
    background: linear-gradient(120deg, #2c3e50, #6b89ff);
    -webkit-background-clip: text;
    /* 把背景裁剪成文字形状 */
    background-clip: text;
    color: transparent;
    /* 让文字本身透明，露出后面的渐变背景 */
}
</style>
