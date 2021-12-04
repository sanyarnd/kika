import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";

import { FontAwesomeIcon, FontAwesomeLayers } from "@fortawesome/vue-fontawesome";
import { library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { fab } from "@fortawesome/free-brands-svg-icons";
import { far } from "@fortawesome/free-regular-svg-icons";

import App from "@/pages/login/App.vue";
import { router } from "@/pages/login/router";
import { store } from "@/store";

// import all FontsAwesome icons
library.add(fas, fab, far);
Vue.component("FontAwesomeIcon", FontAwesomeIcon);
Vue.component("FontAwesomeLayers", FontAwesomeLayers);

Vue.use(BootstrapVue);

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount("#app");
