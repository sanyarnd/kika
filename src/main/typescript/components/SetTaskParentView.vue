<template>
  <span>
    <b-list-group v-for="(item, key) in items" :id="taskElementId(item.id)" :key="key" flush @click.stop>
      <b-list-group-item
        v-if="task.id !== item.id"
        :active="highlight(item.id)"
        :disabled="frozenElem.type === 'GROUP' && frozenElem.id === item.id"
        :style="{
          'text-decoration': frozenElem.type === 'GROUP' && frozenElem.id === item.id ? 'line-through' : 'none'
        }"
      >
        <b-row v-b-toggle="getTaskChildrenCollapseId(item.id)" @click="toggleSelection(item.id)">
          <b-col class="text-truncate">
            <font-awesome-icon icon="circle" class="kika-icon"/>
            {{ item.name }}
          </b-col>
          <b-col v-if="item.children.length > 0" class="text-right" cols="2">
            <font-awesome-icon icon="chevron-right" :id="taskChevronId(item.id)" class="ml-1 rotate"/>
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="getTaskChildrenCollapseId(item.id)" class="ml-3">
        <set-task-parent-view :task="task" :items="item.children" :move-to="moveTo" :frozen-elem="frozenElem" />
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import SetTaskParentView from "@/components/SetTaskParentView.vue";
import { Status } from "@/pages/app/views/Task.vue";
import { ElemInfo, MoveInfoType } from "@/components/MoveObjectComponent.vue";

@Component({ name: "SetTaskParentView", components: { SetTaskParentView } })
export default class extends Vue {
  @Prop()
  private readonly task!: Task;

  @Prop({ default: [] })
  private readonly items!: Task[];

  @Prop()
  private moveTo!: ElemInfo;

  @Prop()
  private readonly frozenElem!: ElemInfo;

  private toggleRotation(id: string): void {
    const chevron = document.getElementById(this.taskChevronId(id));
    if (chevron != null) {
      chevron.classList.toggle("down");
    }
  }

  private taskChevronId(id: string): string {
    return `task-chevron-${id}`;
  }

  private toggleSelection(id: string): void {
    this.toggleRotation(id);
    this.moveTo.id = id;
    this.moveTo.type = "TASK";
  }

  private getTaskChildrenCollapseId(id: string): string {
    return "task-" + id + "-children";
  }

  private taskElementId(id: string): string {
    return `task-${id}`;
  }

  private highlight(id: string): boolean {
    return this.moveTo.type == "TASK" && this.moveTo.id == id;
  }
}

export interface Task {
  id: string;
  name: string;
  description: string;
  status: Status;
  children: Task[];
  listId: string;
  parentId: string | null;
}

export interface CurrentSelection {
  id: string | null;
}
</script>
<style scoped lang="scss">
.rotate {
  transition: all 0.2s linear;
}

.rotate.down {
  transform: rotate(90deg);
}

.kika-icon {
  width: 24px;
}
</style>
