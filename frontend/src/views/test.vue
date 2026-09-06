<template>
    <!-- ==================== 视图一：好友列表 ==================== -->
    <div v-if="view === 'list'" class="friend-page">
        <header class="page-header">
            <div class="brand">
                <span class="brand-icon">👥</span>
                <div>
                    <h1 class="brand-title">好友空间</h1>
                    <p class="brand-sub">Friend Space · 纯前端演示</p>
                </div>
            </div>
            <el-input v-model="keyword" class="search-input" placeholder="搜索好友..." clearable>
                <template #prefix>
                    <el-icon><Search /></el-icon>
                </template>
            </el-input>
        </header>

        <div class="demo-banner">🎪 演示模式：数据全部为前端模拟，点好友右侧的「主页」进入对方的主页（只读）</div>

        <div class="stats">
            <div class="stat-card"><b>{{ friends.length }}</b><span>好友</span></div>
            <div class="stat-card"><b>{{ onlineCount }}</b><span>在线</span></div>
            <div class="stat-card"><b>{{ requests.length }}</b><span>申请</span></div>
        </div>

        <section class="section">
            <h2 class="section-title">💌 好友申请</h2>
            <div v-for="r in requests" :key="r.id" class="req-card">
                <div class="avatar-ring ring-1">
                    <el-avatar :size="46">{{ r.name.charAt(0) }}</el-avatar>
                </div>
                <div class="req-info">
                    <span class="name">{{ r.name }}</span>
                    <span class="msg">留言：{{ r.msg }}</span>
                </div>
                <div class="actions">
                    <button class="btn-accept" @click="toast('演示模式：已同意申请')">同意</button>
                    <button class="btn-reject" @click="toast('演示模式：已拒绝申请')">拒绝</button>
                </div>
            </div>
            <div v-if="!requests.length" class="empty">暂无申请</div>
        </section>

        <section class="section">
            <h2 class="section-title">🫶 好友列表</h2>
            <div v-for="f in filteredFriends" :key="f.id" class="friend-card">
                <div class="avatar-wrap">
                    <div class="avatar-ring" :class="'ring-' + (f.id % 3)">
                        <el-avatar :size="52">{{ f.name.charAt(0) }}</el-avatar>
                    </div>
                    <span class="status-dot" :class="f.online ? 'online' : 'offline'"></span>
                </div>
                <div class="friend-info">
                    <span class="name">{{ f.name }}</span>
                    <span class="meta">{{ f.signature }}</span>
                </div>
                <div class="friend-actions">
                    <el-button class="btn-home" size="small" round @click="openHome(f)">主页</el-button>
                    <el-button class="btn-del" size="small" round @click="toast('演示模式：好友不可删除')">删除</el-button>
                </div>
            </div>
            <div v-if="!filteredFriends.length" class="empty">没有匹配的好友 🤷</div>
        </section>
    </div>

    <!-- ==================== 视图二：好友主页（仿 MainView，只读） ==================== -->
    <div v-else class="home-bg">
        <!-- 顶部栏：仿 MainView 的 pagehead -->
        <header class="home-head">
            <el-button class="btn-back" round @click="view = 'list'">
                <el-icon><Back /></el-icon>&nbsp;返回
            </el-button>
            <div class="home-title">
                <span class="home-title-name">{{ currentFriend.name }} 的游戏库</span>
                <span class="visitor-chip">🔒 访客模式</span>
            </div>
            <div class="home-user">
                <el-avatar :size="34">{{ currentFriend.name.charAt(0) }}</el-avatar>
                <span>{{ currentFriend.name }}</span>
            </div>
        </header>

        <!-- 中间面板：仿 MainView 的 list_container -->
        <div class="home-panel">
            <div class="readonly-bar">
                👀 你正在以访客身份浏览 <b>{{ currentFriend.name }}</b> 的主页
                · 共 {{ currentFriendGames.length }} 款游戏 · 仅可查看，不可修改
            </div>

            <!-- 搜索 + 排序：与 MainView 一致 -->
            <div class="toolbar">
                <el-input v-model="homeKeyword" class="search-input home-search"
                    placeholder="输入 名称/制作商/平台/类型 搜索..." clearable>
                    <template #prefix>
                        <el-icon><Search /></el-icon>
                    </template>
                </el-input>
                <el-popover v-model:visible="sortPopVisible" placement="bottom" trigger="click" :width="170">
                    <span style="font-weight: 600;">排序选项</span>
                    <el-divider style="margin: 6px 0;" />
                    <el-radio-group v-model="sortField" style="display: flex; flex-direction: column; gap: 5px;">
                        <el-radio value="name">名称(A-Z)</el-radio>
                        <el-radio value="price">价格</el-radio>
                        <el-radio value="mcRating">MC评分</el-radio>
                        <el-radio value="releaseDate">发售日期</el-radio>
                        <el-radio value="addDate">添加日期</el-radio>
                    </el-radio-group>
                    <el-radio-group v-model="sortOrder" size="small" style="margin-top: 6px;">
                        <el-radio-button label="asc">↑升序</el-radio-button>
                        <el-radio-button label="desc">↓降序</el-radio-button>
                    </el-radio-group>
                    <div style="display: flex; justify-content: space-between; margin-top: 8px;">
                        <el-button size="small" @click="resetSort">重置</el-button>
                        <el-button size="small" type="primary" @click="sortPopVisible = false">确认</el-button>
                    </div>
                    <template #reference>
                        <el-button type="primary" circle :icon="Sort" class="tool-btn" />
                    </template>
                </el-popover>
            </div>

            <!-- 统计小条 -->
            <div class="mini-stats">
                <span>🕜 待玩 {{ countByStatus(0) }}</span>
                <span>✅ 已玩 {{ countByStatus(1) }}</span>
                <span>❌ 弃坑 {{ countByStatus(2) }}</span>
                <span>⭐ 平均评分 {{ avgRating }}</span>
                <span>💬 评价 {{ commentCount }}</span>
            </div>

            <!-- 分栏：与 MainView 的四个 tab 一致 -->
            <el-tabs v-model="homeTab" class="home-tabs">
                <el-tab-pane v-for="p in tabs" :key="p.name" :label="p.label" :name="p.name">
                    <div v-if="!gamesByTab(p.name).length" class="empty">这位好友还没有这类游戏 🤷</div>
                    <div v-for="game in gamesByTab(p.name)" :key="game.id" class="readonly-card"
                        :class="{ expanded: expandedId === game.id }" @click="toggleExpand(game)">
                        <div class="lock-badge">🔒 只读</div>
                        <div class="game-cover" :class="'cover-' + (game.id % 5)">{{ game.cover }}</div>
                        <div class="game-main">
                            <div class="game-title-row">
                                <span class="game-name">🎯 {{ game.name }}</span>
                                <span class="status-chip" :class="'st-' + game.played">{{ statusText(game.played) }}</span>
                            </div>
                            <div class="game-tags">
                                <span class="game-tag">🏢 {{ game.company }}</span>
                                <span class="game-tag">🖥️ {{ game.platform }}</span>
                                <span class="game-tag">🕹️ {{ game.type }}</span>
                            </div>
                            <div class="game-meta">
                                <span>💰 {{ game.price }}</span>
                                <span>MC {{ game.mcRating }}</span>
                                <span>📅 {{ game.releaseDate }} 发售</span>
                                <span>➕ {{ game.addDate }} 添加</span>
                            </div>
                            <div class="game-rating">
                                <el-rate v-if="game.played !== 0" v-model="game.rating" disabled
                                    :texts="['拉完了', '拉', 'NPC', '夯', '夯爆了']" show-text />
                                <span v-else class="not-rated">还未玩过，暂无评分</span>
                            </div>
                        </div>
                        <div class="expand-arrow">{{ expandedId === game.id ? '▴' : '▾' }}</div>

                        <!-- 展开详情：介绍 + 好友评价（只读） -->
                        <div v-if="expandedId === game.id" class="game-detail" @click.stop>
                            <div class="divider-label">📖 游戏介绍</div>
                            <p class="game-info">{{ game.info }}</p>
                            <div class="divider-label">💬 好友评价（{{ (game.comments || []).length }}）</div>
                            <div v-if="game.comments && game.comments.length">
                                <div v-for="c in game.comments" :key="c.id" class="comment-bubble">
                                    <el-avatar :size="40" class="comment-avatar">{{ c.user.charAt(0) }}</el-avatar>
                                    <div class="comment-body">
                                        <div class="comment-head">
                                            <span class="comment-user">{{ c.user }}</span>
                                            <el-rate v-model="c.rating" disabled size="small" />
                                        </div>
                                        <span class="comment-date">{{ c.date }}</span>
                                        <p class="comment-content">{{ c.content }}</p>
                                    </div>
                                </div>
                            </div>
                            <div v-else class="no-comment">— 还没有评价 —</div>
                            <div class="readonly-tip">🔒 访客只能浏览，不能发表评价或修改信息</div>
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Search, Sort, Back } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const view = ref('list')
const keyword = ref('')
const homeKeyword = ref('')
const homeTab = ref('notplayed')
const expandedId = ref(null)
const currentFriend = ref(null)
const sortField = ref('addDate')
const sortOrder = ref('desc')
const sortPopVisible = ref(false)

const toast = (msg) => ElMessage.info(msg)

/* ===== 好友申请（演示数据） ===== */
const requests = ref([
    { id: 1, name: '雾岛', msg: '一起玩老头环吗？' },
    { id: 2, name: 'Luna', msg: '求带原神！' }
])

/* ===== 好友列表（演示数据） ===== */
const friends = ref([
    { id: 1, name: '星野', signature: '今天的我也在交界地受苦', date: '08-12', online: true },
    { id: 2, name: 'Nova', signature: 'Persona 天下第一！', date: '08-20', online: true },
    { id: 3, name: '阿澈', signature: '海拉鲁老流氓，偶尔打打铁', date: '08-25', online: false },
    { id: 4, name: 'Momo', signature: '异度神剑忠实粉丝', date: '08-28', online: true },
    { id: 5, name: '远山', signature: '剧情党，巫师三神作', date: '08-30', online: false }
])

const onlineCount = computed(() => friends.value.filter(f => f.online).length)
const filteredFriends = computed(() => {
    const kw = keyword.value.trim().toLowerCase()
    if (!kw) return friends.value
    return friends.value.filter(f => f.name.toLowerCase().includes(kw))
})

const openHome = (f) => {
    currentFriend.value = f
    expandedId.value = null
    homeTab.value = 'notplayed'
    homeKeyword.value = ''
    sortField.value = 'addDate'
    sortOrder.value = 'desc'
    view.value = 'home'
}

/* ===== 好友游戏库（演示数据，按好友 id 分组） ===== */
const friendGames = {
    1: [
        {
            id: 101, cover: '⚔️', name: '艾尔登法环', company: 'FromSoftware', platform: 'PC / PS5', type: 'ARPG',
            price: '¥298', mcRating: 96, releaseDate: '2022-02-25', addDate: '2022-03-01', played: 1, rating: 5,
            info: '在交界地以褪色者身份重铸艾尔登法环。开放世界自由探索，Boss 战难度硬核，但成就感十足。',
            comments: [
                { id: 1, user: '星野', rating: 5, date: '2022-03-20', content: '宫崎英高的怜悯是不存在的，但 150 小时后我只想说：神作。' },
                { id: 2, user: '雾岛', rating: 5, date: '2022-04-02', content: '拉塔恩打了 40 次才过，值了。' }
            ]
        },
        {
            id: 102, cover: '🐒', name: '黑神话：悟空', company: '游戏科学', platform: 'PC / PS5', type: '动作',
            price: '¥268', mcRating: 90, releaseDate: '2024-08-20', addDate: '2024-08-20', played: 1, rating: 4,
            info: '天命人重走西游路。画面顶级，棍法演出华丽，地图探索和 Boss 战都很有味道。',
            comments: [
                { id: 1, user: '星野', rating: 4, date: '2024-09-01', content: '虎先锋给我上了一课，但二周目真香。' }
            ]
        },
        {
            id: 103, cover: '🎲', name: '博德之门3', company: 'Larian Studios', platform: 'PC', type: 'CRPG',
            price: '¥298', mcRating: 96, releaseDate: '2023-08-03', addDate: '2024-01-15', played: 0, rating: 0,
            info: 'D&D 5E 规则下的开放世界冒险，骰子决定命运，队友个个有故事，支线丰富到离谱。',
            comments: []
        },
        {
            id: 104, cover: '🕳️', name: '空洞骑士', company: 'Team Cherry', platform: 'PC / Switch', type: '类银河城',
            price: '¥45', mcRating: 90, releaseDate: '2017-02-24', addDate: '2023-06-10', played: 2, rating: 2,
            info: '手绘风格的地下王国探索。手感一流，但白宫跳跳乐劝退了我。',
            comments: []
        }
    ],
    2: [
        {
            id: 201, cover: '😈', name: '女神异闻录5 皇家版', company: 'Atlus', platform: 'PS5 / Switch / PC', type: 'JRPG',
            price: '¥468', mcRating: 95, releaseDate: '2019-10-31', addDate: '2022-10-01', played: 1, rating: 5,
            info: '怪盗团偷走人心的欲望。白天上学晚上当怪盗，日常与迷宫两开花，音乐时髦到爆炸。',
            comments: [
                { id: 1, user: 'Nova', rating: 5, date: '2022-12-24', content: 'P5R 是我玩过最有范儿的 RPG，没有之一。' }
            ]
        },
        {
            id: 202, cover: '🗡️', name: '塞尔达传说 王国之泪', company: '任天堂', platform: 'Switch', type: '开放世界',
            price: '¥430', mcRating: 96, releaseDate: '2023-05-12', addDate: '2023-05-12', played: 1, rating: 5,
            info: '空岛、地底、海拉鲁三线探索，究极手建造系统让每个玩家都有自己的解法。',
            comments: []
        },
        {
            id: 203, cover: '🌾', name: '星露谷物语', company: 'ConcernedApe', platform: 'PC / Switch', type: '模拟经营',
            price: '¥48', mcRating: 89, releaseDate: '2016-02-26', addDate: '2024-05-01', played: 0, rating: 0,
            info: '经营爷爷留下的农场，种田钓鱼挖矿谈恋爱，一玩就是 300 小时。',
            comments: []
        },
        {
            id: 204, cover: '🌃', name: '赛博朋克2077', company: 'CD Projekt RED', platform: 'PC / PS5', type: '开放世界',
            price: '¥298', mcRating: 86, releaseDate: '2020-12-10', addDate: '2021-02-14', played: 2, rating: 3,
            info: '夜之城的霓虹与义体。主线支线剧情出色，但发售时的 Bug 让我弃了坑。',
            comments: []
        }
    ],
    3: [
        {
            id: 301, cover: '🏹', name: '塞尔达传说 旷野之息', company: '任天堂', platform: 'Switch', type: '开放世界',
            price: '¥399', mcRating: 97, releaseDate: '2017-03-03', addDate: '2021-08-01', played: 1, rating: 5,
            info: '重新定义了开放世界。翻山越岭、解密神庙、讨伐盖侬，世界每个角落都值得探索。',
            comments: []
        },
        {
            id: 302, cover: '⚔️', name: '艾尔登法环', company: 'FromSoftware', platform: 'PS5', type: 'ARPG',
            price: '¥398', mcRating: 96, releaseDate: '2022-02-25', addDate: '2024-03-10', played: 0, rating: 0,
            info: '慕名入手，还没勇气踏入盖利德。',
            comments: []
        },
        {
            id: 303, cover: '⛏️', name: '我的世界', company: 'Mojang', platform: 'PC / Switch', type: '沙盒',
            price: '¥165', mcRating: 93, releaseDate: '2011-11-18', addDate: '2019-06-01', played: 1, rating: 4,
            info: '和朋友搭了一个城堡，然后炸了它。像素世界的无限可能。',
            comments: []
        }
    ],
    4: [
        {
            id: 401, cover: '⚡', name: '异度神剑3', company: 'Monolith Soft', platform: 'Switch', type: 'JRPG',
            price: '¥430', mcRating: 89, releaseDate: '2022-07-29', addDate: '2022-08-01', played: 1, rating: 5,
            info: '艾欧尼翁的壮阔大地图，无缝战斗与剧情演出都拉满，结局泪目。',
            comments: [
                { id: 1, user: 'Momo', rating: 5, date: '2022-09-30', content: '诺亚与弥央的故事我记一辈子。' }
            ]
        },
        {
            id: 402, cover: '🔥', name: '火焰纹章：风花雪月', company: 'Intelligent Systems', platform: 'Switch', type: 'SRPG',
            price: '¥398', mcRating: 89, releaseDate: '2019-07-26', addDate: '2023-04-05', played: 0, rating: 0,
            info: '学园养成 + 战棋战斗，三条学级三种命运，二周目预定。',
            comments: []
        },
        {
            id: 403, cover: '🛡️', name: '最终幻想7 重制版', company: 'Square Enix', platform: 'PS5 / PC', type: 'ARPG',
            price: '¥468', mcRating: 89, releaseDate: '2020-04-10', addDate: '2021-11-11', played: 2, rating: 3,
            info: '画面和战斗都进化了，但章节制流程拖得太长，玩到贫民窟就弃了。',
            comments: []
        }
    ],
    5: [
        {
            id: 501, cover: '🪄', name: '霍格沃茨之遗', company: 'Avalanche Software', platform: 'PC / PS5', type: '开放世界',
            price: '¥298', mcRating: 84, releaseDate: '2023-02-10', addDate: '2023-02-25', played: 1, rating: 4,
            info: '圆梦霍格沃茨。城堡细节惊人，飞行扫帚手感一流，支线略重复但值得一逛。',
            comments: []
        },
        {
            id: 502, cover: '🤖', name: '底特律：化身为人', company: 'Quantic Dream', platform: 'PC / PS5', type: '互动电影',
            price: '¥199', mcRating: 78, releaseDate: '2018-05-25', addDate: '2024-02-14', played: 0, rating: 0,
            info: '三个仿生人的命运由你决定，每个选择都有后果，结局全靠自己打出来。',
            comments: []
        },
        {
            id: 503, cover: '🐺', name: '巫师3：狂猎', company: 'CD Projekt RED', platform: 'PC / PS5', type: 'ARPG',
            price: '¥127', mcRating: 93, releaseDate: '2015-05-19', addDate: '2019-01-01', played: 1, rating: 5,
            info: '杰洛特寻女之旅。任务设计教科书，石之心与血与酒两大 DLC 一样神。',
            comments: [
                { id: 1, user: '远山', rating: 5, date: '2019-06-15', content: '昆特牌启动器？不，这是最好的开放世界 RPG。' }
            ]
        }
    ]
}

const currentFriendGames = computed(() => friendGames[currentFriend.value?.id] || [])
const statusText = (p) => ({ 0: '🕜 待玩', 1: '✅ 已玩', 2: '❌ 弃坑' })[p]
const countByStatus = (p) => currentFriendGames.value.filter(g => g.played === p).length
const commentCount = computed(() => currentFriendGames.value.reduce((s, g) => s + (g.comments?.length || 0), 0))
const avgRating = computed(() => {
    const played = currentFriendGames.value.filter(g => g.played === 1 && g.rating > 0)
    if (!played.length) return '--'
    return (played.reduce((s, g) => s + g.rating, 0) / played.length).toFixed(1)
})

/* ===== 与 MainView 一致的分栏 ===== */
const tabs = [
    { name: 'notplayed', label: '🕜️待玩' },
    { name: 'played', label: '✅️已玩' },
    { name: 'giveup', label: '❌️弃坑' },
    { name: 'all', label: '👀全部' }
]

const filteredHomeGames = computed(() => {
    let list = currentFriendGames.value
    const kw = homeKeyword.value.trim().toLowerCase()
    if (kw) {
        list = list.filter(g => [g.name, g.company, g.platform, g.type]
            .some(v => String(v).toLowerCase().includes(kw)))
    }
    return list
})

function extractNumber(str) {
    const match = String(str).match(/(\d+(\.\d+)?)/)
    return match ? parseFloat(match[1]) : 0
}

const sortedHomeGames = computed(() => {
    const list = [...filteredHomeGames.value]
    const dir = sortOrder.value === 'asc' ? 1 : -1
    list.sort((a, b) => {
        const va = a[sortField.value]
        const vb = b[sortField.value]
        if (va == null || va === '' || va === '暂无') return 1
        if (vb == null || vb === '' || vb === '暂无') return -1
        if (sortField.value === 'name') {
            return va.localeCompare(vb, 'zh') * dir
        }
        if (sortField.value === 'releaseDate' || sortField.value === 'addDate') {
            const da = new Date(va)
            const db = new Date(vb)
            if (isNaN(da)) return 1
            if (isNaN(db)) return -1
            return (da - db) * dir || (b.id - a.id)
        }
        return (extractNumber(va) - extractNumber(vb)) * dir
    })
    return list
})

const gamesByTab = (name) => {
    if (name === 'notplayed') return sortedHomeGames.value.filter(g => g.played === 0)
    if (name === 'played') return sortedHomeGames.value.filter(g => g.played === 1)
    if (name === 'giveup') return sortedHomeGames.value.filter(g => g.played === 2)
    return sortedHomeGames.value
}

const resetSort = () => {
    sortField.value = 'addDate'
    sortOrder.value = 'desc'
    sortPopVisible.value = false
}

const toggleExpand = (game) => {
    expandedId.value = expandedId.value === game.id ? null : game.id
}
</script>

<style scoped>
/* ==================== 视图一：好友列表（暖色日落主题） ==================== */
.friend-page {
    min-height: 100vh;
    padding: 30px 40px 60px;
    box-sizing: border-box;
    background:
        radial-gradient(1000px 500px at 85% -10%, rgba(255, 140, 90, 0.35), transparent 60%),
        radial-gradient(800px 400px at -10% 110%, rgba(176, 107, 255, 0.35), transparent 60%),
        linear-gradient(135deg, #241b3d 0%, #5b2a63 45%, #a33c6e 80%, #e86a4d 100%);
    color: #fff;
}

.page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    flex-wrap: wrap;
    margin-bottom: 24px;
}

.brand {
    display: flex;
    align-items: center;
    gap: 14px;
}

.brand-icon {
    font-size: 40px;
    filter: drop-shadow(0 4px 12px rgba(255, 140, 90, 0.4));
}

.brand-title {
    margin: 0;
    font-size: 26px;
    font-weight: 700;
    background: linear-gradient(120deg, #ffd36b, #ff8c5a, #ff5c8a, #b06bff);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    letter-spacing: 2px;
}

.brand-sub {
    margin: 4px 0 0;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.55);
    letter-spacing: 1px;
}

.search-input {
    width: 260px;
}

.search-input :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-radius: 999px;
    box-shadow: none !important;
    color: #fff;
    transition: all 0.25s ease;
}

.search-input :deep(.el-input__wrapper.is-focus) {
    border-color: rgba(255, 140, 90, 0.7);
    box-shadow: 0 0 0 3px rgba(255, 140, 90, 0.18) !important;
}

.search-input :deep(.el-input__inner) {
    color: #fff;
}

.search-input :deep(.el-input__inner::placeholder) {
    color: rgba(255, 255, 255, 0.4);
}

.demo-banner {
    margin-bottom: 18px;
    padding: 10px 16px;
    font-size: 13px;
    border-radius: 12px 12px 12px 4px;
    background: rgba(255, 211, 107, 0.12);
    border: 1px dashed rgba(255, 211, 107, 0.4);
    color: rgba(255, 211, 107, 0.95);
}

.stats {
    display: flex;
    gap: 14px;
    margin-bottom: 28px;
}

.stat-card {
    flex: 1;
    max-width: 140px;
    padding: 14px 0;
    text-align: center;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.12);
    border-radius: 16px 16px 16px 6px;
    backdrop-filter: blur(8px);
    transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.stat-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(255, 140, 90, 0.25);
}

.stat-card b {
    display: block;
    font-size: 26px;
    background: linear-gradient(120deg, #ffd36b, #ff8c5a);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
}

.stat-card span {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.65);
}

.section {
    margin-bottom: 30px;
}

.section-title {
    margin: 0 0 12px 4px;
    font-size: 18px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.9);
}

.req-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 12px 16px;
    margin-bottom: 10px;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.12);
    border-radius: 18px 18px 18px 6px;
    backdrop-filter: blur(8px);
    transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.req-card:hover {
    transform: translateX(-4px);
    box-shadow: 0 8px 24px rgba(255, 92, 138, 0.25);
}

.req-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.req-info .msg {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.55);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.actions {
    display: flex;
    gap: 8px;
}

.btn-accept,
.btn-reject {
    border: none;
    border-radius: 999px;
    padding: 6px 16px;
    font-size: 13px;
    cursor: pointer;
    color: #fff;
    transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
}

.btn-accept {
    background: linear-gradient(135deg, #ff9a5a, #ff5c8a);
    box-shadow: 0 4px 12px rgba(255, 92, 138, 0.35);
}

.btn-reject {
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-accept:hover,
.btn-reject:hover {
    transform: translateY(-1px);
    filter: brightness(1.1);
}

.friend-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 14px 18px;
    margin-bottom: 12px;
    background: rgba(255, 255, 255, 0.09);
    border: 1px solid rgba(255, 255, 255, 0.13);
    border-radius: 20px 20px 20px 6px;
    backdrop-filter: blur(10px);
    transition: transform 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;
}

.friend-card:hover {
    transform: translateX(-4px);
    background: rgba(255, 255, 255, 0.14);
    box-shadow: 0 10px 30px rgba(255, 140, 90, 0.28);
}

.avatar-wrap {
    position: relative;
    flex-shrink: 0;
}

.avatar-ring {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    padding: 3px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.25);
}

.ring-0 { background: linear-gradient(135deg, #ffd36b, #ff8c5a); }
.ring-1 { background: linear-gradient(135deg, #ff8c5a, #ff5c8a); }
.ring-2 { background: linear-gradient(135deg, #ff5c8a, #b06bff); }

.avatar-ring :deep(.el-avatar) {
    --el-avatar-bg-color: rgba(255, 255, 255, 0.2);
    width: 54px;
    height: 54px;
    font-size: 20px;
    color: #fff;
    font-weight: 600;
}

.status-dot {
    position: absolute;
    right: 2px;
    bottom: 4px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    border: 2px solid #241b3d;
}

.status-dot.online {
    background: #4ade80;
    box-shadow: 0 0 8px rgba(74, 222, 128, 0.8);
}

.status-dot.offline {
    background: #9ca3af;
}

.friend-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.friend-info .name {
    font-size: 17px;
    font-weight: 600;
}

.friend-info .meta {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.55);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.friend-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
}

.btn-home {
    background: linear-gradient(135deg, #ff9a5a, #ff5c8a) !important;
    border: none !important;
    color: #fff !important;
    box-shadow: 0 4px 12px rgba(255, 92, 138, 0.3);
}

.btn-del {
    background: transparent !important;
    border: 1px solid rgba(255, 92, 138, 0.5) !important;
    color: rgba(255, 255, 255, 0.85) !important;
}

.btn-del:hover {
    background: rgba(255, 92, 138, 0.15) !important;
    border-color: #ff5c8a !important;
    color: #fff !important;
}

.empty {
    padding: 24px;
    text-align: center;
    color: rgba(255, 255, 255, 0.45);
    background: rgba(255, 255, 255, 0.05);
    border-radius: 16px;
    border: 1px dashed rgba(255, 255, 255, 0.15);
}

/* ==================== 视图二：好友主页（仿 MainView 布局 + 暖色只读风格） ==================== */
.home-bg {
    position: relative;
    height: 100vh;
    width: 100vw;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    align-items: center;
    background: url('@/assets/ER.jpg') center 20% / cover no-repeat;
    color: #fff;
}

/* 暖色滤镜层：与 MainView 冷色背景区分 */
.home-bg::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(55, 20, 65, 0.86) 0%, rgba(120, 45, 90, 0.78) 45%, rgba(215, 100, 60, 0.62) 100%);
    z-index: 0;
}

.home-bg > * {
    position: relative;
    z-index: 1;
}

/* 顶部栏：仿 MainView 的 pagehead */
.home-head {
    width: 100%;
    height: 60px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    box-sizing: border-box;
    background: rgba(45, 25, 55, 0.9);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
}

.btn-back {
    background: rgba(255, 255, 255, 0.1) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    color: #fff !important;
    transition: all 0.25s ease;
}

.btn-back:hover {
    background: rgba(255, 255, 255, 0.2) !important;
    border-color: rgba(255, 211, 107, 0.6) !important;
    transform: translateX(-2px);
}

.home-title {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 18px;
    font-weight: 700;
    letter-spacing: 1px;
}

.home-title-name {
    background: linear-gradient(120deg, #ffd36b, #ff8c5a, #ff5c8a);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
}

.visitor-chip {
    padding: 3px 12px;
    font-size: 12px;
    font-weight: 500;
    border-radius: 999px;
    background: rgba(255, 211, 107, 0.14);
    border: 1px solid rgba(255, 211, 107, 0.45);
    color: #ffd36b;
    letter-spacing: 1px;
}

.home-user {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
}

.home-user :deep(.el-avatar) {
    --el-avatar-bg-color: rgba(255, 255, 255, 0.2);
    background: linear-gradient(135deg, #ff9a5a, #ff5c8a);
    color: #fff;
    font-weight: 600;
}

/* 中间面板：仿 MainView 的 list_container */
.home-panel {
    width: 72%;
    max-width: 1100px;
    height: calc(100% - 84px);
    margin-top: 16px;
    margin-bottom: 20px;
    padding: 14px 30px 18px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background: rgba(80, 45, 70, 0.72);
    border: 1px solid rgba(255, 255, 255, 0.14);
    border-radius: 24px 24px 24px 8px;
    backdrop-filter: blur(12px);
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4);
    animation: panelIn 0.35s ease;
}

.readonly-bar {
    flex-shrink: 0;
    margin-bottom: 10px;
    padding: 8px 14px;
    font-size: 12px;
    border-radius: 10px 10px 10px 4px;
    background: rgba(255, 211, 107, 0.1);
    border: 1px dashed rgba(255, 211, 107, 0.4);
    color: rgba(255, 211, 107, 0.9);
}

.readonly-bar b {
    color: #fff;
}

.toolbar {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 8px;
}

.home-search {
    width: 480px;
    max-width: 100%;
}

.tool-btn {
    width: 40px;
    height: 40px;
    font-size: 20px;
    background: linear-gradient(135deg, #ff9a5a, #ff5c8a) !important;
    border: none !important;
    color: #fff !important;
    box-shadow: 0 4px 12px rgba(255, 92, 138, 0.3);
}

.mini-stats {
    flex-shrink: 0;
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    margin-bottom: 6px;
}

.mini-stats span {
    padding: 4px 12px;
    font-size: 12px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.12);
    color: rgba(255, 255, 255, 0.85);
}

/* 分栏：与 MainView 结构一致，暖色 active 条 */
.home-tabs {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
}

.home-tabs :deep(.el-tabs__content) {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    min-height: 0;
    padding-top: 6px;
}

.home-tabs :deep(.el-tabs__content::-webkit-scrollbar) {
    width: 6px;
    height: 6px;
}

.home-tabs :deep(.el-tabs__content::-webkit-scrollbar-track) {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 4px;
}

.home-tabs :deep(.el-tabs__content::-webkit-scrollbar-thumb) {
    background: rgba(255, 211, 107, 0.35);
    border-radius: 4px;
}

.home-tabs :deep(.el-tabs__header) {
    flex-shrink: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.18);
    margin-bottom: 10px;
}

.home-tabs :deep(.el-tabs__nav-wrap::after) {
    display: none;
}

.home-tabs :deep(.el-tabs__item) {
    height: 42px;
    padding: 5px 8px;
    font-size: 16px;
    color: rgba(255, 255, 255, 0.65);
    transition: all 0.25s ease;
}

.home-tabs :deep(.el-tabs__item:hover) {
    color: #fff;
    transform: translateY(-2px);
}

.home-tabs :deep(.el-tabs__item.is-active) {
    color: #ffd36b;
    font-weight: 600;
    transform: translateY(-2px);
}

.home-tabs :deep(.el-tabs__active-bar) {
    height: 3px;
    border-radius: 3px;
    background: linear-gradient(90deg, #ffd36b, #ff8c5a, #ff5c8a, #b06bff);
}

/* 只读游戏卡片 */
.readonly-card {
    position: relative;
    display: flex;
    align-items: center;
    gap: 18px;
    padding: 14px 20px;
    margin-bottom: 10px;
    background: rgba(255, 255, 255, 0.09);
    border: 1px solid rgba(255, 255, 255, 0.13);
    border-radius: 20px 20px 20px 7px;
    cursor: pointer;
    transition: transform 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;
    animation: cardIn 0.3s ease both;
}

.readonly-card:hover {
    transform: translateY(-2px);
    background: rgba(255, 255, 255, 0.14);
    box-shadow: 0 10px 28px rgba(255, 140, 90, 0.25);
}

.readonly-card.expanded {
    background: rgba(255, 255, 255, 0.16);
    box-shadow: 0 10px 32px rgba(255, 140, 90, 0.35);
}

.lock-badge {
    position: absolute;
    top: 8px;
    right: 10px;
    padding: 2px 10px;
    font-size: 11px;
    border-radius: 999px;
    background: rgba(255, 211, 107, 0.14);
    border: 1px solid rgba(255, 211, 107, 0.4);
    color: #ffd36b;
    letter-spacing: 1px;
    opacity: 0;
    transform: translateY(-4px);
    transition: all 0.25s ease;
    pointer-events: none;
}

.readonly-card:hover .lock-badge {
    opacity: 1;
    transform: translateY(0);
}

.game-cover {
    flex-shrink: 0;
    width: 68px;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36px;
    border-radius: 16px 16px 16px 6px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.35);
}

.cover-0 { background: linear-gradient(135deg, #ff9a5a, #ff5c8a); }
.cover-1 { background: linear-gradient(135deg, #ffd36b, #ff8c5a); }
.cover-2 { background: linear-gradient(135deg, #ff5c8a, #b06bff); }
.cover-3 { background: linear-gradient(135deg, #38ef7d, #00b894); }
.cover-4 { background: linear-gradient(135deg, #6b89ff, #b06bff); }

.game-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 7px;
}

.game-title-row {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}

.game-name {
    font-size: 19px;
    font-weight: 700;
    color: #fff;
    letter-spacing: 0.5px;
}

.status-chip {
    padding: 3px 12px;
    font-size: 12px;
    border-radius: 999px;
    color: #fff;
}

.status-chip.st-0 { background: rgba(107, 137, 255, 0.85); }
.status-chip.st-1 { background: rgba(56, 239, 125, 0.85); }
.status-chip.st-2 { background: rgba(255, 107, 107, 0.85); }

.game-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.game-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.85);
    background: rgba(255, 255, 255, 0.12);
    white-space: nowrap;
}

.game-meta {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
    font-size: 12px;
    color: rgba(255, 255, 255, 0.55);
}

.game-rating :deep(.el-rate) {
    --el-rate-void-color: rgba(255, 255, 255, 0.25);
    --el-rate-disabled-void-color: rgba(255, 255, 255, 0.25);
    height: 22px;
}

.game-rating :deep(.el-rate__text) {
    order: -1;
    margin-right: 10px;
    color: #ffd36b;
    font-size: 13px;
}

.game-rating :deep(.el-rate__item) {
    cursor: default !important;
}

.not-rated {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.45);
}

.expand-arrow {
    flex-shrink: 0;
    font-size: 18px;
    color: rgba(255, 255, 255, 0.6);
}

.game-detail {
    width: 100%;
    padding: 12px 4px 4px;
    border-top: 1px dashed rgba(255, 255, 255, 0.2);
    animation: detailIn 0.3s ease;
}

.divider-label {
    display: flex;
    align-items: center;
    gap: 14px;
    font-weight: 600;
    color: #fff;
    font-size: 15px;
    margin: 0 0 10px;
}

.divider-label::after {
    content: '';
    flex: 1;
    height: 2px;
    background: rgba(255, 255, 255, 0.18);
    border-radius: 4px;
}

.game-info {
    margin: 0 0 16px;
    font-size: 14px;
    line-height: 1.7;
    text-indent: 2em;
    color: rgba(255, 255, 255, 0.85);
}

.comment-bubble {
    display: flex;
    gap: 12px;
    padding: 12px 14px;
    margin-bottom: 10px;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 18px 18px 18px 6px;
}

.comment-avatar {
    background: linear-gradient(135deg, #ff9a5a, #ff5c8a);
    color: #fff;
    flex-shrink: 0;
}

.comment-body {
    flex: 1;
    min-width: 0;
}

.comment-head {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
}

.comment-user {
    font-size: 15px;
    font-weight: 600;
    color: #ffd36b;
}

.comment-head :deep(.el-rate) {
    --el-rate-void-color: rgba(255, 255, 255, 0.25);
    --el-rate-disabled-void-color: rgba(255, 255, 255, 0.25);
    height: 20px;
}

.comment-head :deep(.el-rate__item) {
    cursor: default !important;
}

.comment-date {
    display: block;
    margin-top: 2px;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.4);
}

.comment-content {
    margin: 6px 0 0;
    font-size: 13px;
    line-height: 1.6;
    color: rgba(255, 255, 255, 0.85);
}

.no-comment {
    padding: 16px;
    text-align: center;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.4);
    border: 1px dashed rgba(255, 255, 255, 0.15);
    border-radius: 14px;
}

.readonly-tip {
    margin-top: 6px;
    padding: 8px 14px;
    font-size: 12px;
    text-align: center;
    color: rgba(255, 211, 107, 0.75);
    background: rgba(255, 211, 107, 0.08);
    border-radius: 10px;
}

/* ===== 动效 ===== */
@keyframes panelIn {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
}

@keyframes cardIn {
    from { opacity: 0; transform: translateY(8px); }
    to { opacity: 1; transform: translateY(0); }
}

@keyframes detailIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

/* ===== 响应式 ===== */
@media (max-width: 720px) {
    .friend-page {
        padding: 20px 16px 40px;
    }

    .search-input,
    .home-search {
        width: 100%;
    }

    .friend-actions {
        flex-direction: column;
    }

    .home-panel {
        width: 94%;
        padding: 12px 14px 16px;
    }

    .home-title {
        position: static;
        transform: none;
        font-size: 14px;
    }

    .home-user span {
        display: none;
    }

    .game-cover {
        width: 52px;
        height: 62px;
        font-size: 28px;
    }
}
</style>
