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
  // 开发环境把 /api 转发到后端，前端代码里只写 /api，上线后交给 nginx（配置见 deploy/nginx.conf.example）
  devServer: {
    // 必须关掉压缩：dev server 的 compress 对代理转发的响应也生效，
    // 而 text/event-stream 属于可压缩类型，响应会被 gzip/br 缓冲住，
    // 浏览器连响应头都拿不到，EventSource 就永远停在 readyState=0（CONNECTING）
    compress: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        // 后端路由没有 /api 前缀（/login、/ai、/chatmsg…），所以这里把前缀去掉
        pathRewrite: { '^/api': '' }
      }
    }
  },
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
