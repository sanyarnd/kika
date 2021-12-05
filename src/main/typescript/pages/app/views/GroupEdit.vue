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
                  <b-form-textarea id="name" v-model="group.name" max-rows="6" rows="2" />
                </b-form-group>
                <b-form-invalid-feedback class="mb-3" :state="valid">
                  <span v-for="(error, index) in errors" :key="index">{{ error }}</span>
                </b-form-invalid-feedback>
              </validation-provider>

              <group-members-component v-model="members" />

              <b-button v-b-modal:confirmMessageDeletion size="sm" :disabled="clearedMessages" variant="danger" block>
                <font-awesome-icon :icon="['fas', 'comment-slash']" />
                Очистить сообщения
              </b-button>
              <b-modal id="confirmMessageDeletion" centered hide-header hide-footer hide-header-close :busy="true">
                Пожалуйста, подтвердите удаление сообщений в группе <i>{{ group.name }}</i>
                <b-button-group class="mt-3 d-flex">
                  <b-button variant="danger" class="text-center" @click="clearMessages">Удалить</b-button>
                  <b-button variant="outline-dark" class="text-center" @click="$bvModal.hide('confirmMessageDeletion')"
                    >Отмена
                  </b-button>
                </b-button-group>
              </b-modal>
            </b-card-text>
            <b-row>
              <b-col>
                <b-button :disabled="invalid" variant="info" block @click="save">Сохранить</b-button>
              </b-col>
              <b-col>
                <b-button :to="{ name: 'group', params: { id: id } }" block>Отмена</b-button>
              </b-col>
            </b-row>
          </validation-observer>

          <hr />
          <b-button v-b-modal:confirmDeletion variant="danger" block>
            <font-awesome-icon :icon="['fas', 'trash-alt']" />
          </b-button>
          <b-modal id="confirmDeletion" centered hide-header hide-footer hide-header-close :busy="true">
            Пожалуйста, подтвердите удаление группы <i>{{ group.name }}</i>
            <b-button-group class="mt-3 d-flex">
              <b-button variant="danger" class="text-center" @click="deleteGroup">Удалить</b-button>
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
import { appModule } from "@/store/app-module";
import { api } from "@/backend";
import { GroupEditInfo, NavbarItem } from "@/models";
import { GetGroupMemberResponse } from "@/backend/dto";
import GroupMembersComponent from "@/components/GroupMembersComponent.vue";

@Component({ components: { BreadcrumbNavbarComponent, GroupMembersComponent } })
export default class extends Vue {
  private loaded: boolean = false;

  @Prop()
  private readonly id!: string;

  private readonly navbarItems: NavbarItem[] = [];

  private readonly appStore = appModule.context(this.$store);
  private group!: GroupEditInfo;

  private members: GetGroupMemberResponse[] = [];

  private clearedMessages: boolean = false;

  async created() {
    const groupEditInfo = await api.getGroupEditInfo(this.id);
    if (groupEditInfo == null) {
      await this.$router.push({ name: "index" });
      return;
    }
    // console.log(groupEditInfo.messageCount);
    this.group = groupEditInfo;
    this.clearedMessages = groupEditInfo.messageCount == 0;

    // const membersResponse = await api.getGroupMembers(this.id);
    // if (membersResponse == null) {
    //   await this.$router.push({name: "index"});
    //   return;
    // }
    this.members = this.group.members;

    this.navbarItems.push({ id: this.group.id, name: this.group.name, type: "GROUP" });

    this.loaded = true;
  }

  private async save(): Promise<void> {
    let ownerPos = this.members.indexOf(this.members.find(m => m.role === "OWNER")!);
    const newMembers = this.members.filter((e, idx) => idx != ownerPos);
    await api.editGroup(this.id, { name: this.group.name, members: newMembers });

    // await this.appStore.actions.fetchAll();
    // this.group = this.appStore.getters.group(this.id)!;
    await this.$router.push({ name: "group", params: { id: this.id } });
  }

  private async deleteGroup(): Promise<void> {
    await api.deleteGroup(this.id);
    await this.appStore.actions.fetchAll();
    await this.$router.push({ name: "index" });
  }

  private async clearMessages(): Promise<void> {
    await api.clearMessages(this.id);
    this.clearedMessages = true;
  }
}
</script>

<style lang="scss">
.highlighted-member {
  background-color: var(--warning);
  transition: background-color 500ms linear;
}
</style>
