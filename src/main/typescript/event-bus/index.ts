import Vue from "vue";

export enum Events {
  NETWORK_ERROR = "NETWORK_ERROR"
}

export const EventBus = new Vue();
