import Vue from "vue";
import { BootstrapVue, IconsPlugin } from "bootstrap-vue";

import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";

import { library } from "@fortawesome/fontawesome-svg-core";
import { faUserSecret } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";

import App from "@/pages/index/views/Index.vue";
import { router } from "@/pages/index/router";

Vue.use(BootstrapVue);
Vue.use(IconsPlugin);

library.add(faUserSecret);
Vue.component("FontAwesomeIcon", FontAwesomeIcon);

new Vue({
  router,
  render: h => h(App)
}).$mount("#app");
