import { MenuFoldOutlined, MenuUnfoldOutlined } from "@ant-design/icons";
import { Button, Divider } from "antd";

type CollapsedIconProps = {
  collapsed: boolean | undefined;
  onToggle: () => void;
  type: "inner" | "outer";
};

const CollapsedIcon: React.FC<CollapsedIconProps> = ({
  collapsed,
  onToggle,
  type,
}) => {
  if (type == "inner") {
    return collapsed ? (
      <div
        style={{
          height: 56,
          borderRightStyle: "inset",
          borderRightWidth: 1,
        }}
      >
        <div style={{ marginLeft: 8, marginRight: 8 }}>
          <Divider
            type="horizontal"
            style={{ marginTop: 0, marginBottom: 0, paddingTop: 12 }}
          ></Divider>
          <div style={{ paddingLeft: 6 }}>
            <Button
              type="text"
              icon={<MenuUnfoldOutlined />}
              onClick={() => {
                onToggle();
              }}
            ></Button>
          </div>
        </div>
      </div>
    ) : (
      <div
        style={{
          height: 56,
          borderRightStyle: "inset",
          borderRightWidth: 1,
        }}
      >
        <div style={{ marginLeft: 8, marginRight: 8 }}>
          <Divider
            type="horizontal"
            style={{ marginTop: 0, marginBottom: 0, paddingTop: 12 }}
          ></Divider>
          <div style={{ paddingLeft: 6 }}>
            <Button
              type="text"
              icon={<MenuFoldOutlined />}
              onClick={() => {
                onToggle();
              }}
            ></Button>
          </div>
        </div>
      </div>
    );
  } else {
    return collapsed ? (
      <div style={{ height: 56 }}>
        <Divider
          type="horizontal"
          style={{ marginTop: 0, marginBottom: 0, paddingTop: 12 }}
        ></Divider>
        <div style={{ paddingLeft: 6 }}>
          <Button
            type="text"
            icon={<MenuUnfoldOutlined />}
            onClick={onToggle}
          ></Button>
        </div>
      </div>
    ) : (
      <div style={{ height: 56 }}>
        <Divider
          type="horizontal"
          style={{ marginTop: 0, marginBottom: 0, paddingTop: 12 }}
        ></Divider>
        <div style={{ paddingLeft: 6 }}>
          <Button
            type="text"
            icon={<MenuFoldOutlined />}
            onClick={onToggle}
          ></Button>
        </div>
      </div>
    );
  }
};
export default CollapsedIcon;
