<template>
  <span>
    <ul v-for="(item, key) in items" :id="item.id" :key="key" class="list-group list-group-flush">
      <li class="list-group-item">
        <b-row>
          <b-col class="text-truncate">
            {{ item.name }}
            <span v-b-toggle="getChildrenCollapseId(item.id)">
              <font-awesome-icon
                v-if="item.children.length > 0"
                :id="'chevron-' + item.id"
                class="ml-1 rotate"
                icon="chevron-right"
                @click="toggleRotation(item.id)"
              ></font-awesome-icon
            ></span>
          </b-col>
        </b-row>
      </li>
      <b-collapse :id="getChildrenCollapseId(item.id)" class="ml-3">
        <task-tree-view :items="item.children"></task-tree-view>
      </b-collapse>
    </ul>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import TaskTreeView from "@/components/TaskTreeView.vue";

@Component({ name: "TaskTreeView", components: { TaskTreeView } })
export default class extends Vue {
  @Prop({ default: [] })
  private readonly items!: TreeItem[];

  private getChildrenCollapseId(id: number): string {
    return "node-" + id + "-children-collapse";
  }

  private getIcon(item: TreeItem): string {
    if (item.children.length > 0) {
      if (item.children.every(child => child.ticked)) {
        return "check-all";
      }
      return "";
    } else {
      if (item.ticked) {
        return "check";
      }
      return "";
    }
  }

  private toggleRotation(id: number): void {
    const chevron = document.getElementById("chevron-" + id);
    if (chevron != null) {
      chevron.classList.toggle("down");
    }
  }
}

export interface TreeItem {
  name: string;
  id: number;
  ticked: boolean;
  children: TreeItem[];
}
</script>
