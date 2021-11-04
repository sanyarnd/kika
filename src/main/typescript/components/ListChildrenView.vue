<template>
  <b-list-group flush>
    <b-list-group-item
      v-for="(child, key) in lists"
      :key="key"
      :class="{ 'border-top': key === 0 }"
      class="border-bottom border-right border-left text-truncate"
      :to="{ name: 'list', params: { id: child.id } }"
    >
      <font-awesome-icon class="kika-icon mr-1" icon="list-ul"/>{{ child.name }}
    </b-list-group-item>
  </b-list-group>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import { List } from "@/components/SetListView.vue";

@Component({ components: {} })
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  @Prop()
  private lists!: List[];
}
</script>

<style scoped lang="scss">
.kika-icon {
  width: 24px;
}
</style>
