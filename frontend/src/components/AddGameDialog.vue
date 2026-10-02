<template>
    <el-dialog v-model="dialogVisible" class="add-game-dialog" width="600" @closed="resetForm"
        transition="dialog-bounce">
        <template #header>
            <span class="dialog-title">🎮 添加游戏</span>
        </template>
        <el-form :model="addGame.data" label-width="90px" ref="formRef" :rules="addGame.rules"
            class="add-game-form">
            <el-form-item label="名称：" prop="name" class="span-2">
                <el-input v-model="addGame.data.name" placeholder="请输入(15字以内)" maxlength="15" style="width: 180px;margin-right: 20px;" />
                <AiGenButton @click="aiGenerate()" :loading="aiLoading"></AiGenButton>
                <div style="display: flex;margin: 0 15px 0 10px;">
                    <el-switch v-model="effectSwitch" class="effect-switch" size="small" active-text="开"
                        inactive-text="特效：关" />
                </div>
                <el-tooltip class="box-item" effect="dark" placement="right-start">
                    <template #content>
                        <div>输入名称后，AI自动生成剩下信息。</div>
                        <div>⚠注：使用deepseek-v4-flash模型。</div>
                        <div>token很贵😭，每天限100次生成</div>
                        <div>返回数据大概需要5-15s。</div>
                        <div>AI返回的数据可能不准确，可手动修改。</div>
                        <div>点击还有可爱的猫咪特效🩷，可通过开关控制。</div>
                    </template>
                    <span style="display: inline-flex; align-items: center; height: 100%;">
                        <el-icon :size="18">
                            <InfoFilled />
                        </el-icon>
                    </span>
                </el-tooltip>
            </el-form-item>
            <el-form-item label="制作商：" prop="company">
                <el-input v-model="addGame.data.company" placeholder="请输入" style="width: 180px;" />
            </el-form-item>
            <el-form-item label="平台：" prop="platform" class="span-2">
                <el-checkbox-group v-model="addGame.data.platform" @change="onPlatformChange"
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
                <el-input v-model="addGame.data.type" placeholder="请输入游戏类型" style="width: 180px;" />
            </el-form-item>
            <el-form-item label="发售日期：" prop="releaseDate">
                <el-date-picker v-model="addGame.data.releaseDate" type="date" placeholder="选择发售日期"
                    value-format="YYYY-MM-DD" style="width: 180px;" />
            </el-form-item>
            <el-form-item label="价格：" prop="price">
                <el-input v-model="addGame.data.price" placeholder="请输入价格￥..." style="width: 180px;" />
            </el-form-item>
            <el-form-item label="MC评分：" prop="mcRating">
                <el-input v-model="addGame.data.mcRating" placeholder="请输入MC评分" style="width: 180px;" />
            </el-form-item>
            <el-form-item label="游戏介绍：" prop="info" class="span-2">
                <el-input v-model="addGame.data.info" maxlength="300" placeholder="300字以内游戏介绍" show-word-limit
                    type="textarea" :rows="7" />
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="resetForm">重置</el-button>
                <el-button type="primary" @click="handleGames"> 提交 </el-button>
            </div>
        </template>
    </el-dialog>
    <SkillOverlay v-if="effectSwitch" :visible="skillActive && effectSwitch" @finished="onSkillFinished" />
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import SkillOverlay from '@/components/SkillOverlay.vue'
import AiGenButton from '@/components/AiGenButton.vue'
import { gameApi } from '@/api'

const props = defineProps({
    visible: Boolean
})
const emit = defineEmits(['update:visible', 'saved'])

// v-model 桥接：父级传 visible，组件内部通过 emit('update:visible') 同步回去
const dialogVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
})

const formRef = ref(null)
const aiLoading = ref(false)
const effectSwitch = ref(true)
const skillActive = ref(false)
const addGame = reactive({
    data: { name: '', company: '', platform: [], type: '', info: '', price: '', mcRating: '', releaseDate: '', cover: '' },
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
    formRef.value?.resetFields()
}
const onPlatformChange = (val) => {
    if (val.includes('全平台')) {
        addGame.data.platform =
            val.length > 1 ? val.filter(v => v !== '全平台') : ['全平台']
    }
}
const onSkillFinished = () => {
    skillActive.value = false
}
const aiGenerate = async () => {
    try {
        if (addGame.data.name.trim()) {
            skillActive.value = effectSwitch.value
            aiLoading.value = true
            let res = await gameApi.getAiResponse(addGame.data.name);
            if (res.data.code === "200") {
                res.data.data.platform = res.data.data.platform ? res.data.data.platform.split('/') : []
                addGame.data = res.data.data
            } else {
                ElMessage.error(res.data.msg)
            }

        }
        else {
            ElMessage.error('游戏名不能为空')
        }
    } catch (error) {
        ElMessage.error('请求失败')
    } finally { aiLoading.value = false }
}
const handleGames = async () => {
    try {
        await formRef.value.validate()   // 校验不通过会 reject，直接跳出
    } catch {
        ElMessage.error('缺少必填字段')
        return                           // 有字段不合法，终止提交
    }
    const payload = { ...addGame.data, platform: addGame.data.platform.join('/') }
    const res = await gameApi.insertGame(payload)
    if (res.data.code == '200') {
        resetForm()
        emit('saved')
        ElMessage.success('添加游戏成功')
    } else { ElMessage.error('提交失败') }
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

/* ===== 添加游戏对话框美化 ===== */
.add-game-dialog {
    --el-dialog-bg-color: rgba(20, 24, 40, 0.88);
    backdrop-filter: blur(14px);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
    /* 让顶部光带被圆角裁剪，贴住面板边缘 */
    overflow: hidden;
}

/* 输入文字/占位符/光标改白色（el-input 内部会重定义变量，需直接覆盖 inner） */
.add-game-dialog .el-input__inner,
.add-game-dialog .el-textarea__inner {
    color: #fff;
    caret-color: #fff;
}

.add-game-dialog .el-input__inner::placeholder,
.add-game-dialog .el-textarea__inner::placeholder {
    color: rgba(255, 255, 255, 0.45);
}

.add-game-dialog .el-input__icon {
    color: rgba(255, 255, 255, 0.6);
}

/* 字数统计：背景与输入框一致，文字白色 */
.add-game-dialog .el-input__count {
    background: transparent;
    color: #fff;
}

/* 顶部渐变光带 */
.add-game-dialog::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #00bfff, #8a5cff, #00e08a);
    z-index: 1;
}

/* 渐变标题 */
.dialog-title {
    background: linear-gradient(120deg, #00bfff, #8a5cff, #00e08a);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    font-weight: 700;
    font-size: 18px;
}

.add-game-dialog .el-dialog__header {
    padding: 18px 20px 6px;
}

.add-game-dialog .el-dialog__headerbtn .el-icon {
    color: rgba(255, 255, 255, 0.7);
}

.add-game-dialog .el-form-item__label {
    color: rgba(255, 255, 255, 0.85);
}

.add-game-dialog .el-input__wrapper,
.add-game-dialog .el-textarea__inner {
    background: rgba(255, 255, 255, 0.06);
    border-radius: 10px;
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
    transition: box-shadow 0.2s ease;
}

.add-game-dialog .el-input__wrapper.is-focus,
.add-game-dialog .el-textarea__inner:focus {
    box-shadow: 0 0 0 1px #00bfff inset, 0 0 10px rgba(0, 191, 255, 0.35);
}

.add-game-dialog .el-checkbox {
    color: rgba(255, 255, 255, 0.85);
}

/* 表单项两列布局：减少弹窗高度，宽的项跨整行 */
.add-game-dialog .el-form.add-game-form {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    column-gap: 24px;
}

.add-game-dialog .add-game-form .span-2 {
    grid-column: 1 / -1;
}

.add-game-dialog .el-button--primary {
    border: none;
    background: linear-gradient(120deg, #00bfff, #8a5cff 50%, #00e08a);
    color: #fff;
    box-shadow: 0 0 12px rgba(138, 92, 255, 0.4);
    transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.add-game-dialog .el-button--primary:hover {
    transform: translateY(-1px);
    box-shadow: 0 0 18px rgba(138, 92, 255, 0.6);
}
</style>

<style scoped>
/* 特效开关：未激活文字改白色（激活文字保持蓝色） */
.effect-switch :deep(.el-switch__label:not(.is-active)) {
    color: #fff;
}
</style>
