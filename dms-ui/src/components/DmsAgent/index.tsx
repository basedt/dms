import AiImg from '@/icons/ai-primary.svg';
import { AiChatService } from '@/services/workspace/aiChat.service';
import '@/tailwind.css';
import {
  ArrowUpOutlined,
  CloseOutlined,
  CopyOutlined,
  LoadingOutlined,
  PlusOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Bubble, useXAgent, useXChat, Welcome } from '@ant-design/x';
import { EventSourceMessage } from '@microsoft/fetch-event-source';
import { useIntl } from '@umijs/max';
import { Avatar, Button, Col, Flex, Input, Row, Tooltip, Typography } from 'antd';
import classNames from 'classnames';
import copy from 'copy-to-clipboard';
import React, { useCallback, useEffect, useRef, useState } from 'react';
import Markdown from 'react-markdown';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import rehypeRow from 'rehype-raw';
import remarkGfm from 'remark-gfm';
import { v4 as uuidv4 } from 'uuid';
import './index.less';

type RoleConfig = {
  placement: 'start' | 'end';
  avatar: {
    icon: React.ReactNode;
    style: React.CSSProperties;
  };
};

const ROLES: Record<'ai' | 'local', RoleConfig> = {
  ai: {
    placement: 'start',
    avatar: {
      icon: (
        <Avatar src={<img src={AiImg} style={{ width: 24, height: 24 }} alt="ai"></img>}></Avatar>
      ),
      style: { background: 'linear-gradient(97deg, #f2f9fe 0%, #f7f3ff 100%)' },
    },
  },
  local: {
    placement: 'end',
    avatar: {
      icon: <UserOutlined />,
      style: { background: '#87d068' },
    },
  },
};
interface StreamChatParams {
  cid: string;
  messages: string[];
}
interface ChatMessage {
  id: string;
  message: string;
  status: 'local' | 'ai';
}
interface BubbleItem {
  key: string;
  role: 'local' | 'ai';
  content: React.ReactNode;
}

interface DmsAgentProps {
  msgs: string[];
  onClose: () => void;
}

const { Text } = Typography;

const DmsAgent: React.FC<DmsAgentProps> = (props) => {
  const intl = useIntl();
  const chatBoxRef = useRef<HTMLDivElement>(null);
  const { msgs, onClose } = props;
  const abortRef = useRef<() => void>(() => {});
  const cidRef = useRef<string>(uuidv4());
  const [content, setContent] = useState<string>('');
  const viewRef = useRef<boolean>(false);
  const [isRequesting, setIsRequesting] = useState<boolean>(false);

  useEffect(() => {
    return () => {
      abortRef.current();
    };
  }, []);

  const scrollToBottom = () => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  };

  const [agent] = useXAgent({
    request: async (
      input: { message: string } | null,
      {
        onSuccess,
        onUpdate,
      }: {
        onSuccess: (data: string) => void;
        onUpdate: (data: string) => void;
      },
    ) => {
      let currentResponse = '';

      try {
        const { controller } = AiChatService.streamChat(
          {
            cid: cidRef.current,
            messages: input ? [...msgs, input.message] : [],
          } as StreamChatParams,
          (resp: Response) => {
            if (!resp.ok) {
              console.error('Failed to connect to chat service', resp);
              throw new Error(`Connection failed with status ${resp.status}`);
            }
          },
          (event: EventSourceMessage) => {
            let data: { content: string } = JSON.parse(event.data);
            currentResponse += data.content || '';
            onUpdate(currentResponse);
            viewRef.current ? null : scrollToBottom();
          },
          () => {
            onSuccess(currentResponse);
            setIsRequesting(false);
          },
          (error: Error) => {
            console.error('Chat stream error:', error);
            setIsRequesting(false);
          },
        );

        abortRef.current = () => {
          controller.abort();
          setIsRequesting(false);
        };
      } catch (error) {
        console.error('Error in chat request:', error);
        setIsRequesting(false);
      }
    },
  });

  const { onRequest, messages: chatMessages, setMessages } = useXChat({ agent });

  const handleSubmit = useCallback(() => {
    if (content.trim()) {
      setIsRequesting(true);
      onRequest(content);
      viewRef.current = false;
      setContent('');
    }
  }, [content, onRequest]);

  const handleCancel = useCallback(() => {
    abortRef.current();
    setIsRequesting(false);
  }, []);

  const resetChat = useCallback(() => {
    cidRef.current = uuidv4();
    setIsRequesting(false);
    setMessages([]);
    setContent('');
  }, []);

  const handleKeyPress = useCallback(
    (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
      if (e.key === 'Enter' && !e.nativeEvent.isComposing) {
        e.preventDefault();
        handleSubmit();
      }
    },
    [handleSubmit],
  );

  const placeholderNode = (
    <>
      <Welcome
        icon={<img src={AiImg} style={{ maxWidth: 48 }} alt="" />}
        style={{
          background: 'linear-gradient(97deg, #f2f9fe 0%, #f7f3ff 100%)',
          borderStartStartRadius: 4,
        }}
        title={intl.formatMessage({ id: 'dms.console.ai.agent.hello' })}
        description={intl.formatMessage({ id: 'dms.console.ai.agent.desc' })}
      ></Welcome>
    </>
  );

  const PreNode = (children: React.ReactNode, className: string | undefined) => {
    let language = 'plaintext';
    let code = '';
    if (React.isValidElement(children)) {
      const codeLang = children.props.className;
      language = codeLang ? codeLang.replace(/language-/, '') : 'plaintext';
      code = children.props.children;
    }
    return (
      <pre className={className}>
        <Row>
          <Col span={12}>
            <Text type="secondary" style={{ color: 'white' }}>
              {language}
            </Text>
          </Col>
          <Col span={12} style={{ textAlign: 'right' }}>
            <Button
              size="small"
              icon={<CopyOutlined />}
              onClick={() => {
                copy(code);
              }}
            ></Button>
          </Col>
        </Row>
        {children}
      </pre>
    );
  };

  const bubbleItems: BubbleItem[] = chatMessages.map(({ id, message, status }: ChatMessage) => ({
    key: id,
    role: status === 'local' ? 'local' : 'ai',
    content: (
      <div className="prose prose-sm">
        <Markdown
          remarkPlugins={[remarkGfm]}
          rehypePlugins={[rehypeRow]}
          components={{
            pre: (props) => {
              const { children, className, node, ...rest } = props;
              return PreNode(children, className);
            },
            code(props) {
              const { children, className, node, ...rest } = props;
              const match = /language-(\w+)/.exec(className || '');
              return match ? (
                <SyntaxHighlighter
                  {...rest}
                  PreTag="div"
                  children={String(children).replace(/\n$/, '')}
                  language={match[1]}
                  style={vscDarkPlus}
                />
              ) : (
                <code {...rest} className={className}>
                  {children}
                </code>
              );
            },
          }}
        >
          {message}
        </Markdown>
      </div>
    ),
  }));

  return (
    <Flex vertical gap="middle" className="codeAi">
      <div className="codeAiTop">
        <Row style={{ width: '100%' }}>
          <Col flex={1} style={{ alignItems: 'center', display: 'flex' }}>
            <Text strong>{intl.formatMessage({ id: 'dms.console.ai.agent.title' })}</Text>
          </Col>
          <Col flex={1} style={{ textAlign: 'right' }}>
            <Tooltip
              title={intl.formatMessage({ id: 'dms.console.ai.agent.newchat' })}
              placement="bottom"
              arrow={true}
            >
              <Button
                icon={<PlusOutlined />}
                type="text"
                onClick={resetChat}
                aria-label="Reset chat"
              />
            </Tooltip>
            <Tooltip
              title={intl.formatMessage({ id: 'dms.common.operate.close' })}
              placement="bottom"
              arrow={true}
            >
              <Button
                icon={<CloseOutlined />}
                type="text"
                onClick={onClose}
                aria-label="close chat"
              />
            </Tooltip>
          </Col>
        </Row>
      </div>

      <div
        className={classNames('codeAiList')}
        ref={chatBoxRef}
        onScroll={() => {
          if (chatBoxRef.current) {
            const isNearBottom: boolean =
              chatBoxRef.current.scrollHeight - chatBoxRef.current.scrollTop <=
              chatBoxRef.current.clientHeight + 80;
            if (isNearBottom) {
              viewRef.current = false;
            } else {
              viewRef.current = true;
            }
          }
        }}
      >
        <Bubble.List
          roles={ROLES}
          items={
            bubbleItems.length > 0
              ? bubbleItems
              : [{ content: placeholderNode, variant: 'borderless' }]
          }
        />
      </div>

      <div className={classNames('codetext')}>
        <Input.TextArea
          value={content}
          onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setContent(e.target.value)}
          onKeyDown={handleKeyPress}
          placeholder={`${intl.formatMessage({ id: 'dms.console.ai.agent.input' })}...`}
          autoSize={{ minRows: 1 }}
          disabled={isRequesting}
        />

        <div className={classNames('codetext_button')}>
          {isRequesting ? (
            <Button
              type="primary"
              shape="circle"
              size="small"
              danger
              onClick={handleCancel}
              aria-label="Cancel request"
            >
              <LoadingOutlined />
            </Button>
          ) : (
            <Button
              type="primary"
              shape="circle"
              size="small"
              disabled={!content.trim()}
              onClick={handleSubmit}
              aria-label="Send message"
            >
              <ArrowUpOutlined />
            </Button>
          )}
        </div>
      </div>
    </Flex>
  );
};

export default DmsAgent;
