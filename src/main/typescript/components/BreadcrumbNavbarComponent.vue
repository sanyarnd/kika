<template>
  <b-navbar variant="faded">
    <b-navbar-brand v-b-toggle:sidebar-1>
      <font-awesome-icon icon="bars" />
    </b-navbar-brand>
    <b-link
      v-for="(item, key) in items"
      :key="key"
      class="text-truncate breadcrumb-item text-dark"
      :to="{ name: item.type, params: { id: item.id } }"
    >
      {{ item.name }}
    </b-link>
  </b-navbar>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";

@Component({ components: {} })
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
