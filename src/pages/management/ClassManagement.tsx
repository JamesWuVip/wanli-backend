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
  Progress,
  DatePicker,
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
  PlayCircleOutlined,
  PauseCircleOutlined,
  UserOutlined,
  BookOutlined,
  CalendarOutlined,
  TeamOutlined,
  MoreOutlined
} from '@ant-design/icons';
import type { ColumnsType, TableProps } from 'antd/es/table';
import type { MenuProps } from 'antd';
import { PermissionGuard } from '../../components/common';
import ClassForm from '../../components/forms/ClassForm';
import ClassDetail from '../../components/forms/ClassDetail';
import type { Class } from '../../types/class';
import type { Course } from '../../types';
import type { User } from '../../types/user';
import type { Store } from '../../types/store';
import { ClassStatus, ClassSchedule } from '../../types/class';

const { Search } = Input;
const { Option } = Select;
const { RangePicker } = DatePicker;

interface ClassManagementProps {}

const ClassManagement: React.FC<ClassManagementProps> = () => {
  const { handleError } = useErrorHandler();
  const [classes, setClasses] = useState<Class[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<ClassStatus | 'all'>('all');
  const [courseFilter, setCourseFilter] = useState<string>('all');
  const [teacherFilter, setTeacherFilter] = useState<string>('all');
  const [storeFilter, setStoreFilter] = useState<string>('all');
  const [isFormVisible, setIsFormVisible] = useState(false);
  const [isDetailVisible, setIsDetailVisible] = useState(false);
  const [editingClass, setEditingClass] = useState<Class | null>(null);
  const [viewingClass, setViewingClass] = useState<Class | null>(null);
  const [courses, setCourses] = useState<Course[]>([]);
  const [teachers, setTeachers] = useState<User[]>([]);
  const [stores, setStores] = useState<Store[]>([]);

  // 模拟数据
  useEffect(() => {
    loadClasses();
    loadCourses();
    loadTeachers();
    loadStores();
  }, []);

  const loadClasses = async () => {
    setLoading(true);
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      const mockClasses: Class[] = [
        {
          id: '1',
          name: '数学基础班A',
          courseId: '1',
          courseName: '小学数学基础',
          teacherId: '1',
          teacherName: '张老师',
          storeId: '1',
          storeName: '朝阳门店',
          description: '针对小学1-3年级学生的数学基础课程',
          startDate: '2024-02-01',
          endDate: '2024-06-30',
          maxStudents: 30,
          currentStudents: 25,
          status: ClassStatus.ACTIVE,
          isPublic: true,
          allowSelfEnroll: true,
          schedule: [
            { dayOfWeek: 1, startTime: '09:00', endTime: '10:30' },
            { dayOfWeek: 3, startTime: '09:00', endTime: '10:30' }
          ],
          tags: ['基础', '小学'],
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-01-20T15:30:00Z',
          createdBy: '1'
        },
        {
          id: '2',
          name: '英语提高班B',
          courseId: '2',
          courseName: '小学英语提高',
          teacherId: '2',
          teacherName: '李老师',
          storeId: '2',
          storeName: '海淀门店',
          description: '针对有一定英语基础的小学生',
          startDate: '2024-03-01',
          endDate: '2024-07-31',
          maxStudents: 20,
          currentStudents: 18,
          status: ClassStatus.ACTIVE,
          isPublic: true,
          allowSelfEnroll: false,
          schedule: [
            { dayOfWeek: 2, startTime: '14:00', endTime: '15:30' },
            { dayOfWeek: 4, startTime: '14:00', endTime: '15:30' }
          ],
          tags: ['提高', '英语'],
          createdAt: '2024-02-10T09:00:00Z',
          updatedAt: '2024-02-15T11:20:00Z',
          createdBy: '2'
        },
        {
          id: '3',
          name: '科学实验班',
          courseId: '3',
          courseName: '小学科学实验',
          teacherId: '3',
          teacherName: '王老师',
          storeId: '1',
          storeName: '朝阳门店',
          description: '通过实验培养学生的科学思维',
          startDate: '2024-04-01',
          endDate: '2024-08-31',
          maxStudents: 15,
          currentStudents: 12,
          status: ClassStatus.INACTIVE,
          isPublic: false,
          allowSelfEnroll: false,
          schedule: [
            { dayOfWeek: 6, startTime: '10:00', endTime: '11:30' }
          ],
          tags: ['实验', '科学'],
          createdAt: '2024-03-20T14:00:00Z',
          updatedAt: '2024-03-25T16:45:00Z',
          createdBy: '3'
        }
      ];
      setClasses(mockClasses);
    } catch (error) {
      message.error('加载班级列表失败');
    } finally {
      setLoading(false);
    }
  };

  const loadCourses = async () => {
    try {
      // 模拟API调用
      const mockCourses: Course[] = [
        { id: '1', title: '小学数学基础', teacherId: '1', teacherName: '张老师' },
        { id: '2', title: '小学英语提高', teacherId: '2', teacherName: '李老师' },
        { id: '3', title: '小学科学实验', teacherId: '3', teacherName: '王老师' }
      ] as Course[];
      setCourses(mockCourses);
    } catch (error) {
      handleError(error);
    }
  };

  const loadTeachers = async () => {
    try {
      // 模拟API调用
      const mockTeachers: User[] = [
        { id: '1', username: 'teacher1', fullName: '张老师', email: 'zhang@example.com' },
        { id: '2', username: 'teacher2', fullName: '李老师', email: 'li@example.com' },
        { id: '3', username: 'teacher3', fullName: '王老师', email: 'wang@example.com' }
      ] as User[];
      setTeachers(mockTeachers);
    } catch (error) {
      handleError(error);
    }
  };

  const loadStores = async () => {
    try {
      // 模拟API调用
      const mockStores: Store[] = [
        { id: '1', name: '朝阳门店', code: 'CY001' },
        { id: '2', name: '海淀门店', code: 'HD001' },
        { id: '3', name: '西城门店', code: 'XC001' }
      ] as Store[];
      setStores(mockStores);
    } catch (error) {
      handleError(error);
    }
  };

  // 筛选数据
  const filteredClasses = classes.filter(classItem => {
    const matchesSearch = !searchText || 
      classItem.name.toLowerCase().includes(searchText.toLowerCase()) ||
      classItem.courseName.toLowerCase().includes(searchText.toLowerCase()) ||
      classItem.teacherName.toLowerCase().includes(searchText.toLowerCase());
    
    const matchesStatus = statusFilter === 'all' || classItem.status === statusFilter;
    const matchesCourse = courseFilter === 'all' || classItem.courseId === courseFilter;
    const matchesTeacher = teacherFilter === 'all' || classItem.teacherId === teacherFilter;
    const matchesStore = storeFilter === 'all' || classItem.storeId === storeFilter;
    
    return matchesSearch && matchesStatus && matchesCourse && matchesTeacher && matchesStore;
  });

  // 统计数据
  const stats = {
    total: classes.length,
    active: classes.filter(c => c.status === ClassStatus.ACTIVE).length,
    inactive: classes.filter(c => c.status === ClassStatus.INACTIVE).length,
    totalStudents: classes.reduce((sum, c) => sum + c.currentStudents, 0)
  };

  const handleCreate = () => {
    setEditingClass(null);
    setIsFormVisible(true);
  };

  const handleEdit = (classItem: Class) => {
    setEditingClass(classItem);
    setIsFormVisible(true);
  };

  const handleView = (classItem: Class) => {
    setViewingClass(classItem);
    setIsDetailVisible(true);
  };

  const handleDelete = (classId: string) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这个班级吗？此操作不可恢复。',
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          // 模拟API调用
          await new Promise(resolve => setTimeout(resolve, 1000));
          setClasses(classes.filter(c => c.id !== classId));
          message.success('班级删除成功');
        } catch (error) {
          message.error('班级删除失败');
        }
      }
    });
  };

  const handleStatusChange = async (classId: string, newStatus: ClassStatus) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      setClasses(classes.map(c => 
        c.id === classId ? { ...c, status: newStatus, updatedAt: new Date().toISOString() } : c
      ));
      message.success(`班级${newStatus === ClassStatus.ACTIVE ? '激活' : '停用'}成功`);
    } catch (error) {
      message.error(`班级${newStatus === ClassStatus.ACTIVE ? '激活' : '停用'}失败`);
    }
  };

  const handleCopy = async (classItem: Class) => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      const newClass: Class = {
        ...classItem,
        id: Date.now().toString(),
        name: `${classItem.name} - 副本`,
        currentStudents: 0,
        status: ClassStatus.INACTIVE,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      setClasses([newClass, ...classes]);
      message.success('班级复制成功');
    } catch (error) {
      message.error('班级复制失败');
    }
  };

  const handleBatchDelete = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的班级');
      return;
    }

    Modal.confirm({
      title: '批量删除',
      content: `确定要删除选中的 ${selectedRowKeys.length} 个班级吗？此操作不可恢复。`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          // 模拟API调用
          await new Promise(resolve => setTimeout(resolve, 1000));
          setClasses(classes.filter(c => !selectedRowKeys.includes(c.id)));
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
    message.success('班级数据导出成功');
  };

  const getStatusColor = (status: ClassStatus) => {
    switch (status) {
      case ClassStatus.ACTIVE: return 'green';
      case ClassStatus.INACTIVE: return 'red';
      default: return 'default';
    }
  };

  const getStatusText = (status: ClassStatus) => {
    switch (status) {
      case ClassStatus.ACTIVE: return '进行中';
      case ClassStatus.INACTIVE: return '已结束';
      default: return '未知';
    }
  };

  const getProgressColor = (rate: number) => {
    if (rate >= 90) return '#52c41a';
    if (rate >= 70) return '#1890ff';
    if (rate >= 50) return '#faad14';
    return '#f5222d';
  };

  const columns: ColumnsType<Class> = [
    {
      title: '班级信息',
      key: 'classInfo',
      width: 250,
      render: (_, record) => (
        <div>
          <div className="font-medium text-gray-900">{record.name}</div>
          <div className="text-sm text-gray-500">{record.courseName}</div>
          <div className="flex flex-wrap gap-1 mt-1">
            {record.tags?.map(tag => (
              <Tag key={tag}>{tag}</Tag>
            ))}
          </div>
        </div>
      )
    },
    {
      title: '授课教师',
      key: 'teacher',
      width: 120,
      render: (_, record) => (
        <div className="flex items-center">
          <Avatar size="small" icon={<UserOutlined />} className="mr-2" />
          <span>{record.teacherName}</span>
        </div>
      )
    },
    {
      title: '所属门店',
      dataIndex: 'storeName',
      key: 'storeName',
      width: 120
    },
    {
      title: '学生情况',
      key: 'students',
      width: 150,
      render: (_, record) => {
        const rate = (record.currentStudents / record.maxStudents) * 100;
        return (
          <div>
            <div className="flex justify-between text-sm">
              <span>{record.currentStudents}/{record.maxStudents}</span>
              <span>{rate.toFixed(0)}%</span>
            </div>
            <Progress 
              percent={rate} 
              size="small" 
              strokeColor={getProgressColor(rate)}
              showInfo={false}
            />
          </div>
        );
      }
    },
    {
      title: '时间安排',
      key: 'schedule',
      width: 200,
      render: (_, record) => (
        <div>
          <div className="text-sm">
            <CalendarOutlined className="mr-1" />
            {record.startDate} ~ {record.endDate}
          </div>
          <div className="text-xs text-gray-500 mt-1">
            {record.schedule?.map((s, index) => {
              const days = ['日', '一', '二', '三', '四', '五', '六'];
              return (
                <div key={index}>
                  周{days[s.dayOfWeek]} {s.startTime}-{s.endTime}
                </div>
              );
            })}
          </div>
        </div>
      )
    },
    {
      title: '状态',
      key: 'status',
      width: 100,
      render: (_, record) => (
        <div>
          <Tag color={getStatusColor(record.status)}>
            {getStatusText(record.status)}
          </Tag>
          <div className="text-xs text-gray-500 mt-1">
            {record.isPublic && <Tag color="blue">公开</Tag>}
            {record.allowSelfEnroll && <Tag color="green">自主报名</Tag>}
          </div>
        </div>
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
            label: record.status === ClassStatus.ACTIVE ? '停用' : '激活',
            icon: record.status === ClassStatus.ACTIVE ? <PauseCircleOutlined /> : <PlayCircleOutlined />,
            onClick: () => handleStatusChange(record.id, record.status === ClassStatus.ACTIVE ? ClassStatus.INACTIVE : ClassStatus.ACTIVE)
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

  const rowSelection: TableProps<Class>['rowSelection'] = {
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
        <h1 className="text-2xl font-bold text-gray-900">班级管理</h1>
        <p className="text-gray-600 mt-1">管理系统中的所有班级信息</p>
      </div>

      {/* 统计卡片 */}
      <Row gutter={16} className="mb-6">
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总班级数"
              value={stats.total}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="进行中"
              value={stats.active}
              prefix={<PlayCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="已结束"
              value={stats.inactive}
              prefix={<PauseCircleOutlined />}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
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
      <Card className="mb-4">
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={12} md={8} lg={6}>
            <Search
              placeholder="搜索班级名称、课程或教师"
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
              <Option value={ClassStatus.ACTIVE}>进行中</Option>
              <Option value={ClassStatus.INACTIVE}>已结束</Option>
            </Select>
          </Col>
          <Col xs={12} sm={6} md={4} lg={3}>
            <Select
              value={courseFilter}
              onChange={setCourseFilter}
              style={{ width: '100%' }}
              placeholder="课程筛选"
            >
              <Option value="all">全部课程</Option>
              {courses.map(course => (
                <Option key={course.id} value={course.id}>{course.title}</Option>
              ))}
            </Select>
          </Col>
          <Col xs={12} sm={6} md={4} lg={3}>
            <Select
              value={teacherFilter}
              onChange={setTeacherFilter}
              style={{ width: '100%' }}
              placeholder="教师筛选"
            >
              <Option value="all">全部教师</Option>
              {teachers.map(teacher => (
                <Option key={teacher.id} value={teacher.id}>{teacher.fullName}</Option>
              ))}
            </Select>
          </Col>
          <Col xs={12} sm={6} md={4} lg={3}>
            <Select
              value={storeFilter}
              onChange={setStoreFilter}
              style={{ width: '100%' }}
              placeholder="门店筛选"
            >
              <Option value="all">全部门店</Option>
              {stores.map(store => (
                <Option key={store.id} value={store.id}>{store.name}</Option>
              ))}
            </Select>
          </Col>
          <Col xs={24} sm={12} md={8} lg={6}>
            <Space>
              <PermissionGuard requiredPermissions={['class:create']}>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleCreate}
                >
                  新建班级
                </Button>
              </PermissionGuard>
              <Button
                icon={<ExportOutlined />}
                onClick={handleExport}
              >
                导出
              </Button>
              {selectedRowKeys.length > 0 && (
                <PermissionGuard requiredPermissions={['class:delete']}>
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

      {/* 班级列表 */}
      <Card>
        <Table<Class>
          columns={columns}
          dataSource={filteredClasses}
          rowKey="id"
          rowSelection={rowSelection}
          loading={loading}
          scroll={{ x: 1200 }}
          pagination={{
            total: filteredClasses.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
        />
      </Card>

      {/* 班级表单弹窗 */}
      <Modal
        title={editingClass ? '编辑班级' : '新建班级'}
        open={isFormVisible}
        onCancel={() => {
          setIsFormVisible(false);
          setEditingClass(null);
        }}
        footer={null}
        width={800}
        destroyOnHidden
      >
        <ClassForm
          initialValues={editingClass}
          onSubmit={async (classData) => {
            try {
              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 1000));
              
              if (editingClass) {
                // 更新班级
                setClasses(classes.map(classItem =>
                  classItem.id === editingClass.id
                    ? { ...classItem, ...classData, updatedAt: new Date().toISOString() }
                    : classItem
                ));
                message.success('班级更新成功');
              } else {
                // 创建新班级
                const newClass: Class = {
                  id: Date.now().toString(),
                  ...classData,
                  currentStudents: 0,
                  createdAt: new Date().toISOString(),
                  updatedAt: new Date().toISOString()
                } as Class;
                setClasses([newClass, ...classes]);
                message.success('班级创建成功');
              }
              
              setIsFormVisible(false);
              setEditingClass(null);
            } catch (error) {
              message.error(editingClass ? '班级更新失败' : '班级创建失败');
            }
          }}
          onCancel={() => {
            setIsFormVisible(false);
            setEditingClass(null);
          }}
        />
      </Modal>

      {/* 班级详情弹窗 */}
      <Modal
        title="班级详情"
        open={isDetailVisible}
        onCancel={() => {
          setIsDetailVisible(false);
          setViewingClass(null);
        }}
        footer={null}
        width={1000}
        destroyOnHidden
      >
        {viewingClass && (
          <ClassDetail
            classInfo={viewingClass}
            onEdit={() => {
              setIsDetailVisible(false);
              handleEdit(viewingClass);
            }}
            onDelete={() => {
              setIsDetailVisible(false);
              handleDelete(viewingClass.id);
            }}
            onCopy={() => {
              setIsDetailVisible(false);
              handleCopy(viewingClass);
            }}
            onActivate={() => {
              handleStatusChange(viewingClass.id, ClassStatus.ACTIVE);
              setViewingClass({ ...viewingClass, status: ClassStatus.ACTIVE });
            }}
            onDeactivate={() => {
              handleStatusChange(viewingClass.id, ClassStatus.INACTIVE);
              setViewingClass({ ...viewingClass, status: ClassStatus.INACTIVE });
            }}
            onComplete={() => {
              handleStatusChange(viewingClass.id, ClassStatus.COMPLETED);
              setViewingClass({ ...viewingClass, status: ClassStatus.COMPLETED });
            }}
          />
        )}
      </Modal>
    </div>
  );
};

export default ClassManagement;