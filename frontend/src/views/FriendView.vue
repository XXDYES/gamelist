<template>
    <div class="bg" :style="{ backgroundImage: `url(${curbgurl})` }">
        <div class="pagehead">
            <div style="margin-right: auto;display: flex;align-items: center;">
                <el-icon :size="35" style="color:white;cursor: pointer;margin: auto 15px;" @click="backhome">
                    <Back />
                </el-icon>
                <span style="color: white;font-size: 16px;cursor: pointer;line-height: 1;" @click="backhome">返回主页</span>
            </div>
            <div
                style="color: white;position: absolute;left: 50%;top:50%; transform: translate(-50%, -50%);font-size: 20px; font-weight: bold;">
                正在访问好友主页🏠...
            </div>
            <div style="margin-left: auto;display: flex;align-items: center;">
                <el-icon :size="20" style="color: white;">
                    <Message />
                </el-icon>
                <span style="margin: 0 25px 0 5px;color: #fff;">消息</span>
                <FriendTab />
                <el-avatar :size="35"> user </el-avatar><span
                    style="color: white;margin: 0 20px 0 10px;font-size: large;">{{
                        userStore.username || '未登录' }}</span>
            </div>
        </div>
        <WallpaperSwitch v-model:bgurl="curbgurl" :bglist="bglist" />
        <div style="display: flex;flex: 1; min-height: 0;width: 95%;gap: 30px;margin-top: 20px;">
            <div class="info-tab">
                <el-avatar :size="120" class="friend-avatar">
                    <span style="font-size: 60px;">{{ (friInfo.username || '?').charAt(0) }}</span>
                </el-avatar>
                <span style="font-size: 50px;">{{ friInfo.username }}</span>
                <span style="font-size: 20px;">个性签名：{{ friInfo.signature || '无' }}</span>
                <div style="flex: 1;background-color: rgba(131, 167, 181, 0.8); width: 100%;border-radius: 20px;">
                    <span style="font-size: 30px;">拓展功能区</span>
                </div>
            </div>
            <div class="list_container">
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
                    <el-tab-pane label="🕜️待玩" name="notplayed" lazy>
                        <GameCardList :games="notplayedgames" :comments="comments" @change-status="onChangeStatus"
                            @delete="fetchGameList" @edit="fetchGameList" @addcmt="getComment" readonly />
                    </el-tab-pane>
                    <el-tab-pane label="✅️已玩" name="played" lazy>
                        <GameCardList :games="playedgames" :comments="comments" @change-status="onChangeStatus"
                            @delete="fetchGameList" @edit="fetchGameList" @addcmt="getComment" readonly />
                    </el-tab-pane>
                    <el-tab-pane label="❌️弃坑" name="giveup" lazy>
                        <GameCardList :games="giveupgames" :comments="comments" @change-status="onChangeStatus"
                            @delete="fetchGameList" @edit="fetchGameList" @addcmt="getComment" readonly />
                    </el-tab-pane>
                    <el-tab-pane label="👀全部" name="all" lazy>
                        <GameCardList :games="sortedgames" :comments="comments" @change-status="onChangeStatus"
                            @delete="fetchGameList" @edit="fetchGameList" @addcmt="getComment" readonly />
                    </el-tab-pane>
                </el-tabs>
            </div>
        </div>
    </div>
</template>
<script setup>
import { Back, Plus, Search, Sort, Menu, User, Message } from '@element-plus/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus'
import WallpaperSwitch from '@/components/WallpaperSwitch.vue';
import GameCardList from '@/components/GameCardList.vue';
import FriendTab from '@/components/FriendTab.vue';
import 'element-plus/dist/index.css'
import router from '@/router';
import { userApi, gameApi, commentApi, friendApi } from '@/api'
import { useUserStore } from '@/store/user'
import { useRoute } from 'vue-router';
const userStore = useUserStore()
const route = useRoute()
const bglist = [
    require('@/assets/sora.webp'),
    require('@/assets/FF16.jpg')
]
const bgIndex = Number(localStorage.getItem('bgIndex')) || 0
const curbgurl = ref(bglist[bgIndex % bglist.length])
// ========== 新增：用户信息 ==========
const userInfo = ref({ username: '未登录' });
const keyword = ref('')
const inputword = ref('')
const sortField = ref('addDate')
const sortOrder = ref('desc')
const sortPopVisible = ref(false)
const comments = ref({})
const friInfo = ref({})
const getComment = async () => {
    const res = await commentApi.getFriComment(route.params.id)
    if (res.data.code === '200') {
        const map = {}
        res.data.data.forEach(c => {
            if (!map[c.gameId]) { map[c.gameId] = [] }
            map[c.gameId].push(c)
        });
        comments.value = map
        console.log('评论列表：', comments.value)
    } else { ElMessage.error('获取评论失败') }
}
const getFriInfo = async () => {
    const res = await friendApi.getFriInfo(route.params.id)
    if(res.data.code === '200'){
        friInfo.value = res.data.data
    }else{ElMessage.error("获取好友信息失败")}
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
const fetchGameList = async () => {
    try {
        const res = await friendApi.getFriGame(route.params.id);
        if (res.data.code === "200") {
            gamelist.value = res.data.data;
            console.log('好友游戏列表:', gamelist.value);
        }else{ElMessage.error(res.data.msg);router.push('/gamelist')}
    } catch (error) {
        console.error('获取列表失败：', error);
        ElMessage.error('获取列表失败');
    }
};
const activeTab = ref('notplayed')
const value = ref()
const dialogVisable = ref(false)
const backhome = () => {
    router.push("/gamelist")
}
const gamelist = ref([])
onMounted(() => {
    // 获取用户信息
    fetchGameList()
    userStore.fetchUserInfo()
    getComment()
    getFriInfo()
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
const giveupgames = computed(() => {
    return sortedgames.value.filter(game => game.played === 2)
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
    flex: 1;          /* 吃掉剩余宽度 */
    min-width: 0;
    width: auto;      
    height: 95%;    
    background-color: rgba(147, 168, 176, 0.7);
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
    color: rgba(255, 255, 255, 0.85);
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
    background: linear-gradient(90deg, #6bc9ff, #00b894);
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
    flex-wrap: wrap;
    flex-direction: column;
    align-items: flex-start;
    background-size: cover;
    background-position: 50% 20%;
    gap: 15px;
    /* 核心：淡入过渡，时长0.5秒，消除切换间隙 */
    transition: background-image 0.4s ease-in-out, opacity 0.4s ease-in-out;
    z-index: 1;
    /* 背景图层级最低 */
    position: relative;
}

.search-input :deep(.el-input__wrapper) {
    border-radius: 50px !important;
    box-shadow: 0 0 0 1px #dcdfe6 inset !important;
    transition: all 0.3s ease;
}

.info-tab {
    width: 20%;        
    min-width: 0;         /* 防内容把它撑宽 */
    box-sizing: border-box;
    height: 95%;
    background-color: rgba(147, 168, 176, 0.7);
    border-radius: 25px;
    padding: 10px 30px;
    margin-left: 30px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    align-items: center;
}
</style>
