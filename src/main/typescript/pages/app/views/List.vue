<template>
  <div v-if="loaded">
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col class="h4 text-truncate">{{ list.name }}</b-col>
            <b-col v-if="list.group.role !== 'RESTRICTED'" cols="2" class="text-right mr-3 mt-1">
              <b-link :to="{ name: 'list-edit', params: { id: id } }" class="text-dark">
                <font-awesome-icon :icon="['fas', 'pencil-alt']" size="2x" class="mt-n1 mr-n2" />
              </b-link>
            </b-col>
          </b-row>
          <hr />

          <b-list-group flush>
            <b-list-group-item
              v-for="(child, key) in list.children"
              :key="key"
              :class="{ 'border-top': key === 0 }"
              :to="{ name: 'list', params: { id: child.id } }"
              class="border-bottom border-right border-left text-truncate"
            >
              <font-awesome-icon :icon="['fas', 'list-ul']" class="kika-icon mr-2" />
              {{ child.name }}
            </b-list-group-item>
          </b-list-group>

          <b-button
            :to="{
              name: 'list-new',
              params: {
                groupId: list.group.id,
                parentListId: list.id
              }
            }"
            variant="outline-dark mt-1"
            block
            squared
            size="sm"
          >
            <font-awesome-icon class="kika-icon mr-1" :icon="['fas', 'plus']" />
            Новый список
          </b-button>

          <b-list-group flush>
            <b-list-group-item
              v-for="(task, key) in list.tasks"
              :key="key"
              :style="{ 'text-decoration': task.status === 'COMPLETED' ? 'line-through' : 'none' }"
              :to="{ name: 'task', params: { id: task.id } }"
              class="text-truncate border-0"
            >
              <font-awesome-icon :icon="['fas', 'circle']" class="kika-icon mr-1" />
              {{ task.name }}
            </b-list-group-item>
          </b-list-group>

          <b-button
            :to="{ name: 'task-new', params: { groupId: list.group.id, parentListId: list.id } }"
            variant="outline-dark mt-1"
            block
            squared
            size="sm"
          >
            <font-awesome-icon class="kika-icon mr-1" :icon="['fas', 'plus']" />
            Новая задача
          </b-button>
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import ListChildrenView from "@/components/ListChildrenView.vue";
import ListTasksView from "@/components/ListTasksView.vue";
import { ListInfo, NavbarItem } from "@/models";
import { api } from "@/backend";

@Component({ components: { BreadcrumbNavbarComponent, ListChildrenView, ListTasksView } })
export default class extends Vue {
  private loaded: boolean = false;

  @Prop()
  private readonly id!: string;

  private navbarItems: NavbarItem[] = [];
  private list: ListInfo | null = null;

  async created() {
    this.list = await api.getListInfo(this.id);
    if (this.list == null) {
      await this.$router.push({ name: "index" });
      return;
    }

    this.navbarItems.push({ id: this.list.group.id, name: this.list.group.name, type: "GROUP" });
    if (this.list.parent != null) {
      this.navbarItems.push({ id: this.list.parent.id, name: this.list.parent.name, type: "LIST" });
    }

    this.loaded = true;
  }
}
</script>
