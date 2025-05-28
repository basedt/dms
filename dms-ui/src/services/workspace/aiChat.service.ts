import { EventSourceMessage, fetchEventSource } from '@microsoft/fetch-event-source';
import { request } from '@umijs/max';

export const AiChatService = {
  url: '/api/workspace/ai',

  simpleChat: async (chatMsg: DMS.ChatMsg) => {
    return request<DMS.Page<DMS.Workspace>>(`${AiChatService.url}/simple/chat`, {
      method: 'POST',
      data: chatMsg,
    });
  },

  streamChat: (
    chatMsg: DMS.ChatMsg,
    onopen?: (response: Response) => void,
    onmessage?: (event: EventSourceMessage) => void,
    onclose?: () => void,
    onerror?: (err: any) => number | null | undefined | void,
  ) => {
    const controller = new AbortController();
    const signal = controller.signal;

    fetchEventSource(`${AiChatService.url}/stream/chat`, {
      method: 'POST',
      body: new URLSearchParams({ query: JSON.stringify(chatMsg) }),
      signal,
      onopen: async (response) => {
        try {
          if (onopen) {
            await onopen(response);
          }
        } catch (error) {
          console.error('onopen handler error:', error);
        }
      },
      onmessage: (event) => {
        try {
          onmessage?.(event);
        } catch (error) {
          console.error('onmessage handler error:', error);
        }
      },
      onerror: (err) => {
        console.error('EventSource error:', err);
        controller.abort();
        onerror?.(err);
      },
      onclose: () => {
        onclose?.();
      },
    });
    return {
      controller,
    };
  },
};
