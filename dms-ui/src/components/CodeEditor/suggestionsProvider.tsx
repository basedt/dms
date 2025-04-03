import * as monaco from "monaco-editor";
import { idbAPI } from "@/idb";
import { language as sqlLanguage } from "monaco-editor/esm/vs/basic-languages/sql/sql";

function getTextBeforePointer(
  model: monaco.editor.ITextModel,
  position: monaco.Position,
  startLineNumber: number
): string {
  return model.getValueInRange({
    startLineNumber,
    startColumn: 1,
    endLineNumber: position.lineNumber,
    endColumn: position.column,
  });
}

function getTextAfterPointer(
  model: monaco.editor.ITextModel,
  position: monaco.Position
): string {
  const totalLines = model.getLineCount();
  return model.getValueInRange({
    startLineNumber: position.lineNumber,
    startColumn: position.column,
    endLineNumber: totalLines,
    endColumn: model.getLineMaxColumn(totalLines),
  });
}

async function getTableSuggest(
  dataSourceId: any
): Promise<monaco.languages.CompletionItem[]> {
  try {
    // 使用 await 等待异步操作完成
    const item = await idbAPI.getTablesByName(dataSourceId, "");
    if (item) {
      // 映射数据为 monaco 所需的 CompletionItem 数组
      return item.map((table) => ({
        label: table.tableName,
        kind: monaco.languages.CompletionItemKind.Class,
        documentation: `Table: ${table.tableName}`,
        insertText: table.tableName,
        detail: table.identifier,
        insertTextRules:
          monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
      })) as monaco.languages.CompletionItem[];
    }
    return [];
  } catch (error) {
    console.error("Error fetching table suggestions:", error);
    return [];
  }
}

function getKeywordSuggest(): monaco.languages.CompletionItem[] {
  const keywordSuggestions = sqlLanguage.keywords.map((key: string) => ({
    label: key,
    kind: monaco.languages.CompletionItemKind.Keyword,
    insertText: key,
    detail: "Keyword",
  }));

  const operatorSuggestions = sqlLanguage.operators.map((key: string) => ({
    label: key,
    kind: monaco.languages.CompletionItemKind.Operator,
    insertText: key,
    detail: "Operator",
  }));

  const functionSuggestions = sqlLanguage.builtinFunctions.map(
    (key: string) => ({
      label: key,
      kind: monaco.languages.CompletionItemKind.Function,
      insertText: key,
      detail: "Function",
    })
  );

  return [
    ...keywordSuggestions,
    ...operatorSuggestions,
    ...functionSuggestions,
  ];
}

async function getColumnsByTableName(tableName: string, dataSourceId: string) {
  try {
    const item = await idbAPI.getTablesByName(dataSourceId, tableName);
    if (item) {
      return item
        ?.map((field) => {
          return field?.columns?.map((sitem: string) => ({
            label: sitem,
            kind: monaco.languages.CompletionItemKind.Field,
            documentation: `Field: ${sitem} in table ${tableName}`,
            detail: field.sign,
            insertText: sitem,
            insertTextRules:
              monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          }));
        })
        .flat(Infinity);
    }
    return [];
  } catch (error) {
    console.error("Error fetching table suggestions:", error);
    return [];
  }
}

async function getTableColumnSuggest(
  item: string,
  dataSourceId: string
): Promise<monaco.languages.CompletionItem[]> {
  const fromMatch = item.match(/FROM\s+(\S+)/i);
  if (fromMatch) {
    const tableName = fromMatch[1].trim().replaceAll(";", "");
    const columns = await getColumnsByTableName(tableName, dataSourceId);
    return columns;
  }
  return [];
}

function isSpecificKeyword(token: string, keywordList: string[]): boolean {
  return keywordList.includes(token);
}

function shouldGetTableColumnSuggest(
  lastToken: string,
  textBeforePointer: string
): boolean {
  return (
    isSpecificKeyword(lastToken, [
      "select",
      "where",
      "order by",
      "group by",
      "by",
      "and",
      "or",
      "having",
      "distinct",
      "on",
    ]) ||
    lastToken.endsWith(".") ||
    /(select|where|order by|group by|by|and|or|having|distinct|on)\s+.*?\s?,\s*$/.test(
      textBeforePointer.toLowerCase()
    )
  );
}

const suggestionsProvider = (
  dataSourceId: string
): monaco.languages.CompletionItemProvider => {
  return {
    provideCompletionItems: async (
      model: monaco.editor.ITextModel,
      position: monaco.Position
    ): Promise<monaco.languages.CompletionList> => {
      const { lineNumber } = position;
      const textBeforePointer = getTextBeforePointer(
        model,
        position,
        lineNumber
      );
      const textAfterPointerMulti = getTextAfterPointer(model, position);

      const tokens = textBeforePointer.trim().split(/\s+/);
      const lastToken = tokens[tokens.length - 1].toLowerCase();

      let newSuggestions: monaco.languages.CompletionItem[] = [];
      try {
        if (
          lastToken === "from" ||
          lastToken === "join" ||
          /(from|join)\s+.*?\s?,\s*$/.test(
            textBeforePointer.replace(/.*?\(/gm, "").toLowerCase()
          )
        ) {
          const suggestions = await getTableSuggest(dataSourceId);
          newSuggestions = suggestions;
        } else if (shouldGetTableColumnSuggest(lastToken, textBeforePointer)) {
          newSuggestions = await getTableColumnSuggest(
            textAfterPointerMulti,
            dataSourceId
          );
        } else {
          const suggestions = await getTableSuggest(dataSourceId);
          newSuggestions = [...suggestions, ...getKeywordSuggest()];
        }
      } catch (error) {
        console.error("Error generating suggestions:", error);
      }
      return {
        suggestions: newSuggestions.map((suggestion) => ({
          ...suggestion,
        })),
      };
    },
    triggerCharacters: [" ", ".", ","],
  };
};

export default suggestionsProvider;
