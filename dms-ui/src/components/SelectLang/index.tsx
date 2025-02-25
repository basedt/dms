import { TranslationOutlined } from "@ant-design/icons";
import { setLocale } from "@umijs/max";
import { Dropdown, MenuProps, Space } from "antd";

const SelectLang: React.FC<{ className?: string }> = (props) => {
  const { className } = props;

  const items: MenuProps["items"] = [
    {
      key: "zh-CN",
      label: (
        <Space
          onClick={() => {
            changeLang("zh-CN");
          }}
        >
          <span>🇨🇳</span>
          <a>简体中文</a>
        </Space>
      ),
    },
    {
      key: "en-US",
      label: (
        <Space
          onClick={() => {
            changeLang("en-US");
          }}
        >
          <span>🇺🇸</span>
          <a>English</a>
        </Space>
      ),
    },
  ];

  const changeLang = (lang: string): void => {
    setLocale(lang, true);
  };

  return (
    <span className={className}>
      <Dropdown
        menu={{ items }}
        placement="bottomLeft"
        arrow={{ pointAtCenter: true }}
      >
        <TranslationOutlined />
      </Dropdown>
    </span>
  );
};

export default SelectLang;
