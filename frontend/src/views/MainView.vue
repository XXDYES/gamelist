<template>
    <div class="bg" :style="{ backgroundImage: `url(${curbgurl})` }">
        <div class="pagehead">
            <div style="margin-right: auto;display: flex;align-items: center;">
                <el-icon :size="35" style="color:white;cursor: pointer;margin: auto 15px;" @click="loginout">
                    <Back />
                </el-icon>
                <span style="color: white;font-size: 16px;cursor: pointer;line-height: 1;" @click="loginout">退出登入</span>
            </div>
            <div
                style="color: white;position: absolute;left: 50%;top:50%; transform: translate(-50%, -50%);font-size: 20px; font-weight: bold;">
                GAMELIST V1.0
            </div>
            <div style="margin-left: auto;">
                <el-avatar :size="35"> user </el-avatar><span style="color: white;margin: 0 15px;">{{
                    userInfo.username || '未登录' }}</span>
            </div>
        </div>
        <WallpaperSwitch v-model:bgurl="curbgurl" :bglist="bglist" />
        <el-dialog v-model="dialogVisable" class="add-game-dialog" width="600" @closed="resetForm" transition="dialog-bounce">
            <template #header>
                <span class="dialog-title">🎮 添加游戏</span>
            </template>
            <el-form :model="addGame.data" label-width="90px" ref="formRef" :rules="addGame.rules" class="add-game-form">
                <el-form-item label="名称：" prop="name" class="span-2">
                    <el-input v-model="addGame.data.name" placeholder="请输入" style="width: 180px;margin-right: 20px;" />
                    <AiGenButton @click="aiGenerate()" :loading="aiLoading"></AiGenButton>
                    <div style="display: flex;margin: 0 15px 0 10px;">
                        <el-switch v-model="effectSwitch" class="effect-switch" size="small" active-text="开" inactive-text="特效：关" />
                    </div>
                    <el-tooltip class="box-item" effect="dark" placement="right-start">
                        <template #content>
                            <div>输入名称后，AI自动生成剩下信息。</div>
                            <div>⚠注：使用deepseek-v4-flash模型。</div>
                            <div>返回数据大概需要5-15s，且数据截止日期为2025.5。</div>
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
        <SkillOverlay v-if="effectSwitch" :visible="skillActive && effectSwitch"
                        @finished="onSkillFinished" />
        <div class="list_container">
            <el-tooltip class="box-item" effect="dark" content="添加游戏" placement="top-start">
                <el-button class="plusbutton" circle :icon="Plus" @click="dialogVisable = true"></el-button>
            </el-tooltip>
            <div style="display: flex;justify-content: center;gap: 5px;">
                <el-input v-model="inputword" class="search-input" style="width: 500px;height: 40px;"
                    placeholder="输入 名称/制作商/平台/类型 搜索..." :prefix-icon="Search" clearable @clear="clearkeyword" />
                <el-button type="primary" circle :icon="Search" style="width: 40px;height: 40px;font-size: 20px;"
                    @click="clicksearch">
                </el-button>
                <el-popover class="box-item" v-model:visible="sortPopVisible" transition="none" placement="bottom"
                    trigger="click" :width="160">
                    <span style="display: inline-flex; align-items: center; gap: 4px;">
                        <el-icon>
                            <Menu />
                        </el-icon>
                        <span>排序选项</span>
                    </span>
                    <el-divider style="margin: 5px 0;" />
                    <div style="display: flex;flex-direction: column;width: 100%;gap: 5px;">
                        <el-radio-group v-model="sortField">
                            <el-radio value="name">名称(A-Z)</el-radio>
                            <el-radio value="price">价格</el-radio>
                            <el-radio value="mcRating">MC评分</el-radio>
                            <el-radio value="releaseDate">发售日期</el-radio>
                            <el-radio value="addDate">添加日期</el-radio>
                        </el-radio-group>
                        <el-radio-group v-model="sortOrder" size="small">
                            <el-radio-button label="asc">
                                ↑升序
                            </el-radio-button>
                            <el-radio-button label="desc">
                                ↓降序
                            </el-radio-button>
                        </el-radio-group>
                        <div style="display: flex;margin-top: 5px;">
                            <el-button size="middle" @click="resetSort">重置</el-button>
                            <el-button size="middle" type="primary" @click="sortPopVisible = false">确认</el-button>
                        </div>
                    </div>
                    <template #reference>
                        <el-button type="primary" circle :icon="Sort"
                            style="width: 40px;height: 40px;font-size: 20px;margin-left: 0;">
                        </el-button>
                    </template>
                </el-popover>
            </div>
            <el-tabs v-model="activeTab" class="custom-tabs">
                <el-tab-pane label="🕜️待玩" name="notplayed">
                    <div v-for="game in notplayedgames" :key="game.id">
                        <div class="itembox">
                            <span>名称：{{ game.name }}</span> <span>制作商：{{ game.company }} </span>
                        </div>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="✅️已玩" name="played">
                    <div v-for="game in playedgames" :key="game.id">
                        <div class="itembox">
                            <span>名称：{{ game.name }}</span> <span>制作商：{{ game.company }} </span>
                            <span><el-rate v-model="value" :texts="['拉完了', '拉', 'NPC', '夯', '夯爆了']" show-text /></span>
                        </div>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="❌️弃坑" name="giveup">
                    333
                </el-tab-pane>
                <el-tab-pane label="👀全部" name="all">
                    <el-collapse v-model="activeNames" accordion class="custom-collapse">
                        <el-collapse-item v-for="game in sortedgames" :key="game.id" :name="String(game.id)">
                            <template #title>
                                <div class="itembox-content">
                                    <span style="font-size: 26px;">🎯{{ game.name }}</span>
                                    <span class="game-tag">🏢:{{ game.company }}</span>
                                    <span class="game-tag">🖥️:{{ game.platform }}</span>
                                    <span class="game-tag">🕹️:{{ game.type }}</span>
                                </div>
                                <div @click.stop>
                                    <!-- <el-tooltip class="box-item" effect="dark" content="确认信息" placement="top">
                                        <el-button type="success" :icon="Select" circle
                                            style="width: 40px; height: 40px; font-size: 20px;margin-left: 6px;
                                            pointer-events: auto !important;"></el-button>
                                    </el-tooltip> -->
                                    <el-tooltip class="box-item" effect="dark" content="编辑信息" placement="top">
                                        <el-button type="primary" :icon="Edit" circle style="width: 40px; height: 40px; font-size: 20px;margin-left: 6px;
                                            pointer-events: auto !important;"></el-button>
                                    </el-tooltip>
                                    <el-tooltip class="box-item" effect="dark" content="分享信息" placement="top">
                                        <el-button type="warning" :icon="Share" circle style="width: 40px; height: 40px; font-size: 20px;margin-left: 6px;
                                            pointer-events: auto !important;"></el-button>
                                    </el-tooltip>
                                    <el-tooltip class="box-item" effect="dark" content="删除信息" placement="top">
                                        <el-button type="danger" :icon="Delete" circle style="width: 40px; height: 40px; font-size: 20px;margin-left: 6px;
                                            pointer-events: auto !important;"></el-button>
                                    </el-tooltip>
                                </div>
                            </template>
                            <div style="display: flex;gap: 10px;">
                                <span class="game-tag">💰:{{ game.price }}</span>
                                <span class="game-tag">MC评分:{{ game.mcRating }}</span>
                                <span class="game-tag">📅发售日期:{{ game.releaseDate }}</span>
                                <span class="game-tag">📅添加日期:{{ game.addDate }}</span>
                            </div>
                            <div class="divider-label">📖 游戏介绍</div>
                            <div style="text-align: left;text-indent: 2em;">
                                <div style="text-indent: 2em;">{{ game.info }}</div>
                            </div>
                            <div class="divider-label">💬 游戏评价</div>
                        </el-collapse-item>
                    </el-collapse>
                </el-tab-pane>
                <!-- <el-tab-pane label="👀全部" name="all">
                    <div v-for="game in gamelist" :key="game.id">
                        <div class="itembox">
                            <span style="font-size: 24px;text-shadow: 2px 2px 8px rgba(0,0,0,0.3);">🎯{{ game.name
                            }}</span>
                            <span>🏢:{{ game.company }}</span>
                            <span>🖥️:{{ game.platform }}</span>
                            <span>🕹️:{{ game.type }}</span>
                        </div>
                    </div>
                </el-tab-pane> -->
            </el-tabs>
        </div>
    </div>
</template>
<script setup>
import { Back, Operation, Picture, CirclePlus, Search, Delete, Edit, Select, Share, Plus, Sort, Menu, InfoFilled } from '@element-plus/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus'
import WallpaperSwitch from '@/components/WallpaperSwitch.vue';
import SkillOverlay from '@/components/SkillOverlay.vue';
import AiGenButton from '@/components/AiGenButton.vue';
import 'element-plus/dist/index.css'
import router from '@/router';
import { userApi, gameApi } from '@/api'
const bglist = [
    require('@/assets/re9.jpg'),
    require('@/assets/ER.jpg'),
    require('@/assets/XB2.webp'),
    require('@/assets/zelda1.jpg'),
    require('@/assets/FF7.jpg')
]
const curbgurl = ref(bglist[Number(localStorage.getItem('bgIndex')) || 0])
// ========== 新增：用户信息 ==========
const userInfo = ref({ username: '未登录' });
const keyword = ref('')
const inputword = ref('')
const sortField = ref('addDate')
const sortOrder = ref('desc')
const sortPopVisible = ref(false)
const skillActive = ref(false)
const effectSwitch = ref(true)
const formRef = ref(null)
const aiLoading = ref(false)
const addGame = reactive({
    data: { name: '', company: '', platform: [], type: '', info: '', price: '', mcRating: '', releaseDate: '',cover: '' },
    rules: {
        name: [{ required: true, message: '请输入游戏名称', trigger: 'blur' }],
        company: [{required: true,message: '请输入制作商', trigger: 'blur'}],
        platform: [{required: true,message: '请输入游戏平台', trigger: 'blur'}]
    }
})
const handleGames = async () => {
     try {
        await formRef.value.validate()   // 校验不通过会 reject，直接跳出
    } catch {
        ElMessage.error('缺少必填字段')
        return                           // 有字段不合法，终止提交
    }
    const payload = { ...addGame.data, platform: addGame.data.platform.join('/') }
    const res = await gameApi.insertGame(payload)
    if (res.data.code == '200'){
        resetForm()
        await fetchGameList()
        ElMessage.success('添加游戏成功')
    }else{ElMessage.error('提交失败')}
}
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
    // TODO: 动画结束后在这里执行真正的后续逻辑
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
            }else{
                ElMessage.error(res.data.msg)
            }

        }
        else {
            ElMessage.error('游戏名不能为空')
        }
    } catch (error) {
        console.error('请求失败：', error);
        ElMessage.error('请求失败')
    }finally{aiLoading.value = false}
}
const resetSort = () => {
    sortField.value = 'addDate'
    sortOrder.value = 'desc'
    sortPopVisible.value = false   // 重置完顺便关掉
}
const clicksearch = () => {
    keyword.value = inputword.value.trim()
}
const clearkeyword = () => { keyword.value = "" }
const fetchUserInfo = async () => {
    try {
        const res = await userApi.getUserInfo();
        if (res.data.code === "200") {
            userInfo.value = res.data.data;
            await fetchGameList();
        } else {
            userInfo.value = { username: '未登录' };
        }
    } catch (error) {
        console.error('获取用户信息失败：', error);
        userInfo.value = { username: '未登录' };
    }
};
const fetchGameList = async () => {
    try {
        const userId = userInfo.value.id;
        if (!userId) {
            console.warn('用户ID不存在，无法获取游戏列表');
            return;
        }
        const res = await gameApi.getGameList(userId);
        if (res.data.code === "200") {
            gamelist.value = res.data.data;
            console.log('游戏列表:', gamelist.value);
        }
    } catch (error) {
        console.error('获取列表失败：', error);
        ElMessage.error('获取列表失败');
    }
};
const activeTab = ref('notplayed')
const value = ref()
const handleClick = (tab, event) => {
    console.log(tab, event)
}
const dialogVisable = ref(false)
const loginout = () => {
    localStorage.removeItem("userToken")
    router.push("/login")
}
const gamelist = ref([])
onMounted(() => {
    // 获取用户信息
    fetchUserInfo();
})
function extractNumber(str) {
    const match = String(str).match(/(\d+(\.\d+)?)/)
    return match ? parseFloat(match[1]) : 0
}
const filteredgames = computed(() => {
    // ⭐ 如果关键词为空，直接返回全部
    if (!keyword.value) {
        return gamelist.value
    }
    const searchText = keyword.value.toLowerCase()
    return gamelist.value.filter(game => {
        return (
            game.name?.toLowerCase().includes(searchText) ||
            game.company?.toLowerCase().includes(searchText) ||
            game.platform?.toLowerCase().includes(searchText) ||
            game.type?.toLowerCase().includes(searchText)
        )
    })
})
const sortedgames = computed(() => {
    const list = [...filteredgames.value]
    const dir = sortOrder.value === 'asc' ? 1 : -1
    list.sort((a, b) => {
        let va = a[sortField.value]
        let vb = b[sortField.value]
        if (va == null || va == '暂无' || va == '') return 1
        if (vb == null || vb == '暂无' || vb == '') return -1
        if (sortField.value === 'name') {
            return va.localeCompare(vb, 'zh') * dir
        }
        if (sortField.value === 'releaseDate' || sortField.value === 'addDate') {
            const da = new Date(va)
            const db = new Date(vb)
            if (isNaN(da)) return 1      // 空/无效日期排最后
            if (isNaN(db)) return -1
            return (da - db) * dir || (b.id - a.id)
        }
        const na = extractNumber(va)
        const nb = extractNumber(vb)
        if (isNaN(na)) return 1
        if (isNaN(nb)) return -1
        return (na - nb) * dir
    })
    return list
})
const notplayedgames = computed(() => {
    return sortedgames.value.filter(game => game.played === 0)
})
const playedgames = computed(() => {
    return sortedgames.value.filter(game => game.played === 1)
})
</script>
<style scoped>
:global(html),
:global(body) {
    margin: 0;
    /* 清除默认外边距 */
    padding: 0;
    /* 清除默认内边距 */
    height: 100%;
    /* 让 html/body 占满视口高度 */
    overflow-y: auto;
    /* overflow-x: hidden; */
    overscroll-behavior: none;
    /* 禁止所有方向的回弹 */
}

.list_container {
    /* flex: 1; */
    width: 70%;
    height: 90%;
    background-color: rgba(183, 183, 183, 0.7);
    border-radius: 25px;
    padding: 10px 30px;
    box-sizing: border-box;
    display: flex;
    /* ✅ 变成 flex 容器 */
    flex-direction: column;
    /* ✅ 垂直排列 */
    overflow: hidden;
    /* ✅ 防止溢出 */
    position: relative;
    backdrop-filter: blur(2px);
    box-shadow: 0, 25px, 25px, rgba(0, 0, 0, 0.25);
}

.custom-tabs {
    flex: 1;
    /* ✅ 撑满剩余高度 */
    display: flex;
    flex-direction: column;
    min-height: 0;
    /* ✅ 允许 flex 收缩 */
}

.custom-tabs :deep(.el-tabs__content) {
    flex: 1;
    /* ✅ 自动撑满剩余高度 */
    overflow-y: auto;
    /* ✅ 超出时滚动 */
    overflow-x: hidden;
    min-height: 0;
    /* ✅ 允许 flex 收缩 */
    padding-top: 5px;
}

.custom-tabs :deep(.el-tabs__content::-webkit-scrollbar) {
    width: 6px;
    /* 滚动条宽度 */
    height: 6px;
    /* 水平滚动条高度 */
}

.custom-tabs :deep(.el-tabs__content::-webkit-scrollbar-track) {
    background: rgba(0, 0, 0, 0.05);
    /* 轨道背景 */
    border-radius: 4px;
}

.custom-tabs :deep(.el-tabs__content::-webkit-scrollbar-thumb) {
    background: rgba(0, 0, 0, 0.25);
    /* 滑块颜色 */
    border-radius: 4px;
    transition: background 0.3s;
}

.custom-tabs :deep(.el-tabs__content::-webkit-scrollbar-thumb:hover) {
    background: rgba(0, 0, 0, 0.4);
    /* 悬停时变深 */
}

.custom-tabs :deep(.el-tabs__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.6);
    margin-bottom: 15px;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
    display: none;
}

.custom-tabs :deep(.el-tabs__active-bar) {
    background-color: #6bc9ff;
    /* 红色 */
    height: 3px;
    /* 加粗 */
    border-radius: 4px;
}

.custom-tabs :deep(.el-tabs__item) {
    height: 50px;
    padding: 5px 8px;
    font-size: 30px;
    font-weight: 400;
    color: rgba(255, 255, 255, 1);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    letter-spacing: 0.5px;
}

/* ===== 悬停效果 ===== */
.custom-tabs :deep(.el-tabs__item:hover) {
    color: rgba(205, 205, 205, 0.85);
    transform: translateY(-4px);
}

/* ===== 选中状态 ===== */
.custom-tabs :deep(.el-tabs__item.is-active[aria-controls="pane-notplayed"]) {
    color: #6b89ff;
    font-weight: 600;
    transform: translateY(-4px);
}

.custom-tabs :deep(.el-tabs__item.is-active[aria-controls="pane-played"]) {
    color: #70ff6b;
    font-weight: 600;
    transform: translateY(-4px);
}

.custom-tabs :deep(.el-tabs__item.is-active[aria-controls="pane-giveup"]) {
    color: #ff6b6b;
    font-weight: 600;
    transform: translateY(-4px);
}

.custom-tabs :deep(.el-tabs__item.is-active[aria-controls="pane-all"]) {
    color: #000000;
    font-weight: 600;
    transform: translateY(-4px);
}

.pagehead {
    position: sticky;
    top: 0px;
    width: 100%;
    height: 5%;
    min-height: 20px;
    max-height: 80px;
    background-color: #444;
    z-index: 999;
    display: flex;
    align-items: center;
    padding: 5px 10px;
}

.bg {
    height: 100vh;
    width: 100vw;
    overflow: hidden;
    /* overflow-y: auto; */
    display: flex;
    /* flex-wrap: wrap; */
    flex-direction: column;
    align-items: center;
    background-color: rgba(0, 0, 0, 0.2);
    /* background-image: url("@/assets/re9.jpg"); */
    background-size: cover;
    background-position: 50% 20%;
    gap: 15px;
    /* 核心：淡入过渡，时长0.5秒，消除切换间隙 */
    transition: background-image 0.4s ease-in-out, opacity 0.4s ease-in-out;
    z-index: 1;
    /* 背景图层级最低 */
    position: relative;
}

.itembox {
    width: 95%;
    /* height: 100px; */
    background-color: rgba(231, 231, 231, 0.8);
    border-radius: 30px;
    gap: 20px;
    display: flex;
    align-items: center;
    padding: 15px 20px;
    margin-bottom: 10px;
    font-size: 20px;
    line-height: 1;
    transition: all 0.25s ease;
}

.itembox .el-rate {
    display: flex;
    align-items: center;
    height: auto;
    /* 控制组件高度 */
}

.itembox:hover {
    /* 上浮：向上移动5px */
    transform: translateY(-2.5px);
    /* 阴影加深放大，更有立体感 */
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
    /* 背景稍微亮一点（可选） */
    background-color: rgba(245, 245, 245, 0.9);
}

.textstyle {
    color: #2c3e50;
    /* 文字颜色 */
    font-size: 18px;
    /* 字体大小 */
    font-weight: bold;
    /* 字体粗细 */
    font-family: 'Arial', sans-serif;
    /* 字体类型 */
}

.custom-collapse {
    --el-collapse-border-color: transparent;
    --el-collapse-header-bg-color: transparent;
    --el-collapse-content-bg-color: transparent;
    border: none;
}

/* ⭐ 整个折叠项 = 卡片 */
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
    /* ⭐ 关键：让 __wrap 在卡片内部，不溢出圆角 */
    overflow: hidden;
}

/* ⭐ 卡片悬停 */
.custom-collapse :deep(.el-collapse-item:not(.is-active):hover) {
    transform: translateY(-2.5px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
    background-color: rgba(245, 245, 245, 0.9);
}

/* ⭐ header 去掉所有样式，透明 + 无内边距 */
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
    /* ⭐ 关键：整个 header 不响应点击 */
}

/* ⭐ title 撑满 */
.custom-collapse :deep(.el-collapse-item__title) {
    display: flex;
    align-items: center;
    flex: 1;
    padding: 0;
    background: transparent;
}

/* ⭐ itembox-content 内容布局 */
.custom-collapse :deep(.el-collapse-item__title .itembox-content) {
    display: flex;
    align-items: center;
    gap: 10px;
    flex: 1;
    width: 100%;
}

/* ⭐ 箭头样式 */
.custom-collapse :deep(.el-collapse-item__arrow) {
    margin-left: auto;
    font-size: 18px;
    color: #666;
    transition: transform 0.3s ease;
    flex-shrink: 0;
    pointer-events: auto !important;
    cursor: pointer !important;
}

/* ⭐ 展开时箭头旋转 */
.custom-collapse :deep(.el-collapse-item__header.is-active .el-collapse-item__arrow) {
    transform: rotate(90deg);
}

/* ⭐ 内容包裹层 - 去掉边框和背景 */
/* .custom-collapse :deep(.el-collapse-item__wrap) {
    background: transparent !important;
    border: none !important;
} */

/* ⭐ 内容区域 - 内边距底部和左右保持一致 */
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

.search-input :deep(.el-input__wrapper) {
    border-radius: 50px !important;
    box-shadow: 0 0 0 1px #dcdfe6 inset !important;
    transition: all 0.3s ease;
}

.plusbutton {
    position: absolute;
    width: 70px;
    height: 70px;
    top: 10px;
    right: 10px;
    font-size: 30px;
    color: white;
    border: none;
    /* ===== 绿色基调渐变色 ===== */
    background: linear-gradient(135deg, #38ef7d 0%, #00b894 100%);
    background-size: 200% 200%;

    /* ===== 绿色系四周发光 ===== */
    box-shadow:
        0 0 20px rgba(56, 239, 125, 0.5),
        0 0 40px rgba(17, 153, 142, 0.3),
        0 0 60px rgba(0, 184, 148, 0.2);

    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.plusbutton:hover {
    transform: scale(1.1);
    box-shadow:
        0 0 30px rgba(56, 239, 125, 0.7),
        0 0 60px rgba(17, 153, 142, 0.5),
        0 0 90px rgba(0, 184, 148, 0.3);
}

.el-checkbox {
    margin-right: 10px;
}

/* 特效开关：未激活文字改白色（激活文字保持蓝色） */
.effect-switch :deep(.el-switch__label:not(.is-active)) {
    color: #fff;
}
</style>
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
