<template>
  <div>
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col class="h4 text-truncate">{{ list.name }} </b-col>
            <b-col v-if="user.role !== 'RESTRICTED'" cols="2" class="text-right mr-3 mt-1">
              <b-link :to="{ name: 'list-edit', params: { id: id } }" class="text-dark">
                <font-awesome-icon :icon="['fas', 'pencil-alt']" size="2x" class="mt-n1 mr-n2"/>
              </b-link>
            </b-col>
          </b-row>
          <hr />
          <list-children-view :lists="list.children" />

          <list-tasks-view :tasks="list.tasks" class="mt-2" />
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
import { List } from "@/components/SetListView.vue";
import ListChildrenView from "@/components/ListChildrenView.vue";
import ListTasksView from "@/components/ListTasksView.vue";

@Component({ components: { BreadcrumbNavbarComponent, ListChildrenView, ListTasksView } })
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  private navbarItems = [{ id: "1", name: "Группа 1", type: "group" }];

  private user: User = { id: "1", name: "kate", role: "MEMBER", subscribedTo: [], assignedTo: [] };

  private list: List = {
    name: "Список 1",
    id: "1",
    children: [
      { name: "Список 11", id: "2", children: [], parentId: "1", groupId: "1", tasks: [] },
      { name: "Список 12", id: "3", children: [], parentId: "1", groupId: "1", tasks: [] },
      { name: "Список 13", id: "4", children: [], parentId: "1", groupId: "1", tasks: [] }
    ],
    parentId: "1",
    groupId: "1",
    tasks: [
      {
        id: this.id,
        name: "Полить цветы и подстричь кусты",
        description: "Нужно полить цветы, а то они сдохнут, и подстричь кусты, а то они некрасивые",
        status: "NOT_COMPLETED",
        children: [
          {
            id: "14",
            name: "Найти ножницы",
            description: "Кто опять взял и не вернул",
            status: "NOT_COMPLETED",
            children: [
              {
                id: "24",
                name: "Встать со стула",
                description: "Здесь их явно нет",
                status: "COMPLETED",
                children: [],
                listId: "4",
                parentId: "14"
              }
            ],
            listId: "4",
            parentId: this.id
          },
          {
            id: "34",
            name: "Найти цветы",
            description: "",
            status: "COMPLETED",
            children: [],
            listId: "4",
            parentId: "14"
          }
        ],
        listId: "4",
        parentId: null
      }
    ]
  };
}
</script>
