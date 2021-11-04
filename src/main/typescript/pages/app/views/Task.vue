<template>
  <div>
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col :style="{ 'text-decoration': task.status === 'COMPLETED' ? 'line-through' : 'none' }" class="h4">
              {{ task.name }}
            </b-col>
            <b-col v-if="user.role !== 'RESTRICTED'" cols="2" class="text-right mr-3 mt-1">
              <b-link :to="{ name: 'task-edit', params: { id: id } }" class="text-dark">
                <font-awesome-icon :icon="['fas', 'pencil-alt']" size="2x" class="mt-n1 mr-n2"/>
              </b-link>
            </b-col>
          </b-row>
          <hr />
          <b-row>
            <b-col>
              <font-awesome-icon :icon="['fas', 'calendar-alt']"/>
              02.11.2021
            </b-col>
          </b-row>
          <b-card-text class="lead mt-1">{{ task.description }}</b-card-text>
          <b-row>
            <b-col>
              <b-button
                :pressed="user.subscribedTo.includes(task.id)"
                variant="outline-info"
                size="lg"
                class="rounded-circle mr-2"
                @click="toggleSubscription()"
              >
                <div class="text-center" v-if="user.subscribedTo.includes(task.id)">
                  <font-awesome-icon :icon="['fas', 'bell']" class="task-action-icon"/>
                </div>
                <div class="text-center" v-else>
                  <font-awesome-icon :icon="['far', 'bell']" class="task-action-icon"/>
                </div>
              </b-button>
              <b-button
                :pressed="user.assignedTo.includes(task.id)"
                variant="outline-info"
                size="lg"
                class="rounded-circle"
                @click="toggleAssignment()"
              >
                <div class="text-center" v-if="user.assignedTo.includes(task.id)">
                  <font-awesome-icon :icon="['fas', 'hand-paper']" class="task-action-icon"/>
                </div>
                <div v-else>
                  <font-awesome-icon :icon="['far', 'hand-paper']" class="task-action-icon"/>
                </div>
              </b-button>
            </b-col>
            <b-col class="text-right text-right">
              <span id="complete-button">
                <b-link
                  :disabled="task.children.length > 0 && task.children.some(child => child.status !== 'COMPLETED')"
                  style="white-space: nowrap"
                  class="text-secondary"
                  @click="toggleStatus()"
                >
                  <font-awesome-icon v-if="task.status === 'NOT_COMPLETED'" :icon="['far', 'square']" size="3x"/>
                  <font-awesome-icon v-else :icon="['far', 'check-square']" size="3x"/>
                </b-link>
                <b-tooltip
                  v-if="task.children.length > 0 && task.children.some(child => child.status !== 'COMPLETED')"
                  target="complete-button"
                  placement="topleft"
                  class="h4"
                  custom-class="longTooltip"
                  >Необходимо выполнить оставшиеся подзадачи</b-tooltip
                >
              </span>
            </b-col>
          </b-row>
          <span v-if="task.children.length > 0">
            <hr />
            <list-tasks-view :tasks="task.children" class="mt-2" />
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
import ListTasksView from "@/components/ListTasksView.vue";

@Component({
  components: { TaskTreeView, BreadcrumbNavbarComponent, ListTasksView }
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

<style lang="scss">
.task-action-icon {
  width: 18px;
  height: 20px
}

.kika-icon {
  width: 24px;
}
</style>
