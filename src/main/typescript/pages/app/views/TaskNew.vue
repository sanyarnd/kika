<template>
  <div v-if="loaded">
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col class="h4">Новая задача</b-col>
          </b-row>
          <hr />
          <b-card-text>
            <validation-observer v-slot="{ invalid }">
              <b-form>
                <validation-provider v-slot="{ errors, valid }" name="Название" rules="required|max:128">
                  <b-form-group>
                    <div class="mb-2">Название: <span class="text-danger">*</span></div>
                    <b-form-textarea id="name" v-model="name" max-rows="6" rows="2" />
                  </b-form-group>
                  <b-form-invalid-feedback class="mb-3" :state="valid">
                    <span v-for="(error, index) in errors" :key="index">{{ error }}</span>
                  </b-form-invalid-feedback>
                </validation-provider>

                <validation-provider v-slot="{ errors, valid }" name="Описание" rules="max:512">
                  <b-form-group label="Описание:">
                    <b-form-textarea id="description" v-model="description" max-rows="10" type="text" />
                    <b-form-invalid-feedback :state="valid">
                      <span v-for="(error, index) in errors" :key="index">{{ error }}</span>
                    </b-form-invalid-feedback>
                  </b-form-group>
                </validation-provider>

<!--                <validation-provider v-slot="{ errors, valid }" name="Группа" rules="excluded:-1">-->
<!--                  <b-form-group>-->
<!--                    <div class="mb-2">Группа: <span class="text-danger">*</span></div>-->
<!--                    <b-form-select id="group" v-model="groupId_" :options="groupsToChooseFrom" @change="clearParent" />-->
<!--                  </b-form-group>-->
<!--                  <b-form-invalid-feedback :state="valid">-->
<!--                    <span v-for="(error, index) in errors" :key="index">{{ error }}</span>-->
<!--                  </b-form-invalid-feedback>-->
<!--                </validation-provider>-->

                <validation-provider v-if="groupId_ !== '-1'" name="Родительский компонент">
                  <div class="mb-2">Родительский компонент: <span class="text-danger">*</span></div>
                  <pick-parent-component
                    v-model="parent"
                    :object-type="'TASK'"
                    :group="group"
                    :lists="group.lists"
                    @validation:warning="parentStateIsValid = false"
                  />
                  <b-form-invalid-feedback class="mb-3" :state="parent != null || parentStateIsValid">
                    Необходимо указать родительский компонент
                  </b-form-invalid-feedback>
                </validation-provider>

                <b-row>
                  <b-col>
                    <b-button variant="info" :disabled="invalid || parent == null" block @click="createTask">
                      Создать
                    </b-button>
                  </b-col>
                  <b-col v-if="groupId != null">
                    <b-button :to="{ name: 'group', params: { id: groupId_ } }" block>Отмена</b-button>
                  </b-col>
                </b-row>
              </b-form>
            </validation-observer>
          </b-card-text>
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop, Watch } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import PickParentComponent from "@/components/PickParentComponent.vue";
import {ElemInfo, Group, GroupEditInfo, NavbarItem} from "@/models";
import { api } from "@/backend";
import { appModule } from "@/store/app-module";
// import {settingsModule} from "@/store/settings-module";

@Component({
  components: { PickParentComponent, BreadcrumbNavbarComponent }
})
export default class extends Vue {
  @Prop({ default: null })
  private readonly groupId!: string | null;

  @Prop({ default: null })
  private readonly parentListId!: string | null;

  @Prop({ default: null })
  private readonly parentTaskId!: string | null;

  private readonly appStore = appModule.context(this.$store);

  private readonly navbarItems: NavbarItem[] = [];

  private name: string = "";
  private description: string = "";
  private groupId_: string = "-1";
  private parent: ElemInfo | null = null;

  private group: GroupEditInfo | null = null;
  private groupsToChooseFrom = [{ value: "-1", text: "-- Выбрать группу --", disabled: true }];

  private loaded: boolean = false;
  private parentStateIsValid: boolean = true;

  @Watch("groupId_")
  private async refreshGroupAndListsAndTasks(groupId: number | string): Promise<void> {
    this.group = this.appStore.getters.group(groupId)!;
    for (let list of this.group?.lists) {
      await this.appStore.actions.fetchListTasks(list.id);
    }
    if (this.groupId_ != null) {
      if (this.navbarItems.length == 0) {
        this.navbarItems.push({ id: this.group.id, name: this.group.name, type: "GROUP" });
      } else {
        this.navbarItems[0].id = this.group.id;
        this.navbarItems[0].name = this.group.name;
      }
    }
  }

  async created() {
    if(this.groupId == null) {
      await this.$router.push({ name: "index" });
      return;
    }

    const group = await api.getGroupEditInfo(this.groupId);
    if(group != null) {
      this.parent = {id: parseInt(this.groupId), object: this.group, type: "GROUP"};
      this.navbarItems.push({ id: group.id, name: group.name, type: "GROUP" });
      this.groupId_ = this.groupId;
      await this.refreshGroupAndListsAndTasks(this.groupId);
    } else {
      await this.$router.push({ name: "index" });
      return;
    }

    if (this.parentListId != null) {
      const parentList = await api.getListEditInfo(this.parentListId);
      if (parentList != null) {
        this.parent = { id: parentList.id, object: parentList, type: "LIST" };
        console.log(this.navbarItems)
        this.navbarItems.push({id: +parentList.id, name: parentList.name, type: "LIST"});
        console.log(this.navbarItems)
      } else {
        console.log("Invalid parent list");
        await this.$router.push({ name: "index" });
        return;
      }
    }

    for (let userGroup of this.appStore.getters.groups) {
      this.groupsToChooseFrom.push({ value: `${userGroup.id}`, text: userGroup.name, disabled: false });
    }

    this.loaded = true;
  }

  // @Watch("parent")
  // private async addParentToNavbar() {
  //   if(!this.loaded) {
  //     return;
  //   }
  //   if (this.parent != null && this.parent.type == "TASK") {
  //     if (this.navbarItems.length > 1) {
  //       this.navbarItems[1].id = +this.parent.object?.id!;
  //       this.navbarItems[1].name = this.parent.object?.name!;
  //       this.navbarItems[1].type = "TASK";
  //     } else {
  //       this.navbarItems.push({ id: +this.parent.object?.id!, name: this.parent.object?.name!, type: "TASK" });
  //     }
  //   } else {
  //     this.navbarItems.splice(1, 1);
  //   }
  // }

  private clearParent(): void {
    this.parent = null;
  }

  private async createTask(): Promise<void> {
    let listId: number;
    if (this.parent?.type == "TASK") {
      const parentTask = await api.getTask(this.parent.id!);
      if (parentTask != null) {
        listId = parentTask.listId;
      } else {
        console.log("Invalid parent task");
        await this.$router.push({ name: "index" });
        return;
      }
    } else {
      listId = this.parent?.id!;
    }

    const newTaskId = await api.createTask({
      name: this.name,
      description: this.description,
      parentId: this.parent?.type == "TASK" ? this.parent!.object!.id : null,
      listId: listId
    });
    console.log(newTaskId);
    if (newTaskId != null) {
      await this.appStore.actions.fetchListTasks(listId);
      await this.$router.push({
        name: "task",
        params: {
          id: `${newTaskId}`
        }
      });
    }
  }
}
</script>
