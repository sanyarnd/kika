<template>
  <div>
    <router-view :key="$route.fullPath" />
    <sidebar-component />
    <b-alert
      v-model="showError"
      class="position-fixed fixed-bottom m-0 rounded-0"
      style="z-index: 2000"
      variant="danger"
      dismissible
      ><font-awesome-icon class="kika-icon mr-1" :icon="['fas', 'exclamation-triangle']" />
      {{ errorMessage }}
    </b-alert>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import SidebarComponent from "@/components/SidebarComponent.vue";
import { EventBus, Events } from "@/event-bus";
import { AxiosError } from "axios";
import { ErrorResponse } from "@/backend/dto";
import { appModule } from "@/store/app-module";

@Component({ components: { SidebarComponent } })
export default class extends Vue {
  private readonly appStore = appModule.context(this.$store);

  private showError: boolean = false;
  private errorMessage: string = "";

  async created(): Promise<void> {
    EventBus.$on(Events.NETWORK_ERROR, this.handleNetworkError);

    await this.appStore.actions.fetchAll();
  }

  private handleNetworkError(e: AxiosError<ErrorResponse>) {
    this.showError = true;
    console.log(e.response?.data.detail ?? "Unknown network error");
    this.errorMessage = e.response?.data.detail ?? "Unknown network error";
  }
}
</script>

<style>
@import "../../assets/kika.scss";
</style>
