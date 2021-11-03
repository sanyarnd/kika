<template>
  <div>
    <span v-if="page_type == 'group-tasks'">
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item text-truncate"><a v-b-toggle.sidebar-1 href="#">Kika</a></li>
          <li class="breadcrumb-item text-truncate"><a href="/app">{группа 1}</a></li>
          <!--        <li class="breadcrumb-item"><span class="dropdown show">-->
          <!--          <a class="dropdown-toggle" href="#" role="button" id="dropdownMenuLink" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">-->
          <!--            Задачи-->
          <!--          </a>-->

          <!--          <div class="dropdown-menu" aria-labelledby="dropdownMenuLink">-->
          <!--            <a class="dropdown-item" href="#">Сообщения</a>-->
          <!--            <a class="dropdown-item" href="#">Участники</a>-->
          <!--          </div>-->
          <!--        </span>-->
          <!--        </li>-->
        </ol>
      </nav>
      <div style="display: flex; align-items: center">
        <b-container class="col-lg-11">
          <ul class="nav nav-tabs mb-3">
            <li class="nav-item">
              <a class="nav-link active" href="#">Задачи</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="#">Сообщения</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="#">Участники</a>
            </li>
          </ul>

          <div class="list-group">
            <button
              type="button"
              class="list-group-item list-group-item-action list-group-item-secondary text-truncate"
            >
              {{ list1 }}
            </button>
            <button
              type="button"
              class="list-group-item list-group-item-action list-group-item-secondary text-truncate"
            >
              Список 2
            </button>
            <button
              type="button"
              class="list-group-item list-group-item-action list-group-item-secondary text-truncate"
            >
              Список 3
            </button>
          </div>

          <tree-view :items="treeItems"></tree-view>
        </b-container>
      </div>
    </span>

    <span v-if="page_type == 'list'">
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item"><a v-b-toggle.sidebar-1 href="#">Kika</a></li>
          <li class="breadcrumb-item text-truncate"><a href="#">{группа 1}</a></li>
          <li class="breadcrumb-item text-truncate">
            <a href="#">{{ list1 }}</a>
          </li>
        </ol>
      </nav>
      <div style="display: flex; align-items: center">
        <b-container class="col-lg-11">
          <b-row class="mb-3">
            <b-col class="h4 text-truncate">{{ list1 }}</b-col>
            <b-col cols="2" align-self="center" class="text-right mr-3"
              ><b-icon-gear-fill scale="2"></b-icon-gear-fill></b-col
          ></b-row>
          <div class="list-group">
            <button
              type="button"
              class="list-group-item list-group-item-action list-group-item-secondary text-truncate"
            >
              {{ list1 }}
            </button>
            <button
              type="button"
              class="list-group-item list-group-item-action list-group-item-secondary text-truncate"
            >
              {{ list1 }}2
            </button>
            <button
              type="button"
              class="list-group-item list-group-item-action list-group-item-secondary text-truncate"
            >
              {{ list1 }}3
            </button>
          </div>

          <tree-view :items="treeItems"></tree-view>
        </b-container>
      </div>
    </span>

    <span v-if="page_type == 'list-settings'">
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item"><a v-b-toggle.sidebar-1 href="#">Kika</a></li>
          <li class="breadcrumb-item text-truncate"><a href="#">{группа 1}</a></li>
          <li class="breadcrumb-item text-truncate">
            <a href="#">{{ list1 }}</a>
          </li>
        </ol>
      </nav>
      <div style="display: flex; align-items: center">
        <b-container class="col-lg-11">
          <b-card>
            <b-row>
              <b-col class="h4">{{ list1 }}</b-col></b-row
            >
            <b-card-text>
              <b-form-group label="Название" label-for="input-default">
                <b-form-textarea id="textarea" v-model="list1" max-rows="6" class="mb-3"></b-form-textarea>
                <b-card v-b-toggle:list-1-special-access no-body header="Настройка доступа">
                  <b-collapse id="list-1-special-access">
                    <b-list-group v-for="(user, key) in special_access" :id="user.id" :key="key" flush>
                      <b-list-group-item button @click="user.has_access = !user.has_access" @click.stop>
                        <b-row>
                          <b-col class="text-truncate">
                            {{ user.name }}
                          </b-col>
                          <b-col class="text-right" cols="2">
                            <span v-if="user.has_access">
                              <b-icon-check-square scale="1.5" variant="success"></b-icon-check-square>
                            </span>
                            <span v-else>
                              <b-icon-x-square scale="1.5" variant="danger"></b-icon-x-square>
                            </span>
                          </b-col> </b-row
                      ></b-list-group-item>
                    </b-list-group>
                  </b-collapse>
                </b-card>

                <b-card v-b-toggle:list-1-move no-body header="Переместить">
                  <b-collapse id="list-1-move">
                    <list-tree-view
                      :items="list_tree"
                      :list_id="list_tree[0].id"
                      :selected_list="selected_list"
                    ></list-tree-view>
                  </b-collapse>
                </b-card>
                <b-button class="mt-3" type="submit" block>Сохранить</b-button>
              </b-form-group>
              <hr />
              <b-button v-b-modal:confirmDeletion variant="danger" block
                ><b-icon-trash-fill></b-icon-trash-fill
              ></b-button>
              <b-modal
                id="confirmDeletion"
                v-model="showDeleteDialog"
                centered
                hide-header
                hide-footer
                hide-header-close
                :busy="true"
              >
                Пожалуйста, подтвердите удаление списка <i>{{ list1 }}</i>
                <b-button-group class="mt-3 d-flex">
                  <b-button variant="danger" class="text-center" @click="deleteList(1)">Удалить</b-button>
                  <b-button variant="outline-dark" class="text-center" @click="showDeleteDialog = false"
                    >Отмена</b-button
                  >
                </b-button-group>
              </b-modal>
            </b-card-text>
          </b-card>
        </b-container>
      </div>
    </span>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import { Component } from "vue-property-decorator";
import SidebarComponent from "@/components/SidebarComponent.vue";
import TreeView from "@/components/TaskTreeView.vue";
import ListTreeView from "@/components/ListTreeView.vue";

@Component({ components: { TreeView, SidebarComponent, ListTreeView } })
export default class extends Vue {
  private page_type: string = "group-tasks";

  private selected_list = { id: null };

  private list1: string = "Список 1";
  private list_tree = [
    {
      name: this.list1,
      id: 1,
      children: [
        { name: "Список 11", id: 2, children: [], expanded: false },
        { name: "Список 12", id: 3, children: [], expanded: false },
        { name: "Список 13", id: 4, children: [], expanded: false }
      ], expanded: false
    },
    {
      name: "Список 2",
      id: 5,
      children: [
        { name: "Список 21", id: 7, children: [], expanded: false },
        { name: "Список 22", id: 8, children: [], expanded: false },
        { name: "Список 23", id: 9, children: [], expanded: false }
      ], expanded: false
    },
    { name: "Список 3", id: 6, children: [], expanded: false }
  ];
  private showDeleteDialog = false;
  private restricted_user = false;
  private task1: string = "Полить цветы и подстричь кусты";
  private task1_completed = false;
  private special_access = [
    { id: 1, name: "kate", has_access: true },
    { id: 2, name: "john", has_access: false },
    { id: 3, name: "bob", has_access: false }
  ];

  private notification_button_pressed: boolean = false;
  private subscription_button_pressed: boolean = false;

  private assigned_users = [
    { id: 1, name: "kate" },
    { id: 2, name: "john" }
  ];

  private deleteList(id: bigint): void {
    // ...
    this.showDeleteDialog = false;
  }

  private treeItems = [
    {
      name: "Купить тортик",
      id: 1,
      ticked: true,
      children: [
        {
          name: "Заказать тортик",
          id: 2,
          ticked: true,
          children: [
            { name: "Позвонить в кондитерскую", id: 5, ticked: true, children: [] },
            { name: "Выбрать тортик", id: 6, ticked: true, children: [] }
          ]
        },
        { name: "Забрать тортик", id: 3, ticked: true, children: [] }
      ]
    },
    { name: "Сварить кофе", id: 4, ticked: false, children: [] },
    { name: this.task1, id: 5, ticked: false, children: [] }
  ];
}
</script>
