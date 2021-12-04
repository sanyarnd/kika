<template>
  <div v-if="loaded">
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="[]" />
        <b-card>
          <b-row>
            <b-col class="h4 text-truncate">{{ group.name }}</b-col>
            <b-col v-if="group.role === 'OWNER'" cols="2" class="text-right mr-3 mt-1">
              <b-link :to="{ name: 'group-edit', params: { id: id } }" class="text-dark">
                <font-awesome-icon :icon="['fas', 'pencil-alt']" size="2x" class="mt-n1 mr-n2" />
              </b-link>
            </b-col>
          </b-row>
          <hr />
          <list-children-view
            :lists="group.lists.filter(list => list.parentId == null)"
            :navbar-items="[{ id: group.id, name: group.name, type: 'GROUP' }]"
          />
          <b-button
            v-if="group.role !== 'RESTRICTED'"
            :to="{ name: 'list-new', params: { groupId: id } }"
            variant="outline-dark mt-1"
            block
            squared
            size="sm"
          >
            <font-awesome-icon class="kika-icon mr-1" :icon="['fas', 'plus']" />
            Новый список
          </b-button>
          <hr />
          <b-input-group class="mt-3 mb-3">
            <b-form-input v-model="message" placeholder="Отправить сообщение..." @keydown.enter="postMessage" />
            <b-input-group-append>
              <b-button variant="info" @click="postMessage">
                <font-awesome-icon :icon="['fas', 'paper-plane']" />
              </b-button>
            </b-input-group-append>
          </b-input-group>
          <b-table-simple v-for="(message, key) in messages" :key="key" borderless>
            <colgroup>
              <col width="100%" />
              <col width="0%" />
            </colgroup>
            <b-tr class="align-text-top">
              <b-td
                class="pt-0 pl-1 pb-0 pr-1"
                style="white-space: nowrap; text-overflow: ellipsis; overflow: hidden; max-width: 1px"
              >
                <span class="small">
                  {{ message.sender }}
                </span>
              </b-td>
              <b-td class="pt-0 pl-1 pb-0 pr-1" style="white-space: nowrap">
                <span class="small text-secondary">{{ getDate(message.createdDate) }}</span>
                <span class="pl-4"
                  ><font-awesome-icon
                    style="cursor: pointer"
                    :icon="['fas', 'times']"
                    class="fa-lg"
                    @click="deleteMessage(message.id)" /></span
              ></b-td>
            </b-tr>
            <b-tr>
              <b-td class="pt-0 pr-1 pl-1 pb-0" colspan="2">{{ message.body }}</b-td>
            </b-tr>
          </b-table-simple>
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import ListChildrenView from "@/components/ListChildrenView.vue";
import ListTasksView from "@/components/ListTasksView.vue";
import { api } from "@/backend";
import { appModule } from "@/store/app-module";
import { DateTime } from "luxon";
import { Group, List } from "@/models";
import { GetMessage } from "@/backend/dto";

@Component({ components: { BreadcrumbNavbarComponent, ListChildrenView, ListTasksView } })
export default class extends Vue {
  private loaded: boolean = false;

  @Prop()
  private readonly id!: string;

  private readonly appStore = appModule.context(this.$store);
  private group: Group | null = null;
  private messages: GetMessage[] = [];

  private message: string = "";

  async created() {
    const group = this.appStore.getters.group(this.id);
    if (group == undefined) {
      await this.$router.push({ name: "index" });
      return;
    }
    this.group = group;

    let fetchResponse = await this.fetchMessages(this.id);
    if (!fetchResponse) {
      return;
    }

    this.loaded = true;
  }

  private async fetchMessages(groupId: string): Promise<boolean> {
    const msgResponse = await api.getGroupMessages(groupId);
    if (msgResponse == null) {
      return false;
    }
    this.messages = msgResponse.messages;
    return true;
  }

  private async deleteMessage(id: number): Promise<void> {
    await api.deleteGroupMessage(id);
    await this.fetchMessages(this.id);
  }

  private async postMessage(): Promise<void> {
    await api.postGroupMessage(this.id, this.message);
    this.message = "";
    await this.fetchMessages(this.id);
  }

  private getDate(date: string): string {
    return DateTime.fromISO(date).toLocaleString(DateTime.DATETIME_MED);
  }
}
</script>
