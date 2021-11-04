<template>
  <div>
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col class="h4 text-truncate">{{ group.name }} </b-col>
            <b-col v-if="user.role === 'OWNER'" cols="2" class="text-right mr-3 mt-1">
              <b-link :to="{ name: 'group-edit', params: { id: id } }" class="text-dark">
                <font-awesome-icon :icon="['fas', 'pencil-alt']" size="2x" class="mt-n1 mr-n2"/>
              </b-link>
            </b-col>
          </b-row>
          <hr />
          <list-children-view :lists="group.lists" />
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import { User } from "@/pages/app/views/Task.vue";
import { Group, List } from "@/components/SetListView.vue";
import ListChildrenView from "@/components/ListChildrenView.vue";
import ListTasksView from "@/components/ListTasksView.vue";

@Component({ components: { BreadcrumbNavbarComponent, ListChildrenView, ListTasksView } })
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  private user: User = { id: "1", name: "kate", role: "OWNER", subscribedTo: [], assignedTo: [] };

  private list_tree: List[] = [
    {
      name: "Список 1",
      id: "1",
      children: [
        { name: "Список 11", id: "2", children: [], tasks: [], parentId: "1", groupId: this.id },
        { name: "Список 12", id: "3", children: [], tasks: [], parentId: "1", groupId: this.id },
        {
          name: "Список 13",
          id: "4",
          children: [],
          tasks: [
            {
              id: "1",
              name: "Полить цветы и подстричь кусты",
              description: "Нужно полить цветы, а то они сдохнут, и подстричь кусты, а то они некрасивые",
              status: "COMPLETED",
              parentId: null,
              listId: "4",
              children: []
            }
          ],
          parentId: "1",
          groupId: this.id
        }
      ],
      tasks: [
        {
          id: "10",
          name: "Поесть",
          description: "Нужно не забыть поесть",
          status: "COMPLETED",
          parentId: null,
          listId: "4",
          children: [
            {
              id: "20",
              name: "Приготовить поесть",
              description: "Опять дома есть нечего",
              status: "COMPLETED",
              parentId: null,
              listId: "4",
              children: []
            }
          ]
        }
      ],
      parentId: null,
      groupId: this.id
    },
    {
      name: "Список 2",
      id: "5",
      children: [
        { name: "Список 21", id: "7", children: [], tasks: [], parentId: "5", groupId: this.id },
        { name: "Список 22", id: "8", children: [], tasks: [], parentId: "5", groupId: this.id },
        { name: "Список 23", id: "9", children: [], tasks: [], parentId: "5", groupId: this.id }
      ],
      tasks: [],
      parentId: null,
      groupId: this.id
    },
    { name: "Список 3", id: "6", children: [], tasks: [], parentId: null, groupId: this.id }
  ];

  private group: Group = { id: this.id, name: "Группа 1", ownerId: this.user.id, lists: this.list_tree };

  private navbarItems = [];
}
</script>
