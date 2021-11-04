<template>
  <span>
    <b-list-group v-for="(item, key) in items" :id="listElementId(item.id)" :key="key" flush @click.stop>
      <b-list-group-item
        v-if="group !== null && object.type === 'LIST' && key === 0"
        :active="highlight('GROUP', group.id)"
        :disabled="freeze('GROUP', group.id)"
        href="#"
        @click="toggleSelection('GROUP', group.id)"
      >
        <b-row>
          <b-col
            class="text-truncate"
            :style="{ 'text-decoration': freeze('GROUP', group.id) ? 'line-through' : 'none' }"
          >
            <font-awesome-icon icon="users" class="kika-icon"/>
            {{ group.name }}
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-list-group-item
        v-if="object.type !== 'LIST' || object.object.id !== item.id"
        :disabled="freeze('LIST', item.id)"
        :active="highlight('LIST', item.id)"
        variant="outline-primary"
        :style="{ 'text-decoration': freeze('LIST', item.id) ? 'line-through' : 'none' }"
      >
        <b-row v-b-toggle="listChildrenId(item.id)" @click="toggleSelection('LIST', item.id)">
          <b-col class="text-truncate">
            <font-awesome-icon icon="list-ul" class="kika-icon"/>
            {{ item.name }}
          </b-col>
          <b-col v-if="item.children.length > 0 || item.tasks.length > 0" class="text-right" cols="2">
            <font-awesome-icon icon="chevron-right" :id="listChevronId(item.id)" class="ml-1 rotate"/>
          </b-col>
        </b-row>
      </b-list-group-item>
      <b-collapse :id="listChildrenId(item.id)" class="ml-3">
        <set-list-view
          :items="item.children"
          :object="object"
          :group="null"
          :move-to="moveTo"
          :frozen-elem="frozenElem"
        />
        <set-task-parent-view
          v-if="object.type === 'TASK'"
          :task="object.object"
          :items="item.tasks"
          :move-to="moveTo"
          :group="null"
          :frozen-elem="frozenElem"
        />
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import SetListView from "@/components/SetListView.vue";
import SetTaskParentView from "@/components/SetTaskParentView.vue";
import { Status } from "@/pages/app/views/Task.vue";
import { ElemInfo, MoveInfoType } from "@/components/MoveObjectComponent.vue";

@Component({ name: "SetListView", components: { SetListView, SetTaskParentView } })
export default class extends Vue {
  @Prop()
  private readonly object!: ObjectToMove;

  @Prop()
  private readonly group!: Group | null;

  @Prop({ default: [] })
  private readonly items!: List[];

  @Prop()
  private readonly moveTo!: ElemInfo;

  @Prop()
  private readonly frozenElem!: ElemInfo;

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

  private toggleSelection(type: MoveInfoType, id: string): void {
    this.toggleRotation(id);
    console.log(this.moveTo);
    this.moveTo.id = id;
    this.moveTo.type = type;
  }

  private listElementId(id: string): string {
    return `list-${id}`;
  }

  private highlight(type: MoveInfoType, id: string): boolean {
    return this.moveTo.type == type && this.moveTo.id == id;
  }

  private freeze(type: MoveInfoType, id: string): boolean {
    return this.frozenElem.type === type && this.frozenElem.id === id;
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
  groupId: string;
  parentId: string | null;
}

export interface Group {
  id: string;
  name: string;
  ownerId: string;
  lists: List[];
}

export interface ObjectToMove {
  object: Task | List;
  type: "TASK" | "LIST";
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
