<template>
  <div>
    <router-view />
    <sidebar-component />
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import SidebarComponent from "@/components/SidebarComponent.vue";

@Component({ components: { SidebarComponent } })
export default class extends Vue {}
</script>
