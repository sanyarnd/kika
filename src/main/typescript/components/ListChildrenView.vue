<template>
  <b-list-group flush>
    <b-list-group-item
      v-for="(child, key) in lists"
      :key="key"
      :class="{ 'border-top': key === 0 }"
      :to="{ name: 'list', params: { id: child.id } }"
      class="border-bottom border-right border-left text-truncate"
    >
      <font-awesome-icon :icon="['fas', 'list-ul']" class="kika-icon mr-2" />
      {{ child.name }}
    </b-list-group-item>
  </b-list-group>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import { List } from "@/models";

@Component({ components: {} })
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  @Prop()
  private lists!: List[];
}
</script>
