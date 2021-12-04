<template>
  <b-list-group flush>
    <b-list-group-item
      v-for="(task, key) in tasks"
      :key="key"
      :style="{ 'text-decoration': task.status === 'COMPLETED' ? 'line-through' : 'none' }"
      :to="{ name: 'task', params: { id: task.id } }"
      class="text-truncate border-0"
    >
      <font-awesome-icon :icon="['fas', 'circle']" class="kika-icon mr-1" />
      {{ task.name }}
    </b-list-group-item>
  </b-list-group>
</template>

<script lang="ts">
import { Task } from "@/models";
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";

@Component({ components: {} })
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  @Prop()
  private tasks!: Task[];
}
</script>
