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
                  <b-form-textarea id="name" v-model="taskName" max-rows="6" rows="2" />
                </b-form-group>
                <b-form-invalid-feedback class="mb-3" :state="valid">
                  <span v-for="(error, index) in errors" :key="index">{{ error }}</span>
                </b-form-invalid-feedback>
              </validation-provider>

              <validation-provider v-slot="{ errors, valid }" name="Описание" rules="max:512">
                <b-form-group label="Описание:">
                  <b-form-textarea id="description" v-model="taskDescription" max-rows="10" type="text" />
                  <b-form-invalid-feedback :state="valid">
                    <span v-for="(error, index) in errors" :key="index">{{ error }}</span>
                  </b-form-invalid-feedback>
                </b-form-group>
              </validation-provider>

              <move-object-component
                v-model="parent"
                :object="{ id: task.id, object: task, type: 'TASK' }"
                :group="group"
                :list_tree="group.lists"
                :frozen-elem="
                  task.parent == null ? { id: task.list.id, type: 'LIST' } : { id: task.parent.id, type: 'TASK' }
                "
                @clear-model="restoreParent"
              />

              <b-form-group>
                <b-row>
                  <b-col>
                    <b-button variant="info" :disabled="invalid" block @click="saveTask">Сохранить</b-button>
                  </b-col>
                  <b-col>
                    <b-button :to="{ name: 'task', params: { id: id } }" block>Отмена</b-button>
                  </b-col>
                </b-row>
              </b-form-group>
            </b-card-text>
          </validation-observer>
          <hr />
          <b-button v-b-modal:confirmDeletion variant="danger" block>
            <font-awesome-icon :icon="['fas', 'trash-alt']" />
          </b-button>
          <b-modal id="confirmDeletion" centered hide-header hide-footer hide-header-close :busy="true">
            Пожалуйста, подтвердите удаление задачи <i>{{ task.name }}</i>
            <b-button-group class="mt-3 d-flex">
              <b-button variant="danger" class="text-center" @click="deleteTask">Удалить</b-button>
              <b-button variant="outline-dark" class="text-center" @click="$bvModal.hide('confirmDeletion')"
                >Отмена
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
import { Component, Prop } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import MoveObjectComponent from "@/components/MoveObjectComponent.vue";
import { GroupTree, MoveElemInfo, NavbarItem, TaskEditInfo } from "@/models";
import { appModule } from "@/store/app-module";
import { api } from "@/backend";

@Component({
  components: { MoveObjectComponent, BreadcrumbNavbarComponent }
})
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  private readonly appStore = appModule.context(this.$store);

  private group!: GroupTree;
  private task!: TaskEditInfo;

  private parent: MoveElemInfo = { object: { id: 0, name: "", ownerId: 0, role: "MEMBER", lists: [] }, type: "LIST" };

  private taskName: string = "";
  private taskDescription: string = "";

  private navbarItems: NavbarItem[] = [];

  private newParent: MoveElemInfo | null = null;

  private loaded: boolean = false;

  async created() {
    const task = await api.getTaskEditInfo(this.id);
    if (task == null) {
      await this.$router.push({ name: "index" });
      return;
    }
    this.task = task;
    this.taskName = this.task.name;
    this.taskDescription = this.task.description;

    this.parent =
      this.task.parent != null ? { object: this.task.parent, type: "TASK" } : { object: this.task.list, type: "LIST" };

    const group = await api.getGroupTree(this.task.list.groupId, this.task.id, true);
    if (group == null) {
      await this.$router.push({ name: "index" });
      return;
    }
    this.group = group;

    this.navbarItems.push({ id: this.group.id, name: this.group.name, type: "GROUP" });
    this.navbarItems.push({ id: this.task.list.id, name: this.task.list.name, type: "LIST" });
    this.navbarItems.push({ id: this.task.id, name: this.task.name, type: "TASK" });

    this.loaded = true;
  }

  private async saveTask(): Promise<void> {
    let listId: number | null = -1;
    let parentId: number | null = -1;
    if (this.parent.type == "LIST") {
      listId = this.parent.object!.id;
      parentId = null;
    } else if (this.parent.type == "TASK") {
      parentId = this.parent.object!.id;
      listId = null;
    }

    console.log({
      id: this.id,
      name: this.taskName,
      description: this.taskDescription,
      parentId: parentId,
      listId: listId
    });

    await api.editTask({
      id: this.id,
      name: this.taskName,
      description: this.taskDescription,
      parentId: parentId,
      listId: listId
    });
    // await this.appStore.actions.fetchAll();
    // this.task = this.appStore.getters.task(+this.id)!;
    await this.$router.push({ name: "task", params: { id: this.id } });
  }

  private restoreParent() {
    if (this.task.parent != null) {
      this.parent!.type = "TASK";
      this.parent!.object = this.task.parent;
    } else {
      this.parent!.type = "LIST";
      this.parent!.object = this.task.list;
    }
  }

  private async deleteTask(): Promise<void> {
    const fallback =
      this.task?.parent != null
        ? { id: this.task.parent.id, name: "task" }
        : {
            id: this.task.list.id,
            name: "list"
          };
    await api.deleteTask(this.id);
    await this.$router.push({ name: fallback.name, params: { id: `${fallback.id}` } });
  }
}

export type Status = "COMPLETED" | "NOT_COMPLETED" | "DELETED";
export type Role = "OWNER" | "MEMBER" | "RESTRICTED";
</script>
