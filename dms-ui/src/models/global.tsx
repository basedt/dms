import { useEffect, useState, useRef } from 'react';

const useGlobalModel = () => {
    const [tabsList, setTabsList] = useState({ id: '', oldTitle: '', newTitle: '' });//文件里修改名字时tabs编辑进行更新
    const [upDateFile, setUpDateFile] = useState(0);//控制文件列表更新
    const tabsKey = useRef('');//当前编辑器tabs默认激活的状态、
    const tableKey = useRef(1);//库表更新
    const [agGridkey, setAgGridkey] = useState('');//添加ag-grid的Key
    const [menuKey, setMenuKey] = useState('query');//添加ag-grid的Key

    return {
        tabsKey,
        tabsList,
        tableKey,
        agGridkey,
        menuKey,
        upDateFile,
        setTabsList,
        setAgGridkey,
        setUpDateFile,
        setMenuKey,
    };
};

export default useGlobalModel;
