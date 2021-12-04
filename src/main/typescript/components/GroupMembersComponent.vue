<template>
  <span>
    <div class="mb-2">Участники:</div>
    <b-input-group v-if="newMemberToAdd.id === -1">
      <b-form-input
        :state="memberPickerState"
        v-model="newMemberId"
        placeholder="Добавить участника"
        @keydown.enter="getAccount"
      />
      <b-input-group-append>
        <b-button variant="info" :disabled="newMemberId === '' || !memberPickerState" @click="getAccount">
          <font-awesome-icon :icon="['fas', 'search']" />
        </b-button>
      </b-input-group-append>
    </b-input-group>
    <b-input-group v-else>
      <b-form-input :value="newMemberToAdd.name" readonly />
      <b-input-group-append>
        <b-button variant="info" @click="addMember">
          <font-awesome-icon :icon="['fas', 'plus']" />
        </b-button>
      </b-input-group-append>
    </b-input-group>
    <b-table-simple borderless class="mt-2" small>
      <colgroup>
        <col width="100%" />
        <col width="0%" />
      </colgroup>
      <b-tr v-for="member in members" :key="member.id" :id="'member-' + member.id">
        <b-td
          class="pt-0 pl-1 pb-0 pr-1"
          style="white-space: nowrap; text-overflow: ellipsis; overflow: hidden; max-width: 1px; vertical-align: middle"
        >
          {{ member.name }}
        </b-td>
        <b-td class="pt-0 pl-1 pb-0 pr-1" style="white-space: nowrap">
          <b-button-group>
            <b-dropdown right size="sm" :disabled="member.role === 'OWNER'" variant="outline-dark">
              <template #button-content>
                <font-awesome-layers class="mr-1">
                  <font-awesome-icon icon="user" />
                  <font-awesome-icon
                    v-if="member.role === 'RESTRICTED'"
                    class="pl-2 pt-2 fa-lg pr-n2"
                    style="color: orangered"
                    :icon="['fas', 'ban']"
                  />
                  <font-awesome-icon
                    v-else-if="member.role === 'OWNER'"
                    class="pl-2 pt-2 fa-lg pr-n2"
                    style="color: goldenrod"
                    :icon="['fas', 'star']"
                  />
                  <font-awesome-icon
                    v-else
                    :icon="['fas', 'check']"
                    class="pl-2 pt-2 fa-lg pr-n2"
                    style="color: mediumseagreen"
                  />
                </font-awesome-layers>
              </template>
              <b-dropdown-item style="max-width: min-content" href="#" @click="flipMemberRole(member)">
                <font-awesome-layers class="mr-2">
                  <font-awesome-icon icon="user" />
                  <font-awesome-icon
                    v-if="member.role === 'MEMBER'"
                    class="pl-2 pt-2 fa-lg pr-n2"
                    style="color: orangered"
                    :icon="['fas', 'ban']"
                  />
                  <font-awesome-icon
                    v-else
                    :icon="['fas', 'check']"
                    class="pl-2 pt-2 fa-lg pr-n2"
                    style="color: mediumseagreen"
                  />
                </font-awesome-layers>
                <span v-if="member.role === 'MEMBER'">Ограниченный пользователь</span>
                <span v-else>Обычный пользователь</span></b-dropdown-item
              >
            </b-dropdown>
            <b-button
                squared
              class="pl-3 pr-3"
              size="sm"
              variant="outline-danger"
              :disabled="member.role === 'OWNER'"
              @click="removeMember(member.id)"
            >
              <font-awesome-icon :icon="['fas', 'minus']" />
            </b-button>
          </b-button-group>
        </b-td>
      </b-tr>
    </b-table-simple>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, VModel, Watch } from "vue-property-decorator";
import { appModule } from "@/store/app-module";
import { GetGroupMemberResponse } from "@/backend/dto";
import { api } from "@/backend";

@Component({ components: {} })
export default class extends Vue {
  private readonly appStore = appModule.context(this.$store);

  @VModel()
  private members!: GetGroupMemberResponse[];

  private memberPickerState: boolean | null = null;
  private newMemberId: string = "";
  private highlightedMemberId: string = "";
  private newMemberToAdd: GetGroupMemberResponse = { id: -1, name: "", role: "MEMBER" };

  private async getAccount(): Promise<void> {
    if (!this.memberPickerState) {
      return;
    }
    if (this.newMemberId !== "" && !this.members.map((m) => `${m.id}`).includes(this.newMemberId)) {
      const newMember = await api.getAccountById(this.newMemberId);
      if (newMember != null) {
        this.newMemberToAdd = { id: newMember.id, name: newMember.name, role: "MEMBER" };
        this.newMemberId = "";
      }
    } else {
      this.highlightedMemberId = this.newMemberId;
      this.highlight();
    }
  }

  private addMember(): void {
    this.members.push(this.newMemberToAdd);
    this.newMemberToAdd = { id: -1, name: "", role: "MEMBER" };
  }

  private removeMember(id: number): void {
    const toRemove = this.members.find((m) => m.id === id);
    if (toRemove != undefined) {
      this.members.splice(this.members.indexOf(toRemove), 1);
    }
  }

  private flipMemberRole(member: GetGroupMemberResponse): void {
    if (member.role === "MEMBER") {
      member.role = "RESTRICTED";
    } else if (member.role === "RESTRICTED") {
      member.role = "MEMBER";
    }
  }

  private highlight(): void {
    const member = document.getElementById(`member-${this.highlightedMemberId}`);
    if (member != null) {
      member.classList.add("highlighted-member");
    }
  }

  @Watch("newMemberId")
  private resetHighlight(): void {
    if (this.newMemberId == "") {
      this.memberPickerState = null;
    } else {
      this.memberPickerState = !isNaN(+this.newMemberId) && +this.newMemberId > 0;
    }
    const member = document.getElementById(`member-${this.highlightedMemberId}`);
    if (member != null) {
      member.classList.remove("highlighted-member");
      this.highlightedMemberId = "";
    }
  }
}
</script>
