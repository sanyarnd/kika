import originalAxios from "axios";
import { authenticationExpireHandler } from "@/backend/authenticationExpireHandler";

export const axios = originalAxios.create({
  timeout: 30_000
});

axios.interceptors.response.use(
  value => value,
  error => {
    const response = error.response;
    const status = error.status || (response ? response.status : 0);
    if (status === 401) authenticationExpireHandler();
    return Promise.reject(error);
  }
);
