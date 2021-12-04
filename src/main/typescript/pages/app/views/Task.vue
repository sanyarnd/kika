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
            <b-col v-if="task.group.role !== 'RESTRICTED'" cols="2" class="text-right mr-3 mt-1">
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
                :pressed="task.subscribed"
                variant="outline-info"
                size="lg"
                class="rounded-circle mr-2"
                @click="toggleSubscription()"
              >
                <div v-if="task.subscribed" class="text-center">
                  <font-awesome-icon :icon="['fas', 'bell']" class="task-action-icon" />
                </div>
                <div v-else class="text-center">
                  <font-awesome-icon :icon="['far', 'bell']" class="task-action-icon" />
                </div>
              </b-button>
              <b-button
                :pressed="task.assigned"
                variant="outline-info"
                size="lg"
                class="rounded-circle"
                @click="toggleAssignment()"
              >
                <div v-if="task.assigned" class="text-center">
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
                  :disabled="task.children.length > 0 && task.children.some((child) => child.status !== 'COMPLETED')"
                  style="white-space: nowrap"
                  class="text-secondary"
                  @click="toggleStatus()"
                >
                  <font-awesome-icon
                    v-if="task.status === 'NOT_COMPLETED'"
                    :icon="['far', 'square']"
                    size="3x"
                  />
                  <font-awesome-icon v-else :icon="['far', 'check-square']" size="3x" />
                </b-link>
                <b-tooltip
                  v-if="task.children.length > 0 && task.children.some((child) => child.status !== 'COMPLETED')"
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
            <b-list-group flush>
              <b-list-group-item
                v-for="(child, key) in task.children"
                :key="key"
                :style="{ 'text-decoration': child.status === 'COMPLETED' ? 'line-through' : 'none' }"
                :to="{ name: 'task', params: { id: child.id } }"
                class="text-truncate border-0"
              >
                <font-awesome-icon :icon="['fas', 'circle']" class="kika-icon mr-1" />
                {{ child.name }}
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
import ListTasksView from "@/components/ListTasksView.vue";
import { Account, NavbarItem, TaskInfo } from "@/models";
import { appModule } from "@/store/app-module";
import { api } from "@/backend";

@Component({
  components: { TaskTreeView, BreadcrumbNavbarComponent, ListTasksView },
})
export default class extends Vue {
  private loaded: boolean = false;

  @Prop()
  private readonly id!: string;

  private navbarItems: NavbarItem[] = [];

  private readonly appStore = appModule.context(this.$store);

  private task: TaskInfo | null = null;

  async created() {
    this.task = await api.getTaskInfo(this.id);
    if (this.task == null) {
      console.log(`failed to find task (id=${this.id})`);
      await this.$router.push({ name: "index" });
      return;
    }

    this.navbarItems.push({ id: this.task.group.id, name: this.task.group.name, type: "GROUP" });
    this.navbarItems.push({ id: this.task.list.id, name: this.task.list.name, type: "LIST" });

    if (this.task.parent != null) {
      this.navbarItems.push({ id: this.task.parent.id, name: this.task.parent.name, type: "TASK" });
    }

    this.loaded = true;
  }

  private async toggleStatus(): Promise<void> {
    if (this.task?.status == "COMPLETED") {
      await api.updateTaskStatus({ id: this.id, status: "NOT_COMPLETED" });
      this.task.status = "NOT_COMPLETED";
    } else if (this.task?.status == "NOT_COMPLETED") {
      await api.updateTaskStatus({ id: this.id, status: "COMPLETED" });
      this.task.status = "COMPLETED";
    }
  }

  private async toggleSubscription(): Promise<void> {
    if (this.task!.subscribed) {
      await api.unsubscribe(this.id);
    } else {
      await api.subscribe(this.id);
    }
    this.task!.subscribed = !this.task?.subscribed;
  }

  private async toggleAssignment(): Promise<void> {
    if (this.task!.assigned) {
      await api.retract(this.id);
    } else {
      await api.assign(this.id);
    }
    this.task!.assigned = !this.task?.assigned;
  }
}
</script>
