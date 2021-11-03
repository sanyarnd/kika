<template>
  <span>
    <b-list-group flush v-for="(item, key) in items" :id="item.id" :key="key">
      <b-list-group-item
        v-b-toggle="getChildrenCollapseId(item.id)"
        class="bg-transparent"
        @click="toggleRotation(item.id)"
      >
        <b-row class="bg-transparent">
          <b-col class="text-truncate text-dark bg-transparent">
            {{ item.name }}
          </b-col>
          <b-col class="text-right" cols="2">
              <b-icon-chevron-right
                v-if="item.children.length > 0"
                :id="'chevron-' + item.id"
                class="ml-1 rotate"
              ></b-icon-chevron-right
            >
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="getChildrenCollapseId(item.id)">
        <b-list-group v-for="(subitem, key) in item.children" :id="subitem.id" :key="key" flush>
          <b-list-group-item class="border-bottom text-secondary" button :href="subitem.link">{{ subitem.name }}</b-list-group-item>
        </b-list-group>
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";

@Component({ components: {} })
export default class extends Vue {
  @Prop({ default: [] })
  private readonly items!: SidebarTreeItem[];

  private getChildrenCollapseId(id: number): string {
    return "node-" + id + "-children-collapse";
  }

  private toggleRotation(id: number): void {
    document.getElementById("chevron-" + id)!.classList.toggle("down");
  }
}

export interface SidebarTreeItem {
  name: string;
  id: number;
  link: string;
  children: SidebarTreeItem[];
}
</script>
<style scoped lang="scss">
.rotate {
  transition: all 0.2s linear;
}

.rotate.down {
  transform: rotate(90deg);
}
</style>
