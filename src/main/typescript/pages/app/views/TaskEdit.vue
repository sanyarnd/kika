<template>
  <div>
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col class="h4">Редактирование</b-col>
          </b-row>
          <hr />
          <b-card-text>
            <b-form-group label="Название:" label-for="input-default">
              <b-form-textarea id="name" v-model="task.name" max-rows="6" class="mb-3" />
            </b-form-group>
            <b-form-group label="Описание:" label-for="input-default">
              <b-form-textarea id="description" v-model="task.description" max-rows="10" class="mb-3" />
            </b-form-group>
          </b-card-text>

          <move-object-component :object="{ object: task, type: 'TASK' }" :group="group" :list_tree="list_tree" />

          <b-form-group>
            <b-row>
              <b-col>
                <b-button variant="info" type="submit" block>Сохранить</b-button>
              </b-col>
              <b-col>
                <b-button :to="{ name: 'task', params: { id: id } }" block>Отмена</b-button>
              </b-col>
            </b-row>
          </b-form-group>
          <hr />
          <b-button v-b-modal:confirmDeletion variant="danger" block>
            <font-awesome-icon :icon="['fas', 'trash-alt']"/>
          </b-button>
          <b-modal
            id="confirmDeletion"
            v-model="showDeleteDialog"
            centered
            hide-header
            hide-footer
            hide-header-close
            :busy="true"
          >
            Пожалуйста, подтвердите удаление задачи <i>{{ task.name }}</i>
            <b-button-group class="mt-3 d-flex">
              <b-button variant="danger" class="text-center" @click="deleteTask(task.id)">Удалить</b-button>
              <b-button variant="outline-dark" class="text-center" @click="showDeleteDialog = false">Отмена</b-button>
            </b-button-group>
          </b-modal>
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import { Group } from "@/components/SetListView.vue";
import MoveObjectComponent from "@/components/MoveObjectComponent.vue";

@Component({
  components: { MoveObjectComponent, BreadcrumbNavbarComponent }
})
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  private user: User = { id: "1", name: "kate", role: "MEMBER", subscribedTo: [], assignedTo: [] };

  private showDeleteDialog = false;

  private task: Task = {
    id: this.id,
    name: "Полить цветы и подстричь кусты",
    description: "Нужно полить цветы, а то они сдохнут, и подстричь кусты, а то они некрасивые",
    status: "COMPLETED",
    parentId: null,
    listId: "4",
    children: []
  };

  private navbarItems = [
    { id: "1", name: "Группа 1", type: "group" },
    { id: "1", name: "Список 1", type: "list" },
    { id: this.task.id, name: this.task.name, type: "task" }
  ];

  private groupId = "1";

  private list_tree: List[] = [
    {
      name: "Список 1",
      id: "1",
      children: [
        { name: "Список 11", id: "2", children: [], tasks: [], parentId: "1", groupId: this.groupId },
        { name: "Список 12", id: "3", children: [], tasks: [], parentId: "1", groupId: this.groupId },
        { name: "Список 13", id: "4", children: [], tasks: [this.task], parentId: "1", groupId: this.groupId }
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
      groupId: this.groupId
    },
    {
      name: "Список 2",
      id: "5",
      children: [
        { name: "Список 21", id: "7", children: [], tasks: [], parentId: "5", groupId: this.groupId },
        { name: "Список 22", id: "8", children: [], tasks: [], parentId: "5", groupId: this.groupId },
        { name: "Список 23", id: "9", children: [], tasks: [], parentId: "5", groupId: this.groupId }
      ],
      tasks: [],
      parentId: null,
      groupId: this.groupId
    },
    { name: "Список 3", id: "6", children: [], tasks: [], parentId: null, groupId: this.groupId }
  ];

  private group: Group = { id: "1", name: "Группа 1", ownerId: this.user.id, lists: this.list_tree };

  private deleteTask(id: string): void {
    this.showDeleteDialog = false;
    console.log("DELETED TASK " + id);
  }
}

export interface Task {
  id: string;
  name: string;
  description: string;
  status: Status;
  listId: string;
  parentId: string | null;
  children: Task[];
}

export interface User {
  id: string;
  name: string;
  role: Role;
  subscribedTo: string[];
  assignedTo: string[];
}

export interface List {
  id: string;
  name: string;
  children: List[];
  tasks: Task[];
  parentId: string | null;
  groupId: string;
}

export type Status = "COMPLETED" | "NOT_COMPLETED" | "DELETED";
export type Role = "OWNER" | "MEMBER" | "RESTRICTED";
</script>
