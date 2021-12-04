<template>
  <b-navbar variant="faded">
    <b-navbar-brand v-b-toggle:sidebar-1>
      <font-awesome-icon icon="bars" />
    </b-navbar-brand>
    <b-link
      v-for="(item, key) in items"
      :key="key"
      :to="{ name: item.type.toLowerCase(), params: { id: `${item.id}` } }"
      class="breadcrumb-item text-dark text-truncate"
    >
      {{ item.name }}
    </b-link>
  </b-navbar>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import { NavbarItem } from "@/models";

@Component({ components: {} })
export default class extends Vue {
  @Prop()
  private readonly items!: NavbarItem[];
}
</script>
