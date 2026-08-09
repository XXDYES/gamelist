<template>
    <div class="app-wrapper">
        <el-card class="game-card" shadow="never">
            <!-- 头部 -->
            <div class="header">
                <h2>🎮 游戏列表</h2>
                <span class="count">共 {{ games.length }} 款</span>
            </div>

            <!-- 使用 el-collapse 实现展开/收起 -->
            <el-collapse v-model="activeNames" accordion class="custom-collapse">
                <el-collapse-item
                    v-for="game in games"
                    :key="game.id"
                    :name="String(game.id)"
                >
                    <!-- 自定义头部 -->
                    <template #title>
                        <div class="collapse-header">
                            <span class="game-icon">🎯</span>
                            <span class="game-name">{{ game.name }}</span>
                            <span class="game-meta">🏢 {{ game.company }}</span>
                            <span class="game-meta">🖥️ {{ game.platform }}</span>
                            <span class="game-meta">🕹️ {{ game.type }}</span>
                        </div>
                    </template>

                    <!-- 展开内容 -->
                    <div class="game-detail">
                        <p class="desc">{{ game.description }}</p>

                        <div class="meta-tags">
                            <span>🖥️ <strong>{{ game.platform }}</strong></span>
                            <span>📂 <strong>{{ game.type }}</strong></span>
                            <span>⭐ <strong>{{ game.rating }}</strong></span>
                            <span>📅 <strong>{{ game.date }}</strong></span>
                        </div>

                        <!-- ===== 评价区域 ===== -->
                        <div class="review-section">
                            <span class="review-label">💬 添加评价</span>

                            <!-- 星星评分 -->
                            <div class="star-rating-custom">
                                <span
                                    v-for="star in 5"
                                    :key="star"
                                    class="star"
                                    :class="{ active: star <= getDisplayStars(game) }"
                                    @click="setStarRating(game, star)"
                                    @mouseenter="previewStar(game, star)"
                                    @mouseleave="resetPreview(game)"
                                >★</span>
                                <span class="star-text">{{ getStarText(game) }}</span>
                            </div>

                            <!-- 输入框 + 提交按钮 -->
                            <div style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
                                <el-input
                                    v-model="game.reviewInput"
                                    placeholder="写下你的评价..."
                                    size="default"
                                    style="flex: 1; min-width: 160px;"
                                    @keyup.enter="submitReview(game)"
                                    clearable
                                />
                                <el-button type="primary" size="default" @click="submitReview(game)">
                                    📝 提交评价
                                </el-button>
                            </div>

                            <!-- 已提交的评价 -->
                            <div v-if="game.review.submitted && game.review.text" class="submitted-review-box">
                                <span class="stars-display">
                                    {{ '★'.repeat(game.review.stars) }}{{ '☆'.repeat(5 - game.review.stars) }}
                                </span>
                                <span class="review-text">{{ game.review.text }}</span>
                                <span class="review-time">📅 {{ game.review.time }}</span>
                            </div>
                        </div>
                        <!-- 查看详情按钮 -->
                        <el-button class="btn-detail" text @click.stop="showDetail(game)">
                            📖 查看完整信息
                        </el-button>
                    </div>
                </el-collapse-item>
            </el-collapse>

            <!-- 空状态 -->
            <div v-if="!games.length" class="empty-tip">📭 暂无游戏</div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// ========== 星星文本映射 ==========
const starTextMap = ['', '💩 拉完了', '😐 拉', '👍 NPC', '🔥 夯', '💯 夯爆了']

// ========== 模拟数据 ==========
const games = reactive([
    {
        id: 1,
        name: 'P5R 女神异闻录5 皇家版',
        company: 'ATLUS',
        platform: 'PS5 / PC / Switch',
        type: 'JRPG',
        rating: '⭐⭐⭐⭐⭐',
        date: '2026-02-26',
        description: '《女神异闻录5 皇家版》是由ATLUS开发的经典JRPG作品。玩家扮演一名被冤枉的学生，在白天过着普通的高中生活，夜晚则化身为"心之怪盗团"的成员，潜入扭曲的宫殿，改变恶人的内心。游戏以其独特的视觉风格、精彩的剧情和深邃的角色塑造而闻名，被誉为"史上最好的JRPG之一"。新增的第三学期内容更是为玩家带来了更加完整的故事体验。',
        review: { stars: 0, text: '', submitted: false, time: '' },
        reviewInput: ''
    },
    {
        id: 2,
        name: '塞尔达传说 旷野之息',
        company: '任天堂',
        platform: 'Switch / Wii U',
        type: '开放世界冒险',
        rating: '⭐⭐⭐⭐⭐',
        date: '2026-03-03',
        description: '《塞尔达传说 旷野之息》是任天堂最具革命性的开放世界游戏之一。玩家控制林克在海拉鲁大陆上自由探索，攀爬任何山壁、游泳过河、狩猎烹饪，用物理引擎创造无数种解谜方式。游戏以其"如果你能想到，你就能做到"的设计理念，重新定义了开放世界的边界。从初始台地眺望远方的震撼，到最终击败灾厄盖侬的感动，每一个玩家都会在这片大陆上书写属于自己的冒险故事。',
        review: { stars: 0, text: '', submitted: false, time: '' },
        reviewInput: ''
    },
    {
        id: 3,
        name: '艾尔登法环',
        company: 'FromSoftware',
        platform: 'PS5 / PC / Xbox Series X',
        type: '动作角色扮演',
        rating: '⭐⭐⭐⭐⭐',
        date: '2026-04-15',
        description: '《艾尔登法环》是由"魂系游戏之父"宫崎英高与奇幻小说家乔治·R·R·马丁联手打造的开放世界动作RPG。玩家扮演褪色者，穿越广袤的"狭间之地"，挑战半神，收集法环碎片，最终成为艾尔登之王。游戏继承了《黑暗之魂》系列的高难度战斗与深邃的碎片化叙事，同时融合了开放世界的自由探索，带来了前所未有的史诗冒险体验。',
        review: { stars: 0, text: '', submitted: false, time: '' },
        reviewInput: ''
    },
    {
        id: 4,
        name: '异度神剑3',
        company: 'Monolith Soft',
        platform: 'Switch',
        type: 'JRPG',
        rating: '⭐⭐⭐⭐',
        date: '2026-05-10',
        description: '《异度神剑3》是Monolith Soft开发的JRPG系列最新作。故事发生在两个敌对国家"科维斯"与"安格努斯"之间的战场上，主角诺亚与弥央分别来自敌对阵营，却因一场意外而并肩作战，探寻世界的真相。游戏拥有宏大的地图、深刻的剧情和独特的"衔尾蛇"战斗系统，将前两作的世界观完美融合，为系列画上了一个圆满的句号。',
        review: { stars: 0, text: '', submitted: false, time: '' },
        reviewInput: ''
    },
    {
        id: 5,
        name: '霍格沃茨之遗',
        company: 'Avalanche Software',
        platform: 'PS5 / PC / Xbox Series X',
        type: '开放世界冒险',
        rating: '⭐⭐⭐⭐',
        date: '2026-06-20',
        description: '《霍格沃茨之遗》是一款以《哈利·波特》魔法世界为背景的开放世界动作RPG。玩家扮演一名五年级学生，进入霍格沃茨魔法学校，学习咒语、熬制魔药、驯服神奇生物，并揭开一个古老的秘密。游戏高度还原了电影中的霍格沃茨城堡，你可以自由探索每一个角落，从格兰芬多公共休息室到有求必应屋，沉浸感十足。',
        review: { stars: 0, text: '', submitted: false, time: '' },
        reviewInput: ''
    }
])

// ========== 状态 ==========
const activeNames = ref(['1']) // 默认展开第一个
const previewMap = ref({})

// ========== 方法 ==========

// 获取显示的星星数（考虑预览）
const getDisplayStars = (game) => {
    if (previewMap.value[game.id] !== undefined) {
        return previewMap.value[game.id]
    }
    return game.review.stars || 0
}

// 获取星星文本
const getStarText = (game) => {
    const stars = getDisplayStars(game)
    return stars > 0 ? starTextMap[stars] : '点击星星评分'
}

// 设置星星评分
const setStarRating = (game, stars) => {
    game.review.stars = stars
    delete previewMap.value[game.id]
}

// 预览星星（hover）
const previewStar = (game, stars) => {
    previewMap.value[game.id] = stars
}

// 重置预览
const resetPreview = (game) => {
    delete previewMap.value[game.id]
}

// 提交评价
const submitReview = (game) => {
    const text = game.reviewInput?.trim() || ''
    if (!text) {
        ElMessage.warning('⚠️ 请先写下你的评价内容')
        return
    }
    if (game.review.stars === 0) {
        ElMessage.warning('⭐ 请先点击星星评分')
        return
    }

    const now = new Date()
    const timeStr =
        `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`

    game.review.text = text
    game.review.time = timeStr
    game.review.submitted = true
    game.reviewInput = ''

    ElMessage.success('✅ 评价提交成功！')
}

// 查看详情
const showDetail = (game) => {
    ElMessageBox.alert(
        `<div style="max-height: 400px; overflow-y: auto; line-height: 1.8;">
            <p><b>🎮 名称：</b>${game.name}</p>
            <p><b>🏢 制作商：</b>${game.company}</p>
            <p><b>🖥️ 平台：</b>${game.platform}</p>
            <p><b>📂 类型：</b>${game.type}</p>
            <p><b>⭐ 评分：</b>${game.rating}</p>
            <p><b>📅 入库：</b>${game.date}</p>
            <p style="margin-top: 12px;"><b>📖 简介：</b></p>
            <p style="color: rgba(255,255,255,0.8);">${game.description}</p>
        </div>`,
        '游戏详情',
        {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '知道了',
            customClass: 'game-detail-box',
        }
    )
}

// ========== 生命周期 ==========
onMounted(() => {
    // 可以做一些初始化操作
})
</script>

<style scoped>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.app-wrapper {
    width: 780px;
    max-width: 100%;
    margin: 0 auto;
}

/* ===== 主卡片 ===== */
.game-card {
    background: rgba(255, 255, 255, 0.06) !important;
    backdrop-filter: blur(12px) !important;
    border: 1px solid rgba(255, 255, 255, 0.06) !important;
    border-radius: 20px !important;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5) !important;
}

.game-card :deep(.el-card__body) {
    padding: 24px 28px 28px !important;
}

/* ===== 头部 ===== */
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    color: #fff;
}

.header h2 {
    font-size: 22px;
    font-weight: 600;
    letter-spacing: 1px;
}

.header .count {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.5);
    background: rgba(255, 255, 255, 0.08);
    padding: 4px 16px;
    border-radius: 20px;
}

/* ===== Collapse 全局覆盖 ===== */
.custom-collapse {
    --el-collapse-border-color: transparent;
    --el-collapse-header-bg-color: transparent;
    --el-collapse-content-bg-color: transparent;
    border: none;
}

.custom-collapse .el-collapse-item {
    margin-bottom: 10px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 14px;
    border: 1px solid rgba(255, 255, 255, 0.05);
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.custom-collapse .el-collapse-item:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.1);
}

.custom-collapse .el-collapse-item.is-active {
    background: rgba(255, 255, 255, 0.07);
    border-color: rgba(255, 255, 255, 0.12);
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.2);
}

/* ===== 头部样式 ===== */
.custom-collapse :deep(.el-collapse-item__header) {
    background: transparent !important;
    border: none !important;
    color: #fff;
    font-size: 15px;
    padding: 14px 18px !important;
    height: auto !important;
    line-height: 1.6;
    transition: all 0.25s ease;
    border-radius: 14px 14px 0 0 !important;
}

.custom-collapse :deep(.el-collapse-item__header:hover) {
    background: rgba(255, 255, 255, 0.03) !important;
}

/* ===== 箭头图标 ===== */
.custom-collapse :deep(.el-collapse-item__arrow) {
    color: rgba(255, 255, 255, 0.3) !important;
    font-size: 18px !important;
    transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.custom-collapse :deep(.el-collapse-item__arrow.is-active) {
    color: rgba(255, 255, 255, 0.6) !important;
    transform: rotate(180deg) !important;
}

/* ===== 内容区域 ===== */
.custom-collapse :deep(.el-collapse-item__wrap) {
    background: transparent !important;
    border: none !important;
}

.custom-collapse :deep(.el-collapse-item__content) {
    padding: 0 18px 18px 18px !important;
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
}

/* ===== 自定义头部布局 ===== */
.collapse-header {
    display: flex;
    align-items: center;
    gap: 14px;
    width: 100%;
    padding: 2px 0;
}

.collapse-header .game-icon {
    font-size: 20px;
    flex-shrink: 0;
}

.collapse-header .game-name {
    font-weight: 500;
    color: #fff;
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 15px;
}

.collapse-header .game-meta {
    color: rgba(255, 255, 255, 0.4);
    font-size: 13px;
    flex-shrink: 0;
    white-space: nowrap;
}

/* ===== 展开内容 ===== */
.game-detail .desc {
    color: rgba(255, 255, 255, 0.7);
    font-size: 14px;
    line-height: 1.8;
    margin-bottom: 12px;
}

.game-detail .meta-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px 20px;
    padding: 10px 0 4px 0;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.game-detail .meta-tags span {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.4);
}

.game-detail .meta-tags span strong {
    color: rgba(255, 255, 255, 0.7);
    font-weight: 500;
}

/* ===== 评价区域 ===== */
.review-section {
    margin-top: 14px;
    padding-top: 14px;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.review-section .review-label {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.5);
    margin-bottom: 8px;
    display: block;
}

/* 星星评分 */
.star-rating-custom {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 10px;
}

.star-rating-custom .star {
    font-size: 28px;
    cursor: pointer;
    color: rgba(255, 255, 255, 0.12);
    transition: color 0.2s, transform 0.15s;
    line-height: 1;
    user-select: none;
}

.star-rating-custom .star.active {
    color: #fbbf24;
}

.star-rating-custom .star:hover {
    transform: scale(1.12);
}

.star-rating-custom .star-text {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.4);
    margin-left: 8px;
    line-height: 32px;
}

/* 提交按钮 */
.review-section :deep(.el-button--primary) {
    background: rgba(255, 255, 255, 0.08) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    color: rgba(255, 255, 255, 0.7) !important;
    border-radius: 30px !important;
}

.review-section :deep(.el-button--primary:hover) {
    background: rgba(255, 255, 255, 0.16) !important;
    color: #fff !important;
}

.review-section :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.06) !important;
    border: 1px solid rgba(255, 255, 255, 0.08) !important;
    border-radius: 30px !important;
    box-shadow: none !important;
}

.review-section :deep(.el-input__wrapper:hover),
.review-section :deep(.el-input__wrapper.is-focus) {
    border-color: rgba(255, 255, 255, 0.2) !important;
}

.review-section :deep(.el-input__inner) {
    color: #fff !important;
}

.review-section :deep(.el-input__inner::placeholder) {
    color: rgba(255, 255, 255, 0.3) !important;
}

/* 已提交评价 */
.submitted-review-box {
    margin-top: 10px;
    padding: 10px 16px;
    background: rgba(255, 255, 255, 0.04);
    border-radius: 12px;
    border-left: 3px solid #fbbf24;
    display: flex;
    flex-wrap: wrap;
    align-items: baseline;
    gap: 6px 14px;
}

.submitted-review-box .stars-display {
    color: #fbbf24;
    font-size: 16px;
}

.submitted-review-box .review-text {
    color: rgba(255, 255, 255, 0.75);
    font-size: 14px;
    word-break: break-word;
    flex: 1;
}

.submitted-review-box .review-time {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.25);
    white-space: nowrap;
}

/* 查看详情按钮 */
.btn-detail {
    margin-top: 12px;
    color: rgba(255, 255, 255, 0.5) !important;
}

.btn-detail:hover {
    color: rgba(255, 255, 255, 0.8) !important;
}

/* 空状态 */
.empty-tip {
    text-align: center;
    color: rgba(255, 255, 255, 0.3);
    padding: 30px 0;
    font-size: 15px;
}

/* 滚动条 */
::-webkit-scrollbar {
    width: 4px;
}
::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.15);
    border-radius: 4px;
}
::-webkit-scrollbar-track {
    background: transparent;
}

/* ===== 响应式 ===== */
@media (max-width: 600px) {
    .game-card :deep(.el-card__body) {
        padding: 16px !important;
    }
    .collapse-header .game-meta {
        display: none;
    }
    .collapse-header .game-meta:first-of-type {
        display: inline;
    }
}
</style>