<template>
  <div>ID: {{ id }}</div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";

@Component({})
export default class extends Vue {
  @Prop()
  private readonly id!: string;
}
</script>
