import { AuthService } from "@/services/admin/auth.service";
import { FileService } from "@/services/workspace/file.service";
import { SqlSocketCreator } from "@/socket";
import Editor, { loader, useMonaco } from "@monaco-editor/react";
import { history, useIntl, useModel } from "@umijs/max";
import { Button, message, Modal, notification, Space, Tag } from "antd";
import * as monaco from "monaco-editor";
import { language as sqlLanguage } from "monaco-editor/esm/vs/basic-languages/sql/sql";
import * as monacoApi from "monaco-editor/esm/vs/editor/editor.api";
import { useCallback, useEffect, useRef, useState } from "react";
import * as sqlFormatter from "sql-formatter";
import DmsGrid from "../DmsAgGrid";
import { SqlHistoryService } from "@/services/workspace/sql.service";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import classNames from "classnames";
import suggestionsProvider from './suggestionsProvider'
import "./index.less";

type CoderEditorProps = {
  theme: string;
  language?: string;
  maxHeight: number;
  dataSourceId: string | number;
  fileId?: string | number;
  workspaceId?: string | number;
  fileName?: string;
  parentId?: string | number;
  keyId?: string;
  unSave: (d: string | number | undefined, type: string) => void;
};

const monacoThemes: Map<string, string> = new Map([
  ["github", "GitHub"],
  ["clouds", "Clouds"],
  ["eiffel", "Eiffel"],
]);

const defineTheme = (theme: string) => {
  return new Promise<void>((res) => {
    Promise.all([
      loader.init(),
      import(`monaco-themes/themes/${monacoThemes.get(theme)}.json`),
    ]).then(([monaco, themeData]) => {
      monaco.editor.defineTheme(theme, themeData);
      res();
    });
  });
};

loader.config({ monaco });

const CodeEditor: React.FC<CoderEditorProps> = (props) => {
  const { current: socket } = useRef(SqlSocketCreator());
  const { tabsKey } = useModel("global");
  const intl = useIntl();
  const monaco = useMonaco();
  const editorRef: any = useRef(null);
  const monacoRef: any = useRef(null);
  const {
    language,
    theme,
    maxHeight,
    dataSourceId,
    fileId,
    unSave,
    workspaceId,
    fileName,
    parentId,
  } = props;
  const [isConnected, setIsConnected] = useState<boolean>(socket.connected);
  const [dataColumns, setDataColumns] = useState<any[]>([]);
  const [consoleList, setConsoleList] = useState<any[]>([]);
  const file: any = useRef(null);
  const [sqlScript, setSqlScript] = useState<any>(""); // sql编辑器内容
  const [totalButtonDisable, setTotalButtonDisable] =
    useState<DMS.sqlTopButton>({
      runButton: false,
      saveButton: false,
      stopButton: true,
      publishButton: true,
    }); // sql编辑按钮
  const totalButtonDisableRef = useRef<DMS.sqlTopButton>({ ...totalButtonDisable }); // sql编辑按钮
  const labelCounterRef = useRef(0); //存储当前执行的数量

  useEffect(() => {
    socket.on("connect", () => {
      console.log("socket connect on", socket.id);
      setIsConnected(true);
    });

    socket.on("resultSet", (value) => {
      sqlListData(value);
    });

    socket.on("info", (value) => {
      setConsoleList((prevDataColumns) => [
        ...prevDataColumns,
        { value, type: "info" },
      ]);
    });

    socket.on("finished", (value) => {
      const runningResults = {
        ...totalButtonDisable,
        stopButton: true,
        runButton: false,
      }
      setTotalButtonDisable(runningResults);
      totalButtonDisableRef.current = runningResults
    });

    socket.on("error", (value) => {
      setConsoleList((prevDataColumns) => [
        ...prevDataColumns,
        { value, type: "error" },
      ]);

      notification.error({
        message: "ERROR",
        description: value,
        placement: "topRight",
      });

      socket.disconnect();

      const runningResults = {
        ...totalButtonDisable,
        stopButton: true,
      }
      setTotalButtonDisable(runningResults);
      totalButtonDisableRef.current = runningResults
    });

    socket.on("stop", (value) => {
      socket.disconnect();
    });

    socket.on("disconnect", () => {
      setIsConnected(false);
    });

    return () => {
      socket.off("connect");
      socket.off("resultSet");
      socket.off("info");
      socket.off("error");
      socket.off("stop");
      socket.off("disconnect");
      socket.off("finished");
    };
  }, []);

  useEffect(() => {
    if (sqlScript && file.current?.content !== sqlScript) {
      unSave(fileId, "notSave");
    }
  }, [sqlScript]);

  // 初始化请求保存的数据
  const initialData = (params: DMS.File = {} as DMS.File) => {
    const {
      workspaceId: paramsWorkspaceId = workspaceId,
      datasourceId: paramsParentId = parentId,
      fileName: paramsFileName = fileName,
    } = params;
    FileService.getLatestFile(
      paramsWorkspaceId,
      paramsParentId,
      paramsFileName
    ).then((res) => {
      if (res.success) {
        file.current = res.data;
        setSqlScript(res.data?.content);
      }
    });
  };

  useEffect(() => {
    //init themes
    monacoThemes.forEach((value, key) => {
      defineTheme(key);
    });
    //初始化请求内容数据
    initialData();

    window.addEventListener("keydown", (e) => handleKeyDown(e, fileId));
    return () => {
      window.removeEventListener("keydown", (e) => handleKeyDown(e, fileId));
    };
  }, []);

  useEffect(() => {
    if (monaco) {
      //config monaco
    }
  }, [monaco]);

  const handleEditorDidMount = (editor: any, monaco: any) => {
    editorRef.current = editor;
    monacoRef.current = monaco;
  };

  /**
   *
   * if selected value is not null then return value of editor
   */
  const getSelectedValue = () => {
    const selectedValue: string = editorRef.current
      .getModel()
      .getValueInRange(editorRef.current.getSelection());

    if (
      selectedValue == null ||
      selectedValue == undefined ||
      selectedValue == ""
    ) {
      return editorRef.current?.getValue();
    } else {
      return selectedValue;
    }
  };

  const reConnection = () => {
    //check user token and reconnection or goto login page
    AuthService.getCurrentUser(false).then((resp) => {
      if (!resp.data) {
        history.push("/user/login");
      } else if (!socket.connected) {
        socket.connect();
      }
      setConsoleList([]);
      const sql = getSelectedValue();
      const runningResults = {
        ...totalButtonDisable,
        stopButton: false,
        runButton: true,
      }
      setTotalButtonDisable(runningResults);
      totalButtonDisableRef.current = runningResults

      socket.emit("exec", {
        script: sql,
        dataSourceId: dataSourceId,
        workspaceId: workspaceId,
      });
    });
  };

  const clearListData = (keyArray: any): void => {
    setDataColumns((prevItems) =>
      prevItems.filter((item) =>
        keyArray.some((res: any) => res.key === item.key)
      )
    );
  };

  const DmsGridCallback = useCallback(() => {
    return (
      <DmsGrid
        dataColumns={dataColumns}
        clearListData={clearListData}
        consoleList={consoleList}
        workspaceId={workspaceId as string}
        datasourceId={dataSourceId}
      />
    );
  }, [labelCounterRef.current, consoleList]);

  const sqlListData = (value: any) => {
    setDataColumns((prevDataColumns) => {
      const newColumn = {
        ...value,
        key: Math.random().toString(36).substr(2, 6),
        label: `${intl.formatMessage({ id: "dms.common.tabs.result" })}${labelCounterRef.current + 1
          }`,
      };
      labelCounterRef.current += 1;
      return [...prevDataColumns, newColumn];
    });
  };

  //保存
  const saveFile = () => {
    setTotalButtonDisable({ ...totalButtonDisableRef.current, publishButton: false });
    if (!file.current) return;
    const content = editorRef?.current?.getModel()?.getValue();
    FileService.save({ ...file.current, content } as DMS.File).then((resp) => {
      if (resp.success) {
        message.success(
          intl.formatMessage({ id: "dms.common.message.save.success" })
        );
        initialData();
      }
    });
    unSave(fileId, "Save");
  };

  // 监听键盘事件
  const handleKeyDown = (
    event: KeyboardEvent,
    activeKeyP: string | number | undefined
  ) => {
    if (
      ((event.metaKey && event.key === "s") ||
        (event.ctrlKey && event.key === "s")) &&
      activeKeyP == tabsKey.current
    ) {
      event.preventDefault();
      saveFile();
    }
  };

  const publishFile = () => {
    return Modal.confirm({
      title: intl.formatMessage({
        id: "dms.console.workspace.dataquery.publish.confirm",
      }),
      content:
        intl.formatMessage({
          id: "dms.console.workspace.dataquery.file",
        }) +
        " : " +
        file.current?.fileName,
      onOk: () => {
        FileService.publish(file.current?.id as string).then((resp) => {
          if (resp.success) {
            message.success(
              intl.formatMessage({
                id: "dms.console.workspace.dataquery.publish.success",
              })
            );
            const runningResults = {
              ...totalButtonDisable,
              publishButton: true,
            }
            setTotalButtonDisable(runningResults);
            totalButtonDisableRef.current = runningResults
            initialData();
          }
        });
      },
    });
  };

  // 点击格式化按钮
  const onFormat = () => {
    try {
      const editor = editorRef.current;
      if (!editor || !editor.getModel()) return;
      const model = editor.getModel();
      const selection = editor.getSelection();
      let textToFormat = selection.isEmpty()
        ? model.getValue()
        : model.getValueInRange(selection);
      if (!textToFormat) return;
      const formattedScript = sqlFormatter.format(textToFormat);
      if (selection.isEmpty()) {
        setSqlScript(formattedScript || "");
      } else {
        editor.executeEdits("formatSelection", [
          {
            range: selection,
            text: formattedScript,
            forceMoveMarkers: true,
          },
        ]);
      }
      message.success(
        intl.formatMessage({
          id: "dms.common.message.operate.formatting.success",
        })
      );
    } catch (error) { }
  };

  return (
    <div style={{ height: maxHeight }}>
      <PanelGroup direction="vertical">
        <Panel defaultSize={70} minSize={0}>
          <div style={{ paddingBottom: 6, borderBottom: "1px solid #eee" }}>
            <Space wrap>
              <Button
                size="small"
                type="primary"
                onClick={() => {
                  reConnection();
                }}
                style={{ height: 22, fontSize: 12, marginLeft: 6 }}
                loading={totalButtonDisable.runButton}
              >
                {intl.formatMessage({
                  id: "dms.console.workspace.dataquery.exec",
                })}
              </Button>
              <Button
                size="small"
                type="default"
                danger
                onClick={() => {
                  SqlHistoryService.stop(socket.id as string).then((resp) => {
                    if (resp.success) {
                      message.info(
                        intl.formatMessage({
                          id: "dms.console.workspace.dataquery.stop.success",
                        })
                      );
                      socket.disconnect();
                      const runningResults = {
                        ...totalButtonDisable,
                        stopButton: true,
                        runButton: false,
                      }
                      setTotalButtonDisable(runningResults);
                      totalButtonDisableRef.current = runningResults
                    }
                  });
                }}
                style={{ height: 22, fontSize: 12 }}
                disabled={totalButtonDisable.stopButton}
              >
                {intl.formatMessage({
                  id: "dms.console.workspace.dataquery.stop",
                })}
              </Button>
              <Button
                size="small"
                type="default"
                onClick={() => {
                  saveFile();
                }}
                style={{ height: 22, fontSize: 12 }}
              >
                {intl.formatMessage({
                  id: "dms.console.workspace.dataquery.save",
                })}
              </Button>
              {/* <Button
                size="small"
                type="default"
                onClick={() => {
                  initialData();
                }}
                style={{ height: 22, fontSize: 12 }}
              >
                {intl.formatMessage({
                  id: "dms.console.workspace.dataquery.refresh",
                })}
              </Button> */}
              <Button
                size="small"
                type="default"
                onClick={() => {
                  onFormat();
                }}
                style={{ height: 22, fontSize: 12 }}
              >
                {intl.formatMessage({
                  id: "dms.console.workspace.dataquery.format",
                })}
              </Button>
              {/* <Button
                size="small"
                type="default"
                onClick={() => {
                  publishFile();
                }}
                style={{ height: 22, fontSize: 12 }}
                disabled={totalButtonDisable.publishButton}
              >
                {intl.formatMessage({
                  id: "dms.console.workspace.dataquery.publish",
                })}
              </Button> */}
            </Space>
          </div>

          <Editor
            height={"100%"}
            width={"100%"}
            theme={theme}
            value={sqlScript}
            // beforeMount={(monaco) => {
            //   monaco.languages.registerCompletionItemProvider("sql", {
            //     provideCompletionItems,
            //   });
            // }}
            beforeMount={(monaco) => {
              monaco.languages.registerCompletionItemProvider("sql", suggestionsProvider);
            }}
            defaultLanguage={language}
            onChange={(value) => {
              setSqlScript(value || "");
            }}
            onMount={handleEditorDidMount}
          />
        </Panel>
        <PanelResizeHandle className={classNames("panel_handle_hover")} />
        <Panel defaultSize={30} minSize={0}>
          <DmsGridCallback />
        </Panel>
      </PanelGroup>
    </div>
  );
};
export default CodeEditor;
