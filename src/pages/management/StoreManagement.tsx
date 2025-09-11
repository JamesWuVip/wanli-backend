import React, { useState, useEffect } from 'react';
import { useErrorHandler } from '../../hooks/useErrorHandler';
import {
  Table,
  Button,
  Space,
  Input,
  Select,
  Card,
  Row,
  Col,
  Statistic,
  Tag,
  Dropdown,
  Modal,
  message,
  Tooltip,
  Badge,
  Avatar,
  Image,
  Rate,
  Popconfirm
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  FilterOutlined,
  ExportOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  CopyOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ShopOutlined,
  UserOutlined,
  PhoneOutlined,
  MailOutlined,
  EnvironmentOutlined,
  MoreOutlined,
  StarOutlined,
  TeamOutlined
} from '@ant-design/icons';
import type { ColumnsType, TableProps } from 'antd/es/table';
import type { MenuProps } from 'antd';
import { PermissionGuard } from '../../components/common';
import StoreForm from '../../components/forms/StoreForm';
import StoreDetail from '../../components/forms/StoreDetail';
import type { Store, StoreStatus, User } from '../../types';

const { Search } = Input;
const { Option } = Select;

interface StoreManagementProps {}

const StoreManagement: React.FC<StoreManagementProps> = () => {
  const { handleError } = useErrorHandler();
  const [stores, setStores] = useState<Store[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<StoreStatus | 'all'>('all');
  const [provinceFilter, setProvinceFilter] = useState<string>('all');
  const [isFormVisible, setIsFormVisible] = useState(false);
  const [isDetailVisible, setIsDetailVisible] = useState(false);
  const [editingStore, setEditingStore] = useState<Store | null>(null);
  const [viewingStore, setViewingStore] = useState<Store | null>(null);
  const [managers, setManagers] = useState<User[]>([]);

  // 模拟数据
  useEffect(() => {
    loadStores();
    loadManagers();
  }, []);

  const loadStores = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      const mockStores: Store[] = [
        {
          id: '1',
          name: '朝阳门店',
          code: 'CY001',
          province: '北京市',
          city: '朝阳区',
          district: '建国路街道',
          address: '建国路88号现代城A座1层',
          phone: '010-85123456',
          email: 'chaoyang@wanli.com',
          managerId: '1',
          managerName: '张经理',
          managerPhone: '13800138001',
          businessHours: '09:00-21:00',
          area: 300,
          capacity: 100,
          description: '位于朝阳区核心商圈，交通便利，设施完善',
          facilities: ['多媒体教室', '图书角', '休息区', '停车场'],
          tags: ['旗舰店', '核心商圈'],
          images: [],
          isActive: true,
          status: 'active' as StoreStatus,
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-15T10:30:00Z'
        },
        {
          id: '2',
          name: '海淀门店',
          code: 'HD001',
          province: '北京市',
          city: '海淀区',
          district: '中关村街道',
          address: '中关村大街123号科技大厦2层',
          phone: '010-62345678',
          email: 'haidian@wanli.com',
          managerId: '2',
          managerName: '李经理',
          managerPhone: '13800138002',
          businessHours: '08:30-20:30',
          area: 250,
          capacity: 80,
          description: '位于中关村科技园区，周边高校众多',
          facilities: ['智能教室', '实验室', '咖啡区'],
          tags: ['科技园区', '高校周边'],
          images: [],
          isActive: true,
          status: 'active' as StoreStatus,
          createdAt: '2024-01-10T00:00:00Z',
          updatedAt: '2024-01-20T14:20:00Z'
        },
        {
          id: '3',
          name: '西城门店',
          code: 'XC001',
          province: '北京市',
          city: '西城区',
          district: '西单街道',
          address: '西单北大街56号教育大厦3层',
          phone: '010-66789012',
          email: 'xicheng@wanli.com',
          managerId: '3',
          managerName: '王经理',
          managerPhone: '13800138003',
          businessHours: '09:00-20:00',
          area: 200,
          capacity: 60,
          description: '位于西城区教育核心区域，环境优雅',
          facilities: ['标准教室', '阅览室'],
          tags: ['教育区域'],
          images: [],
          isActive: false,
          status: 'inactive' as StoreStatus,
          createdAt: '2024-02-01T00:00:00Z',
          updatedAt: '2024-02-10T09:15:00Z'
        }
      ];
      setStores(mockStores);
    } catch (error) {
      message.error('加载门店列表失败');
    } finally {
      setLoading(false);
    }
  };

  const loadManagers = async () => {
    try {
      // 模拟API调用
      const mockManagers: User[] = [
        { id: '1', username: 'manager1', fullName: '张经理', email: 'zhang@example.com' },
        { id: '2', username: 'manager2', fullName: '李经理', email: 'li@example.com' },
        { id: '3', username: 'manager3', fullName: '王经理', email: 'wang@example.com' }
      ] as User[];
      setManagers(mockManagers);
    } catch (error) {
      handleError(error);
    }
  };

  // 筛选数据
  const filteredStores = stores.filter(store => {
    const matchesSearch = !searchText || 
      store.name.toLowerCase().includes(searchText.toLowerCase()) ||
      store.code.toLowerCase().includes(searchText.toLowerCase()) ||
      store.address.toLowerCase().includes(searchText.toLowerCase());
    
    const matchesStatus = statusFilter === 'all' || store.status === statusFilter;
    const matchesProvince = provinceFilter === 'all' || store.address.includes(provinceFilter);
    
    return matchesSearch && matchesStatus && matchesProvince;
  });

  // 统计数据
  const stats = {
    totalStores: stores.length,
    activeStores: stores.filter(s => s.status === 'active').length,
    inactiveStores: stores.filter(s => s.status === 'inactive').length,
    maintenanceStores: stores.filter(s => s.status === 'maintenance').length,
    closedStores: stores.filter(s => s.status === 'closed').length
  };

  const handleCreate = () => {
    setEditingStore(null);
    setIsFormVisible(true);
  };

  const handleEdit = (store: Store) => {
    setEditingStore(store);
    setIsFormVisible(true);
  };

  const handleView = (store: Store) => {
    setViewingStore(store);
    setIsDetailVisible(true);
  };

  const handleDelete = (storeId: string) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个门店吗？此操作不可恢复。',
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          // 模拟API调用
          await new Promise(resolve => setTimeout(resolve, 1000));
          setStores(stores.filter(s => s.id !== storeId));
          message.success('门店删除成功');
        } catch (error) {
          message.error('门店删除失败');
        }
      }
    });
  };

  const handleStatusChange = async (storeId: string, newStatus: string) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      setStores(stores.map(s => 
        s.id === storeId ? { ...s, status: newStatus as StoreStatus, updatedAt: new Date().toISOString() } : s
      ));
      message.success(`门店${newStatus === 'active' ? '激活' : '停用'}成功`);
    } catch (error) {
      message.error(`门店${newStatus === 'active' ? '激活' : '停用'}失败`);
    }
  };

  const handleCopy = async (store: Store) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      const newStore: Store = {
        ...store,
        id: Date.now().toString(),
        name: `${store.name} - 副本`,
        code: `${store.code}_COPY`,
        status: 'inactive' as StoreStatus,

        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      setStores([newStore, ...stores]);
      message.success('门店复制成功');
    } catch (error) {
      message.error('门店复制失败');
    }
  };

  const handleBatchDelete = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的门店');
      return;
    }

    Modal.confirm({
      title: '批量删除',
      content: `确定要删除选中的 ${selectedRowKeys.length} 个门店吗？此操作不可恢复。`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          // 模拟API调用
          await new Promise(resolve => setTimeout(resolve, 1000));
          setStores(stores.filter(s => !selectedRowKeys.includes(s.id)));
          setSelectedRowKeys([]);
          message.success('批量删除成功');
        } catch (error) {
          message.error('批量删除失败');
        }
      }
    });
  };

  const handleExport = () => {
    // 模拟导出功能
    message.success('门店数据导出成功');
  };

  const getStatusColor = (status: StoreStatus) => {
    switch (status) {
      case 'active': return 'green';
      case 'inactive': return 'red';
      case 'maintenance': return 'orange';
      case 'closed': return 'gray';
      default: return 'default';
    }
  };

  const getStatusText = (status: StoreStatus) => {
    switch (status) {
      case 'active': return '营业中';
      case 'inactive': return '暂停营业';
      case 'maintenance': return '维护中';
      case 'closed': return '已关闭';
      default: return '未知';
    }
  };

  const columns: ColumnsType<Store> = [
    {
      title: '门店信息',
      key: 'storeInfo',
      width: 250,
      render: (_, record) => (
        <div>
          <div className="flex items-center mb-2">
            <Avatar 
              size="small" 
              icon={<ShopOutlined />} 
              className="mr-2"
              style={{ backgroundColor: '#1890ff' }}
            />
            <div>
              <div className="font-medium text-gray-900">{record.name}</div>
              <div className="text-sm text-gray-500">编码: {record.code}</div>
            </div>
          </div>
          <div className="flex items-center text-sm text-gray-600 mb-1">
            <Rate disabled defaultValue={4.5} className="mr-2" />
            <span>4.5</span>
          </div>
          <div className="flex flex-wrap gap-1">
            <Tag>门店标签</Tag>
          </div>
        </div>
      )
    },
    {
      title: '地址信息',
      key: 'address',
      width: 200,
      render: (_, record) => (
        <div>
          <div className="text-sm font-medium">
            {record.address}
          </div>
          <div className="text-sm text-gray-600">
            {record.phone}
          </div>
          <div className="text-xs text-gray-500 mt-1">
            {record.email}
          </div>
        </div>
      )
    },
    {
      title: '店长信息',
      key: 'manager',
      width: 120,
      render: (_, record) => (
        <div className="flex items-center">
          <Avatar size="small" icon={<UserOutlined />} className="mr-2" />
          <span>管理员</span>
        </div>
      )
    },
    {
      title: '联系方式',
      key: 'contact',
      width: 150,
      render: (_, record) => (
        <div>
          <div className="flex items-center text-sm mb-1">
            <PhoneOutlined className="mr-1 text-gray-400" />
            <span className="truncate">{record.phone}</span>
          </div>
          <div className="flex items-center text-sm">
            <MailOutlined className="mr-1 text-gray-400" />
            <span className="truncate">{record.email}</span>
          </div>
        </div>
      )
    },
    {
      title: '规模信息',
      key: 'scale',
      width: 120,
      render: (_, record) => (
        <div>
          <div className="text-sm">
            <span className="text-gray-600">地址:</span> {record.address}
          </div>
          <div className="text-sm">
            <span className="text-gray-600">电话:</span> {record.phone}
          </div>
        </div>
      )
    },
    {
      title: '联系电话',
      dataIndex: 'phone',
      key: 'phone',
      width: 150,
      render: (phone) => (
        <div className="text-sm">
          <PhoneOutlined className="mr-1" />
          {phone}
        </div>
      )
    },
    {
      title: '状态',
      key: 'status',
      width: 100,
      render: (_, record) => (
        <Tag color={getStatusColor(record.status)}>
          {getStatusText(record.status)}
        </Tag>
      )
    },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      fixed: 'right',
      render: (_, record) => {
        const items: MenuProps['items'] = [
          {
            key: 'view',
            label: '查看详情',
            icon: <EyeOutlined />,
            onClick: () => handleView(record)
          },
          {
            key: 'edit',
            label: '编辑',
            icon: <EditOutlined />,
            onClick: () => handleEdit(record)
          },
          {
            key: 'copy',
            label: '复制',
            icon: <CopyOutlined />,
            onClick: () => handleCopy(record)
          },
          {
            type: 'divider'
          },
          {
            key: 'status',
            label: record.status === 'active' ? '暂停营业' : '恢复营业',
            icon: record.status === 'active' ? <CloseCircleOutlined /> : <CheckCircleOutlined />,
            onClick: () => handleStatusChange(record.id, record.status === 'active' ? 'inactive' : 'active')
          },
          {
            type: 'divider'
          },
          {
            key: 'delete',
            label: '删除',
            icon: <DeleteOutlined />,
            danger: true,
            onClick: () => handleDelete(record.id)
          }
        ];

        return (
          <Space>
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleView(record)}
            >
              查看
            </Button>
            <Dropdown menu={{ items }} trigger={['click']}>
              <Button type="link" size="small" icon={<MoreOutlined />} />
            </Dropdown>
          </Space>
        );
      }
    }
  ];

  const rowSelection: TableProps<Store>['rowSelection'] = {
    selectedRowKeys,
    onChange: setSelectedRowKeys,
    getCheckboxProps: (record) => ({
      name: record.name
    })
  };

  return (
    <div className="p-6">
      {/* 页面标题 */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">门店管理</h1>
        <p className="text-gray-600 mt-1">管理系统中的所有门店信息</p>
      </div>

      {/* 统计卡片 */}
      <Row gutter={16} className="mb-6">
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总门店数"
              value={stats.totalStores}
              prefix={<ShopOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="营业中"
              value={stats.activeStores}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="暂停营业"
              value={stats.inactiveStores}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="已暂停"
              value={stats.maintenanceStores + stats.closedStores}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 操作栏 */}
      <Card className="mb-4">
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={12} md={8} lg={6}>
            <Search
              placeholder="搜索门店名称、编码或店长"
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              onSearch={setSearchText}
              allowClear
            />
          </Col>
          <Col xs={12} sm={6} md={4} lg={3}>
            <Select
              value={statusFilter}
              onChange={setStatusFilter}
              style={{ width: '100%' }}
              placeholder="状态筛选"
            >
              <Option value="all">全部状态</Option>
              <Option value="active">营业中</Option>
              <Option value="inactive">暂停营业</Option>
              <Option value="closed">已关闭</Option>
            </Select>
          </Col>
          <Col xs={12} sm={6} md={4} lg={3}>
            <Select
              value={provinceFilter}
              onChange={setProvinceFilter}
              style={{ width: '100%' }}
              placeholder="省份筛选"
            >
              <Option value="all">全部省份</Option>
              <Option value="北京市">北京市</Option>
              <Option value="上海市">上海市</Option>
              <Option value="广东省">广东省</Option>
              <Option value="浙江省">浙江省</Option>
            </Select>
          </Col>
          <Col xs={24} sm={12} md={8} lg={12}>
            <Space>
              <PermissionGuard requiredPermissions={['store:create']}>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleCreate}
                >
                  新建门店
                </Button>
              </PermissionGuard>
              <Button
                icon={<ExportOutlined />}
                onClick={handleExport}
              >
                导出
              </Button>
              {selectedRowKeys.length > 0 && (
                <PermissionGuard requiredPermissions={['store:delete']}>
                  <Button
                    danger
                    icon={<DeleteOutlined />}
                    onClick={handleBatchDelete}
                  >
                    批量删除 ({selectedRowKeys.length})
                  </Button>
                </PermissionGuard>
              )}
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 门店列表 */}
      <Card>
        <Table<Store>
          columns={columns}
          dataSource={filteredStores}
          rowKey="id"
          rowSelection={rowSelection}
          loading={loading}
          scroll={{ x: 1400 }}
          pagination={{
            total: filteredStores.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
        />
      </Card>

      {/* 门店表单弹窗 */}
      <Modal
        title={editingStore ? '编辑门店' : '新建门店'}
        open={isFormVisible}
        onCancel={() => {
          setIsFormVisible(false);
          setEditingStore(null);
        }}
        footer={null}
        width={800}
        destroyOnHidden
      >
        <StoreForm
          initialValues={editingStore}
          onSubmit={async (storeData) => {
            try {
              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 1000));
              
              if (editingStore) {
                // 更新门店
                setStores(stores.map(store =>
                  store.id === editingStore.id
                    ? { ...store, ...storeData, updatedAt: new Date().toISOString() }
                    : store
                ));
                message.success('门店更新成功');
              } else {
                // 创建新门店
                const newStore: Store = {
                  id: Date.now().toString(),
                  ...storeData,
                  rating: 0,
                  createdAt: new Date().toISOString(),
                  updatedAt: new Date().toISOString()
                } as Store;
                setStores([newStore, ...stores]);
                message.success('门店创建成功');
              }
              
              setIsFormVisible(false);
              setEditingStore(null);
            } catch (error) {
              message.error(editingStore ? '门店更新失败' : '门店创建失败');
            }
          }}
          onCancel={() => {
            setIsFormVisible(false);
            setEditingStore(null);
          }}
        />
      </Modal>

      {/* 门店详情弹窗 */}
      <Modal
        title="门店详情"
        open={isDetailVisible}
        onCancel={() => {
          setIsDetailVisible(false);
          setViewingStore(null);
        }}
        footer={null}
        width={1000}
        destroyOnHidden
      >
        {viewingStore && (
          <StoreDetail
            store={viewingStore}
            onEdit={() => {
              setIsDetailVisible(false);
              handleEdit(viewingStore);
            }}
            onDelete={() => {
              setIsDetailVisible(false);
              handleDelete(viewingStore.id);
            }}
            onStatusChange={(newStatus) => {
              handleStatusChange(viewingStore.id, newStatus);
              setViewingStore({ ...viewingStore, status: newStatus as StoreStatus });
            }}
          />
        )}
      </Modal>
    </div>
  );
};

export default StoreManagement;