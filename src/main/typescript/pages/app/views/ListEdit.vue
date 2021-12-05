<template>
  <div v-if="loaded">
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col class="h4">Редактирование</b-col>
          </b-row>
          <hr />
          <validation-observer v-slot="{ invalid }">
            <b-card-text>
              <validation-provider v-slot="{ errors, valid }" name="Название" rules="required|max:128">
                <b-form-group>
                  <div class="mb-2">Название:</div>
                  <b-form-textarea id="name" v-model="listName" max-rows="6" rows="2" />
                </b-form-group>
                <b-form-invalid-feedback class="mb-3" :state="valid">
                  <span v-for="(error, index) in errors" :key="index">{{ error }}</span>
                </b-form-invalid-feedback>
              </validation-provider>

              <move-object-component
                v-model="parent"
                :object="{ id: list.id, object: list, type: 'LIST' }"
                :group="group"
                :list_tree="group.lists"
                :frozen-elem="
                  list.parent == null ? { id: list.group.id, type: 'GROUP' } : { id: list.parent.id, type: 'LIST' }
                "
                @clear-model="restoreParent"
              />
              <list-access-component v-model="specialAccess" />
            </b-card-text>

            <b-form-group>
              <b-row>
                <b-col>
                  <b-button variant="info" :disabled="invalid" block @click="saveList">Сохранить</b-button>
                </b-col>
                <b-col>
                  <b-button :to="{ name: 'list', params: { id: id } }" block>Отмена</b-button>
                </b-col>
              </b-row>
            </b-form-group>
          </validation-observer>
          <hr />
          <b-button v-b-modal:confirmDeletion variant="danger" block>
            <font-awesome-icon :icon="['fas', 'trash-alt']" />
          </b-button>
          <b-modal id="confirmDeletion" centered hide-header hide-footer hide-header-close :busy="true">
            Пожалуйста, подтвердите удаление списка <i>{{ list.name }}</i>
            <b-button-group class="mt-3 d-flex">
              <b-button variant="danger" class="text-center" @click="deleteList">Удалить</b-button>
              <b-button variant="outline-dark" class="text-center" @click="$bvModal.hide('confirmDeletion')">
                Отмена
              </b-button>
            </b-button-group>
          </b-modal>
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop, Watch } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import MoveObjectComponent from "@/components/MoveObjectComponent.vue";
import ListAccessComponent from "@/components/ListAccessComponent.vue";
import { AccessData, GroupTree, ListEditInfo, MoveElemInfo, NavbarItem } from "@/models";
import { appModule } from "@/store/app-module";
import { api } from "@/backend";

@Component({ components: { BreadcrumbNavbarComponent, MoveObjectComponent, ListAccessComponent } })
export default class extends Vue {
  private loaded: boolean = false;

  @Prop()
  private readonly id!: string;

  private navbarItems: NavbarItem[] = [];

  private readonly appStore = appModule.context(this.$store);
  private list!: ListEditInfo;
  private group!: GroupTree;

  private listName: string = "";

  private parent: MoveElemInfo = { object: { id: 0, name: "", ownerId: 0, role: "MEMBER", lists: [] }, type: "LIST" };
  // private specialAccessOriginal!: AccessData;
  private specialAccessNew!: AccessData;
  private specialAccess: AccessData = { set: false, accounts: [] };

  private useNewAccessList: boolean = false;

  async created() {
    const list = await api.getListEditInfo(this.id);
    if (list == null) {
      await this.$router.push({ name: "index" });
      return;
    }
    this.list = list;
    this.listName = this.list.name;

    const group = await api.getGroupTree(this.list.group.id, this.list.id, false);
    if (group == null) {
      await this.$router.push({ name: "index" });
      return;
    }
    this.group = group;

    this.parent =
      this.list.parent != null
        ? { object: this.list.parent, type: "LIST" }
        : { object: this.list.group, type: "GROUP" };

    this.specialAccessNew = { set: this.list.accessData.set, accounts: this.list.accessData.accounts };
    this.specialAccess = this.specialAccessNew;

    this.navbarItems.push({ id: this.list.group.id, name: this.list.group.name, type: "GROUP" });
    this.navbarItems.push({ id: +this.list.id, name: this.list.name, type: "LIST" });

    this.loaded = true;
  }

  private async saveList(): Promise<void> {
    await api.editList({
      id: this.id,
      name: this.listName,
      parentId: this.parent.type == "GROUP" || this.parent.object == null ? null : this.parent.object.id,
      accessList: this.specialAccess.set
        ? this.specialAccess.accounts.filter(acc => acc.hasAccess).map(acc => acc.id)
        : []
    });
    // await this.appStore.actions.fetchAll();
    // this.list = this.appStore.getters.list(+this.id)!;
    await this.$router.push({ name: "list", params: { id: this.id } });
  }

  private async deleteList(): Promise<void> {
    const fallback =
      this.list?.parent != null
        ? { id: this.list.parent.id, name: "list" }
        : {
            id: this.list.group.id,
            name: "group"
          };
    await api.deleteList(this.id);
    await this.$router.push({ name: fallback.name, params: { id: `${fallback.id}` } });
  }

  private restoreParent() {
    if (this.list.parent != null) {
      this.parent.type = "LIST";
      this.parent.object = this.list.parent;
    } else {
      this.parent.type = "GROUP";
      this.parent.object = this.list.group;
    }

    this.useNewAccessList = false;
    this.specialAccess = { set: this.list.accessData.set, accounts: this.list.accessData.accounts };
  }

  @Watch("parent")
  private async parentChanged(value: MoveElemInfo): Promise<void> {
    if (
      (this.list.parent == null && value.type == "GROUP") ||
      (this.list.parent != null && this.list.parent.id == value.object!.id && value.type == "LIST")
    ) {
      this.useNewAccessList = false;
    } else {
      if (value.type == "GROUP") {
        const resp = await api.getGroupMembers(value.object!.id);
        if (resp == null) {
          return;
        }
        this.specialAccessNew = {
          set: false,
          accounts: resp.members.map(m => {
            return { id: m.id, name: m.name, hasAccess: true };
          })
        };
      } else {
        const resp = await api.getListAccessData(value.object!.id);
        if (resp == null) {
          return;
        }
        this.specialAccessNew = resp;
        const parentAccessIds = this.specialAccessNew.accounts.filter(acc => acc.hasAccess).map(acc => acc.id);
        this.specialAccessNew.accounts = this.specialAccessNew.accounts
            .filter(acc => parentAccessIds.includes(acc.id));
      }
      this.useNewAccessList = true;
    }

    this.specialAccess = this.useNewAccessList ? this.specialAccessNew : this.list.accessData;
  }
}
</script>
