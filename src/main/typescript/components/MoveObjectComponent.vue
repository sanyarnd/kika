<template>
  <b-alert show>
    <b-button v-b-modal.pick-destination block variant="info">Переместить в...</b-button>
    <b-modal
      id="pick-destination"
      modal-class="modal-fullscreen"
      :title="`Перемещение ${object.type === 'TASK' ? 'задачи' : 'списка'}`"
      ok-title="Применить"
      cancel-title="Отмена"
      footer-class="border-0"
      @ok="copyMoveData()"
    >
      <set-list-view
        :frozen-elem="getFrozenElem()"
        :object="object"
        :items="list_tree"
        :move-to="moveTo"
        :group="group"
      />
    </b-modal>
    <span v-if="moveToFinal.type != null" class="text-center">
      <b-row>
        <b-col class="mt-3 text-truncate">
          <span v-if="moveToFinal.type === 'TASK'" class="h4 mt-2">задачу(id={{ moveToFinal.id }})</span>
          <span v-else-if="moveToFinal.type === 'LIST'" class="h4 mt-2">список(id={{ moveToFinal.id }})</span>
          <span v-else class="h4 mt-2">группу(id={{ moveToFinal.id }})</span>
        </b-col>
      </b-row>
      <div class="text-center flex-content">
        <b-button variant="outline-dark" href="#" class="mt-3" @click="clearMoveData()">
          <font-awesome-icon class="mr-1" icon="times"/>
          Отмена
        </b-button>
      </div>
    </span>
  </b-alert>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import SetListView, { Group, List, ObjectToMove, Task } from "@/components/SetListView.vue";

@Component({
  components: { SetListView, BreadcrumbNavbarComponent }
})
export default class extends Vue {
  @Prop()
  private readonly object!: ObjectToMove;

  @Prop()
  private list_tree!: List[];

  @Prop()
  private group!: Group | null;

  private moveTo: ElemInfo = { type: null, id: null };
  private moveToFinal: ElemInfo = { type: null, id: null };

  private clearMoveData(): void {
    this.moveToFinal.id = null;
    this.moveToFinal.type = null;
  }

  private copyMoveData(): void {
    this.moveToFinal.id = this.moveTo.id;
    this.moveToFinal.type = this.moveTo.type;
  }

  private getFrozenElem(): ElemInfo {
    if (this.object.type == "LIST") {
      if ((this.object.object as List).parentId == null) {
        return { id: (this.object.object as List).groupId, type: "GROUP" };
      }
      return { id: (this.object.object as List).parentId, type: "LIST" };
    } else if (this.object.type == "TASK") {
      if ((this.object.object as Task).parentId == null) {
        return { id: (this.object.object as Task).listId, type: "LIST" };
      }
      return { id: (this.object.object as Task).parentId, type: "TASK" };
    }
    console.log("MOVING INVALID ELEMENT");
    return { id: null, type: null };
  }
}

export interface ElemInfo {
  id: string | null;
  type: MoveInfoType | null;
}

export type MoveInfoType = "TASK" | "LIST" | "GROUP";
</script>

<style lang="scss">
.modal-fullscreen .modal-dialog {
  max-width: 100%;
  margin: 0;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  position: fixed;
  z-index: 100000;
}
</style>
