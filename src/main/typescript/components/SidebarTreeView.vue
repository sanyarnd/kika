<template>
  <span>
    <b-list-group v-for="(item, key) in items" :id="item.id" :key="key" flush>
      <b-list-group-item
        v-b-toggle="getChildrenCollapseId(item.id)"
        class="bg-transparent"
        @click="toggleRotation(item.id)"
      >
        <b-row class="bg-transparent">
          <b-col class="text-truncate text-dark bg-transparent">
            <font-awesome-icon :icon="item.icon" class="kika-icon" />
            {{ item.name }}
          </b-col>
          <b-col class="text-right" cols="2">
            <font-awesome-icon
              v-if="item.children.length > 0"
              :id="'menu-chevron-' + item.id"
              class="ml-1 rotate"
              icon="chevron-right"
            />
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="getChildrenCollapseId(item.id)">
        <b-list-group v-for="(subitem, kkey) in item.children" :id="subitem.id" :key="kkey" flush>
          <b-list-group-item :to="subitem.link" class="border-bottom text-secondary">
            {{ subitem.name }}
          </b-list-group-item>
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
    const chevron = document.getElementById("menu-chevron-" + id);
    if (chevron != null) {
      chevron.classList.toggle("down");
    }
  }
}

export interface SidebarTreeItem {
  name: string;
  id: number;
  icon: string;
  link: SidebarTreeItemLink;
  children: SidebarTreeItem[];
}

export interface SidebarTreeItemLink {
  name: string;
}
</script>
