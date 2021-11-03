<template>
  <span>
    <b-list-group flush v-for="(item, key) in items" :id="taskElementId(item.id)" @click.stop :key="key">
      <b-list-group-item v-if="task.id === item.id" disabled>
        <b-col class="text-truncate">
          <i class="fas fa-circle fa-xs"></i>
          {{ item.name }}
        </b-col>
      </b-list-group-item>
      <b-list-group-item
        v-else
        :active="newParent.id === item.id"
      >
        <b-row @click="toggleSelection(item.id)" v-b-toggle="getTaskChildrenCollapseId(item.id)">
          <b-col class="text-truncate">
            <i class="fas fa-circle fa-xs"></i>
            {{ item.name }}
            </b-col>
          <b-col v-if="item.children.length > 0" class="text-right" cols="2">
            <b-icon-chevron-right
              :id="taskChevronId(item.id)"
              class="ml-1 rotate"
            />
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="getTaskChildrenCollapseId(item.id)" class="ml-3">
        <set-task-parent-view :task="task" :items="item.children" :new-list="newList" :new-parent="newParent"/>
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import SetTaskParentView from "@/components/SetTaskParentView.vue";
import { Status } from "@/pages/app/views/Task.vue";

@Component({ name: "SetTaskParentView", components: { SetTaskParentView } })
export default class extends Vue {
  @Prop()
  private readonly task!: Task;

  @Prop({ default: [] })
  private readonly items!: Task[];

  @Prop()
  public newList!: CurrentSelection;

  @Prop()
  public newParent!: CurrentSelection;

  private toggleRotation(id: string): void {
    const chevron = document.getElementById(this.taskChevronId(id));
    if(chevron != null) {
      chevron.classList.toggle("down");
    }
  }

  private taskChevronId(id: string): string {
    return `task-chevron-${id}`;
  }

  private toggleSelection(id: string): void {
    // if (this.newParent.id == id) {
    //   this.newParent.id = null;
    // } else {
    this.toggleRotation(id)
      this.newParent.id = id;
      this.newList.id = null;
    // }
    console.log(this.newList.id);
    console.log(this.newParent.id);
  }

  private getTaskChildrenCollapseId(id: string): string {
    return "task-" + id + "-children";
  }

  private taskElementId(id: string): string {
    return `task-${id}`;
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
</style>
