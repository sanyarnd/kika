<template>
  <div v-if="loaded">
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
                <font-awesome-icon :icon="['fas', 'pencil-alt']" size="2x" class="mt-n1 mr-n2" />
              </b-link>
            </b-col>
          </b-row>
          <hr />
          <b-row>
            <b-col>
              <font-awesome-icon :icon="['fas', 'calendar-alt']" />
              02.11.2021
            </b-col>
          </b-row>
          <b-card-text class="lead mt-1">{{ task.description }}</b-card-text>
          <b-row>
            <b-col>
              <b-button
                :pressed="subscribedTo.includes(task.id)"
                variant="outline-info"
                size="lg"
                class="rounded-circle mr-2"
                @click="toggleSubscription()"
              >
                <div v-if="subscribedTo.includes(task.id)" class="text-center">
                  <font-awesome-icon :icon="['fas', 'bell']" class="task-action-icon" />
                </div>
                <div v-else class="text-center">
                  <font-awesome-icon :icon="['far', 'bell']" class="task-action-icon" />
                </div>
              </b-button>
              <b-button
                :pressed="assignedTo.includes(task.id)"
                variant="outline-info"
                size="lg"
                class="rounded-circle"
                @click="toggleAssignment()"
              >
                <div v-if="assignedTo.includes(task.id)" class="text-center">
                  <font-awesome-icon :icon="['fas', 'hand-paper']" class="task-action-icon" />
                </div>
                <div v-else>
                  <font-awesome-icon :icon="['far', 'hand-paper']" class="task-action-icon" />
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
                  <font-awesome-icon
                    v-if="appStore.getters.task(id).status === 'NOT_COMPLETED'"
                    :icon="['far', 'square']"
                    size="3x"
                  />
                  <font-awesome-icon v-else :icon="['far', 'check-square']" size="3x" />
                </b-link>
                <b-tooltip
                  v-if="task.children.length > 0 && task.children.some(child => child.status !== 'COMPLETED')"
                  target="complete-button"
                  placement="topleft"
                  class="h4"
                  custom-class="longTooltip"
                >
                  Необходимо выполнить оставшиеся подзадачи
                </b-tooltip>
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
import { Account, Group, List, NavbarItem, Task } from "@/models";
import { appModule } from "@/store/app-module";
import { api } from "@/backend";

@Component({
  components: { TaskTreeView, BreadcrumbNavbarComponent, ListTasksView }
})
export default class extends Vue {
  private loaded: boolean = false;

  @Prop()
  private readonly id!: string;

  private navbarItems: NavbarItem[] = [];

  private readonly appStore = appModule.context(this.$store);

  private list: List | undefined;
  private group: Group | undefined;
  private task: Task | undefined;
  private user: Account = this.appStore.getters.account;

  private subscribedTo: number[] = [];
  private assignedTo: number[] = [];

  async created() {
    this.task = this.appStore.getters.task(this.id);
    if (this.task == undefined) {
      console.log(`failed to find task (id=${this.id})`);
      await this.$router.push({ name: "index" });
      return;
    }

    this.list = this.appStore.getters.list(this.task?.listId);
    if (this.list == undefined) {
      await this.$router.push({ name: "index" });
      return;
    }

    this.group = this.appStore.getters.group(this.list.groupId);
    if (this.group == undefined) {
      await this.$router.push({ name: "index" });
      return;
    }

    this.navbarItems.push({ id: this.group.id, name: this.group.name, type: "GROUP" });
    this.navbarItems.push({ id: this.list.id, name: this.list.name, type: "LIST" });
    if (this.task.parentId != null) {
      const parentTask = this.appStore.getters.task(this.task.parentId);
      if (parentTask == undefined) {
        await this.$router.push({ name: "index" });
        return;
      }
      this.navbarItems.push({ id: +parentTask.id, name: parentTask.name, type: "TASK" });
    }

    const resp1 = await api.getSubscribedTasks();
    if (resp1 == null) {
      await this.$router.push({ name: "index" });
      return;
    } else {
      resp1.map(sub => sub.id).forEach(sub => this.subscribedTo.push(sub));
    }

    const resp2 = await api.getAssignedTasks();
    if (resp2 == null) {
      await this.$router.push({ name: "index" });
      return;
    } else {
      resp2.map(a => a.id).forEach(a => this.assignedTo.push(a));
    }

    this.loaded = true;
  }

  private async toggleStatus(): Promise<void> {
    if (this.task?.status == "COMPLETED") {
      await api.updateTaskStatus({ id: this.id, status: "NOT_COMPLETED" });
    } else if (this.task?.status == "NOT_COMPLETED") {
      await api.updateTaskStatus({ id: this.id, status: "COMPLETED" });
    }

    await this.appStore.actions.fetchGroupLists(this.group!.id);
    this.task = this.appStore.getters.task(this.id);
  }

  private async toggleSubscription(): Promise<void> {
    if (this.subscribedTo.includes(+this.id)) {
      await api.unsubscribe(this.id);
    } else {
      await api.subscribe(this.id);
    }

    const resp = await api.getSubscribedTasks();
    if (resp == null) {
      await this.$router.push({ name: "index" });
      return;
    } else {
      this.subscribedTo.splice(0, this.subscribedTo.length);
      resp.map(sub => sub.id).forEach(sub => this.subscribedTo.push(sub));
    }
  }

  private async toggleAssignment(): Promise<void> {
    if (this.assignedTo.includes(+this.id)) {
      await api.retract(this.id);
    } else {
      await api.assign(this.id);
    }

    const resp = await api.getAssignedTasks();
    if (resp == null) {
      await this.$router.push({ name: "index" });
      return;
    } else {
      this.assignedTo.splice(0, this.subscribedTo.length);
      resp.map(a => a.id).forEach(a => this.assignedTo.push(a));
    }
  }
}
</script>
