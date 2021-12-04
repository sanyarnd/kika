import Vue from "vue";
import VueRouter, { RouteConfig } from "vue-router";

Vue.use(VueRouter);

const routes: RouteConfig[] = [
  {
    path: "",
    name: "index",
    component: () => import("@/pages/app/views/Index.vue")
  },
  {
    path: "/settings",
    name: "app-settings",
    component: () => import("@/pages/app/views/AppSettings.vue")
  },
  {
    path: "/account/edit",
    name: "account-edit",
    component: () => import("@/pages/app/views/AccountEdit.vue")
  },
  {
    path: "/group/new",
    name: "group-new",
    component: () => import("@/pages/app/views/GroupNew.vue")
  },
  {
    path: "/group/:id",
    props: true,
    name: "group",
    component: () => import("@/pages/app/views/Group.vue")
  },
  {
    path: "/group/:id/edit",
    props: true,
    name: "group-edit",
    component: () => import("@/pages/app/views/GroupEdit.vue")
  },
  {
    path: "/list/new",
    props: true,
    name: "list-new",
    component: () => import("@/pages/app/views/ListNew.vue")
  },
  {
    path: "/list/:id/edit",
    props: true,
    name: "list-edit",
    component: () => import("@/pages/app/views/ListEdit.vue")
  },
  {
    path: "/list/:id",
    props: true,
    name: "list",
    component: () => import("@/pages/app/views/List.vue")
  },
  {
    path: "/task/new",
    props: true,
    name: "task-new",
    component: () => import("@/pages/app/views/TaskNew.vue")
  },
  {
    path: "/task/:id",
    props: true,
    name: "task",
    component: () => import("@/pages/app/views/Task.vue")
  },
  {
    path: "/task/:id/edit",
    props: true,
    name: "task-edit",
    component: () => import("@/pages/app/views/TaskEdit.vue")
  },
  {
    path: "*",
    name: "404",
    component: () => import("@/pages/app/views/404.vue")
  }
];

export const router = new VueRouter({
  base: `${process.env.BASE_URL}/app`,
  mode: "history",
  routes: routes
});
