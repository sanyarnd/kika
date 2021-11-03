<template>
  <span>
    <b-list-group v-for="(item, key) in items" :id="listElementId(item.id)" :key="key" flush @click.stop>
      <b-list-group-item
          :active="newList.id === item.id"
          variant="outline-primary"
      >
        <b-row @click="toggleSelection('list', item.id)" v-b-toggle="listChildrenId(item.id)">
            <b-col class="text-truncate">
              <i class="fas fa-list-ul fa-xs"></i>
              {{ item.name }}
            </b-col>
            <b-col v-if="item.children.length > 0 || item.tasks.length > 0" class="text-right" cols="2">
              <b-icon-chevron-right
                  :id="listChevronId(item.id)"
                  class="ml-1 rotate"
              />
            </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="listChildrenId(item.id)" class="ml-3">
        <set-task-list-view :items="item.children" :task="task" :new-list="newList" :new-parent="newParent"/>
        <set-task-parent-view :task="task" :items="item.tasks" :new-list="newList" :new-parent="newParent"/>
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import {Component, Prop} from "vue-property-decorator";
import SetTaskListView from "@/components/SetTaskListView.vue";
import SetTaskParentView from "@/components/SetTaskParentView.vue";
import {CurrentSelection} from "@/components/SetTaskParentView.vue";
import {Status} from "@/pages/app/views/Task.vue";

@Component({name: "SetTaskListView", components: {SetTaskListView, SetTaskParentView}})
export default class extends Vue {
  @Prop()
  private readonly task!: Task;

  @Prop({default: []})
  private readonly items!: List[];

  @Prop()
  public newList!: CurrentSelection;
  @Prop()
  public newParent!: CurrentSelection;

  private toggleRotation(id: string): void {
    const chevron = document.getElementById(this.listChevronId(id));
    if (chevron != null) {
      chevron.classList.toggle("down");
    }
  }

  private listChevronId(id: string): string {
    return `list-chevron-${id}`;
  }

  private listChildrenId(id: string): string {
    return `list-${id}-children`;
  }

  private toggleSelection(property: string, id: string): void {
    // if (property == "list") {
    //   if (this.newList.id == id) {
    //     this.newList.id = null;
    //   } else {
    //     this.newList.id = id;
    //     this.newParent.id = null;
    //   }
    // } else if (property == "task") {
    //   if (this.newParent.id == id) {
    //     this.newParent.id = null;
    //   } else {
    //     this.newParent.id = id;
    //     this.newList.id = null;
    //   }
    // }
    this.toggleRotation(id)
    if(property == 'list') {
      this.newList.id = id;
      this.newParent.id = null;
    } else if (property == 'task') {
      this.newParent.id = id;
      this.newList.id = null;
    }

    console.log(this.newList.id);
    console.log(this.newParent.id);
  }

  private listElementId(id: string): string {
    return `list-${id}`;
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

export interface List {
  name: string;
  id: string;
  children: List[];
  tasks: Task[];
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
