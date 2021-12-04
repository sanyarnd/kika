<template>
  <div>
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="[]" />
        <b-card>
          <b-row>
            <b-col class="h4">Настройка аккаунта</b-col>
          </b-row>
          <hr />
          <b-card-text>
            <b-form-group label="Имя:" label-for="name">
              <b-form-input id="name" v-model="accountName" class="mb-3" />
            </b-form-group>

            <div class="mb-2">Идентификатор в системе:</div>
            <b-input-group class="mb-3">
              <b-form-input id="nn" readonly :value="accountId"/>
              <b-input-group-append>
                <b-button variant="info" v-clipboard:copy="accountId">
                  <font-awesome-icon :icon="['far', 'copy']" />
                </b-button>
              </b-input-group-append>
            </b-input-group>
          </b-card-text>

          <b-form-group>
            <b-row>
              <b-col>
                <b-button variant="info" type="submit" block @click="save">Сохранить</b-button>
              </b-col>
              <b-col>
                <b-button block :to="{ name: 'index' }">Отмена</b-button>
              </b-col>
            </b-row>
          </b-form-group>
          <hr />
          <b-button v-b-modal:confirmDeletion variant="danger" block>
            <font-awesome-icon :icon="['fas', 'trash-alt']" />
          </b-button>
          <b-modal
            id="confirmDeletion"
            centered
            hide-header
            hide-footer
            hide-header-close
            :busy="true"
          >
            Пожалуйста, подтвердите удаление аккаунта
            <b-button-group class="mt-3 d-flex">
              <!--              <b-button variant="danger" class="text-center" @click="deleteGroup(group.id)">Удалить</b-button>-->
              <b-button variant="outline-dark" class="text-center" @click="$bvModal.hide('confirmDeletion')">Отмена</b-button>
            </b-button-group>
          </b-modal>
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import MoveObjectComponent from "@/components/MoveObjectComponent.vue";
import ListAccessComponent from "@/components/ListAccessComponent.vue";
import { appModule } from "@/store/app-module";
import { api } from "@/backend";
import VueClipboard from "vue-clipboard2";

Vue.use(VueClipboard);

@Component({ components: { BreadcrumbNavbarComponent, MoveObjectComponent, ListAccessComponent } })
export default class extends Vue {
  private readonly appStore = appModule.context(this.$store);

  private accountName: string = this.appStore.getters.account.name;
  private accountId: number = this.appStore.getters.account.id;

  private async save(): Promise<void> {
    await api.editAccount({ name: this.accountName });
    await this.appStore.actions.fetchAccount();
    await this.$router.push({ name: "index" });
  }
}
</script>
