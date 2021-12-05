<template>
  <span>
    <b-alert variant="secondary" class="" show>
      <b-form-group v-slot="{ accessMode }" label="Доступ к списку:">
        <b-form-radio v-model="specialAccess.set" :aria-describedby="accessMode" :value="false" name="some-radios">
          без ограничений
        </b-form-radio>
        <b-form-radio v-model="specialAccess.set" :aria-describedby="accessMode" :value="true" name="some-radios">
          по разрешению
        </b-form-radio>
      </b-form-group>

      <b-list-group v-for="(user, key) in specialAccess.accounts" :key="key" flush>
        <b-list-group-item
          v-if="specialAccess.set"
          :class="(key === 0 ? 'rounded-top' : key === specialAccess.accounts.length - 1 ? 'rounded-bottom' : '') +
           (appStore.getters.account.id === user.id ? 'text-secondary' : 'text-dark')"
          :disabled="appStore.getters.account.id === user.id"
          button
          class="border-0"
          @click="switchAccess(user)"
        >
          <font-awesome-icon
            v-if="user.hasAccess || appStore.getters.account.id === user.id"
            :icon="['far', 'check-square']"
            class="kika-icon"
          />
          <font-awesome-icon v-else :icon="['far', 'square']" class="kika-icon" />
          {{ user.name }}
        </b-list-group-item>
        <b-list-group-item
          v-else
          :class="key === 0 ? 'rounded-top' : key === specialAccess.accounts.length - 1 ? 'rounded-bottom' : ''"
          class="border-0 text-secondary"
        >
          <font-awesome-icon :icon="['far', 'check-square']" class="kika-icon" />
          {{ user.name }}
        </b-list-group-item>
      </b-list-group>
    </b-alert>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop, VModel } from "vue-property-decorator";
import SidebarComponent from "@/components/SidebarComponent.vue";
import TreeView from "@/components/TaskTreeView.vue";
import ListTreeView from "@/components/ListTreeView.vue";
import { appModule } from "@/store/app-module";
import { AccessData, ParentInfoType, UserSpecialAccess } from "@/models";

@Component({ components: { TreeView, SidebarComponent, ListTreeView } })
export default class extends Vue {
  private readonly appStore = appModule.context(this.$store);

  @VModel()
  private specialAccess!: AccessData;

  @Prop({ default: "GROUP" })
  private readonly parentType!: ParentInfoType;

  private switchAccess(user: UserSpecialAccess) {
    console.log(user);
    user.hasAccess = !user.hasAccess;
  }
}
</script>
