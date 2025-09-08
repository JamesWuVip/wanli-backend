import React, { useState, useEffect } from 'react';
import { useErrorHandler } from '../../hooks/useErrorHandler';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  Tag,
  Modal,
  message,
  Popconfirm,
  Row,
  Col,
  Statistic,
  Avatar,
  Tooltip,
  Badge,
  Dropdown,
  MenuProps,
  Progress
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  ExportOutlined,
  FilterOutlined,
  BookOutlined,
  UserOutlined,
  CalendarOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  CopyOutlined,
  MoreOutlined,
  TeamOutlined,
  FileTextOutlined
} from '@ant-design/icons';
import { ColumnsType } from 'antd/es/table';
import { PermissionGuard } from '../../components/common';
import CourseForm from '../../components/forms/CourseForm';
import { CourseDetail } from '../../components/forms/CourseDetail';
import { Course, CourseStatus } from '../../types';

const { Search } = Input;
const { Option } = Select;

interface CourseStats {
  total: number;
  published: number;
  draft: number;
  completed: number;
  totalStudents: number;
}

const CourseManagement: React.FC = () => {
  const { handleError } = useErrorHandler();
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [teacherFilter, setTeacherFilter] = useState<string>('all');
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [isFormVisible, setIsFormVisible] = useState(false);
  const [isDetailVisible, setIsDetailVisible] = useState(false);
  const [editingCourse, setEditingCourse] = useState<Course | null>(null);
  const [viewingCourse, setViewingCourse] = useState<Course | null>(null);
  const [stats, setStats] = useState<CourseStats>({
    total: 0,
    published: 0,
    draft: 0,
    completed: 0,
    totalStudents: 0
  });

  useEffect(() => {
    loadCourses();
    loadStats();
  }, [searchKeyword, statusFilter, teacherFilter]);

  const loadCourses = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      const mockCourses: Course[] = [
        {
          id: '1',
          title: 'React 基础入门',
          description: 'React框架基础知识和实战项目',
          content: '详细的React学习内容...',
          teacherId: 'teacher1',
          teacherName: '张老师',
          teacherAvatar: '',
          startDate: '2024-01-15',
          endDate: '2024-03-15',
          maxStudents: 30,
          currentStudents: 25,
          status: CourseStatus.PUBLISHED,
          isPublic: true,
          coverImage: '',
          tags: ['前端', 'React', '入门'],
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-10T00:00:00Z',
          progress: 65,
          assignmentCount: 8,
          classCount: 3
        },
        {
          id: '2',
          title: 'Vue.js 进阶开发',
          description: 'Vue.js高级特性和实战项目开发',
          content: '详细的Vue.js学习内容...',
          teacherId: 'teacher2',
          teacherName: '李老师',
          teacherAvatar: '',
          startDate: '2024-02-01',
          endDate: '2024-04-01',
          maxStudents: 25,
          currentStudents: 20,
          status: CourseStatus.PUBLISHED,
          isPublic: true,
          coverImage: '',
          tags: ['前端', 'Vue', '进阶'],
          createdAt: '2024-01-15T00:00:00Z',
          updatedAt: '2024-01-20T00:00:00Z',
          progress: 40,
          assignmentCount: 6,
          classCount: 2
        },
        {
          id: '3',
          title: 'Node.js 后端开发',
          description: '使用Node.js构建高性能后端应用',
          content: '详细的Node.js学习内容...',
          teacherId: 'teacher3',
          teacherName: '王老师',
          teacherAvatar: '',
          startDate: '2024-03-01',
          endDate: '2024-05-01',
          maxStudents: 20,
          currentStudents: 15,
          status: CourseStatus.DRAFT,
          isPublic: false,
          coverImage: '',
          tags: ['后端', 'Node.js', '服务器'],
          createdAt: '2024-02-01T00:00:00Z',
          updatedAt: '2024-02-15T00:00:00Z',
          progress: 0,
          assignmentCount: 0,
          classCount: 0
        }
      ];
      
      // 应用筛选
      let filteredCourses = mockCourses;
      
      if (searchKeyword) {
        filteredCourses = filteredCourses.filter(course =>
          course.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
          course.description.toLowerCase().includes(searchKeyword.toLowerCase()) ||
          course.teacherName.toLowerCase().includes(searchKeyword.toLowerCase())
        );
      }
      
      if (statusFilter !== 'all') {
        filteredCourses = filteredCourses.filter(course => course.status === statusFilter);
      }
      
      if (teacherFilter !== 'all') {
        filteredCourses = filteredCourses.filter(course => course.teacherId === teacherFilter);
      }
      
      setCourses(filteredCourses);
    } catch (error) {
      message.error('加载课程列表失败');
    } finally {
      setLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      // 模拟统计数据
      const mockStats: CourseStats = {
        total: 156,
        published: 89,
        draft: 45,
        completed: 22,
        totalStudents: 2340
      };
      setStats(mockStats);
    } catch (error) {
      handleError(error);
    }
  };

  const handleCreate = () => {
    setEditingCourse(null);
    setIsFormVisible(true);
  };

  const handleEdit = (course: Course) => {
    setEditingCourse(course);
    setIsFormVisible(true);
  };

  const handleView = (course: Course) => {
    setViewingCourse(course);
    setIsDetailVisible(true);
  };

  const handleDelete = async (courseId: string) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      setCourses(courses.filter(course => course.id !== courseId));
      message.success('课程删除成功');
    } catch (error) {
      message.error('课程删除失败');
    }
  };

  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的课程');
      return;
    }
    
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      setCourses(courses.filter(course => !selectedRowKeys.includes(course.id)));
      setSelectedRowKeys([]);
      message.success(`成功删除 ${selectedRowKeys.length} 个课程`);
    } catch (error) {
      message.error('批量删除失败');
    }
  };

  const handleStatusChange = async (courseId: string, newStatus: Course['status']) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      setCourses(courses.map(course =>
        course.id === courseId ? { ...course, status: newStatus } : course
      ));
      message.success('课程状态更新成功');
    } catch (error) {
      message.error('状态更新失败');
    }
  };

  const handleCopy = async (course: Course) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      const newCourse: Course = {
        ...course,
        id: Date.now().toString(),
        title: `${course.title} (副本)`,
        status: CourseStatus.DRAFT,
        currentStudents: 0,
        progress: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      setCourses([newCourse, ...courses]);
      message.success('课程复制成功');
    } catch (error) {
      message.error('课程复制失败');
    }
  };

  const handleExport = async () => {
    try {
      // 模拟导出功能
      await new Promise(resolve => setTimeout(resolve, 1000));
      message.success('课程数据导出成功');
    } catch (error) {
      message.error('导出失败');
    }
  };

  const getStatusColor = (status: Course['status']) => {
    const colors = {
      [CourseStatus.DRAFT]: 'default',
      [CourseStatus.PUBLISHED]: 'success',
      [CourseStatus.COMPLETED]: 'blue',
      [CourseStatus.ARCHIVED]: 'error'
    };
    return colors[status];
  };

  const getStatusText = (status: Course['status']) => {
    const texts = {
      [CourseStatus.DRAFT]: '草稿',
      [CourseStatus.PUBLISHED]: '已发布',
      [CourseStatus.COMPLETED]: '已完成',
      [CourseStatus.ARCHIVED]: '已归档'
    };
    return texts[status];
  };

  const getActionMenuItems = (course: Course): MenuProps['items'] => [
    {
      key: 'view',
      icon: <EyeOutlined />,
      label: '查看详情',
      onClick: () => handleView(course)
    },
    {
      key: 'edit',
      icon: <EditOutlined />,
      label: '编辑',
      onClick: () => handleEdit(course)
    },
    {
      key: 'copy',
      icon: <CopyOutlined />,
      label: '复制',
      onClick: () => handleCopy(course)
    },
    {
      type: 'divider'
    },
    {
      key: 'publish',
      icon: <PlayCircleOutlined />,
      label: course.status === CourseStatus.PUBLISHED ? '取消发布' : '发布',
      onClick: () => handleStatusChange(
        course.id,
        course.status === CourseStatus.PUBLISHED ? CourseStatus.DRAFT : CourseStatus.PUBLISHED
      )
    },
    {
      key: 'complete',
      icon: <StopOutlined />,
      label: '标记完成',
      disabled: course.status !== CourseStatus.PUBLISHED,
      onClick: () => handleStatusChange(course.id, CourseStatus.COMPLETED)
    },
    {
      type: 'divider'
    },
    {
      key: 'delete',
      icon: <DeleteOutlined />,
      label: '删除',
      danger: true,
      onClick: () => {
        Modal.confirm({
          title: '确认删除',
          content: `确定要删除课程「${course.title}」吗？此操作不可恢复。`,
          onOk: () => handleDelete(course.id)
        });
      }
    }
  ];

  const columns: ColumnsType<Course> = [
    {
      title: '课程信息',
      key: 'courseInfo',
      width: 300,
      render: (_, course) => (
        <Space>
          <Avatar
            size={48}
            src={course.coverImage}
            icon={<BookOutlined />}
            style={{ backgroundColor: '#1890ff' }}
          />
          <div>
            <div style={{ fontWeight: 500, marginBottom: 4 }}>
              {course.title}
              {course.isPublic && (
                <Tag color="green" style={{ marginLeft: 8 }}>
                  公开
                </Tag>
              )}
            </div>
            <div style={{ color: '#666', fontSize: '12px' }}>
              {course.description}
            </div>
            <Space size={4} style={{ marginTop: 4 }}>
              {course.tags.map(tag => (
                <Tag key={tag}>{tag}</Tag>
              ))}
            </Space>
          </div>
        </Space>
      )
    },
    {
      title: '授课教师',
      key: 'teacher',
      width: 120,
      render: (_, course) => (
        <Space>
          <Avatar
            size="small"
            src={course.teacherAvatar}
            icon={<UserOutlined />}
          />
          {course.teacherName}
        </Space>
      )
    },
    {
      title: '学生情况',
      key: 'students',
      width: 120,
      render: (_, course) => (
        <div>
          <div style={{ marginBottom: 4 }}>
            <TeamOutlined style={{ marginRight: 4 }} />
            {course.currentStudents}/{course.maxStudents}
          </div>
          <Progress
            percent={(course.currentStudents / course.maxStudents) * 100}
            size="small"
            showInfo={false}
          />
        </div>
      )
    },
    {
      title: '课程进度',
      dataIndex: 'progress',
      key: 'progress',
      width: 100,
      render: (progress: number) => (
        <div>
          <Progress
            type="circle"
            size={40}
            percent={progress}
            format={percent => `${percent}%`}
          />
        </div>
      )
    },
    {
      title: '时间安排',
      key: 'schedule',
      width: 150,
      render: (_, course) => (
        <div>
          <div style={{ fontSize: '12px', color: '#666' }}>
            <CalendarOutlined style={{ marginRight: 4 }} />
            开始: {course.startDate}
          </div>
          <div style={{ fontSize: '12px', color: '#666', marginTop: 2 }}>
            结束: {course.endDate}
          </div>
        </div>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: Course['status']) => (
        <Tag color={getStatusColor(status)}>
          {getStatusText(status)}
        </Tag>
      )
    },
    {
      title: '统计',
      key: 'stats',
      width: 100,
      render: (_, course) => (
        <Space direction="vertical" size={0}>
          <div style={{ fontSize: '12px' }}>
            <FileTextOutlined style={{ marginRight: 4 }} />
            作业: {course.assignmentCount}
          </div>
          <div style={{ fontSize: '12px' }}>
            <TeamOutlined style={{ marginRight: 4 }} />
            班级: {course.classCount}
          </div>
        </Space>
      )
    },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      fixed: 'right',
      render: (_, course) => (
        <Space>
          <Tooltip title="查看详情">
            <Button
              type="text"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleView(course)}
            />
          </Tooltip>
          <PermissionGuard requiredPermissions={['course:update']}>
            <Tooltip title="编辑">
              <Button
                type="text"
                size="small"
                icon={<EditOutlined />}
                onClick={() => handleEdit(course)}
              />
            </Tooltip>
          </PermissionGuard>
          <Dropdown
            menu={{ items: getActionMenuItems(course) }}
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
    getCheckboxProps: (record: Course) => ({
      disabled: record.status === 'completed'
    })
  };

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面标题和统计 */}
      <Row justify="space-between" align="middle" style={{ marginBottom: 24 }}>
        <Col>
          <h1 style={{ margin: 0, fontSize: '24px', fontWeight: 600 }}>
            课程管理
          </h1>
          <p style={{ margin: '8px 0 0 0', color: '#666' }}>
            管理系统中的所有课程信息
          </p>
        </Col>
      </Row>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title="总课程数"
              value={stats.total}
              prefix={<BookOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title="已发布"
              value={stats.published}
              prefix={<PlayCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title="草稿"
              value={stats.draft}
              prefix={<EditOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title="总学生数"
              value={stats.totalStudents}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#722ed1' }}
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
                placeholder="搜索课程名称、描述或教师"
                allowClear
                style={{ width: 300 }}
                onSearch={setSearchKeyword}
                onChange={(e) => !e.target.value && setSearchKeyword('')}
                data-testid="course-search-input"
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
                <Option value="completed">已完成</Option>
                <Option value="cancelled">已取消</Option>
              </Select>
              <Select
                value={teacherFilter}
                onChange={setTeacherFilter}
                style={{ width: 120 }}
                placeholder="教师筛选"
              >
                <Option value="all">全部教师</Option>
                <Option value="teacher1">张老师</Option>
                <Option value="teacher2">李老师</Option>
                <Option value="teacher3">王老师</Option>
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
              <PermissionGuard requiredPermissions={['course:delete']}>
                <Popconfirm
                  title="确认批量删除"
                  description={`确定要删除选中的 ${selectedRowKeys.length} 个课程吗？`}
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
              <PermissionGuard requiredPermissions={['course:create']}>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleCreate}
                  data-testid="create-course-btn"
                >
                  新建课程
                </Button>
              </PermissionGuard>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 课程列表 */}
      <Card>
        <Table<Course>
          columns={columns}
          dataSource={courses}
          rowKey="id"
          loading={loading}
          rowSelection={rowSelection}
          scroll={{ x: 1200 }}
          data-testid="course-list-table"
          pagination={{
            total: courses.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条记录`
          }}
        />
      </Card>

      {/* 课程表单弹窗 */}
      <Modal
        title={editingCourse ? '编辑课程' : '新建课程'}
        open={isFormVisible}
        onCancel={() => {
          setIsFormVisible(false);
          setEditingCourse(null);
        }}
        footer={null}
        width={800}
        destroyOnClose
      >
        <CourseForm
          course={editingCourse}
          onSubmit={async (courseData) => {
            try {
              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 1000));
              
              if (editingCourse) {
                // 更新课程
                setCourses(courses.map(course =>
                  course.id === editingCourse.id
                    ? { ...course, ...courseData, updatedAt: new Date().toISOString() }
                    : course
                ));
                message.success('课程更新成功');
              } else {
                // 创建新课程
                const newCourse: Course = {
                  id: Date.now().toString(),
                  ...courseData,
                  teacherId: 'current-user-id',
                  teacherName: '当前用户',
                  currentStudents: 0,
                  progress: 0,
                  assignmentCount: 0,
                  classCount: 0,
                  createdAt: new Date().toISOString(),
                  updatedAt: new Date().toISOString()
                } as Course;
                setCourses([newCourse, ...courses]);
                message.success('课程创建成功');
              }
              
              setIsFormVisible(false);
              setEditingCourse(null);
            } catch (error) {
              message.error(editingCourse ? '课程更新失败' : '课程创建失败');
            }
          }}
          onCancel={() => {
            setIsFormVisible(false);
            setEditingCourse(null);
          }}
        />
      </Modal>

      {/* 课程详情弹窗 */}
      <Modal
        title="课程详情"
        open={isDetailVisible}
        onCancel={() => {
          setIsDetailVisible(false);
          setViewingCourse(null);
        }}
        footer={null}
        width={1000}
        destroyOnClose
      >
        {viewingCourse && (
          <CourseDetail
            course={viewingCourse}
            onEdit={() => {
              setIsDetailVisible(false);
              handleEdit(viewingCourse);
            }}
            onDelete={() => {
              setIsDetailVisible(false);
              handleDelete(viewingCourse.id);
            }}
            onCopy={() => {
              // 复制课程逻辑
              const copiedCourse: Course = {
                ...viewingCourse,
                id: Date.now().toString(),
                title: `${viewingCourse.title} (副本)`,
                status: CourseStatus.DRAFT,
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString()
              };
              setCourses([copiedCourse, ...courses]);
              message.success('课程复制成功');
              setIsDetailVisible(false);
            }}
            onPublish={(published) => {
              const newStatus = published ? CourseStatus.PUBLISHED : CourseStatus.DRAFT;
              handleStatusChange(viewingCourse.id, newStatus);
              setViewingCourse({ ...viewingCourse, status: newStatus });
              message.success(published ? '课程发布成功' : '课程取消发布成功');
            }}
            onStatusChange={(newStatus) => {
              handleStatusChange(viewingCourse.id, newStatus);
              setViewingCourse({ ...viewingCourse, status: newStatus });
            }}
          />
        )}
      </Modal>
    </div>
  );
};

export default CourseManagement;