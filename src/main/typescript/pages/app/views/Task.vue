<template>
  <div>
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
          <b-card>
            <b-row>
              <b-col v-if="task.status === 'COMPLETED'" class="h4"><s>{{ task.name }}</s></b-col>
              <b-col v-else class="h4">{{ task.name }}</b-col>
              <b-col v-if="user.role !== 'RESTRICTED'" cols="2" align-self="center" class="text-right mr-3">
                <b-link
                    :to="{ name: 'task-edit', params: { id: id } }"
                    class="text-dark">
                  <b-icon-gear-fill scale="2"></b-icon-gear-fill>
                </b-link>
              </b-col>
            </b-row>
            <hr />
            <b-row>
              <b-col><span><i class="fas fa-calendar-alt fa-xs mr-1"></i>02.11.2021</span></b-col>
            </b-row>
            <b-card-text class="lead">{{ task.description }}</b-card-text>
            <b-row>
              <b-col>
            <b-button
              :pressed="user.subscribedTo.includes(task.id)"
              variant="outline-info"
              size="lg"
              class="rounded-circle mr-2"
              @click="toggleSubscription()"
            >
              <span v-if="user.subscribedTo.includes(task.id)"><i class="fas fa-bell"></i></span>
              <span v-else><i class="far fa-bell"></i></span>
            </b-button>
            <b-button
              :pressed="user.assignedTo.includes(task.id)"
              variant="outline-info"
              size="lg"
              class="rounded-circle"
              @click="toggleAssignment()"
            >
              <span v-if="user.assignedTo.includes(task.id)"><i class="fas fa-hand-paper"></i></span>
              <span v-else><i class="far fa-hand-paper"></i></span>
            </b-button>
              </b-col>
              <b-col
                  class="text-right text-right">
                <span id="complete-button">
                <b-link
                    :disabled="task.children.length > 0 && task.children.some(child => child.status !== 'COMPLETED')"
                    @click="toggleStatus()"
                    style="white-space: nowrap;"
                    class="text-secondary">
                  <i v-if="task.status === 'NOT_COMPLETED'" class="far fa-square fa-3x"/>
                  <i v-else class="far fa-check-square fa-3x"/>
                </b-link>
                <b-tooltip v-if="task.children.length > 0 && task.children.some(child => child.status !== 'COMPLETED')"
                           target="complete-button" placement="topleft">Необходимо выполнить оставшиеся подзадачи</b-tooltip>
                </span>
              </b-col>
            </b-row>
            <span v-if="task.children.length > 0">
              <hr>
              <b-list-group flush v-for="(child, key) in task.children" :key="key">
                <b-list-group-item :class="{'border-top': key===0}" class="border-bottom border-right border-left" :to="{ name: 'task', params: { id: child.id } }">
                  <s v-if="child.status==='COMPLETED'"><i class="fas fa-circle fa-xs mr-1"/>{{child.name}}</s>
                  <span v-else><i class="fas fa-circle fa-xs mr-1"/>{{child.name}}</span>
                </b-list-group-item>
              </b-list-group>
            </span>
          </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import TaskTreeView from "@/components/TaskTreeView.vue";

@Component({
  components: {TaskTreeView, BreadcrumbNavbarComponent }
})
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  private user: User = { id: "1", name: "kate", role: "MEMBER", subscribedTo: [], assignedTo: [] };

  private navbarItems = [
    { id: "1", name: "Группа 1", type: "group" },
    { id: "1", name: "Список 1", type: "list" }
  ];

  private task: Task = {
    id: this.id,
    name: "Полить цветы и подстричь кусты",
    description: "Нужно полить цветы, а то они сдохнут, и подстричь кусты, а то они некрасивые",
    status: "NOT_COMPLETED",
    children: [{
      id: "14",
      name: "Найти ножницы",
      description: "Кто опять взял и не вернул",
      status: "NOT_COMPLETED",
      children: [{
        id: "24",
        name: "Встать со стула",
        description: "Здесь их явно нет",
        status: "COMPLETED",
        children: [],
        listId: "4",
        parentId: "14"
      }],
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
      }],
    listId: "4",
    parentId: null
  };

  private toggleStatus(): void {
    if (this.task.status == "NOT_COMPLETED") {
      this.task.status = "COMPLETED";
    } else if (this.task.status == "COMPLETED") {
      this.task.status = "NOT_COMPLETED";
    }
  }

  private toggleSubscription(): void {
    if (this.user.subscribedTo.includes(this.task.id)) {
      this.user.subscribedTo.splice(this.user.subscribedTo.indexOf(this.task.id), 1);
    } else {
      this.user.subscribedTo.push(this.task.id);
    }
  }

  private toggleAssignment(): void {
    if (this.user.assignedTo.includes(this.task.id)) {
      this.user.assignedTo.splice(this.user.assignedTo.indexOf(this.task.id), 1);
    } else {
      this.user.assignedTo.push(this.task.id);
    }
  }
}

export interface Task {
  id: string;
  name: string;
  description: string;
  status: Status;
  children: Task[];
  listId: string;
  parentId: string | null;
}

export interface User {
  id: string;
  name: string;
  role: Role;
  subscribedTo: string[];
  assignedTo: string[];
}

export type Status = "COMPLETED" | "NOT_COMPLETED" | "DELETED";
export type Role = "OWNER" | "MEMBER" | "RESTRICTED";
</script>
