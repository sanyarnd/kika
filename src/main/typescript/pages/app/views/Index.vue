<template>
  <div>
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="[]" />
        <b-card>
          <b-row>
            <b-col class="h4 text-truncate">Мои группы</b-col>
          </b-row>
          <hr />
          <b-list-group flush>
            <b-list-group-item
              v-for="(group, key) in groups"
              :key="key"
              :class="{ 'border-top': key === 0 }"
              class="border-bottom border-right border-left text-truncate"
              :to="{ name: 'group', params: { id: group.id } }"
            >
              <b-table-simple borderless class="m-0">
                <col width="100%" />
                <col width="0%" />
                <b-tr>
                  <b-td
                    style="white-space: nowrap; text-overflow: ellipsis; overflow: hidden; max-width: 1px"
                    class="p-0"
                  >
                    <font-awesome-icon class="kika-icon mr-2" :icon="['fas', 'users']" />
                    {{ group.name }}
                  </b-td>
                  <b-td class="text-right p-0" style="white-space: nowrap">
                    <font-awesome-icon v-if="group.role === 'OWNER'" class="kika-icon ml-2" :icon="['fas', 'star']" />
                  </b-td>
                </b-tr>
              </b-table-simple>
            </b-list-group-item>
          </b-list-group>
          <b-button :to="{ name: 'group-new' }" variant="outline-dark mt-1" block squared size="sm">
            <font-awesome-icon class="kika-icon" :icon="['fas', 'plus']" />
            Новая группа
          </b-button>
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import { appModule } from "@/store/app-module";
import { Group } from "@/models";

@Component({ components: { BreadcrumbNavbarComponent } })
export default class extends Vue {
  private readonly appStore = appModule.context(this.$store);
  private readonly groups: Group[] = this.appStore.getters.groups;

  // async created() {
  //   await this.appStore.actions.fetchAccount();
  //   await this.appStore.actions.fetchGroups();
  // }
}
</script>
