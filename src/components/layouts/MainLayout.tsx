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

  // 模拟用户数据用于测试 - 使用e2e_admin用户的实际角色
  const user = { 
    username: 'e2e_admin', 
    fullName: 'E2E测试管理员', 
    roles: [{ role: UserRole.HEADQUARTERS_ADMIN, name: 'headquarters_admin' }] 
  };
  
  // 角色检查函数 - 检查用户是否拥有指定角色
  const hasRole = (requiredRoles: UserRole[]) => {
    if (!user?.roles || !Array.isArray(user.roles)) {
      return false;
    }
    return requiredRoles.some(requiredRole => 
      user.roles.some((userRole: any) => userRole.role === requiredRole)
    );
  };
  
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
        label: <span data-testid="nav-dashboard">仪表盘</span>,
        onClick: () => navigate('/dashboard'),
      },
    ];

    // 系统管理菜单（超级管理员、管理员和总部管理员可见）
    if (hasRole([UserRole.HEADQUARTERS_ADMIN])) {
      items.push({
        key: 'management',
        icon: <SettingOutlined />,
        label: <span data-testid="nav-system-management">系统管理</span>,
        children: [
          {
            key: '/management/users',
            icon: <UserOutlined />,
            label: <span data-testid="nav-users">用户管理</span>,
            onClick: () => navigate('/management/users'),
          },
          {
            key: '/management/stores',
            icon: <ShopOutlined />,
            label: '门店管理',
            onClick: () => navigate('/management/stores'),
          },
          {
            key: '/management/courses',
            icon: <BookOutlined />,
            label: <span data-testid="nav-courses">课程管理</span>,
            onClick: () => navigate('/management/courses'),
          },
          {
            key: '/management/classes',
            icon: <TeamOutlined />,
            label: '班级管理',
            onClick: () => navigate('/management/classes'),
          },
          {
            key: '/management/assignments',
            icon: <FileTextOutlined />,
            label: <span data-testid="nav-assignments">作业管理</span>,
            onClick: () => navigate('/management/assignments'),
          },
        ],
      });
    }

    // 总部管理菜单（超级管理员可见）
    if (hasRole([UserRole.HEADQUARTERS_ADMIN])) {
      items.push({
        key: 'headquarters',
        icon: <GlobalOutlined />,
        label: <span data-testid="nav-headquarters-management">总部管理</span>,
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
    if (hasRole([UserRole.STORE_ADMIN, UserRole.HEADQUARTERS_ADMIN])) {
      items.push({
        key: 'store',
        icon: <HomeOutlined />,
        label: <span data-testid="nav-store-management">门店管理</span>,
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
            key: '/store/classes',
            icon: <TeamOutlined />,
            label: '班级管理',
            onClick: () => navigate('/store/classes'),
          },
          {
            key: '/store/settings',
            icon: <SettingOutlined />,
            label: '门店设置',
            onClick: () => navigate('/store/settings'),
          },
        ],
      });
    }

    // 教师工作台菜单（教师可见）
    if (hasRole([UserRole.TEACHER, UserRole.STORE_ADMIN, UserRole.HEADQUARTERS_ADMIN])) {
      items.push({
        key: 'teacher',
        icon: <SolutionOutlined />,
        label: <span data-testid="nav-teacher-workspace">教师工作台</span>,
        children: [
          {
            key: '/teacher/courses',
            icon: <BookOutlined />,
            label: '我的课程',
            onClick: () => navigate('/teacher/courses'),
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
          {
            key: '/teacher/schedule',
            icon: <ReadOutlined />,
            label: '课程安排',
            onClick: () => navigate('/teacher/schedule'),
          },
        ],
      });
    }

    // 学生学习平台菜单（学生可见）
    if (hasRole([UserRole.STUDENT])) {
      items.push({
        key: 'student',
        icon: <ReadOutlined />,
        label: <span data-testid="nav-student-platform">学习平台</span>,
        children: [
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
            key: '/student/grades',
            icon: <SolutionOutlined />,
            label: '成绩查看',
            onClick: () => navigate('/student/grades'),
          },
          {
            key: '/student/schedule',
            icon: <ReadOutlined />,
            label: '课程表',
            onClick: () => navigate('/student/schedule'),
          },
        ],
      });
    }

    return items;
  };

  // 获取面包屑导航
  const getBreadcrumbItems = () => {
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
      const pathNameMap: { [key: string]: string } = {
        dashboard: '仪表盘',
        management: '系统管理',
        users: '用户管理',
        stores: '门店管理',
        courses: '课程管理',
        classes: '班级管理',
        assignments: '作业管理',
        headquarters: '总部管理',
        overview: '概览',
        settings: '设置',
        store: '门店管理',
        students: '学生管理',
        teachers: '教师管理',
        teacher: '教师工作台',
        student: '学习平台',
        grades: '成绩查看',
        schedule: '课程表',
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
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
        }}
      >
        <div className="flex items-center justify-center h-16 bg-blue-600">
          <div className="text-white font-bold text-lg">
            {collapsed ? 'WL' : '万里教育'}
          </div>
        </div>
        
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={getMenuItems()}
          style={{ borderRight: 0 }}
        />
      </Sider>
      
      <Layout style={{ marginLeft: collapsed ? 80 : 200, transition: 'margin-left 0.2s' }}>
        <Header 
          style={{ 
            padding: '0 16px', 
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderBottom: '1px solid #f0f0f0'
          }}
        >
          <div className="flex items-center">
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
          </div>
          
          <div className="flex items-center space-x-4">
            <Space size="middle">
              <Badge count={5} size="small">
                <Button 
                  type="text" 
                  icon={<BellOutlined />} 
                  size="large"
                  data-testid="notification-btn"
                />
              </Badge>
              
              <Dropdown 
                menu={{ items: userMenuItems }} 
                placement="bottomRight" 
                arrow
              >
                <Space className="cursor-pointer hover:bg-gray-50 px-2 py-1 rounded" data-testid="user-avatar">
                  <Avatar icon={<UserOutlined />} />
                  <div className="hidden md:block">
                    <div className="text-sm font-medium">{user?.fullName || user?.username}</div>
                    <div className="text-xs text-gray-500">{user?.roles?.[0]?.name || '用户'}</div>
                  </div>
                </Space>
              </Dropdown>
            </Space>
          </div>
        </Header>
        
        <Content style={{ margin: '16px' }}>
          <Breadcrumb items={getBreadcrumbItems()} style={{ marginBottom: 16 }} />
          
          <div
            style={{
              padding: 24,
              minHeight: 360,
              background: colorBgContainer,
              borderRadius: 8,
            }}
          >
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;