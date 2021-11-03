import Vue from "vue";
import { BootstrapVue, IconsPlugin } from "bootstrap-vue";

import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";
import Login from "@/pages/login/views/Login.vue";

import { router } from "@/pages/login/router";
Vue.use(BootstrapVue);
Vue.use(IconsPlugin);

new Vue({
  router,
  render: h => h(Login)
}).$mount("#app");
