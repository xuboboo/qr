const isProd = process.env.NODE_ENV === 'production'

module.exports = {
  publicPath: isProd ? '/qr/' : '/',
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/q': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
