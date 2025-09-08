import React, { useState, useEffect } from 'react';
import { useErrorHandler } from '../../hooks/useErrorHandler';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  Row,
  Col,
  Tag,
  Avatar,
  Progress,
  Statistic,
  Modal,
  Popconfirm,
  message,
  Tooltip,
  Dropdown,
  Badge,
  Typography
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  FilterOutlined,
  ExportOutlined,
  MoreOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  UserOutlined,
  TeamOutlined,
  CalendarOutlined,
  BookOutlined,
  PlayCircleOutlined
} from '@ant-design/icons';
import { ColumnsType } from 'antd/es/table';
import { Assignment, AssignmentStatus } from '../../types';
import { PermissionGuard } from '../../components/common';
import AssignmentForm from '../../components/forms/AssignmentForm';
import AssignmentDetail from '../../components/forms/AssignmentDetail';

const { Search } = Input;
const { Option } = Select;
const { Text } = Typography;

const AssignmentManagement: React.FC = () => {
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [courseFilter, setCourseFilter] = useState<string>('all');
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [isFormVisible, setIsFormVisible] = useState(false);
  const [isDetailVisible, setIsDetailVisible] = useState(false);
  const [editingAssignment, setEditingAssignment] = useState<Assignment | null>(null);
  const [viewingAssignment, setViewingAssignment] = useState<Assignment | null>(null);
  const [stats, setStats] = useState({
    total: 0,
    published: 0,
    draft: 0,
    completed: 0,
    totalSubmissions: 0,
    averageScore: 0
  });

  const { handleError } = useErrorHandler();

  useEffect(() => {
    loadAssignments();
    loadStats();
  }, []);

  // 模拟数据
  const mockAssignments: Assignment[] = [
    {
      id: '1',
      title: 'React组件开发练习',
      description: '完成一个完整的React组件，包括状态管理和事件处理',
      courseId: 'course1',
      courseName: 'React入门课程',
      teacherId: 'teacher1',
      teacherName: '张老师',
      status: AssignmentStatus.PUBLISHED,
      startDate: '2024-01-15',
      endDate: '2024-01-25',
      allowLateSubmission: true,
      maxScore: 100,
      submissionCount: 25,
      completionRate: 80,
      averageScore: 85.5,
      attachments: [
        { id: '1', name: '作业要求.pdf', url: '/files/assignment1.pdf' }
      ],
      createdAt: '2024-01-10T10:00:00Z',
      updatedAt: '2024-01-12T15:30:00Z'
    },
    {
      id: '2',
      title: 'JavaScript异步编程',
      description: '使用Promise和async/await处理异步操作',
      courseId: 'course2',
      courseName: 'JavaScript进阶',
      teacherId: 'teacher2',
      teacherName: '李老师',
      status: AssignmentStatus.DRAFT,
      startDate: '2024-01-20',
      endDate: '2024-01-30',
      allowLateSubmission: false,
      maxScore: 100,
      submissionCount: 0,
      completionRate: 0,
      averageScore: 0,
      attachments: [],
      createdAt: '2024-01-18T09:00:00Z',
      updatedAt: '2024-01-18T09:00:00Z'
    }
  ];

  const loadAssignments = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      let filteredAssignments = mockAssignments;
      
      // 应用搜索过滤
      if (searchKeyword) {
        filteredAssignments = filteredAssignments.filter(assignment =>
          assignment.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
          assignment.description.toLowerCase().includes(searchKeyword.toLowerCase()) ||
          assignment.courseName.toLowerCase().includes(searchKeyword.toLowerCase())
        );
      }
      
      // 应用状态过滤
      if (statusFilter !== 'all') {
        filteredAssignments = filteredAssignments.filter(assignment =>
          assignment.status === statusFilter
        );
      }
      
      // 应用课程过滤
      if (courseFilter !== 'all') {
        filteredAssignments = filteredAssignments.filter(assignment =>
          assignment.courseId === courseFilter
        );
      }
      
      setAssignments(filteredAssignments);
    } catch (error) {
      handleError(error, '加载作业列表失败');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      // 模拟统计数据
      const mockStats = {
        total: mockAssignments.length,
        published: mockAssignments.filter(a => a.status === AssignmentStatus.PUBLISHED).length,
        draft: mockAssignments.filter(a => a.status === AssignmentStatus.DRAFT).length,
        completed: mockAssignments.filter(a => a.status === AssignmentStatus.CLOSED).length,
        totalSubmissions: mockAssignments.reduce((sum, a) => sum + a.submissionCount, 0),
        averageScore: mockAssignments.reduce((sum, a) => sum + a.averageScore, 0) / mockAssignments.length
      };
      setStats(mockStats);
    } catch (error) {
      handleError(error, '加载统计数据失败');
    }
  };

  const handleCreate = () => {
    setEditingAssignment(null);
    setIsFormVisible(true);
  };

  const handleEdit = (assignment: Assignment) => {
    setEditingAssignment(assignment);
    setIsFormVisible(true);
  };

  const handleView = (assignment: Assignment) => {
    setViewingAssignment(assignment);
    setIsDetailVisible(true);
  };

  const handleDelete = async (assignmentId: string) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500));
      setAssignments(assignments.filter(assignment => assignment.id !== assignmentId));
      message.success('作业删除成功');
    } catch (error) {
      handleError(error, '删除作业失败');
    }
  };

  const handleBatchDelete = async () => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      setAssignments(assignments.filter(assignment => !selectedRowKeys.includes(assignment.id)));
      setSelectedRowKeys([]);
      message.success(`成功删除 ${selectedRowKeys.length} 个作业`);
    } catch (error) {
      handleError(error, '批量删除失败');
    }
  };

  const handleStatusChange = async (assignmentId: string, newStatus: AssignmentStatus) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500));
      setAssignments(assignments.map(assignment =>
        assignment.id === assignmentId
          ? { ...assignment, status: newStatus, updatedAt: new Date().toISOString() }
          : assignment
      ));
      message.success('状态更新成功');
    } catch (error) {
      handleError(error, '状态更新失败');
    }
  };

  const handleCopy = async (assignment: Assignment) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500));
      const copiedAssignment: Assignment = {
        ...assignment,
        id: Date.now().toString(),
        title: `${assignment.title} (副本)`,
        status: AssignmentStatus.DRAFT,
        submissionCount: 0,
        completionRate: 0,
        averageScore: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      setAssignments([copiedAssignment, ...assignments]);
      message.success('作业复制成功');
    } catch (error) {
      handleError(error, '复制作业失败');
    }
  };

  const handleExport = async () => {
    try {
      // 模拟导出功能
      await new Promise(resolve => setTimeout(resolve, 1000));
      message.success('导出成功');
    } catch (error) {
      handleError(error, '导出失败');
    }
  };

  const getStatusColor = (status: AssignmentStatus): string => {
    switch (status) {
      case AssignmentStatus.DRAFT:
        return 'default';
      case AssignmentStatus.PUBLISHED:
        return 'success';
      case AssignmentStatus.CLOSED:
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: AssignmentStatus): string => {
    switch (status) {
      case AssignmentStatus.DRAFT:
        return '草稿';
      case AssignmentStatus.PUBLISHED:
        return '已发布';
      case AssignmentStatus.CLOSED:
        return '已关闭';
      default:
        return '未知';
    }
  };

  const getActionMenuItems = (assignment: Assignment) => [
    {
      key: 'copy',
      label: '复制作业',
      icon: <FileTextOutlined />,
      onClick: () => handleCopy(assignment)
    },
    {
      key: 'publish',
      label: assignment.status === AssignmentStatus.PUBLISHED ? '取消发布' : '发布',
      icon: assignment.status === AssignmentStatus.PUBLISHED ? <ExclamationCircleOutlined /> : <PlayCircleOutlined />,
      onClick: () => handleStatusChange(
        assignment.id,
        assignment.status === AssignmentStatus.PUBLISHED
          ? AssignmentStatus.DRAFT
          : AssignmentStatus.PUBLISHED
      )
    },
    {
      key: 'close',
      label: '关闭作业',
      icon: <ClockCircleOutlined />,
      disabled: assignment.status === AssignmentStatus.CLOSED,
      onClick: () => handleStatusChange(assignment.id, AssignmentStatus.CLOSED)
    },
    {
      type: 'divider'
    },
    {
      key: 'delete',
      label: '删除',
      icon: <DeleteOutlined />,
      danger: true,
      onClick: () => {
        Modal.confirm({
          title: '确认删除',
          content: `确定要删除作业"${assignment.title}"吗？`,
          onOk: () => handleDelete(assignment.id)
        });
      }
    }
  ];

  const columns: ColumnsType<Assignment> = [
    {
      title: '作业信息',
      key: 'info',
      width: 300,
      render: (_, assignment) => (
        <div>
          <div style={{ fontWeight: 500, marginBottom: 4 }}>
            {assignment.title}
          </div>
          <div style={{ fontSize: '12px', color: '#666', marginBottom: 4 }}>
            {assignment.description}
          </div>
          <div style={{ fontSize: '12px', color: '#999' }}>
            ID: {assignment.id}
          </div>
        </div>
      )
    },
    {
      title: '课程信息',
      key: 'course',
      width: 200,
      render: (_, assignment) => (
        <div>
          <div style={{ fontWeight: 500, marginBottom: 4 }}>
            {assignment.courseName}
          </div>
          <div style={{ fontSize: '12px', color: '#666' }}>
            <UserOutlined style={{ marginRight: 4 }} />
            {assignment.teacherName}
          </div>
        </div>
      )
    },
    {
      title: '提交情况',
      key: 'submission',
      width: 150,
      render: (_, assignment) => (
        <div>
          <div style={{ marginBottom: 8 }}>
            <Text strong>{assignment.submissionCount}</Text>
            <Text type="secondary"> 人提交</Text>
          </div>
          <Progress
            percent={assignment.completionRate}
            size="small"
            format={(percent) => `${percent}%`}
          />
          <div style={{ fontSize: '12px', color: '#666', marginTop: 4 }}>
            平均分: {assignment.averageScore.toFixed(1)}
          </div>
        </div>
      )
    },
    {
      title: '时间安排',
      key: 'schedule',
      width: 150,
      render: (_, assignment) => {
        const now = new Date();
        const endDate = new Date(assignment.endDate);
        const isOverdue = now > endDate;
        
        return (
          <div>
            <div style={{ fontSize: '12px', color: '#666' }}>
              <CalendarOutlined style={{ marginRight: 4 }} />
              开始: {assignment.startDate}
            </div>
            <div style={{ 
              fontSize: '12px', 
              color: isOverdue ? '#ff4d4f' : '#666',
              marginTop: 2 
            }}>
              截止: {assignment.endDate}
              {isOverdue && <Badge status="error" style={{ marginLeft: 4 }} />}
            </div>
            {assignment.allowLateSubmission && (
              <Tag color="orange" style={{ marginTop: 4 }}>
                允许迟交
              </Tag>
            )}
          </div>
        );
      }
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: Assignment['status']) => (
        <Tag color={getStatusColor(status)}>
          {getStatusText(status)}
        </Tag>
      )
    },
    {
      title: '附件',
      key: 'attachments',
      width: 80,
      render: (_, assignment) => (
        <div>
          {assignment.attachments.length > 0 ? (
            <Badge count={assignment.attachments.length}>
              <FileTextOutlined style={{ fontSize: '16px' }} />
            </Badge>
          ) : (
            <Text type="secondary">无</Text>
          )}
        </div>
      )
    },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      fixed: 'right',
      render: (_, assignment) => (
        <Space>
          <Tooltip title="查看详情">
            <Button
              type="text"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleView(assignment)}
              data-testid="view-assignment-btn"
            />
          </Tooltip>
          <PermissionGuard requiredPermissions={['assignment:update']}>
            <Tooltip title="编辑">
              <Button
                type="text"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleEdit(assignment)}
                data-testid="edit-assignment-btn"
              />
            </Tooltip>
          </PermissionGuard>
          <Dropdown
            menu={{ items: getActionMenuItems(assignment) }}
            trigger={['click']}
          >
            <Button
              type="text"
              size="small"
              icon={<MoreOutlined />}
            />
          </Dropdown>
        </Space>
      )
    }
  ];

  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: React.Key[]) => {
      setSelectedRowKeys(newSelectedRowKeys);
    },
    getCheckboxProps: (record: Assignment) => ({
      disabled: record.status === AssignmentStatus.CLOSED
    })
  };

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面标题和统计 */}
      <Row justify="space-between" align="middle" style={{ marginBottom: 24 }}>
        <Col>
          <h1 style={{ margin: 0, fontSize: '24px', fontWeight: 600 }}>
            作业管理
          </h1>
          <p style={{ margin: '8px 0 0 0', color: '#666' }}>
            管理系统中的所有作业信息
          </p>
        </Col>
      </Row>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6} lg={4}>
          <Card>
            <Statistic
              title="总作业数"
              value={stats.total}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6} lg={4}>
          <Card>
            <Statistic
              title="已发布"
              value={stats.published}
              prefix={<PlayCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6} lg={4}>
          <Card>
            <Statistic
              title="草稿"
              value={stats.draft}
              prefix={<EditOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6} lg={4}>
          <Card>
            <Statistic
              title="已完成"
              value={stats.completed}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6} lg={4}>
          <Card>
            <Statistic
              title="总提交数"
              value={stats.totalSubmissions}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#13c2c2' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6} lg={4}>
          <Card>
            <Statistic
              title="平均分"
              value={stats.averageScore}
              suffix="分"
              prefix={<BookOutlined />}
              valueStyle={{ color: '#eb2f96' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 操作栏 */}
      <Card style={{ marginBottom: 16 }}>
        <Row justify="space-between" align="middle">
          <Col flex="auto">
            <Space wrap>
              <Search
                placeholder="搜索作业名称、描述或课程"
                allowClear
                style={{ width: 300 }}
                onSearch={setSearchKeyword}
                onChange={(e) => !e.target.value && setSearchKeyword('')}
                data-testid="assignment-search-input"
              />
              <Select
                value={statusFilter}
                onChange={setStatusFilter}
                style={{ width: 120 }}
                placeholder="状态筛选"
              >
                <Option value="all">全部状态</Option>
                <Option value="draft">草稿</Option>
                <Option value="published">已发布</Option>
                <Option value="closed">已关闭</Option>
              </Select>
              <Select
                value={courseFilter}
                onChange={setCourseFilter}
                style={{ width: 150 }}
                placeholder="课程筛选"
              >
                <Option value="all">全部课程</Option>
                <Option value="course1">React入门课程</Option>
                <Option value="course2">JavaScript进阶</Option>
                <Option value="course3">CSS高级技巧</Option>
              </Select>
              <Button icon={<FilterOutlined />}>高级筛选</Button>
            </Space>
          </Col>
          <Col>
            <Space>
              <Button
                icon={<ExportOutlined />}
                onClick={handleExport}
              >
                导出
              </Button>
              <PermissionGuard requiredPermissions={['assignment:delete']}>
                <Popconfirm
                  title="确认批量删除"
                  description={`确定要删除选中的 ${selectedRowKeys.length} 个作业吗？`}
                  onConfirm={handleBatchDelete}
                  disabled={selectedRowKeys.length === 0}
                >
                  <Button
                    danger
                    disabled={selectedRowKeys.length === 0}
                    icon={<DeleteOutlined />}
                  >
                    批量删除 ({selectedRowKeys.length})
                  </Button>
                </Popconfirm>
              </PermissionGuard>
              <PermissionGuard requiredPermissions={['assignment:create']}>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleCreate}
                  data-testid="create-assignment-btn"
                >
                  新建作业
                </Button>
              </PermissionGuard>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 作业列表 */}
      <Card>
        <Table<Assignment>
          columns={columns}
          dataSource={assignments}
          rowKey="id"
          loading={loading}
          rowSelection={rowSelection}
          scroll={{ x: 1400 }}
          data-testid="assignment-list-table"
          pagination={{
            total: assignments.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条记录`
          }}
        />
      </Card>

      {/* 作业表单弹窗 */}
      <Modal
        title={editingAssignment ? '编辑作业' : '新建作业'}
        open={isFormVisible}
        onCancel={() => {
          setIsFormVisible(false);
          setEditingAssignment(null);
        }}
        footer={null}
        width={800}
        destroyOnClose
      >
        <AssignmentForm
          assignment={editingAssignment}
          onSubmit={async (assignmentData) => {
            try {
              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 1000));
              
              if (editingAssignment) {
                // 更新作业
                setAssignments(assignments.map(assignment =>
                  assignment.id === editingAssignment.id
                    ? { ...assignment, ...assignmentData, updatedAt: new Date().toISOString() }
                    : assignment
                ));
                message.success('作业更新成功');
              } else {
                // 创建新作业
                const newAssignment: Assignment = {
                  id: Date.now().toString(),
                  ...assignmentData,
                  teacherId: 'current-user-id',
                  teacherName: '当前用户',
                  submissionCount: 0,
                  completionRate: 0,
                  averageScore: 0,
                  createdAt: new Date().toISOString(),
                  updatedAt: new Date().toISOString()
                } as Assignment;
                setAssignments([newAssignment, ...assignments]);
                message.success('作业创建成功');
              }
              
              setIsFormVisible(false);
              setEditingAssignment(null);
            } catch (error) {
              message.error(editingAssignment ? '作业更新失败' : '作业创建失败');
            }
          }}
          onCancel={() => {
            setIsFormVisible(false);
            setEditingAssignment(null);
          }}
        />
      </Modal>

      {/* 作业详情弹窗 */}
      <Modal
        title="作业详情"
        open={isDetailVisible}
        onCancel={() => {
          setIsDetailVisible(false);
          setViewingAssignment(null);
        }}
        footer={null}
        width={1000}
        destroyOnClose
      >
        {viewingAssignment && (
          <AssignmentDetail
            assignment={viewingAssignment}
            onEdit={() => {
              setIsDetailVisible(false);
              handleEdit(viewingAssignment);
            }}
            onDelete={() => {
              setIsDetailVisible(false);
              handleDelete(viewingAssignment.id);
            }}
            onCopy={() => {
              // TODO: 实现复制功能
              message.info('复制功能待实现');
            }}
            onPublish={() => {
              // TODO: 实现发布功能
              message.info('发布功能待实现');
            }}
            onUnpublish={() => {
              // TODO: 实现取消发布功能
              message.info('取消发布功能待实现');
            }}
            onStatusChange={(newStatus) => {
              handleStatusChange(viewingAssignment.id, newStatus as any);
              setViewingAssignment({ ...viewingAssignment, status: newStatus as any });
            }}
          />
        )}
      </Modal>
    </div>
  );
};

export default AssignmentManagement;