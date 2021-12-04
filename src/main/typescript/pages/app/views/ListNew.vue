<template>
  <div v-if="loaded">
    <div style="display: flex; align-items: center" class="mb-3">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col class="h4">Новый список</b-col>
          </b-row>
          <hr />
          <validation-observer v-slot="{ invalid }">
            <b-card-text>
              <validation-provider v-slot="{ errors, valid }" name="Название" rules="required|max:128">
                <b-form-group>
                  <div class="mb-2">Название: <span class="text-danger">*</span></div>
                  <b-form-textarea id="name" v-model="name" max-rows="6" rows="2" />
                </b-form-group>
                <b-form-invalid-feedback class="mb-3" :state="valid">
                  <span v-for="(error, index) in errors" :key="index">{{ error }}</span>
                </b-form-invalid-feedback>
              </validation-provider>

<!--              <validation-provider v-slot="{ errors, valid }" name="Группа" rules="excluded:-1">-->
<!--                <b-form-group>-->
<!--                  <div class="mb-2">Группа: <span class="text-danger">*</span></div>-->
<!--                  <b-form-select id="group" v-model="groupId_" :options="groupsToChooseFrom" @change="clearParent" />-->
<!--                </b-form-group>-->
<!--                <b-form-invalid-feedback :state="valid">-->
<!--                  <span v-for="(error, index) in errors" :key="index">{{ error }}</span>-->
<!--                </b-form-invalid-feedback>-->
<!--              </validation-provider>-->

              <validation-provider v-if="groupId_ !== '-1'" name="Родительский компонент">
                <div class="mb-2">Родительский компонент: <span class="text-danger">*</span></div>
                <pick-parent-component
                  v-model="parent"
                  :object-type="'LIST'"
                  :group="group"
                  :lists="group.lists"
                  @validation:warning="parentStateIsValid = false"
                />
                <b-form-invalid-feedback class="mb-3" :state="parent != null || parentStateIsValid">
                  Необходимо указать родительский компонент
                </b-form-invalid-feedback>
              </validation-provider>

              <list-access-component
                v-if="groupId_ != null && parent != null"
                :parent-type="parent.type"
                v-model="specialAccess"
              />
            </b-card-text>

            <b-row>
              <b-col>
                <b-button variant="info" :disabled="invalid || parent == null" block @click="createList">
                  Создать
                </b-button>
              </b-col>
              <b-col v-if="groupId != null">
                <b-button :to="{ name: 'group', params: { id: groupId_ } }" block>Отмена</b-button>
              </b-col>
            </b-row>
          </validation-observer>
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
import PickParentComponent from "@/components/PickParentComponent.vue";
import { appModule } from "@/store/app-module";
import { api } from "@/backend";
import { ValidationObserver, ValidationProvider } from "vee-validate";
import { AccessData, Account, ElemInfo, Group, NavbarItem } from "@/models";
import accountEdit from "@/pages/app/views/AccountEdit.vue";

@Component({
  components: {
    ValidationObserver,
    ValidationProvider,
    PickParentComponent,
    BreadcrumbNavbarComponent,
    MoveObjectComponent,
    ListAccessComponent
  }
})
export default class extends Vue {
  private loaded: boolean = false;

  @Prop({ default: null })
  private readonly groupId!: string | null;

  @Prop({ default: null })
  private readonly parentListId!: string | null;

  private readonly navbarItems: NavbarItem[] = [];

  private readonly appStore = appModule.context(this.$store);
  private group: Group | null = null;

  private name: string = "";
  private groupId_: string = "-1";
  private parent: ElemInfo | null = null;
  private parentStateIsValid: boolean = true;

  private user: Account = this.appStore.getters.account;

  private groupsToChooseFrom = [{ value: "-1", text: "-- Выбрать группу --", disabled: true }];

  private specialAccess: AccessData = { set: false, accounts: [] };

  @Watch("groupId_")
  private async refreshGroupAndLists(groupId: number | string): Promise<void> {
    this.group = this.appStore.getters.group(groupId)!;
    if (this.groupId_ != null) {
      if (this.navbarItems.length == 0) {
        this.navbarItems.push({ id: this.group.id, name: this.group.name, type: "GROUP" });
      } else {
        this.navbarItems[0].id = this.group.id;
        this.navbarItems[0].name = this.group.name;
      }
    }
  }

  @Watch("parent")
  private async addParentToNavbar() {
    if (this.parent != null && this.parent.type != "GROUP") {
      if (this.navbarItems.length > 1) {
        this.navbarItems[1].id = +this.parent.object?.id!;
        this.navbarItems[1].name = this.parent.object?.name!;
      } else {
        this.navbarItems.push({ id: this.parent.object?.id!, name: this.parent.object?.name!, type: "LIST" });
      }
    } else {
      this.navbarItems.splice(1, 1);
    }

    // if (this.parent != null) {
    //   await this.fetchSpecialAccess(this.parent);
    // }
  }

  async created() {
    if(this.groupId == null) {
      await this.$router.push({ name: "index" });
      return;
    }

    this.navbarItems.push({ id: +this.group?.id!, name: this.group?.name!, type: "GROUP" });

    if (this.parentListId != null) {
      const parentList = await api.getListCreateInfo(this.parentListId);
      if (parentList != null) {
        this.parent = { id: parentList.id, object: parentList, type: "LIST" };
        this.navbarItems.push({ id: +parentList.id, name: parentList.name, type: "LIST" });
        this.specialAccess = parentList.accessData;
        this.specialAccess.accounts;
        this.groupId_ = `${parentList.group.id}`;
      } else {
        console.log("Invalid parent list");
        await this.$router.push({ name: "index" });
        return;
      }
    } else {
      const group = await api.getGroupEditInfo(this.groupId);
      if(group != null) {
        this.parent = {id: parseInt(this.groupId), object: group, type: "GROUP"};
        this.groupId_ = `${group.id}`;
        this.specialAccess = {set: false, accounts: group.members.map(m => {
          return {id: m.id, name: m.name, hasAccess: true};
          })}
      }
    }

    // if (this.groupId != null) {
    //   const group = this.appStore.getters.group(this.groupId);
    //   if (group == undefined) {
    //     await this.$router.push({ name: "index" });
    //     return;
    //   }
    // }

    for (let userGroup of this.appStore.getters.groups) {
      this.groupsToChooseFrom.push({ value: `${userGroup.id}`, text: userGroup.name, disabled: false });
    }

    // this.specialAccess.set = this.parentListId != null;
    // if (this.group != null) {
    //   await this.fetchSpecialAccess(this.parent!);
    // }

    this.loaded = true;
  }

  private async fetchSpecialAccess(parent: ElemInfo): Promise<boolean> {
    if (parent.type == "GROUP") {
      const tempMembers = await api.getGroupMembers(parent.id!);
      if (tempMembers != null) {
        this.specialAccess = {
          set: false,
          accounts: tempMembers.members.map(member => {
            return { id: member.id, name: member.name, hasAccess: true };
          })
        };
      } else {
        await this.$router.push({ name: "index" });
        return false;
      }
    } else {
      const tempMembers = await api.getSpecialAccess(parent.id!);
      if (tempMembers != null) {
        if (!tempMembers.set) {
          this.specialAccess = tempMembers;
        } else {
          this.specialAccess = { set: true, accounts: tempMembers.accounts.filter(acc => acc.hasAccess) };
        }
      } else {
        await this.$router.push({ name: "index" });
        return false;
      }
    }
    return true;
  }

  private async createList(): Promise<void> {
    const newListId = await api.createList({
      name: this.name,
      groupId: this.groupId_,
      parentId: this.parent?.type == "LIST" ? this.parent!.object!.id : null,
      accessList: this.specialAccess.set
        ? this.specialAccess.accounts.filter(value => value.hasAccess).map(value => value.id)!
        : []
    });
    if (newListId != null) {
      await this.appStore.actions.fetchGroupLists(this.groupId_);
      await this.$router.push({
        name: "list",
        params: {
          id: `${newListId}`
        }
      });
    }
  }

  private clearParent(): void {
    this.parent = null;
  }
}
</script>
