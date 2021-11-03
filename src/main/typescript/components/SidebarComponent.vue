<template>
  <b-sidebar id="sidebar-1" title="Управление" shadow>
    <sidebar-tree-view :items="menu_items"></sidebar-tree-view>
  </b-sidebar>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import SidebarTreeView from "@/components/SidebarTreeView.vue";

@Component({ components: { SidebarTreeView } })
export default class extends Vue {
  private menu_items = [
    {
      name: "Группы",
      id: 0,
      children: [
        { name: "Мои группы", id: 1, link: "#", children: [] },
        { name: "Администрирование", id: 2, link: "#", children: [] }
      ]
    },
    {
      name: "Аккаунт",
      id: 3,
      children: [
        { name: "Настройки", id: 4, link: "#", children: [] },
        { name: "Выход", id: 5, link: "#", children: [] }
      ]
    },
    {
      name: "Приложение",
      id: 6,
      children: [
        { name: "Настройки", id: 7, link: "#", children: [] },
        { name: "О приложении", id: 8, link: "#", children: [] }
      ]
    }
  ];
}
</script>
