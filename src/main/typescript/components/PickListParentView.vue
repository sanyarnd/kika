<template>
  <span>
    <b-list-group-item
      v-if="group !== null && objectType === 'LIST'"
      :active="highlight('GROUP', group.id)"
      class="border-top-0 border-left-0 border-right-0"
      href="#"
      @click="toggleSelection('GROUP', group)"
    >
      <b-row>
        <b-col class="text-truncate">
          <font-awesome-icon class="kika-icon mr-1" icon="users" />
          {{ group.name }}
        </b-col>
      </b-row>
    </b-list-group-item>
    <b-list-group v-for="(item, key) in items" :id="listElementId(item.id)" :key="key" flush @click.stop>
      <b-list-group-item :active="highlight('LIST', item.id)" variant="outline-primary">
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
      <b-collapse :id="listChildrenId(item.id)" class="ml-3" visible>
        <pick-list-parent-view v-model="model" :group="null" :items="item.children" :object-type="objectType" />
        <pick-task-parent-view v-if="objectType === 'TASK'" v-model="model" :items="item.tasks" />
      </b-collapse>
    </b-list-group>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop, VModel } from "vue-property-decorator";
import PickListParentView from "@/components/PickListParentView.vue";
import PickTaskParentView from "@/components/PickTaskParentView.vue";
import { ElemInfo, Group, List, ParentInfoType, SubTaskListWithChildren, Task } from "@/models";

@Component({ name: "PickListParentView", components: { PickListParentView, PickTaskParentView } })
export default class extends Vue {
  @Prop()
  private readonly objectType!: "LIST" | "TASK";

  @Prop({ default: [] })
  private readonly items!: List[];

  @VModel()
  private model!: ElemInfo;

  @Prop()
  private readonly group!: Group | null;

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

  private toggleSelection(type: ParentInfoType, object: List | Group): void {
    this.toggleRotation(object.id);
    this.model = { id: object.id, object: object, type: type };
  }

  private listElementId(id: number): string {
    return `list-${id}`;
  }

  private highlight(type: ParentInfoType, id: number): boolean {
    return this.model.type == type && this.model.id == id;
  }

  private displayChevron(item: SubTaskListWithChildren): boolean {
    if (this.objectType === "LIST") {
      return item.children.length > 0;
    } else {
      return item.children.length > 0 || item.tasks.length > 0;
    }
  }
}

export interface ObjectToMove {
  object: Task | List;
  type: "TASK" | "LIST";
}
</script>
