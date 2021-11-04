<template>
  <b-card
    v-b-toggle:list-1-special-access
    no-body
    :header="`Настроить особый доступ (${specialAccess.set ? 'включен' : 'отключен'})`"
  >
    <b-collapse id="list-1-special-access">
      <b-list-group v-for="(user, key) in specialAccess.accounts" :id="user.id" :key="key" flush>
        <b-list-group-item
          v-if="specialAccess.set"
          :style="{ 'text-decoration': specialAccess.set && user.specialAccess ? 'none' : 'line-through' }"
          class="text-truncate"
          button
          @click="user.specialAccess = !user.specialAccess"
          @click.stop
        >
          {{ user.name }}
        </b-list-group-item>
        <b-list-group-item v-else disabled class="text-truncate" @click.stop>
          {{ user.name }}
        </b-list-group-item>
        <b-list-group-item
          v-if="key === specialAccess.accounts.length - 1"
          button
          @click="specialAccess.set = !specialAccess.set"
          @click.stop
        >
          <span v-if="specialAccess.set">
            <font-awesome-icon class="mr-1" icon="toggle-on"/>
            Отключить
          </span>
          <span v-if="!specialAccess.set">
            <font-awesome-icon class="mr-1" icon="toggle-off"/>
            Включить
          </span>
        </b-list-group-item>
      </b-list-group>
    </b-collapse>
  </b-card>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import SidebarComponent from "@/components/SidebarComponent.vue";
import TreeView from "@/components/TaskTreeView.vue";
import ListTreeView from "@/components/ListTreeView.vue";

@Component({ components: { TreeView, SidebarComponent, ListTreeView } })
export default class extends Vue {
  @Prop()
  private readonly specialAccess!: SpecialAccess;
}

export interface UserWithSpecialAccess {
  id: string;
  name: string;
  specialAccess: boolean;
}

export interface SpecialAccess {
  set: boolean;
  accounts: UserWithSpecialAccess[];
}
</script>
