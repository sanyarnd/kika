<template>
  <div>HOME {{ appStore.getters.version }}!</div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import { appModule } from "@/store/app-module";

@Component({})
export default class extends Vue {
  private readonly appStore = appModule.context(this.$store);
}
</script>
