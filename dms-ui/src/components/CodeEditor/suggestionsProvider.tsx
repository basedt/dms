import * as monaco from "monaco-editor";
import * as monacoApi from "monaco-editor/esm/vs/editor/editor.api";
import { language as sqlLanguage } from "monaco-editor/esm/vs/basic-languages/sql/sql";

// 定义数据库表字段类型
type Field = {
    title: string;
    sign: string;
};

// 定义数据库表模式类型
type TableSchema = {
    title: string;
    sign: string;
};

// 定义数据库表字段
const fields: { [table: string]: Field[] } = {
    table1: [
        { title: "id", sign: "字段" },
        { title: "name", sign: "字段" },
        { title: "email", sign: "字段" }
    ],
    table2: [
        { title: "order_id", sign: "字段" },
        { title: "user_id", sign: "字段" },
        { title: "amount", sign: "字段" }
    ],
    table3: [
        { title: "product_id", sign: "字段" },
        { title: "name", sign: "字段" },
        { title: "price", sign: "字段" }
    ],
    table4: [
        { title: "column1", sign: "字段" },
        { title: "column2", sign: "字段" },
        { title: "column3", sign: "字段" }
    ]
};

// 定义数据库表模式
const dbSchema: TableSchema[] = [
    { title: "table1", sign: "表" },
    { title: "table2", sign: "表" },
    { title: "table3", sign: "表" },
    { title: "table4", sign: "表" }
];

// 获取指针前的文本
function getTextBeforePointer(
    model: monaco.editor.ITextModel,
    position: monaco.Position,
    startLineNumber: number
): string {
    return model.getValueInRange({
        startLineNumber,
        startColumn: 1,
        endLineNumber: position.lineNumber,
        endColumn: position.column
    });
}

// 获取指针后的文本
function getTextAfterPointer(
    model: monaco.editor.ITextModel,
    position: monaco.Position
): string {
    const totalLines = model.getLineCount();
    return model.getValueInRange({
        startLineNumber: position.lineNumber,
        startColumn: position.column,
        endLineNumber: totalLines,
        endColumn: model.getLineMaxColumn(totalLines)
    });
}

// 获取表建议
function getTableSuggest(): monaco.languages.CompletionItem[] {
    return dbSchema.map(table => ({
        label: table.title,
        kind: monaco.languages.CompletionItemKind.Class,
        documentation: `Table: ${table.title}`,
        insertText: table.title,
        detail: table.sign,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
    }));
}

// 获取关键字建议
function getKeywordSuggest(): monaco.languages.CompletionItem[] {
    const keywordSuggestions = sqlLanguage.keywords.map(key => ({
        label: key,
        kind: monaco.languages.CompletionItemKind.Keyword,
        insertText: key,
        detail: 'Keyword'
    }));

    const operatorSuggestions = sqlLanguage.operators.map(key => ({
        label: key,
        kind: monaco.languages.CompletionItemKind.Operator,
        insertText: key,
        detail: 'Operator'
    }));

    const functionSuggestions = sqlLanguage.builtinFunctions.map(key => ({
        label: key,
        kind: monaco.languages.CompletionItemKind.Function,
        insertText: key,
        detail: 'Function'
    }));

    return [
        ...keywordSuggestions,
        ...operatorSuggestions,
        ...functionSuggestions
    ];
}


// 获取表列建议
async function getTableColumnSuggest(item: string): Promise<monaco.languages.CompletionItem[]> {
    const fromMatch = item.match(/FROM\s+(\S+)/i);
    if (fromMatch) {
        const tableName = fromMatch[1].trim().replaceAll(";", "");
        return getColumnsByTableName(tableName);
    }
    return [];
}

// 根据表名获取列建议
function getColumnsByTableName(tableName: string): monaco.languages.CompletionItem[] {
    return fields[tableName]?.map(field => ({
        label: field.title,
        kind: monaco.languages.CompletionItemKind.Field,
        documentation: `Field: ${field.title} in table ${tableName}`,
        detail: field.sign,
        insertText: field.title,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
    })) || [];
}

// 判断是否是特定关键字
function isSpecificKeyword(token: string, keywordList: string[]): boolean {
    return keywordList.includes(token);
}

// 获取表名和表别名
function getTableNameAndTableAlia(
    textBefore: string,
    textAfter: string
): { [key: string]: string } {
    const combinedText = textBefore + textAfter;
    const match = combinedText.match(/FROM\s+(\w+)\s+AS\s+(\w+)/i);
    if (match) {
        return { [match[2]]: match[1] };
    }
    return {};
}

// 根据表别名获取表列建议
function getTableColumnSuggestByTableAlia(tableName: string): monaco.languages.CompletionItem[] {
    return fields[tableName]?.map(field => ({
        label: field.title,
        kind: monaco.languages.CompletionItemKind.Field,
        documentation: `Field: ${field.title} in table ${tableName}`,
        detail: field.sign,
        insertText: field.title,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
    })) || [];
}

// 检查是否需要获取表列建议
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
            "on"
        ]) ||
        (lastToken.endsWith(".") &&
            !dbSchema.some(schema => schema.title === lastToken.slice(0, -1))) ||
        /(select|where|order by|group by|by|and|or|having|distinct|on)\s+.*?\s?,\s*$/.test(
            textBeforePointer.toLowerCase()
        )
    );
}

// 建议提供器
const suggestionsProvider: monaco.languages.CompletionItemProvider = {
    provideCompletionItems: async (
        model: monaco.editor.ITextModel,
        position: monaco.Position
    ): Promise<monaco.languages.CompletionList> => {
        const { lineNumber, column } = position;
        const textBeforePointer = getTextBeforePointer(model, position, lineNumber);
        const textBeforePointerMulti = getTextBeforePointer(model, position, 1);
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
                newSuggestions = getTableSuggest();
            } else if (lastToken.endsWith(".")) {
                const tokenNoDot = lastToken.slice(0, -1);
                const tableAlia = getTableNameAndTableAlia(
                    textBeforePointerMulti,
                    textAfterPointerMulti
                );
                if (tableAlia && tableAlia[tokenNoDot]) {
                    newSuggestions = getTableColumnSuggestByTableAlia(tableAlia[tokenNoDot]);
                } else if (
                    dbSchema.some(schema => schema.title === tokenNoDot)
                ) {
                    newSuggestions = getColumnsByTableName(tokenNoDot);
                }
            } else if (shouldGetTableColumnSuggest(lastToken, textBeforePointer)) {
                newSuggestions = await getTableColumnSuggest(textAfterPointerMulti);
            } else {
                newSuggestions = [...getTableSuggest(), ...getKeywordSuggest()];
            }
        } catch (error) {
            console.error("Error generating suggestions:", error);
        }

        return {
            suggestions: newSuggestions.map(suggestion => ({
                ...suggestion
            }))
        };
    },
    triggerCharacters: [" ", ".", ","]
};

export default suggestionsProvider;