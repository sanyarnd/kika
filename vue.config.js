// eslint-disable-next-line @typescript-eslint/no-var-requires
const path = require("path");

module.exports = {
  pages: {
    index: {
      title: "Kika",
      entry: "src/main/typescript/pages/index/main.ts"
    },
    login: {
      title: "Login",
      entry: "src/main/typescript/pages/login/main.ts"
    },
    app: {
      title: "Kika",
      entry: "src/main/typescript/pages/app/main.ts"
    }
  },

  configureWebpack: {
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src/main/typescript")
      }
    }
  },

  outputDir: "target/spa",
  lintOnSave: false,

  css: {
    sourceMap: true
  },

  devServer: {
    proxy: "http://localhost:8080"
  }
};
