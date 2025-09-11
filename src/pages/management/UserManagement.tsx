import React, { useState, useEffect } from 'react';
import { useErrorHandler } from '../../hooks/useErrorHandler';
import {
  Table,
  Card,
  Button,
  Space,
  Input,
  Select,
  Tag,
  Modal,
  message,
  Popconfirm,
  Tooltip,
  Dropdown,
  Avatar,
  Typography,
  Row,
  Col,
  Statistic,
  Badge,
} from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  MoreOutlined,
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  ExportOutlined,
  KeyOutlined,
  TeamOutlined,
  ShopOutlined,
} from '@ant-design/icons';
import { User, UserStatus, UserRole, CreateUserRequest, UpdateUserRequest, UserStats } from '../../types';
import { userService } from '../../services';
import { usePermission } from '../../hooks';
import { PermissionGuard } from '../../components/common';
import { UserForm } from '../../components/forms';
import { UserDetail } from '../../components/user';

const { Search } = Input;
const { Option } = Select;
const { Text } = Typography;

interface UserQueryParams {
  page: number;
  pageSize: number;
  keyword?: string;
  status?: UserStatus;
  role?: UserRole;
  storeId?: string;
}

interface UserManagementState {
  users: User[];
  loading: boolean;
  stats: UserStats;
  selectedRowKeys: React.Key[];
}

export const UserManagement: React.FC = () => {
  const { handleError } = useErrorHandler();
  const { hasPermission, hasAnyPermission } = usePermission();
  
  // 状态管理
  const [state, setState] = useState<UserManagementState>({
    users: [],
    loading: false,
    stats: {
      total: 0,
      active: 0,
      inactive: 0,
      suspended: 0,
    },
    selectedRowKeys: [],
  });
  
  const [queryParams, setQueryParams] = useState<UserQueryParams>({
    page: 1,
    pageSize: 20,
  });
  
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
  });
  
  // 弹窗状态
  const [isFormModalVisible, setIsFormModalVisible] = useState(false);
  const [isDetailModalVisible, setIsDetailModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [viewingUser, setViewingUser] = useState<User | null>(null);
  
  const { users, loading, stats, selectedRowKeys } = state;
  
  // 权限检查
  const canManageUsers = hasPermission('user:manage');
  const canCreateUsers = hasPermission('user:create');
  const canEditUsers = hasPermission('user:edit');
  const canDeleteUsers = hasPermission('user:delete');
  
  // 加载用户列表
  const loadUsers = async (params: Partial<UserQueryParams> = {}) => {
    setState(prev => ({ ...prev, loading: true }));
    
    try {
      const mergedParams = { ...queryParams, ...params };
      const response = await userService.getUsers(mergedParams);
      
      setState(prev => ({
        ...prev,
        users: response.data,
        loading: false,
      }));
      
      setPagination(prev => ({
        ...prev,
        current: mergedParams.page,
        pageSize: mergedParams.pageSize,
        total: response.total,
      }));
      
      setQueryParams(mergedParams);
    } catch (error) {
      setState(prev => ({ ...prev, loading: false }));
      handleError(error, '加载用户列表失败');
    }
  };
  
  // 加载统计数据
  const loadStats = async () => {
    try {
      const stats = await userService.getUserStats();
      setState(prev => ({ ...prev, stats }));
    } catch (error) {
      handleError(error, '加载统计数据失败');
    }
  };
  
  // 初始化加载
  useEffect(() => {
    loadUsers();
    loadStats();
  }, []);
  
  // 表格变化处理
  const handleTableChange = (
    pagination: TablePaginationConfig,
    filters: any,
    sorter: any
  ) => {
    loadUsers({
      page: pagination.current || 1,
      pageSize: pagination.pageSize || 20,
    });
  };
  
  // 搜索处理
  const handleSearch = (keyword: string) => {
    loadUsers({ ...queryParams, keyword, page: 1 });
  };
  
  // 状态筛选
  const handleStatusFilter = (status?: UserStatus) => {
    loadUsers({ ...queryParams, status, page: 1 });
  };
  
  // 角色筛选
  const handleRoleFilter = (role?: UserRole) => {
    loadUsers({ ...queryParams, role, page: 1 });
  };
  
  // 获取状态颜色
  const getStatusColor = (status: UserStatus): string => {
    const colorMap = {
      [UserStatus.ACTIVE]: 'success',
      [UserStatus.INACTIVE]: 'default',
      [UserStatus.SUSPENDED]: 'warning',
    };
    return colorMap[status] || 'default';
  };
  
  // 获取状态文本
  const getStatusText = (status: UserStatus): string => {
    const textMap = {
      [UserStatus.ACTIVE]: '正常',
      [UserStatus.INACTIVE]: '未激活',
      [UserStatus.SUSPENDED]: '已暂停',
    };
    return textMap[status] || '未知';
  };
  
  // 获取角色文本
  const getRoleText = (role: UserRole): string => {
    const textMap = {
      [UserRole.SUPER_ADMIN]: '超级管理员',
      [UserRole.HQ_ADMIN]: '总部管理员',
      [UserRole.STORE_MANAGER]: '门店经理',
      [UserRole.TEACHER]: '教师',
      [UserRole.STUDENT]: '学生',
    };
    return textMap[role] || '未知';
  };
  
  // 用户操作
  const handleCreateUser = () => {
    setEditingUser(null);
    setIsFormModalVisible(true);
  };
  
  const handleEditUser = (user: User) => {
    setEditingUser(user);
    setIsFormModalVisible(true);
  };
  
  const handleViewUser = (user: User) => {
    setViewingUser(user);
    setIsDetailModalVisible(true);
  };
  
  const handleActivateUser = async (userId: string) => {
    try {
      await userService.activateUser(userId);
      message.success('用户激活成功');
      loadUsers();
    } catch (error) {
      handleError(error, '用户激活失败');
    }
  };
  
  const handleDeactivateUser = async (userId: string) => {
    try {
      await userService.deactivateUser(userId);
      message.success('用户停用成功');
      loadUsers();
    } catch (error) {
      handleError(error, '用户停用失败');
    }
  };
  
  const handleSuspendUser = async (userId: string) => {
    try {
      await userService.suspendUser(userId);
      message.success('用户暂停成功');
      loadUsers();
    } catch (error) {
      handleError(error, '用户暂停失败');
    }
  };
  
  const handleResetPassword = async (userId: string) => {
    try {
      await userService.resetPassword(userId);
      message.success('密码重置成功');
    } catch (error) {
      handleError(error, '密码重置失败');
    }
  };
  
  const handleDeleteUser = async (userId: string) => {
    try {
      await userService.deleteUser(userId);
      message.success('用户删除成功');
      loadUsers();
      loadStats();
    } catch (error) {
      handleError(error, '用户删除失败');
    }
  };
  
  const handleBatchUpdate = async (action: string, userIds: string[]) => {
    try {
      await userService.batchUpdateUsers(action, userIds);
      message.success(`批量${action}成功`);
      setState(prev => ({ ...prev, selectedRowKeys: [] }));
      loadUsers();
      loadStats();
    } catch (error) {
      handleError(error, `批量${action}失败`);
    }
  };
  
  const handleExport = async () => {
    try {
      await userService.exportUsers(queryParams);
      message.success('导出成功');
    } catch (error) {
      handleError(error, '导出失败');
    }
  };
  
  const handleFormSubmit = async (userData: CreateUserRequest | UpdateUserRequest) => {
    try {
      if (editingUser) {
        await userService.updateUser(editingUser.id, userData as UpdateUserRequest);
        message.success('用户更新成功');
      } else {
        await userService.createUser(userData as CreateUserRequest);
        message.success('用户创建成功');
      }
      
      setIsFormModalVisible(false);
      setEditingUser(null);
      loadUsers();
      loadStats();
    } catch (error) {
      handleError(error, editingUser ? '用户更新失败' : '用户创建失败');
    }
  };
  
  // 操作菜单
  const getActionMenu = (user: User) => {
    const items = [
      {
        key: 'view',
        icon: <EyeOutlined />,
        label: '查看详情',
        onClick: () => handleViewUser(user),
      },
    ];
    
    if (canEditUsers) {
      items.push({
        key: 'edit',
        icon: <EditOutlined />,
        label: '编辑',
        onClick: () => handleEditUser(user),
      });
    }
    
    if (canManageUsers) {
      if (user.status === UserStatus.ACTIVE) {
        items.push(
          {
            key: 'suspend',
            icon: <PauseCircleOutlined />,
            label: '暂停',
            onClick: () => handleSuspendUser(user.id),
          },
          {
            key: 'deactivate',
            icon: <StopOutlined />,
            label: '停用',
            onClick: () => handleDeactivateUser(user.id),
          }
        );
      } else {
        items.push({
          key: 'activate',
          icon: <PlayCircleOutlined />,
          label: '激活',
          onClick: () => handleActivateUser(user.id),
        });
      }
      
      items.push(
        {
          key: 'reset-password',
          icon: <KeyOutlined />,
          label: '重置密码',
          onClick: () => handleResetPassword(user.id),
        },
        {
          type: 'divider',
        },
        {
          key: 'delete',
          icon: <DeleteOutlined />,
          label: '删除',
          danger: true,
          onClick: () => handleDeleteUser(user.id),
        }
      );
    }
    
    return { items };
  };
  
  // 表格列定义
  const columns: ColumnsType<User> = [
    {
      title: '用户信息',
      key: 'userInfo',
      width: 200,
      render: (_, user) => (
        <Space>
          <Avatar
            size={40}
            src={user.avatar}
            icon={<UserOutlined />}
          />
          <div>
            <div>
              <Text strong>{user.full_name}</Text>
              {user.role === UserRole.SUPER_ADMIN && (
                <Tag color="red" size="small" style={{ marginLeft: 4 }}>超管</Tag>
              )}
            </div>
            <Text type="secondary" style={{ fontSize: 12 }}>
              @{user.username}
            </Text>
          </div>
        </Space>
      ),
    },
    {
      title: '联系方式',
      key: 'contact',
      width: 180,
      render: (_, user) => (
        <div>
          <div style={{ marginBottom: 4 }}>
            <MailOutlined style={{ marginRight: 4, color: '#1890ff' }} />
            <Text style={{ fontSize: 12 }}>{user.email}</Text>
          </div>
          {user.phone && (
            <div>
              <PhoneOutlined style={{ marginRight: 4, color: '#52c41a' }} />
              <Text style={{ fontSize: 12 }}>{user.phone}</Text>
            </div>
          )}
        </div>
      ),
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      width: 120,
      render: (role: UserRole) => (
        <Tag color="blue">{getRoleText(role)}</Tag>
      ),
    },
    {
      title: '所属门店',
      key: 'store',
      width: 120,
      render: (_, user) => (
        user.storeId ? (
          <Space>
            <ShopOutlined />
            <Text>{user.storeName || user.storeId}</Text>
          </Space>
        ) : (
          <Text type="secondary">-</Text>
        )
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: UserStatus) => (
        <Tag color={getStatusColor(status)}>
          {getStatusText(status)}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 120,
      render: (date: string) => (
        <Text style={{ fontSize: 12 }}>
          {new Date(date).toLocaleDateString()}
        </Text>
      ),
    },
    {
      title: '最后登录',
      dataIndex: 'lastLoginAt',
      key: 'lastLoginAt',
      width: 120,
      render: (date?: string) => (
        <Text style={{ fontSize: 12 }}>
          {date ? new Date(date).toLocaleDateString() : '从未登录'}
        </Text>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      width: 80,
      fixed: 'right',
      render: (_, user) => (
        <Dropdown menu={getActionMenu(user)} trigger={['click']}>
          <Button type="text" icon={<MoreOutlined />} />
        </Dropdown>
      ),
    },
  ];
  
  // 行选择配置
  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => {
      setState(prev => ({ ...prev, selectedRowKeys: keys }));
    },
  };
  
  // 批量操作菜单
  const batchActionMenu = {
    items: [
      {
        key: 'activate',
        label: '批量激活',
        icon: <PlayCircleOutlined />,
        onClick: () => handleBatchUpdate('激活', selectedRowKeys as string[]),
      },
      {
        key: 'deactivate',
        label: '批量停用',
        icon: <StopOutlined />,
        onClick: () => handleBatchUpdate('停用', selectedRowKeys as string[]),
      },
      {
        key: 'suspend',
        label: '批量暂停',
        icon: <PauseCircleOutlined />,
        onClick: () => handleBatchUpdate('暂停', selectedRowKeys as string[]),
      },
      {
        type: 'divider',
      },
      {
        key: 'delete',
        label: '批量删除',
        icon: <DeleteOutlined />,
        danger: true,
        onClick: () => {
          Modal.confirm({
            title: '确认删除',
            content: `确定要删除选中的 ${selectedRowKeys.length} 个用户吗？`,
            onOk: () => handleBatchUpdate('删除', selectedRowKeys as string[]),
          });
        },
      },
    ],
  };
  
  return (
    <div className="user-management">
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总用户数"
              value={stats.total}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="正常用户"
              value={stats.active}
              prefix={<Badge status="success" />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="未激活用户"
              value={stats.inactive}
              prefix={<Badge status="default" />}
              valueStyle={{ color: '#8c8c8c' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="暂停用户"
              value={stats.suspended}
              prefix={<Badge status="warning" />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>
      
      {/* 主要内容 */}
      <Card>
        {/* 搜索和筛选 */}
        <div style={{ marginBottom: 16 }}>
          <Row gutter={16}>
            <Col span={8}>
              <Search
                placeholder="搜索用户名、邮箱、姓名"
                allowClear
                onSearch={handleSearch}
                style={{ width: '100%' }}
              />
            </Col>
            <Col span={4}>
              <Select
                placeholder="状态筛选"
                allowClear
                style={{ width: '100%' }}
                onChange={handleStatusFilter}
              >
                <Option value={UserStatus.ACTIVE}>正常</Option>
                <Option value={UserStatus.INACTIVE}>未激活</Option>
                <Option value={UserStatus.SUSPENDED}>已暂停</Option>
              </Select>
            </Col>
            <Col span={4}>
              <Select
                placeholder="角色筛选"
                allowClear
                style={{ width: '100%' }}
                onChange={handleRoleFilter}
              >
                <Option value={UserRole.SUPER_ADMIN}>超级管理员</Option>
                <Option value={UserRole.HQ_ADMIN}>总部管理员</Option>
                <Option value={UserRole.STORE_MANAGER}>门店经理</Option>
                <Option value={UserRole.TEACHER}>教师</Option>
                <Option value={UserRole.STUDENT}>学生</Option>
              </Select>
            </Col>
          </Row>
        </div>
        
        {/* 操作按钮 */}
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
          <Space>
            {selectedRowKeys.length > 0 && (
              <PermissionGuard requiredPermissions={['user:manage']} showFallback={false}>
                <Dropdown menu={batchActionMenu} trigger={['click']}>
                  <Button>
                    批量操作 ({selectedRowKeys.length})
                  </Button>
                </Dropdown>
              </PermissionGuard>
            )}
            
            <Button icon={<ExportOutlined />} onClick={handleExport}>
              导出
            </Button>
            
            <PermissionGuard requiredPermissions={['user:create']} showFallback={false}>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateUser}>
                新建用户
              </Button>
            </PermissionGuard>
          </Space>
        </div>
        
        {/* 用户表格 */}
        <Table<User>
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={loading}
          rowSelection={canManageUsers ? rowSelection : undefined}
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ x: 1200 }}
        />
      </Card>
      
      {/* 用户表单弹窗 */}
      <Modal
        title={editingUser ? '编辑用户' : '新建用户'}
        open={isFormModalVisible}
        onCancel={() => {
          setIsFormModalVisible(false);
          setEditingUser(null);
        }}
        footer={null}
        width={600}
        destroyOnHidden
      >
        <UserForm
          user={editingUser}
          onSubmit={handleFormSubmit}
          onCancel={() => {
            setIsFormModalVisible(false);
            setEditingUser(null);
          }}
        />
      </Modal>
      
      {/* 用户详情弹窗 */}
      <Modal
        title="用户详情"
        open={isDetailModalVisible}
        onCancel={() => {
          setIsDetailModalVisible(false);
          setViewingUser(null);
        }}
        footer={null}
        width={800}
        destroyOnHidden
      >
        {viewingUser && (
          <UserDetail
            user={viewingUser}
            onEdit={() => {
              setIsDetailModalVisible(false);
              handleEditUser(viewingUser);
            }}
            onClose={() => {
              setIsDetailModalVisible(false);
              setViewingUser(null);
            }}
          />
        )}
      </Modal>
    </div>
  );
};

export default UserManagement;