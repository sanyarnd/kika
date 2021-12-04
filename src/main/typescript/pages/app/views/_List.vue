<template>
  <div v-if="loaded">
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col class="h4 text-truncate">{{ list.name }}</b-col>
            <b-col v-if="group.role !== 'RESTRICTED'" cols="2" class="text-right mr-3 mt-1">
              <b-link :to="{ name: 'list-edit', params: { id: id } }" class="text-dark">
                <font-awesome-icon :icon="['fas', 'pencil-alt']" size="2x" class="mt-n1 mr-n2" />
              </b-link>
            </b-col>
          </b-row>
          <hr />
          <list-children-view :lists="list.children" />
          <b-button
            :to="{
              name: 'list-new',
              params: {
                groupId: list.groupId,
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

          <list-tasks-view :tasks="list.tasks" class="mt-2" />
          <b-button
            :to="{
              name: 'task-new',
              params: {
                groupId: list.groupId,
                parentListId: list.id
              }
            }"
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
import { Group, List, NavbarItem } from "@/models";
import { appModule } from "@/store/app-module";

@Component({ components: { BreadcrumbNavbarComponent, ListChildrenView, ListTasksView } })
export default class extends Vue {
  private loaded: boolean = false;

  @Prop()
  private readonly id!: string;

  private navbarItems: NavbarItem[] = [];

  private readonly appStore = appModule.context(this.$store);

  private list: List | undefined;
  private group: Group | undefined;

  async created() {
    this.list = this.appStore.getters.list(this.id);
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
    if (this.list.parentId != null) {
      const parentList = this.appStore.getters.list(this.list.parentId);
      if (parentList == undefined) {
        await this.$router.push({ name: "index" });
        return;
      }
      this.navbarItems.push({ id: +parentList.id, name: parentList.name, type: "LIST" });
    }

    this.loaded = true;
  }
}
</script>
