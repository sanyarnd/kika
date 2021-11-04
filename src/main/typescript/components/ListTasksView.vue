<template>
  <b-list-group flush>
    <b-list-group-item
      class="text-truncate border-0"
      v-for="(task, key) in tasks"
      :key="key"
      :to="{ name: 'task', params: { id: task.id } }"
      :style="{ 'text-decoration': task.status === 'COMPLETED' ? 'line-through' : 'none' }"
    >
      <font-awesome-icon class="kika-icon mr-1" :icon="['fas', 'circle']"/>{{ task.name }}
    </b-list-group-item>
  </b-list-group>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import { Task } from "@/components/SetListView.vue";

@Component({ components: {} })
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  @Prop()
  private tasks!: Task[];
}
</script>

<style scoped lang="scss">
.kika-icon {
  width: 24px;
}
</style>
