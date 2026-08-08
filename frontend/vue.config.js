// const { defineConfig } = require('@vue/cli-service')
// module.exports = defineConfig({
//   transpileDependencies: true
// })
const { defineConfig } = require('@vue/cli-service')
const AutoImport = require('unplugin-auto-import/webpack')
const Components = require('unplugin-vue-components/webpack')
const { ElementPlusResolver } = require('unplugin-vue-components/resolvers')
 
module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  configureWebpack: {
    plugins: [
      AutoImport({
        imports: ['vue'],
        resolvers: [ElementPlusResolver()]
      }),
      Components({
        resolvers: [ElementPlusResolver()],
        dirs: ['src/components']
      })
    ],

    module: {
      rules: [
        {
          test: /\.(mp4|webm|ogg|mov|avi)$/i,  // 匹配视频格式
          type: 'asset/resource',               // Webpack 5 内置资源模块
          generator: {
            filename: 'videos/[name].[hash:8].[ext]' // 输出到 dist/videos/
          }
        }
      ]
    }
  }
})