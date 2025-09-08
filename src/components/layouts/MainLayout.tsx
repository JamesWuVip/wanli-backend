import React, { useState } from 'react';
// import { useErrorHandler } from '../../hooks/useErrorHandler';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Layout,
  Menu,
  Avatar,
  Dropdown,
  Button,
  Badge,
  Space,
  Breadcrumb,
  theme,
  MenuProps,
} from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  UserOutlined,
  BookOutlined,
  FileTextOutlined,
  TeamOutlined,
  ShopOutlined,
  SettingOutlined,
  LogoutOutlined,
  BellOutlined,
  GlobalOutlined,
  HomeOutlined,
  ReadOutlined,
  SolutionOutlined,
} from '@ant-design/icons';
// import { useAuthStore } from '../../stores/authStore';
// import { usePermission, useRole } from '../../hooks';
import { UserRole } from '../../types/user';

const { Header, Sider, Content } = Layout;

/**
 * 主布局组件
 * 提供应用的主要布局结构，包括侧边栏、头部和内容区域
 */
export const MainLayout: React.FC = () => {
  // const { handleError } = useErrorHandler();
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  // const { user, logout } = useAuthStore();
  // const { hasPermission } = usePermission();
  // const { hasRole } = useRole();
  // const {
  //   token: { colorBgContainer },
  // } = theme.useToken();
  const colorBgContainer = '#ffffff'; // 临时硬编码背景色

  // 模拟用户数据用于测试
  const user = { username: 'test_user', fullName: '测试用户', roles: ['admin'] };
  
  // 模拟权限检查函数
  const hasRole = (roles: string[]) => true; // 暂时返回true以跳过权限检查
  
  // 处理用户登出
  const handleLogout = async () => {
    try {
      // await logout();
      navigate('/auth/login');
    } catch (error) {
      // handleError(error);
      console.error('登出错误:', error);
    }
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
      key: 'settings',
      icon: <SettingOutlined />,
      label: '个人设置',
      onClick: () => navigate('/settings'),
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: <span data-testid="logout-btn">退出登录</span>,
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

    // 系统管理菜单（超级管理员和管理员可见）
    if (hasRole([UserRole.SUPER_ADMIN, UserRole.ADMIN])) {
      items.push({
        key: 'management',
        icon: <SettingOutlined />,
        label: '系统管理',
        children: [
          {
            key: '/management/users',
            icon: <UserOutlined />,
            label: '用户管理',
            onClick: () => navigate('/management/users'),
          },
          {
            key: '/management/courses',
            icon: <BookOutlined />,
            label: '课程管理',
            onClick: () => navigate('/management/courses'),
          },
          {
            key: '/management/assignments',
            icon: <FileTextOutlined />,
            label: '作业管理',
            onClick: () => navigate('/management/assignments'),
          },
          {
            key: '/management/classes',
            icon: <TeamOutlined />,
            label: '班级管理',
            onClick: () => navigate('/management/classes'),
          },
          {
            key: '/management/stores',
            icon: <ShopOutlined />,
            label: '门店管理',
            onClick: () => navigate('/management/stores'),
          },
        ],
      });
    }

    // 总部管理菜单（超级管理员可见）
    if (hasRole([UserRole.SUPER_ADMIN])) {
      items.push({
        key: 'headquarters',
        icon: <GlobalOutlined />,
        label: '总部管理',
        children: [
          {
            key: '/headquarters/overview',
            icon: <DashboardOutlined />,
            label: '总部概览',
            onClick: () => navigate('/headquarters/overview'),
          },
          {
            key: '/headquarters/stores',
            icon: <ShopOutlined />,
            label: '门店监控',
            onClick: () => navigate('/headquarters/stores'),
          },
          {
            key: '/headquarters/settings',
            icon: <SettingOutlined />,
            label: '系统设置',
            onClick: () => navigate('/headquarters/settings'),
          },
        ],
      });
    }

    // 门店管理菜单（门店管理员可见）
    if (hasRole([UserRole.STORE_ADMIN])) {
      items.push({
        key: 'store',
        icon: <ShopOutlined />,
        label: '门店管理',
        children: [
          {
            key: '/store/overview',
            icon: <DashboardOutlined />,
            label: '门店概览',
            onClick: () => navigate('/store/overview'),
          },
          {
            key: '/store/students',
            icon: <UserOutlined />,
            label: '学生管理',
            onClick: () => navigate('/store/students'),
          },
          {
            key: '/store/teachers',
            icon: <SolutionOutlined />,
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
            key: '/store/classes',
            icon: <TeamOutlined />,
            label: '班级管理',
            onClick: () => navigate('/store/classes'),
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

    // 教师工作台菜单（教师可见）
    if (hasRole([UserRole.TEACHER])) {
      items.push({
        key: 'teacher',
        icon: <SolutionOutlined />,
        label: '教师工作台',
        children: [
          {
            key: '/teacher/dashboard',
            icon: <DashboardOutlined />,
            label: '工作台',
            onClick: () => navigate('/teacher/dashboard'),
          },
          {
            key: '/teacher/courses',
            icon: <BookOutlined />,
            label: '我的课程',
            onClick: () => navigate('/teacher/courses'),
          },
          {
            key: '/teacher/classes',
            icon: <TeamOutlined />,
            label: '我的班级',
            onClick: () => navigate('/teacher/classes'),
          },
          {
            key: '/teacher/assignments',
            icon: <FileTextOutlined />,
            label: '作业批改',
            onClick: () => navigate('/teacher/assignments'),
          },
          {
            key: '/teacher/students',
            icon: <UserOutlined />,
            label: '学生管理',
            onClick: () => navigate('/teacher/students'),
          },
        ],
      });
    }

    // 学生学习平台菜单（学生可见）
    if (hasRole([UserRole.STUDENT])) {
      items.push({
        key: 'student',
        icon: <ReadOutlined />,
        label: '学习平台',
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
            icon: <FileTextOutlined />,
            label: '我的作业',
            onClick: () => navigate('/student/assignments'),
          },
          {
            key: '/student/progress',
            icon: <DashboardOutlined />,
            label: '学习进度',
            onClick: () => navigate('/student/progress'),
          },
        ],
      });
    }

    return items;
  };

  // 生成面包屑
  const getBreadcrumbItems = () => {
    const pathSnippets = location.pathname.split('/').filter(i => i);
    const breadcrumbItems = [
      {
        title: (
          <span>
            <HomeOutlined />
            <span style={{ marginLeft: 4 }}>首页</span>
          </span>
        ),
        onClick: () => navigate('/dashboard'),
      },
    ];

    pathSnippets.forEach((snippet, index) => {
      const url = `/${pathSnippets.slice(0, index + 1).join('/')}`;
      const isLast = index === pathSnippets.length - 1;
      
      // 路径名称映射
      const pathNameMap: Record<string, string> = {
        dashboard: '仪表盘',
        management: '系统管理',
        users: '用户管理',
        courses: '课程管理',
        assignments: '作业管理',
        classes: '班级管理',
        stores: '门店管理',
        headquarters: '总部管理',
        overview: '概览',
        settings: '设置',
        store: '门店管理',
        students: '学生管理',
        teachers: '教师管理',
        teacher: '教师工作台',
        student: '学习平台',
        progress: '学习进度',
      };

      breadcrumbItems.push({
        title: pathNameMap[snippet] || snippet,
        ...(isLast ? {} : { onClick: () => navigate(url) }),
      });
    });

    return breadcrumbItems;
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={256}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
        }}
      >
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderBottom: '1px solid #f0f0f0',
            color: '#fff',
            fontSize: '18px',
            fontWeight: 'bold',
          }}
        >
          {collapsed ? '万里' : '万里教育管理系统'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={getMenuItems()}
          style={{ borderRight: 0 }}
        />
      </Sider>
      <Layout style={{ marginLeft: collapsed ? 80 : 256, transition: 'margin-left 0.2s' }}>
        <Header
          style={{
            padding: '0 24px',
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderBottom: '1px solid #f0f0f0',
          }}
        >
          <Space>
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
          </Space>
          <Space>
            <Badge count={5}>
              <Button type="text" icon={<BellOutlined />} />
            </Badge>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                <Avatar icon={<UserOutlined />} />
                <span>{user?.fullName || user?.username}</span>
              </Space>
            </Dropdown>
          </Space>
        </Header>
        <Content
          style={{
            margin: '16px',
            padding: '16px',
            background: colorBgContainer,
            borderRadius: '8px',
            minHeight: 'calc(100vh - 112px)',
          }}
        >
          <Breadcrumb
            items={getBreadcrumbItems()}
            style={{ marginBottom: 16 }}
          />
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;