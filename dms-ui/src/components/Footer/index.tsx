import { GithubOutlined } from "@ant-design/icons";
import { Button, Divider, Space } from "antd";
import React from "react";

const Footer: React.FC<{ collapsed: boolean | undefined }> = (props) => {
  const { collapsed } = props;
  const currentYear = new Date().getFullYear();

  return (
    <>
      <Divider type="horizontal" style={{ margin: "12px 0px 0px 0px" }} />
      {collapsed ? (
        <div
          style={{
            textAlign: "center",
            marginTop: "12px",
            marginBottom: "-4px",
          }}
        >
          <a target="_blank">
            <Button type="text" icon={<GithubOutlined />}></Button>
          </a>
        </div>
      ) : (
        <div style={{ textAlign: "center", marginTop: "24px" }}>
          <Space>
            <span>&copy;{currentYear}</span>
            <a href="#">BASEDT.COM</a>
          </Space>
        </div>
      )}
    </>
  );
};

export default Footer;
