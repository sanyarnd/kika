<template>
  <span>
    <b-list-group v-for="(item, key) in items" :id="taskElementId(item.id)" :key="key" flush @click.stop>
      <b-list-group-item :active="highlight('TASK', item.id)" variant="outline-primary">
        <b-row v-b-toggle="taskChildrenId(item.id)" @click="toggleSelection('TASK', item)">
          <b-col class="text-truncate">
            <font-awesome-icon :icon="['fas', 'circle']" class="kika-icon" />
            {{ item.name }}
          </b-col>
          <b-col v-if="item.children.length > 0" class="text-right" cols="2">
            <font-awesome-icon :id="taskChevronId(item.id)" class="ml-1 rotate" icon="chevron-right" />
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="taskChildrenId(item.id)" class="ml-3" visible>
        <pick-task-parent-view v-model="model" :items="item.children" />
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop, VModel } from "vue-property-decorator";
import PickTaskParentView from "@/components/PickTaskParentView.vue";
import { ElemInfo, List, ParentInfoType, Task } from "@/models";

@Component({ name: "PickTaskParentView", components: { PickTaskParentView } })
export default class extends Vue {
  @Prop({ default: [] })
  private readonly items!: Task[];

  @VModel()
  private model!: ElemInfo;

  private toggleRotation(id: number): void {
    const chevron = document.getElementById(this.taskChevronId(id));
    if (chevron != null) {
      chevron.classList.toggle("down");
    }
  }

  private taskChevronId(id: number): string {
    return `task-chevron-${id}`;
  }

  private taskChildrenId(id: number): string {
    return `task-${id}-children`;
  }

  private toggleSelection(type: ParentInfoType, object: Task): void {
    this.toggleRotation(object.id);
    this.model = { id: object.id, object, type: type };
  }

  private taskElementId(id: number): string {
    return `task-${id}`;
  }

  private highlight(type: ParentInfoType, id: number): boolean {
    return this.model.type == type && this.model.id == id;
  }
}

export interface ObjectToMove {
  object: Task | List;
  type: "TASK" | "LIST";
}
</script>
