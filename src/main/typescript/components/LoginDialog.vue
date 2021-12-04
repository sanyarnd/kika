<template>
  <div class="min-vh-100 min-vw-100" style="display: flex; align-items: center">
    <div class="col-lg-3 mx-auto text-center">
      <h1>Kika</h1>
      <div class="container">
        <b-form class="justify-content-center" @submit="login">
          <b-form-group id="input-group-1" label-for="input-1">
            <b-form-input
              id="input-1"
              v-model="username"
              placeholder="Адрес эл. почты"
              required
              @keydown.enter.native="login"
            />
          </b-form-group>

          <b-form-group id="input-group-2" label-for="input-2">
            <b-form-input
              id="input-2"
              v-model="password"
              placeholder="Пароль"
              required
              type="password"
              @keydown.enter.native="login"
            ></b-form-input>
          </b-form-group>
          <b-button :disabled="loggingIn" block variant="primary" @click="login"> Войти</b-button>
          <hr />
          <b-button
            v-b-popover.hover.bottom="'Войти через GitHub'"
            href="/api/oauth2/authorization/github"
            variant="outline-dark"
          >
            <font-awesome-icon :icon="['fab', 'github']" />
          </b-button>
          <b-toast id="error" class="m-0 rounded-0" no-auto-hide title="Error" toaster="toaster" variant="danger">
            {{ alertMessage }}
          </b-toast>
        </b-form>
      </div>
      <b-toaster class="b-toaster-bottom-center" name="toaster"></b-toaster>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import { api } from "@/backend";
import { AxiosError } from "axios";

@Component({ components: {} })
export default class extends Vue {
  private username: string = "";
  private password: string = "";
  private loggingIn: boolean = false;
  private alertMessage: string = "";

  private async login() {
    this.loggingIn = true;
    try {
      // TODO
      let response = await api.authenticateLogin(this.username, this.password);
      if (response != null) {
        await this.$router.push({ name: "index" });
      }
    } catch (err) {
      const e = err as AxiosError;

      this.$bvToast.show("error");
      this.alertMessage = e.response?.data.message;
    } finally {
      this.loggingIn = false;
    }
  }
}
</script>
