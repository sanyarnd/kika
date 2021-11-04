<template>
  <div>
    <div style="display: flex; align-items: center">
      <b-container class="col-lg-11">
        <breadcrumb-navbar-component :items="navbarItems" />
        <b-card>
          <b-row>
            <b-col class="h4">Редактирование</b-col>
          </b-row>
          <hr />
          <b-card-text>
            <b-form-group label="Название:" label-for="input-default">
              <b-form-textarea id="name" v-model="list.name" max-rows="6" rows="2" class="mb-3" />
            </b-form-group>

            <list-access-component :special-access="specialAccess"></list-access-component>

            <move-object-component
              :object="{ object: list, type: 'LIST' }"
              :group="group"
              :list_tree="list_tree"
              class="mt-3"
            ></move-object-component>
          </b-card-text>

          <b-form-group>
            <b-row>
              <b-col>
                <b-button variant="info" type="submit" block>Сохранить</b-button>
              </b-col>
              <b-col>
                <b-button :to="{ name: 'list', params: { id: id } }" block>Отмена</b-button>
              </b-col>
            </b-row>
          </b-form-group>
          <hr />
          <b-button v-b-modal:confirmDeletion variant="danger" block>
            <font-awesome-icon :icon="['fas', 'trash-alt']"/>
          </b-button>
          <b-modal
            id="confirmDeletion"
            v-model="showDeleteDialog"
            centered
            hide-header
            hide-footer
            hide-header-close
            :busy="true"
          >
            Пожалуйста, подтвердите удаление списка <i>{{ list.name }}</i>
            <b-button-group class="mt-3 d-flex">
              <b-button variant="danger" class="text-center" @click="deleteList(list.id)">Удалить</b-button>
              <b-button variant="outline-dark" class="text-center" @click="showDeleteDialog = false">Отмена</b-button>
            </b-button-group>
          </b-modal>
        </b-card>
      </b-container>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import BreadcrumbNavbarComponent from "@/components/BreadcrumbNavbarComponent.vue";
import { Group } from "@/components/SetListView.vue";
import { List, User } from "@/pages/app/views/TaskEdit.vue";
import MoveObjectComponent from "@/components/MoveObjectComponent.vue";
import ListAccessComponent, { SpecialAccess } from "@/components/ListAccessComponent.vue";

@Component({ components: { BreadcrumbNavbarComponent, MoveObjectComponent, ListAccessComponent } })
export default class extends Vue {
  @Prop()
  private readonly id!: string;

  private showDeleteDialog = false;

  private user: User = { id: "1", name: "kate", role: "MEMBER", subscribedTo: [], assignedTo: [] };

  private group: Group = { id: "1", name: "Группа 1", ownerId: this.user.id };

  private list: List = { name: "Список 11", id: "2", children: [], parentId: "1", groupId: this.group.id, tasks: [] };

  private navbarItems = [
    { id: "1", name: "Группа 1", type: "group" },
    { id: this.list.id, name: this.list.name, type: "list" }
  ];

  private list_tree: List[] = [
    {
      name: "Список 1",
      id: "1",
      children: [
        { name: "Список 11", id: "2", children: [], tasks: [], parentId: this.list.id, groupId: this.group.id },
        { name: "Список 12", id: "3", children: [], tasks: [], parentId: this.list.id, groupId: this.group.id },
        { name: "Список 13", id: "4", children: [], tasks: [], parentId: this.list.id, groupId: this.group.id }
      ],
      tasks: [
        {
          id: "10",
          name: "Поесть",
          description: "Нужно не забыть поесть",
          status: "COMPLETED",
          parentId: null,
          listId: "4",
          children: [
            {
              id: "20",
              name: "Приготовить поесть",
              description: "Опять дома есть нечего",
              status: "COMPLETED",
              parentId: null,
              listId: "4",
              children: []
            }
          ]
        }
      ],
      parentId: "1",
      groupId: this.group.id
    },
    {
      name: "Список 2",
      id: "5",
      children: [
        { name: "Список 21", id: "7", children: [], tasks: [], parentId: "1", groupId: this.group.id },
        { name: "Список 22", id: "8", children: [], tasks: [], parentId: "1", groupId: this.group.id },
        { name: "Список 23", id: "9", children: [], tasks: [], parentId: "1", groupId: this.group.id }
      ],
      tasks: [],
      parentId: "1",
      groupId: this.group.id
    },
    { name: "Список 3", id: "6", children: [], tasks: [], parentId: "1", groupId: this.group.id }
  ];

  private specialAccess: SpecialAccess = {
    set: true,
    accounts: [
      { id: "1", name: "kate", specialAccess: true },
      { id: "2", name: "john", specialAccess: false },
      { id: "3", name: "bob", specialAccess: false }
    ]
  };

  private deleteList(id: string): void {
    this.showDeleteDialog = false;
    console.log("DELETED LIST " + id);
  }
}
</script>
