<template>
    <el-dialog v-model="editVisable" class="edit-game-dialog" width="600" append-to-body
        transition="dialog-bounce" @closed="resetForm">
        <template #header>
            <span class="dialog-title">✏️ 编辑游戏</span>
        </template>
        <el-form :model="editGame.data" label-width="90px" ref="formRef" :rules="editGame.rules"
            class="edit-game-form">
            <el-form-item label="名称：" prop="name" class="span-2">
                <el-input v-model="editGame.data.name" placeholder="请输入(15字以内)" maxlength="15"
                    style="width: 180px;margin-right: 20px;" />
            </el-form-item>
            <el-form-item label="制作商：" prop="company">
                <el-input v-model="editGame.data.company" placeholder="请输入" style="width: 180px;" />
            </el-form-item>
            <el-form-item label="平台：" prop="platform" class="span-2">
                <el-checkbox-group v-model="editGame.data.platform" @change="onPlatformChange"
                    style="display: flex;flex-wrap: wrap;align-items: center;gap: 10px;">
                    <el-checkbox label="PC" value="PC" />
                    <el-checkbox label="PS" value="PS" />
                    <el-checkbox label="NS" value="NS" />
                    <el-checkbox label="XBOX" value="XBOX" />
                    <el-checkbox label="移动端" value="移动端" />
                    <el-checkbox label="全平台(单选)" value="全平台" />
                </el-checkbox-group>
            </el-form-item>
            <el-form-item label="游戏类型：" prop="type">
                <el-input v-model="editGame.data.type" placeholder="请输入游戏类型" style="width: 180px;" />
            </el-form-item>
            <el-form-item label="发售日期：" prop="releaseDate">
                <el-date-picker v-model="editGame.data.releaseDate" type="date" placeholder="选择发售日期"
                    value-format="YYYY-MM-DD" style="width: 180px;" />
            </el-form-item>
            <el-form-item label="价格：" prop="price">
                <el-input v-model="editGame.data.price" placeholder="请输入价格￥..." style="width: 180px;" />
            </el-form-item>
            <el-form-item label="MC评分：" prop="mcRating">
                <el-input v-model="editGame.data.mcRating" placeholder="请输入MC评分" style="width: 180px;" />
            </el-form-item>
            <el-form-item label="游戏介绍：" prop="info" class="span-2">
                <el-input v-model="editGame.data.info" maxlength="300" placeholder="300字以内游戏介绍" show-word-limit
                    type="textarea" :rows="7" />
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="resetForm">取消</el-button>
                <el-button type="primary" @click="handleSave"> 保存 </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup>
import { gameApi } from '@/api'
import { ElMessage } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'

const props = defineProps({
    game: { type: Object, default: null },
    visible: Boolean
})
const emit = defineEmits(['update:visible','edit'])
const editVisable = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
})

const formRef = ref(null)
const editGame = reactive({
    data: { id: '', name: '', company: '', platform: [], type: '', info: '', price: '', mcRating: '', releaseDate: '', cover: '' },
    rules: {
        name: [
            { required: true, message: '请输入游戏名称', trigger: 'blur' },
            { max: 15, message: '游戏名称不能超过15个字符', trigger: 'blur' }
        ],
        company: [{ required: true, message: '请输入制作商', trigger: 'blur' }],
        platform: [{ required: true, message: '请输入游戏平台', trigger: 'blur' }]
    }
})

const resetForm = () => {
    editVisable.value = false
    formRef.value?.resetFields()
}
const onPlatformChange = (val) => {
    if (val.includes('全平台')) {
        editGame.data.platform =
            val.length > 1 ? val.filter(v => v !== '全平台') : ['全平台']
    }
}
// 打开弹窗时，把传入的 game 填充到表单
const fillForm = (game) => {
    editGame.data = {
        id: game?.id || '',
        name: game?.name || '',
        company: game?.company || '',
        platform: game?.platform ? String(game.platform).split('/') : [],
        type: game?.type || '',
        info: game?.info || '',
        price: game?.price || '',
        mcRating: game?.mcRating || '',
        releaseDate: game?.releaseDate || '',
        cover: game?.cover || ''
    }
}
watch(() => props.visible, (val) => {
    if (val) fillForm(props.game)
})
// TODO: 保存逻辑（更新接口）后续接入
const handleSave = async() => {
    try {
        await formRef.value.validate()   // 校验不通过会 reject
    } catch {
        ElMessage.error('缺少必填字段')
        return
    }
    const payload = { ...editGame.data, platform: editGame.data.platform.join('/') }
    try {
        const res = await gameApi.editGame(payload)
        if (res.data.code == '200') {
            ElMessage.success('编辑成功')
            emit('edit')
        } else {
            ElMessage.error(res.data.msg)
        }
        editVisable.value = false
    } catch {
        ElMessage.error('编辑失败')
    }
}
</script>

<style>
.dialog-bounce-enter-active,
.dialog-bounce-leave-active,
.dialog-bounce-enter-active .el-dialog,
.dialog-bounce-leave-active .el-dialog {
    transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.dialog-bounce-enter-from,
.dialog-bounce-leave-to {
    opacity: 0;
}

.dialog-bounce-enter-from .el-dialog,
.dialog-bounce-leave-to .el-dialog {
    transform: scale(0.3) translateY(-50px);
    opacity: 0;
}

/* ===== 编辑游戏对话框美化 ===== */
.edit-game-dialog {
    --el-dialog-bg-color: rgba(20, 24, 40, 0.88);
    backdrop-filter: blur(14px);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5), 0 0 40px rgba(0, 210, 255, 0.15);
    /* 让顶部光带被圆角裁剪，贴住面板边缘 */
    overflow: hidden;
}

/* 输入文字/占位符/光标改白色 */
.edit-game-dialog .el-input__inner,
.edit-game-dialog .el-textarea__inner {
    color: #fff;
    caret-color: #fff;
}

.edit-game-dialog .el-input__inner::placeholder,
.edit-game-dialog .el-textarea__inner::placeholder {
    color: rgba(255, 255, 255, 0.45);
}

.edit-game-dialog .el-input__icon {
    color: rgba(255, 255, 255, 0.6);
}

/* 字数统计：背景与输入框一致，文字白色 */
.edit-game-dialog .el-input__count {
    background: transparent;
    color: #fff;
}

/* 顶部渐变光带 */
.edit-game-dialog::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #3a7bd5, #00d2ff);
    z-index: 1;
}

/* 渐变标题 */
.edit-game-dialog .dialog-title {
    background: linear-gradient(120deg, #4facfe, #00d2ff);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    font-weight: 700;
    font-size: 18px;
}

.edit-game-dialog .el-dialog__header {
    padding: 18px 20px 6px;
    text-align: center;
}

.edit-game-dialog .el-dialog__headerbtn .el-icon {
    color: rgba(255, 255, 255, 0.7);
}

.edit-game-dialog .el-form-item__label {
    color: rgba(255, 255, 255, 0.85);
}

.edit-game-dialog .el-input__wrapper,
.edit-game-dialog .el-textarea__inner {
    background: rgba(255, 255, 255, 0.06);
    border-radius: 10px;
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
    transition: box-shadow 0.2s ease;
}

.edit-game-dialog .el-input__wrapper.is-focus,
.edit-game-dialog .el-textarea__inner:focus {
    box-shadow: 0 0 0 1px #00d2ff inset, 0 0 10px rgba(0, 210, 255, 0.35);
}

.edit-game-dialog .el-checkbox {
    color: rgba(255, 255, 255, 0.85);
}

/* 表单项两列布局：宽的项跨整行 */
.edit-game-dialog .el-form.edit-game-form {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    column-gap: 24px;
}

.edit-game-dialog .edit-game-form .span-2 {
    grid-column: 1 / -1;
}

.edit-game-dialog .el-button--primary {
    border: none;
    background: linear-gradient(120deg, #3a7bd5, #00d2ff);
    color: #fff;
    box-shadow: 0 0 12px rgba(0, 210, 255, 0.4);
    transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.edit-game-dialog .el-button--primary:hover {
    transform: translateY(-1px);
    box-shadow: 0 0 18px rgba(0, 210, 255, 0.6);
}
</style>
