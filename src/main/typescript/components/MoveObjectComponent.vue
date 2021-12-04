<template>
  <b-alert show>
    <b-button v-b-modal:pick-destination block variant="info">Переместить в...</b-button>
    <b-modal
      id="pick-destination"
      :title="`Перемещение ${object.type === 'TASK' ? 'задачи' : 'списка'}`"
      footer-class="border-0"
      modal-class="modal-fullscreen"
    >
      <set-list-view
        v-model="moveTempModel"
        :frozen-elem="frozenElem"
        :group="group"
        :items="list_tree"
        :object="object"
      />
      <template #modal-footer>
        <b-button
          :disabled="moveModel != null && moveModel.object.id === moveTempModel.object.id"
          variant="info"
          @click="submit"
        >
          Применить
        </b-button>
        <b-button @click="$bvModal.hide('pick-destination')">Отмена</b-button>
      </template>
    </b-modal>
    <b-table-simple v-if="submitted" borderless class="m-0">
      <col width="100%" />
      <col width="0%" />
      <b-tr class="h5">
        <b-td style="white-space: nowrap; text-overflow: ellipsis; overflow: hidden; max-width: 1px">
          <font-awesome-icon
            :icon="
              moveTempModel.type === 'TASK'
                ? ['fas', 'circle']
                : moveTempModel.type === 'LIST'
                ? ['fas', 'list-ul']
                : ['fas', 'users']
            "
          />
          {{ moveTempModel.object.name }}
        </b-td>
        <b-td class="text-right" style="white-space: nowrap">
          <font-awesome-icon
            :icon="['fas', 'times']"
            class="kika-icon text-danger ml-1 fa-lg"
            style="cursor: pointer"
            @click="removeSelectedModel"
          />
        </b-td>
      </b-tr>
    </b-table-simple>
  </b-alert>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop, VModel } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import SetListView from "@/components/SetListView.vue";
import {ElemInfo, FrozenElem, Group, GroupTree, List, MoveElemInfo, SubTaskListWithChildren, Task} from "@/models";

@Component({
  components: { SetListView, BreadcrumbNavbarComponent }
})
export default class extends Vue {
  @Prop()
  private readonly object!: ElemInfo;

  @Prop()
  private readonly list_tree!: SubTaskListWithChildren[];

  @Prop()
  private readonly group!: GroupTree | null;

  @Prop()
  private readonly frozenElem!: FrozenElem;

  @VModel()
  private moveModel!: MoveElemInfo;
  private moveTempModel: MoveElemInfo = {
    object: { id: -1, name: "", children: [], tasks: [], groupId: 0, parentId: 0 },
    type: "LIST"
  };
  private submitted: boolean = false;

  mounted() {
    this.resetTempModel();
  }

  private resetTempModel() {
    this.moveTempModel.object = this.moveModel.object;
    this.moveTempModel.type = this.moveModel.type;
    this.submitted = false;
  }

  private removeSelectedModel() {
    this.resetTempModel();
    this.submitted = false;
    this.$emit("clear-model");
  }

  // private get frozenElem(): ElemInfo {
  //   if (this.object.type == "LIST") {
  //     if ((this.object.object as List).parentId == null) {
  //       return { id: (this.object.object as List).groupId, object: this.object.object, type: "GROUP" };
  //     }
  //     return { id: (this.object.object as List).parentId, object: this.object.object, type: "LIST" };
  //   } else if (this.object.type == "TASK") {
  //     if ((this.object.object as Task).parentId == null) {
  //       return { id: (this.object.object as Task).listId, object: this.object.object, type: "LIST" };
  //     }
  //     return { id: (this.object.object as Task).parentId, object: this.object.object, type: "TASK" };
  //   }
  //   console.log("MOVING INVALID ELEMENT");
  //   return { id: null, object: null, type: null };
  // }

  private submit(): void {
    this.moveModel = { type: this.moveTempModel.type, object: this.moveTempModel.object };
    this.submitted = true;
    this.$bvModal.hide("pick-destination");
  }
}
</script>
