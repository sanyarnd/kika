import Vue from "vue";
import Vuex from "vuex";
import { createStore, Module } from "vuex-smart-module";
import createPersistedState from "vuex-persistedstate";
import { appModule } from "@/store/app-module";
import { settingsModule } from "@/store/settings-module";

Vue.use(Vuex);

const root = new Module({
  modules: {
    settingsModule: settingsModule,
    appModule: appModule
  }
});

export const store = createStore(root, {
  plugins: [createPersistedState({ key: "state" })],
  strict: process.env.NODE_ENV !== "production"
});
