<template>
  <span>
    <b-list-group-item
        class="border-left-0 border-right-0 border-top-0"
      v-if="object.type === 'LIST'"
      :active="highlight('GROUP', group.id)"
      :variant="freeze('GROUP', group.id) ? 'dark' : 'outline-primary'"
      href="#"
      @click="toggleSelection('GROUP', group)"
    >
      <b-row>
        <b-col
          :style="{ 'text-decoration': freeze('GROUP', group.id) ? 'line-through' : 'none' }"
          class="text-truncate"
        >
          <font-awesome-icon class="kika-icon" icon="users" />
          {{ group.name }}
        </b-col>
      </b-row>
    </b-list-group-item>
    <b-list-group v-for="(item, key) in items" :id="listElementId(item.id)" :key="key" flush @click.stop>
      <b-list-group-item
        v-if="object.type !== 'LIST' || object.object.id !== item.id"
        :active="highlight('LIST', item.id)"
        href="#"
        :style="{ 'text-decoration': freeze('LIST', item.id) ? 'line-through' : 'none' }"
        :variant="freeze('LIST', item.id) ? 'dark' : 'outline-primary'"
      >
        <b-row v-b-toggle="listChildrenId(item.id)" @click="toggleSelection('LIST', item)">
          <b-col class="text-truncate">
            <font-awesome-icon class="kika-icon" icon="list-ul" />
            {{ item.name }}
          </b-col>
          <b-col v-if="displayChevron(item)" class="text-right" cols="2">
            <font-awesome-icon :id="listChevronId(item.id)" class="ml-1 rotate" icon="chevron-right" />
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="listChildrenId(item.id)" visible class="ml-3">
        <set-list-view
          v-if="object.type !== 'LIST' || object.object.id !== item.id"
          v-model="moveTo"
          :frozen-elem="frozenElem"
          :group="null"
          :items="item.children"
          :object="object"
        />
        <set-task-parent-view
          v-if="object.type === 'TASK' && (object.type !== 'LIST' || object.object.id !== item.id)"
          v-model="moveTo"
          :frozen-elem="frozenElem"
          :group="null"
          :items="item.tasks"
          :task="object"
        />
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop, VModel } from "vue-property-decorator";
import SetListView from "@/components/SetListView.vue";
import SetTaskParentView from "@/components/SetTaskParentView.vue";
import {ElemInfo, FrozenElem, GroupTree, MoveElemInfo, ParentInfoType, SubTaskListWithChildren, Task} from "@/models";

@Component({ name: "SetListView", components: { SetListView, SetTaskParentView } })
export default class extends Vue {
  @Prop()
  private readonly object!: ElemInfo;

  @Prop()
  private readonly group!: GroupTree | null;

  @Prop({ default: [] })
  private readonly items!: SubTaskListWithChildren[];

  @VModel()
  private moveTo!: MoveElemInfo;

  @Prop()
  private readonly frozenElem!: FrozenElem;

  private displayChevron(item: SubTaskListWithChildren): boolean {
    if (this.object.type === "LIST") {
      return item.tasks.length > 0 || item.children.map(list => list.id !== this.object.object?.id).length > 0;
    } else {
      return item.children.length > 0 || item.tasks.map(task => task.id !== this.object.object?.id).length > 0;
    }
  }

  private toggleRotation(id: number): void {
    const chevron = document.getElementById(this.listChevronId(id));
    if (chevron != null) {
      chevron.classList.toggle("down");
    }
  }

  private listChevronId(id: number): string {
    return `list-chevron-${id}`;
  }

  private listChildrenId(id: number): string {
    return `list-${id}-children`;
  }

  private toggleSelection(type: ParentInfoType, object: SubTaskListWithChildren | GroupTree): void {
    this.toggleRotation(object.id);
    if (object.id == this.frozenElem.id && type == this.frozenElem.type) {
      return;
    } else if (type == this.frozenElem.type && object.id == this.frozenElem.id) {
      return;
    }
    this.moveTo = { object: object, type: type };
  }

  private listElementId(id: number): string {
    return `list-${id}`;
  }

  private highlight(type: ParentInfoType, id: number): boolean {
    return this.moveTo.type == type && this.moveTo.object?.id == id;
  }

  private freeze(type: ParentInfoType, id: number): boolean {
    return this.frozenElem.type === type && this.frozenElem.id === id;
  }
}

export interface ObjectToMove {
  object: Task | SubTaskListWithChildren;
  type: "TASK" | "LIST";
}
</script>
