<template>
    <transition name="skill-fade">
        <div v-show="visible" class="skill-mask" @click="skip">
            <video ref="videoRef" class="skill-video" autoplay playsinline @canplay="tryPlay" @ended="onEnded"
                :src="videoSrc"></video>
            <!-- <div class="skill-skip-hint">点击任意处跳过</div> -->
        </div>
    </transition>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
    // 是否显示：父组件用 :visible 控制
    visible: { type: Boolean, default: false },
    // 全屏播放的视频（默认用抠好绿幕的 cutin_alpha.webm，可换成自己的素材）
    videoSrc: {
        type: String,
        default: require('@/assets/cutin_alpha.webm')
    }
})
const emit = defineEmits(['finished'])

const videoRef = ref(null)

// 每次 visible 变 true，从头播放
watch(() => props.visible, (val) => {
    if (val) {
        const v = videoRef.value
        if (v) {
            v.currentTime = 0
            v.muted = false
            v.play().catch(() => {
                v.muted = true
                v.play().catch(() => { })
            })
        }
    }
})

const tryPlay = () => {
    if (props.visible && videoRef.value) {
        const v = videoRef.value
        v.muted = false
        v.play().catch(() => {
            v.muted = true
            v.play().catch(() => { })
        })
    }
}

// 视频自然播完 → 通知父组件执行后续逻辑
const onEnded = () => emit('finished')

// 点击遮罩 → 跳过动画
const skip = () => emit('finished')
</script>

<style scoped>
.skill-mask {
    position: fixed;
    inset: 0;
    z-index: 9999;
    background: transparent;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    overflow: hidden;
}

.skill-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
}



.skill-skip-hint {
    position: absolute;
    bottom: 4%;
    left: 50%;
    transform: translateX(-50%);
    font-size: 14px;
    color: rgba(255, 255, 255, 0.6);
    letter-spacing: 2px;
    pointer-events: none;
}



/* 淡入淡出 */
.skill-fade-enter-active,
.skill-fade-leave-active {
    transition: opacity 0.25s ease;
}

.skill-fade-enter-from,
.skill-fade-leave-to {
    opacity: 0;
}
</style>