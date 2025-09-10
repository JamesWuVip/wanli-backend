import React, { useState, useEffect } from 'react';
import { Layout, Menu, Dropdown, Avatar, Button, Space, Breadcrumb } from 'antd';
import {
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  UserAddOutlined,
  ShopOutlined,
  BookOutlined,
  TeamOutlined,
  SettingOutlined,
  HomeOutlined,
  ReadOutlined,
  EditOutlined,
  FileTextOutlined,
  BarChartOutlined,
  CalendarOutlined,
  MessageOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import type { MenuProps } from 'antd';

const { Header, Sider, Content } = Layout;

// 模拟用户数据
const mockUser = {
  id: '1',
  username: 'admin',
  email: 'admin@example.com',
  full_name: '系统管理员',
  avatar_url: '',
  roles: [{ role: 'admin', name: 'admin' }]
};

// 模拟权限检查函数
const hasRole = (roles: string[]): boolean => {
  // 临时关闭权限检查，允许所有用户访问所有功能
  // 已关闭权限保护用于测试
  return true;
};

const MainLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    navigate('/auth/login');
  };

  // 用户下拉菜单
  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人资料',
      onClick: () => navigate('/profile'),
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  // 构建菜单项
  const getMenuItems = (): MenuProps['items'] => {
    const items: MenuProps['items'] = [
      {
        key: '/dashboard',
        icon: <DashboardOutlined />,
        label: '仪表盘',
        onClick: () => navigate('/dashboard'),
      },
    ];

    // 系统管理菜单 - 超级管理员、管理员、总部管理员可见
    if (hasRole(['super_admin', 'admin', 'headquarters_admin'])) {
      items.push({
        key: 'system',
        icon: <SettingOutlined />,
        label: '系统管理',
        children: [
          {
            key: '/system/users',
            icon: <UserOutlined />,
            label: '用户管理',
            onClick: () => navigate('/system/users'),
          },
          {
            key: '/system/stores',
            icon: <ShopOutlined />,
            label: '门店管理',
            onClick: () => navigate('/system/stores'),
          },
          {
            key: '/system/courses',
            icon: <BookOutlined />,
            label: '课程管理',
            onClick: () => navigate('/system/courses'),
          },
        ],
      });
    }

    // 总部管理菜单 - 超级管理员可见
    if (hasRole(['super_admin'])) {
      items.push({
        key: 'headquarters',
        icon: <HomeOutlined />,
        label: '总部管理',
        children: [
          {
            key: '/headquarters/overview',
            icon: <BarChartOutlined />,
            label: '总部概览',
            onClick: () => navigate('/headquarters/overview'),
          },
          {
            key: '/headquarters/stores',
            icon: <ShopOutlined />,
            label: '门店管理',
            onClick: () => navigate('/headquarters/stores'),
          },
          {
            key: '/headquarters/teachers',
            icon: <TeamOutlined />,
            label: '教师管理',
            onClick: () => navigate('/headquarters/teachers'),
          },
          {
            key: '/headquarters/courses',
            icon: <BookOutlined />,
            label: '课程管理',
            onClick: () => navigate('/headquarters/courses'),
          },
          {
            key: '/headquarters/assignments',
            icon: <FileTextOutlined />,
            label: '作业管理',
            onClick: () => navigate('/headquarters/assignments'),
          },
        ],
      });
    }

    // 门店管理菜单 - 门店管理员、总部管理员可见
    if (hasRole(['store_admin', 'headquarters_admin'])) {
      items.push({
        key: 'store',
        icon: <ShopOutlined />,
        label: '门店管理',
        children: [
          {
            key: '/store/overview',
            icon: <BarChartOutlined />,
            label: '门店概览',
            onClick: () => navigate('/store/overview'),
          },
          {
            key: '/store/classes',
            icon: <TeamOutlined />,
            label: '班级管理',
            onClick: () => navigate('/store/classes'),
          },
          {
            key: '/store/students',
            icon: <UserAddOutlined />,
            label: '学生管理',
            onClick: () => navigate('/store/students'),
          },
          {
            key: '/store/teachers',
            icon: <TeamOutlined />,
            label: '教师管理',
            onClick: () => navigate('/store/teachers'),
          },
          {
            key: '/store/courses',
            icon: <BookOutlined />,
            label: '课程管理',
            onClick: () => navigate('/store/courses'),
          },
          {
            key: '/store/assignments',
            icon: <FileTextOutlined />,
            label: '作业管理',
            onClick: () => navigate('/store/assignments'),
          },
        ],
      });
    }

    // 教师工作台菜单 - 教师可见
    if (hasRole(['teacher'])) {
      items.push({
        key: 'teacher',
        icon: <TeamOutlined />,
        label: '教师工作台',
        children: [
          {
            key: '/teacher/dashboard',
            icon: <DashboardOutlined />,
            label: '工作台',
            onClick: () => navigate('/teacher/dashboard'),
          },
          {
            key: '/teacher/classes',
            icon: <TeamOutlined />,
            label: '我的班级',
            onClick: () => navigate('/teacher/classes'),
          },
          {
            key: '/teacher/courses',
            icon: <BookOutlined />,
            label: '我的课程',
            onClick: () => navigate('/teacher/courses'),
          },
          {
            key: '/teacher/assignments',
            icon: <FileTextOutlined />,
            label: '作业管理',
            onClick: () => navigate('/teacher/assignments'),
          },
          {
            key: '/teacher/students',
            icon: <UserAddOutlined />,
            label: '学生管理',
            onClick: () => navigate('/teacher/students'),
          },
          {
            key: '/teacher/schedule',
            icon: <CalendarOutlined />,
            label: '课程表',
            onClick: () => navigate('/teacher/schedule'),
          },
        ],
      });
    }

    // 学生学习平台菜单 - 学生可见
    if (hasRole(['student'])) {
      items.push({
        key: 'student',
        icon: <ReadOutlined />,
        label: '学生学习平台',
        children: [
          {
            key: '/student/dashboard',
            icon: <DashboardOutlined />,
            label: '学习中心',
            onClick: () => navigate('/student/dashboard'),
          },
          {
            key: '/student/courses',
            icon: <BookOutlined />,
            label: '我的课程',
            onClick: () => navigate('/student/courses'),
          },
          {
            key: '/student/assignments',
            icon: <EditOutlined />,
            label: '我的作业',
            onClick: () => navigate('/student/assignments'),
          },
          {
            key: '/student/progress',
            icon: <BarChartOutlined />,
            label: '学习进度',
            onClick: () => navigate('/student/progress'),
          },
          {
            key: '/student/schedule',
            icon: <CalendarOutlined />,
            label: '课程表',
            onClick: () => navigate('/student/schedule'),
          },
          {
            key: '/student/messages',
            icon: <MessageOutlined />,
            label: '消息中心',
            onClick: () => navigate('/student/messages'),
          },
        ],
      });
    }

    return items;
  };

  // 生成面包屑
  const generateBreadcrumb = () => {
    const pathSnippets = location.pathname.split('/').filter(i => i);
    const breadcrumbItems = [
      {
        title: <HomeOutlined />,
        href: '/dashboard',
      },
    ];

    pathSnippets.forEach((snippet, index) => {
      const url = `/${pathSnippets.slice(0, index + 1).join('/')}`;
      const isLast = index === pathSnippets.length - 1;
      
      // 简单的路径名映射
      const pathNameMap: Record<string, string> = {
        dashboard: '仪表盘',
        system: '系统管理',
        users: '用户管理',
        stores: '门店管理',
        courses: '课程管理',
        headquarters: '总部管理',
        overview: '概览',
        teachers: '教师管理',
        assignments: '作业管理',
        store: '门店管理',
        classes: '班级管理',
        students: '学生管理',
        teacher: '教师工作台',
        schedule: '课程表',
        student: '学生学习平台',
        progress: '学习进度',
        messages: '消息中心',
      };

      breadcrumbItems.push({
        title: pathNameMap[snippet] || snippet,
        href: isLast ? undefined : url,
      });
    });

    return breadcrumbItems;
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div className="logo" style={{ 
          height: 32, 
          margin: 16, 
          background: 'rgba(255, 255, 255, 0.3)',
          borderRadius: 6,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontWeight: 'bold'
        }}>
          {collapsed ? '万' : '万里作业系统'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={getMenuItems()}
        />
      </Sider>
      <Layout>
        <Header style={{ 
          padding: '0 16px', 
          background: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{
              fontSize: '16px',
              width: 64,
              height: 64,
            }}
          />
          <Space>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                <Avatar icon={<UserOutlined />} />
                <span>{mockUser.full_name}</span>
              </Space>
            </Dropdown>
          </Space>
        </Header>
        <Content style={{ margin: '16px' }}>
          <Breadcrumb items={generateBreadcrumb()} style={{ marginBottom: 16 }} />
          <div style={{
            padding: 24,
            minHeight: 360,
            background: '#fff',
            borderRadius: 6,
          }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;