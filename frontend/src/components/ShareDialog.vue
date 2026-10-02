<template>
    <el-dialog v-model="dialogVisible" class="share-game-dialog" width="500" append-to-body align-center
        transition="share-bounce">
        <template #header>
            <span class="dialog-title">🤝 分享游戏</span>
        </template>
        <div class="share-body">
            <div v-if="props.game" class="share-game-banner">
                <span class="share-game-icon">🎯</span>
                <span class="share-game-name">{{ props.game.name }}</span>
            </div>
            <div class="share-field">
                <span class="share-label">选择好友：</span>
                <el-select v-model="friId" placeholder="请选择" filterable class="share-select">
                    <el-option v-for="fri in friendList" :key="fri.id" :label="fri.friendName" :value="fri.id" />
                </el-select>
            </div>
            <div class="share-field">
                <span class="share-label">留言：</span>
                <el-input v-model="message" placeholder="请输入留言（20字以内，可空）" maxlength="20" class="share-input" />
            </div>
            <div class="share-note">注：分享后数据将共享，对方可以编辑信息、删除评论等</div>
        </div>
        <template #footer>
            <div class="dialog-footer">
                <el-button class="share-cancel" @click="dialogVisible = false">取消</el-button>
                <el-button class="share-confirm"
                    @click="addShareMsg(friId, props.game.id, message)">确定</el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue';
import { friendApi } from '@/api';
import { messageApi } from '@/api/message';
import { ElMessage } from 'element-plus';
const friendList = ref([])
const message = ref('')
const props = defineProps({
    visible: Boolean,
    game: { type: Object, default: null }
})
const emit = defineEmits(['update:visible'])
const friId = ref('')
const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
})
const getFriendList = async () => {
    try {
        const res = await friendApi.getFriendList()
        if (res.data.code == '200') {
            friendList.value = res.data.data
        }
    } catch (e) { ElMessage.error("获取好友列表失败") }
}
const addShareMsg = async (toId, gameId, msg) => {
     if (!friId.value) return ElMessage.warning('请先选择一位好友')
    const res = await messageApi.addShareMsg({ toId: toId, gameId: gameId, message: msg })
    if(res.data.code === '200'){
        ElMessage.success('分享成功')
        dialogVisible.value = false
        message.value = ''
        friId.value = null
    }else{ElMessage.error(res.data.msg)}
}
onMounted(() => {
    getFriendList()
})
</script>

<style>
/* ===== 弹窗进出场动画 ===== */
.share-bounce-enter-active,
.share-bounce-leave-active,
.share-bounce-enter-active .el-dialog,
.share-bounce-leave-active .el-dialog {
    transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.share-bounce-enter-from,
.share-bounce-leave-to {
    opacity: 0;
}

.share-bounce-enter-from .el-dialog,
.share-bounce-leave-to .el-dialog {
    transform: scale(0.3) translateY(-50px);
    opacity: 0;
}

/* ===== 分享弹窗（黄色主题，参考编辑弹窗） ===== */
.share-game-dialog {
    --el-dialog-bg-color: rgba(20, 24, 40, 0.88);
    /* 与编辑弹窗同款深色底 */
    backdrop-filter: blur(14px);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5), 0 0 40px rgba(255, 200, 80, 0.18);
    overflow: hidden;
}

/* 顶部黄色灯条 */
.share-game-dialog::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #ffe08a, #ffb84d, #ffd700);
    z-index: 1;
}

/* 渐变标题 */
.share-game-dialog .dialog-title {
    background: linear-gradient(120deg, #ffd86b, #ffb347);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    font-weight: 700;
    font-size: 18px;
}

.share-game-dialog .el-dialog__header {
    padding: 18px 20px 6px;
    text-align: center;
}

.share-game-dialog .el-dialog__headerbtn .el-icon {
    color: rgba(255, 255, 255, 0.7);
}

/* 正文 */
.share-body {
    padding: 8px 6px 4px;
}

/* 分享目标：游戏名横幅 */
.share-game-banner {
    display: flex;
    align-items: center;
    gap: 8px;
    width: fit-content;
    /* 宽度只包住内容 */
    max-width: 100%;
    margin: 0 auto 16px;
    /* 水平居中 */
    padding: 6px 14px;
    background: linear-gradient(120deg, rgba(255, 216, 107, 0.12), rgba(255, 179, 71, 0.05));
    border: 1px solid rgba(255, 200, 80, 0.35);
    border-radius: 12px;
}

.share-game-icon {
    font-size: 18px;
    flex-shrink: 0;
}

.share-game-name {
    background: linear-gradient(120deg, #ffd86b, #ffb347);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    font-size: 16px;
    font-weight: 600;
    min-width: 0;
    max-width: 320px;
    /* 名字很长时才截断，平时随内容包裹 */
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.share-field {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 14px;
}

.share-label {
    flex: 0 0 90px;
    /* 固定标签宽度：两行控件起点对齐 */
    text-align: right;
    color: rgba(255, 255, 255, 0.85);
    white-space: nowrap;
    font-size: 14px;
}

.share-note {
    text-align: center;
    color: rgba(255, 255, 255, 0.4);
    font-size: 12px;
}

/* 选择框/输入框：深色底 + 白字 + 金色聚焦 */
.share-game-dialog .share-select {
    flex: 1;
    --el-select-multiple-input-color: #fff;
    /* 手动输入的文字 */
    --el-select-input-color: rgba(255, 255, 255, 0.6);
    /* 箭头/图标 */
}

.share-game-dialog .share-input {
    flex: 1;
}

.share-game-dialog .el-select__wrapper,
.share-game-dialog .el-input__wrapper {
    background: rgba(255, 255, 255, 0.06);
    border-radius: 10px;
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
    transition: box-shadow 0.2s ease;
}

.share-game-dialog .el-select__wrapper.is-focused,
.share-game-dialog .el-input__wrapper.is-focus {
    box-shadow: 0 0 0 1px #ffb347 inset, 0 0 10px rgba(255, 200, 80, 0.35);
}

.share-game-dialog .el-select__wrapper,
.share-game-dialog .el-select__selected-item,
.share-game-dialog .el-select__tags-text,
.share-game-dialog .el-select__input,
.share-game-dialog .el-input__inner {
    color: #fff;
    caret-color: #fff;
}

.share-game-dialog .el-select__placeholder {
    color: #fff;
    /* 选中后的文字：单选时也渲染在这个元素里，纯白显眼 */
}

.share-game-dialog .el-select__placeholder.is-transparent,
.share-game-dialog .el-input__inner::placeholder {
    color: rgba(255, 255, 255, 0.45);
    /* 未选中时的提示文字 */
}

/* 底部按钮 */
.share-game-dialog .el-dialog__footer {
    padding: 8px 20px 20px;
}

.share-cancel.el-button {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: rgba(255, 255, 255, 0.85);
    border-radius: 10px;
    transition: all 0.2s ease;
}

.share-cancel.el-button:hover {
    background: rgba(255, 255, 255, 0.16);
    border-color: rgba(255, 255, 255, 0.35);
    color: #fff;
}

.share-confirm.el-button {
    border: none;
    border-radius: 10px;
    background: linear-gradient(120deg, #ffd86b, #ffb347);
    color: #6b4a00;
    font-weight: 600;
    box-shadow: 0 4px 14px rgba(255, 184, 77, 0.35);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.share-confirm.el-button:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 22px rgba(255, 184, 77, 0.55);
}
</style>
