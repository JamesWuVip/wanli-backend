import React from 'react';
import { Outlet } from 'react-router-dom';
import { Layout, Card, Typography, Space } from 'antd';
import { BookOutlined } from '@ant-design/icons';

const { Content } = Layout;
const { Title, Text } = Typography;

/**
 * 认证布局组件
 * 用于登录、注册等认证相关页面的布局
 */
export const AuthLayout: React.FC = () => {
  return (
    <Layout className="min-h-screen">
      <Content className="flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
        <div className="w-full max-w-md px-4">
          {/* Logo 和标题 */}
          <div className="text-center mb-8">
            <Space direction="vertical" size="small">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-full mb-4">
                <BookOutlined className="text-2xl text-white" />
              </div>
              <Title level={2} className="!mb-0 text-gray-800">
                加盟连锁门店作业管理系统
              </Title>
              <Text type="secondary" className="text-base">
                统一管理，高效协作
              </Text>
            </Space>
          </div>

          {/* 认证表单卡片 */}
          <Card
            className="shadow-lg border-0"
            bodyStyle={{
              padding: '32px',
            }}
          >
            <Outlet />
          </Card>

          {/* 底部信息 */}
          <div className="text-center mt-8">
            <Text type="secondary" className="text-sm">
              © 2024 加盟连锁门店作业管理系统. All rights reserved.
            </Text>
          </div>
        </div>
      </Content>
    </Layout>
  );
};