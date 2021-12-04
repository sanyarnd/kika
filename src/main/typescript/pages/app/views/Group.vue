<template>
  <div v-if="loaded">
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="[]"/>
        <b-card>
          <b-row>
            <b-col class="h4 text-truncate">{{ group.name }}</b-col>
            <b-col v-if="group.role === 'OWNER'" cols="2" class="text-right mr-3 mt-1">
              <b-link :to="{ name: 'group-edit', params: { id: id } }" class="text-dark">
                <font-awesome-icon :icon="['fas', 'pencil-alt']" size="2x" class="mt-n1 mr-n2"/>
              </b-link>
            </b-col>
          </b-row>
          <hr/>

          <b-list-group flush>
            <b-list-group-item
                v-for="(list, key) in group.lists"
                :key="key"
                :class="{ 'border-top': key === 0 }"
                :to="{ name: 'list', params: { id: list.id } }"
                class="border-bottom border-right border-left text-truncate"
            >
              <font-awesome-icon :icon="['fas', 'list-ul']" class="kika-icon mr-2"/>
              {{ list.name }}
            </b-list-group-item>
          </b-list-group>

          <b-button
              v-if="group.role !== 'RESTRICTED'"
              :to="{ name: 'list-new', params: { groupId: id } }"
              variant="outline-dark mt-1"
              block
              squared
              size="sm"
          >
            <font-awesome-icon class="kika-icon mr-1" :icon="['fas', 'plus']"/>
            Новый список
          </b-button>
          <hr/>
          <b-input-group class="mt-3 mb-3">
            <b-form-input v-model="message" placeholder="Отправить сообщение..." @keydown.enter="postMessage"/>
            <b-input-group-append>
              <b-button variant="info" @click="postMessage">
                <font-awesome-icon :icon="['fas', 'paper-plane']"/>
              </b-button>
            </b-input-group-append>
          </b-input-group>
          <b-overlay :show="loadingMessages" rounded="sm">
            <b-table-simple v-for="(message, key) in group.messages.messages" :key="key" borderless>
              <colgroup>
                <col width="100%"/>
                <col width="0%"/>
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
                      @click="deleteMessage(message.id)"/></span
                  ></b-td>
              </b-tr>
              <b-tr>
                <b-td class="pt-0 pr-1 pl-1 pb-0" colspan="2">{{ message.body }}</b-td>
              </b-tr>
            </b-table-simple>
          </b-overlay>
          <div class="overflow-auto align-content-end">
            <b-pagination
                v-model="page"
                :total-rows="group.messages.count"
                :per-page="msgPerPage"
                first-number
                align="center"
                @page-click="(o, p) => loadMessages(p)"
            >
            </b-pagination>
          </div>
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import {Component, Prop} from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import ListChildrenView from "@/components/ListChildrenView.vue";
import ListTasksView from "@/components/ListTasksView.vue";
import {api} from "@/backend";
import {appModule} from "@/store/app-module";
import {DateTime} from "luxon";
import {GroupInfo} from "@/models";

@Component({components: {BreadcrumbNavbarComponent, ListChildrenView, ListTasksView}})
export default class extends Vue {
  private loaded: boolean = false;

  @Prop()
  private readonly id!: string;

  private readonly appStore = appModule.context(this.$store);
  private group: GroupInfo | null = null;

  private message: string = "";

  private loadingMessages = false;
  private page: number = -1;
  private msgPerPage: number = -1;

  async created() {
    this.msgPerPage = this.appStore.getters.msgPerPage;
    this.group = await api.getGroupInfo(this.id, this.msgPerPage);
    if (this.group == null) {
      await this.$router.push({name: "index"});
      return;
    }
    this.page = this.group.messages.offset / this.msgPerPage + 1;

    this.loaded = true;
  }

  private async deleteMessage(id: number): Promise<void> {
    await api.deleteGroupMessage(id);
    if (this.group?.messages.messages.length == 1 && this.page > 1) {
      if (this.page < Math.ceil(this.group.messages.count / this.msgPerPage)) {
      } else {
        this.page -= 1;
      }
    }
    await this.loadMessages(this.page);
  }

  private async postMessage(): Promise<void> {
    await api.postGroupMessage(this.id, this.message);
    this.message = "";
    await this.loadMessages(1);
  }

  private getDate(date: string): string {
    return DateTime.fromISO(date).toLocaleString(DateTime.DATETIME_MED);
  }

  private async loadMessages(page: number): Promise<void> {
    this.loadingMessages = true;
    const resp = await api.loadMessages(this.id, (page - 1) * this.msgPerPage, this.msgPerPage);
    if (resp != null) {
      this.group!.messages.count = resp.count;
      this.group!.messages.offset = resp.offset;
      this.group!.messages.messages = resp.messages;
    }
    this.loadingMessages = false;
  }
}
</script>
