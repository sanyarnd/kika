// eslint-disable-next-line @typescript-eslint/no-var-requires
const path = require("path");

// https://cli.vuejs.org/guide/mode-and-env.html#using-env-variables-in-client-side-code
// eslint-disable-next-line @typescript-eslint/no-var-requires
process.env.VUE_APP_VERSION = require("./package.json").version;

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
    port: 8081,
    disableHostCheck: true,
    proxy: {
      "^/api": {
        target: "http://localhost:8080",
        ws: true,
        changeOrigin: true
      }
    }
  }
};
