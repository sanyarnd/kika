import Vue from "vue";
import { BootstrapVue, IconsPlugin } from "bootstrap-vue";

import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";
import App from "@/pages/app/App.vue";

import { router } from "@/pages/app/router";
Vue.use(BootstrapVue);
Vue.use(IconsPlugin);

new Vue({
  router,
  render: h => h(App)
}).$mount("#app");
