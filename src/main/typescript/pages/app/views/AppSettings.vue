<template>
  <div>
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="[]" />
        <b-card>
          <b-row>
            <b-col class="h4">Настройки приложения</b-col>
          </b-row>
          <hr />
          <b-card-text>
            <b-form-group label="Сообщений на странице группы:" label-for="input-default">
              <b-form-spinbutton step="5" min="5" max="30" @change="dirtyState=true" v-model="msgPerPage"></b-form-spinbutton>
            </b-form-group>
          </b-card-text>

          <b-form-group>
            <b-row>
              <b-col>
                <b-button variant="info" :disabled="!dirtyState" type="submit" block @click="save">Сохранить</b-button>
              </b-col>
              <b-col>
                <b-button block @click="ret">Назад</b-button>
              </b-col>
            </b-row>
          </b-form-group>
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
import {router} from "@/pages/app/router";

@Component({ components: { BreadcrumbNavbarComponent, MoveObjectComponent, ListAccessComponent } })
export default class extends Vue {
  private readonly appStore = appModule.context(this.$store);

  private msgPerPage: number = -1;

  private dirtyState: boolean = false;

  async created() {
    this.msgPerPage = this.appStore.getters.msgPerPage;
  }

  private async save(): Promise<void> {
    await this.appStore.actions.setMsgPerPage(this.msgPerPage);
    this.dirtyState = false;
  }

  private ret(): void {
    router.back();
  }
}
</script>
