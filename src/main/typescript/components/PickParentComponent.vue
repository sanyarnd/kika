<template>
  <b-form-group>
    <b-alert class="mb-n1" show>
      <b-button v-b-modal:pick-parent block data-vv-name="pick-parent" variant="info">Добавить в...</b-button>
      <b-modal
        id="pick-parent"
        :title="'Выбор родительского компонента'"
        footer-class="border-0"
        modal-class="modal-fullscreen"
      >
        <pick-list-parent-view v-model="pickModel" :group="group" :items="lists" :object-type="objectType" />
        <template #modal-footer>
          <b-button :disabled="pickModel.id == null" variant="info" @click="submit">Применить</b-button>
          <b-button @click="$bvModal.hide('pick-parent')">Отмена</b-button>
        </template>
      </b-modal>
      <b-table-simple v-if="finalModel != null" borderless class="m-0">
        <col width="100%" />
        <col width="0%" />
        <b-tr class="h5">
          <b-td style="white-space: nowrap; text-overflow: ellipsis; overflow: hidden; max-width: 1px">
            <font-awesome-icon
              :icon="
                finalModel.type === 'TASK'
                  ? ['fas', 'circle']
                  : finalModel.type === 'LIST'
                  ? ['fas', 'list-ul']
                  : ['fas', 'users']
              "
            />
            <span v-if="finalModel.object != null"> {{ finalModel.object.name }}</span>
          </b-td>
          <b-td class="text-right" style="white-space: nowrap">
            <font-awesome-icon
              :icon="['fas', 'times']"
              class="kika-icon text-danger ml-1 fa-lg"
              style="cursor: pointer"
              @click="clearParentData"
            />
          </b-td>
        </b-tr>
      </b-table-simple>
    </b-alert>
  </b-form-group>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop, VModel } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import PickListParentView from "@/components/PickListParentView.vue";
import SetListView from "@/components/SetListView.vue";
import { ElemInfo, Group, List } from "@/models";

@Component({
  components: { PickListParentView, SetListView, BreadcrumbNavbarComponent }
})
export default class extends Vue {
  @Prop()
  private readonly objectType!: "LIST" | "TASK";

  @Prop()
  private readonly lists!: List[];

  @Prop()
  private readonly group!: Group;

  @VModel()
  private finalModel!: ElemInfo | null;
  private pickModel: ElemInfo = {
    id: 0,
    object: { id: -1, name: "", children: [], tasks: [], groupId: 0, parentId: 0 },
    type: "LIST"
  };

  mounted() {
    if (this.finalModel != null) {
      this.pickModel.id = this.finalModel.id;
      this.pickModel.object = this.finalModel.object;
      this.pickModel.type = this.finalModel.type;
    }
  }

  private clearParentData(): void {
    this.finalModel = null;
    this.$emit("validation:warning", true);
  }

  private submit(): void {
    this.finalModel = { id: this.pickModel.id, object: this.pickModel.object, type: this.pickModel.type };
    this.$bvModal.hide("pick-parent");
  }
}
</script>
