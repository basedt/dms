import { request } from '@umijs/max';

export const ConfigService = {
  url: '/api/sys/config',

  getMailInfo: async () => {
    return request<DMS.ResponseBody<DMS.EmailConfig>>(`${ConfigService.url}` + '/email', {
      method: 'GET',
    });
  },

  setMailInfo: async (info: DMS.EmailConfig) => {
    return request<DMS.ResponseBody<any>>(`${ConfigService.url}` + '/email', {
      method: 'PUT',
      data: info,
    });
  },

  getLLMInfo: async () => {
    return request<DMS.ResponseBody<DMS.LLMConfig>>(`${ConfigService.url}` + '/llm', {
      method: 'GET',
    });
  },

  setLLMInfo: async (info: DMS.LLMConfig) => {
    return request<DMS.ResponseBody<any>>(`${ConfigService.url}` + '/llm', {
      method: 'PUT',
      data: info,
    });
  },
};
