<template>
    <div>
        <el-button class="ai-button">AI</el-button>
    </div>
</template>
<script setup>

</script>
<style scoped>
.ai-button{
    /* 覆盖 Element Plus 悬停/点击时的默认纯色 */
    --el-button-hover-bg-color: transparent;
    --el-button-hover-border-color: transparent;
    --el-button-active-bg-color: transparent;
    --el-button-active-border-color: transparent;
    --el-button-hover-text-color: #fff;
    --el-button-active-text-color: #fff;
    width: 35px;
    height: 35px;
    margin: 0 10px;
    padding: 0;
    border: none;
    /* border-radius: 50%; */
    outline: none;
    color: #fff;
    font-weight: 700;
    letter-spacing: 1px;
    text-shadow: 0 0 10px rgba(0, 229, 255, 0.9);
    /* 蓝紫绿三色渐变，背景持续流动 */
    background: linear-gradient(120deg, #00bfff, #8a5cff 50%, #00e08a);
    background-size: 200% 200%;
    animation: flowGradient 5s ease-in-out infinite alternate;
    box-shadow:
        0 0 12px rgba(0, 190, 255, 0.5),
        0 0 24px rgba(138, 92, 255, 0.4),
        0 0 36px rgba(0, 224, 138, 0.3),
        inset 0 0 8px rgba(255, 255, 255, 0.12);
    transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.ai-button:hover{
    transform: scale(1.12);
    box-shadow:
        0 0 16px rgba(0, 190, 255, 0.7),
        0 0 32px rgba(138, 92, 255, 0.55),
        0 0 48px rgba(0, 224, 138, 0.4),
        inset 0 0 10px rgba(255, 255, 255, 0.18);
}

/* 渐变流动：来回平滑流动 */
@keyframes flowGradient{
    from { background-position: 0% 50%; }
    to { background-position: 100% 50%; }
}

</style>
