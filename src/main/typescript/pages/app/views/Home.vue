<template>
  <div>
    Домашняя страница
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import SidebarComponent from "@/components/SidebarComponent.vue";
import TreeView from "@/components/TaskTreeView.vue";
import ListTreeView from "@/components/ListTreeView.vue";

@Component({ components: { TreeView, SidebarComponent, ListTreeView } })
export default class extends Vue {
  private page_type: string = "group-tasks";

  private selected_list = { id: null };

  private list1: string = "Список 1";
  private list_tree = [
    {
      name: this.list1,
      id: 1,
      children: [
        { name: "Список 11", id: 2, children: [], expanded: false },
        { name: "Список 12", id: 3, children: [], expanded: false },
        { name: "Список 13", id: 4, children: [], expanded: false }
      ],
      expanded: false
    },
    {
      name: "Список 2",
      id: 5,
      children: [
        { name: "Список 21", id: 7, children: [], expanded: false },
        { name: "Список 22", id: 8, children: [], expanded: false },
        { name: "Список 23", id: 9, children: [], expanded: false }
      ],
      expanded: false
    },
    { name: "Список 3", id: 6, children: [], expanded: false }
  ];
  private showDeleteDialog = false;
  private restricted_user = false;
  private task1: string = "Полить цветы и подстричь кусты";
  private task1_completed = false;
  private special_access = [
    { id: 1, name: "kate", has_access: true },
    { id: 2, name: "john", has_access: false },
    { id: 3, name: "bob", has_access: false }
  ];

  private notification_button_pressed: boolean = false;
  private subscription_button_pressed: boolean = false;

  private assigned_users = [
    { id: 1, name: "kate" },
    { id: 2, name: "john" }
  ];

  private deleteList(id: bigint): void {
    // ...
    this.showDeleteDialog = false;
  }

  private treeItems = [
    {
      name: "Купить тортик",
      id: 1,
      ticked: true,
      children: [
        {
          name: "Заказать тортик",
          id: 2,
          ticked: true,
          children: [
            { name: "Позвонить в кондитерскую", id: 5, ticked: true, children: [] },
            { name: "Выбрать тортик", id: 6, ticked: true, children: [] }
          ]
        },
        { name: "Забрать тортик", id: 3, ticked: true, children: [] }
      ]
    },
    { name: "Сварить кофе", id: 4, ticked: false, children: [] },
    { name: this.task1, id: 5, ticked: false, children: [] }
  ];
}
</script>
