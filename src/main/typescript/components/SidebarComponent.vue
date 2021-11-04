<template>
  <b-sidebar id="sidebar-1" title="Управление" shadow>
    <sidebar-tree-view :items="menu_items"></sidebar-tree-view>
    <template #footer>
      <div class="d-flex bg-dark text-light align-items-center px-3 py-2">
        <strong class="mr-auto">Kika v0.01</strong>
      </div>
    </template>
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
      icon: "users",
      id: 0,
      children: [
        { name: "Выбрать группу", id: 9, link: "#", children: [] },
        { name: "Мои группы", id: 1, link: "#", children: [] },
        { name: "Администрирование", id: 2, link: "#", children: [] }
      ]
    },
    {
      name: "Аккаунт",
      id: 3,
      icon: "user-alt",
      children: [
        { name: "Настройки", id: 4, link: "#", children: [] },
        { name: "Выход", id: 5, link: "#", children: [] }
      ]
    },
    {
      name: "Приложение",
      icon: "cogs",
      id: 6,
      children: [
        { name: "Настройки", id: 7, link: "#", children: [] },
        { name: "О приложении", id: 8, link: "#", children: [] }
      ]
    }
  ];
}
</script>
