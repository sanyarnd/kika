import Vue from "vue";
import { BootstrapVue } from "bootstrap-vue";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue/dist/bootstrap-vue.css";

import { extend, localize, setInteractionMode, ValidationObserver, ValidationProvider } from "vee-validate";
import * as rules from "vee-validate/dist/rules";
import en from "vee-validate/dist/locale/en.json";
import ru from "vee-validate/dist/locale/ru.json";

import { FontAwesomeIcon, FontAwesomeLayers } from "@fortawesome/vue-fontawesome";
import { library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { fab } from "@fortawesome/free-brands-svg-icons";
import { far } from "@fortawesome/free-regular-svg-icons";

import App from "@/pages/app/App.vue";
import { router } from "@/pages/app/router";
import { store } from "@/store";

// import all FontsAwesome icons
library.add(fas, fab, far);
Vue.component("FontAwesomeIcon", FontAwesomeIcon);
Vue.component("FontAwesomeLayers", FontAwesomeLayers);

for (const [rule, validation] of Object.entries(rules)) {
  extend(rule, { ...validation });
}
setInteractionMode("aggressive");
localize("en", en);
localize("ru", ru);
Vue.component("ValidationObserver", ValidationObserver);
Vue.component("ValidationProvider", ValidationProvider);

Vue.use(BootstrapVue);

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount("#app");
