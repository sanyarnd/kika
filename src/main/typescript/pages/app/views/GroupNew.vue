<template>
  <div style="display: flex; align-items: center">
    <b-container class="col-lg-11">
      <breadcrumb-navbar-component :items="[]" />
      <b-card>
        <b-row>
          <b-col class="h4">Новая группа</b-col>
        </b-row>
        <hr />
        <b-card-text>
          <validation-observer v-slot="{ invalid }">
            <validation-provider v-slot="{ errors, valid }" name="Название" rules="required|max:128">
              <b-form-group label="Название:">
                <b-form-textarea id="name" v-model="name" required max-rows="6" rows="2" type="text" />
                <b-form-invalid-feedback v-if="errors.length > 0" :state="valid">
                  <span v-for="(error, index) in errors" v-if="!valid || valid == null" :key="index">{{ error }}</span>
                </b-form-invalid-feedback>
              </b-form-group>

              <group-members-component v-model="members"/>

              <b-row>
                <b-col>
                  <b-button variant="info" block :disabled="invalid" @click="createGroup">Сохранить</b-button>
                </b-col>
                <b-col>
                  <b-button :to="{ name: 'index' }" block>Отмена</b-button>
                </b-col>
              </b-row>
            </validation-provider>
          </validation-observer>
        </b-card-text>
      </b-card>
    </b-container>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import { api } from "@/backend";
import { appModule } from "@/store/app-module";
import {GetGroupMemberResponse} from "@/backend/dto";
import GroupMembersComponent from "@/components/GroupMembersComponent.vue";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";

@Component({
  components: { GroupMembersComponent, BreadcrumbNavbarComponent }
})
export default class extends Vue {
  private readonly appStore = appModule.context(this.$store);

  private name: string = "";
  private members: GetGroupMemberResponse[] = [];

  created() {
    this.members.push({id: this.appStore.getters.account.id, name: this.appStore.getters.account.name, role: 'OWNER'});
  }

  private async createGroup(): Promise<void> {
    const newGroupId = await api.createGroup({ name: this.name, members: this.members.filter(m => m.id != this.appStore.getters.account.id) });
    if (newGroupId != null) {
      await this.appStore.actions.fetchAll();
      await this.$router.push({ name: "group", params: { id: `${newGroupId}` } });
    }
  }
}
</script>
