<template>
    <div style="top: 7%; right: 1%; position: fixed;">
        <el-tooltip class="box-item" effect="dark" content="点击切换壁纸" placement="left">
            <el-button type="primary" :icon="Picture" circle style="width: 50px; height: 50px; font-size: 25px;"
                @click="switchbg" />
        </el-tooltip>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
    // 当前壁纸，父组件用 v-model:bgurl 绑定
    bgurl: { type: String, default: '' },
    // 壁纸列表，父组件传入；不传则用默认 5 张
    bglist: {
        type: Array,
        default: () => [
            require('@/assets/re9.jpg'),
            require('@/assets/ER.jpg'),
            require('@/assets/XB2.webp'),
            require('@/assets/zelda1.jpg'),
            require('@/assets/FF7.jpg')
        ]
    }
})
const emit = defineEmits(['update:bgurl'])

const bglistindex = ref(Number(localStorage.getItem('bgIndex')) || 0)

const switchbg = () => {
    const nextIndex = (bglistindex.value + 1) % props.bglist.length
    const nextBgUrl = props.bglist[nextIndex]
    const tempImg = new Image()
    tempImg.onload = () => {
        bglistindex.value = nextIndex
        emit('update:bgurl', nextBgUrl)
        localStorage.setItem('bgIndex', nextIndex)
        ElMessage.success('已切换壁纸')
    }
    tempImg.onerror = () => {
        ElMessage.error('壁纸加载失败，使用当前壁纸')
    }
    tempImg.src = nextBgUrl
}
</script>