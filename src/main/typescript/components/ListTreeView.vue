<template>
  <span>
    <b-list-group v-for="(item, key) in items" :id="item.id" :key="key" flush>
      <b-list-group-item
        v-if="list_id !== item.id"
        v-b-toggle="getListChildrenCollapseId(item.id)"
        :active="selected_list.id === item.id"
        @click="toggleSelection(item)"
        @click.stop
      >
        <b-row>
          <b-col class="text-truncate">{{ item.name }}</b-col>
          <b-col class="text-right" cols="2">
            <span>
              <font-awesome-icon
                v-if="item.children.length > 0"
                :id="'list-chevron-' + item.id"
                :class="item.expanded ? 'down' : ''"
                class="ml-1 rotate"
                icon="chevron-right"
              />
            </span>
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="getListChildrenCollapseId(item.id)" class="ml-3">
        <list-tree-view :items="item.children" :list_id="list_id" :selected_list="selected_list" />
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import ListTreeView from "@/components/ListTreeView.vue";

@Component({ name: "ListTreeView", components: { ListTreeView } })
export default class extends Vue {
  @Prop()
  public selected_list!: CurrentSelection;
  @Prop({ default: [] })
  private readonly items!: ListTreeItem[];
  @Prop()
  private readonly list_id!: number;

  private getListChildrenCollapseId(id: number): string {
    return "list-node-" + id + "-children-collapse";
  }

  private toggleSelection(item: ListTreeItem): void {
    item.expanded = !item.expanded;
    if (this.selected_list.id == item.id) {
      this.selected_list.id = null;
    } else {
      this.selected_list.id = item.id;
    }
  }
}

export interface ListTreeItem {
  name: string;
  id: number;
  children: ListTreeItem[];
  expanded: boolean;
}

export interface CurrentSelection {
  id: number | null;
}
</script>
