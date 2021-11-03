<template>
  <b-navbar variant="faded">
    <b-navbar-brand v-b-toggle:sidebar-1><i class="fas fa-bars"></i></b-navbar-brand>
    <b-link
        class="text-truncate breadcrumb-item text-dark"
        v-for="(item, key) in items"
        :key="key"
        :to="{ name: item.type, params: { id: item.id } }"
    >
      {{ item.name }}
    </b-link>
  </b-navbar>
</template>

<script lang="ts">
import Vue from "vue";
import {Component, Prop} from "vue-property-decorator";

@Component({components: {}})
export default class extends Vue {
  @Prop()
  private readonly items!: NavigationItem[];
}

export interface NavigationItem {
  id: string;
  name: string;
  type: string;
}
</script>
