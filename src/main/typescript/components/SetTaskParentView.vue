<template>
  <span>
    <b-list-group v-for="(item, key) in items" :id="taskElementId(item.id)" :key="key" flush @click.stop>
      <b-list-group-item
        v-if="task.id !== item.id"
        href="#"
        :active="highlight(item.id)"
        :disabled="frozenElem.type === 'GROUP' && frozenElem.id === item.id"
        :style="{
          'text-decoration': frozenElem.type === 'TASK' && frozenElem.id === item.id ? 'line-through' : 'none'
        }"
      >
        <b-row v-b-toggle="getTaskChildrenCollapseId(item.id)" @click="toggleSelection(item)">
          <b-col class="text-truncate">
            <font-awesome-icon class="kika-icon" icon="circle" />
            {{ item.name }}
          </b-col>
          <b-col v-if="item.children.length > 0" class="text-right" cols="2">
            <font-awesome-icon :id="taskChevronId(item.id)" class="ml-1 rotate" icon="chevron-right" />
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="getTaskChildrenCollapseId(item.id)" class="ml-3">
        <set-task-parent-view v-model="moveTo" :frozen-elem="frozenElem" :items="item.children" :task="task" />
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop, VModel } from "vue-property-decorator";
import SetTaskParentView from "@/components/SetTaskParentView.vue";
import { ElemInfo, FrozenElem, MoveElemInfo, SubTaskWithChildren, Task } from "@/models";

@Component({ name: "SetTaskParentView", components: { SetTaskParentView } })
export default class extends Vue {
  @Prop()
  private readonly task!: ElemInfo;

  @Prop({ default: [] })
  private readonly items!: SubTaskWithChildren[];

  @VModel()
  private moveTo!: MoveElemInfo;

  @Prop()
  private readonly frozenElem!: FrozenElem;

  private toggleRotation(id: number): void {
    const chevron = document.getElementById(this.taskChevronId(id));
    if (chevron != null) {
      chevron.classList.toggle("down");
    }
  }

  private taskChevronId(id: number): string {
    return `task-chevron-${id}`;
  }

  private toggleSelection(object: Task): void {
    if (object.id == this.frozenElem.id) {
      return;
    }
    this.toggleRotation(object.id);
    this.moveTo = { object: object, type: "TASK" };
  }

  private getTaskChildrenCollapseId(id: number): string {
    return "task-" + id + "-children";
  }

  private taskElementId(id: number): string {
    return `task-${id}`;
  }

  private highlight(id: number): boolean {
    return this.moveTo.type == "TASK" && this.moveTo.object?.id == id;
  }
}
</script>
