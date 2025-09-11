import React, { useState, useEffect } from 'react';
import { useErrorHandler } from '../../hooks/useErrorHandler';
import {
  Card,
  Descriptions,
  Tag,
  Button,
  Space,
  Modal,
  Table,
  Progress,
  Statistic,
  Row,
  Col,
  Divider,
  List,
  Avatar,
  Typography,
  message,
  Popconfirm,
  Tabs
} from 'antd';
import {
  EditOutlined,
  DeleteOutlined,
  CopyOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  UserAddOutlined,
  UserDeleteOutlined,
  CalendarOutlined,
  UserOutlined,
  BookOutlined
} from '@ant-design/icons';
import type { Class } from '../../types';
import { PermissionGuard } from '../common';
import dayjs from 'dayjs';

const { Title, Paragraph } = Typography;


interface ClassDetailProps {
  classInfo: Class;
  onEdit: () => void;
  onDelete: () => void;
  onCopy: () => void;
  onActivate: () => void;
  onDeactivate: () => void;
  onComplete: () => void;
  loading?: boolean;
}

const ClassDetail: React.FC<ClassDetailProps> = ({
  classInfo,
  onEdit,
  onDelete,
  onCopy,
  onActivate,
  onDeactivate,
  onComplete,
  loading = false
}) => {
  const { handleError } = useErrorHandler();
  const [teacher, setTeacher] = useState<any>(null);
  const [course, setCourse] = useState<any>(null);
  const [store, setStore] = useState<any>(null);
  const [students, setStudents] = useState<any[]>([]);
  const [assignments, setAssignments] = useState<any[]>([]);
  const [stats, setStats] = useState({
    totalStudents: 0,
    activeStudents: 0,
    completedAssignments: 0,
    averageScore: 0,
    attendanceRate: 0
  });
  const [loadingStudents, setLoadingStudents] = useState(false);
  const [loadingAssignments, setLoadingAssignments] = useState(false);

  useEffect(() => {
    loadTeacherInfo();
    loadCourseInfo();
    loadStoreInfo();
    loadStudents();
    loadAssignments();
    loadStats();
  }, [classInfo.id]);

  const loadTeacherInfo = async () => {
    try {
      // 模拟加载教师信息
      setTeacher({
        id: classInfo.teacherId,
        fullName: '张老师',
        email: 'zhang@example.com',
        avatar: null
      });
    } catch (error) {
      handleError(error);
    }
  };

  const loadCourseInfo = async () => {
    try {
      // 模拟加载课程信息
      setCourse({
        id: classInfo.courseId,
        title: 'JavaScript基础课程',
        status: 'published'
      });
    } catch (error) {
      handleError(error);
    }
  };

  const loadStoreInfo = async () => {
    try {
      // 模拟加载门店信息
      setStore({
        id: classInfo.storeId,
        name: '北京朝阳店',
        address: '北京市朝阳区'
      });
    } catch (error) {
      handleError(error);
    }
  };

  const loadStudents = async () => {
    try {
      setLoadingStudents(true);
      // 模拟加载学生列表
      const mockStudents = [
        {
          id: '1',
          fullName: '李小明',
          email: 'lixiaoming@example.com',
          phone: '13800138001',
          enrolledAt: '2024-01-10',
          status: 'active',
          attendanceRate: 95,
          averageScore: 85
        },
        {
          id: '2',
          fullName: '王小红',
          email: 'wangxiaohong@example.com',
          phone: '13800138002',
          enrolledAt: '2024-01-12',
          status: 'active',
          attendanceRate: 88,
          averageScore: 78
        }
      ];
      setStudents(mockStudents);
    } catch (error) {
      message.error('加载学生列表失败');
    } finally {
      setLoadingStudents(false);
    }
  };

  const loadAssignments = async () => {
    try {
      setLoadingAssignments(true);
      // 模拟加载作业列表
      const mockAssignments = [
        {
          id: '1',
          title: 'JavaScript变量和数据类型',
          status: 'published',
          dueDate: '2024-01-20',
          submissionCount: 15,
          totalStudents: 20
        },
        {
          id: '2',
          title: '函数和作用域练习',
          status: 'published',
          dueDate: '2024-01-25',
          submissionCount: 12,
          totalStudents: 20
        }
      ];
      setAssignments(mockAssignments);
    } catch (error) {
      message.error('加载作业列表失败');
    } finally {
      setLoadingAssignments(false);
    }
  };

  const loadStats = async () => {
    try {
      // 模拟统计数据
      setStats({
        totalStudents: 20,
        activeStudents: 18,
        completedAssignments: 8,
        averageScore: 82.5,
        attendanceRate: 91.5
      });
    } catch (error) {
      handleError(error);
    }
  };

  const getStatusTag = (status: string) => {
    const statusMap = {
      active: { color: 'green', text: '活跃' },
      inactive: { color: 'default', text: '停用' },
      completed: { color: 'blue', text: '已完成' },
      cancelled: { color: 'red', text: '已取消' }
    };
    const config = statusMap[status as keyof typeof statusMap] || statusMap.active;
    return <Tag color={config.color}>{config.text}</Tag>;
  };

  const studentColumns = [
    {
      title: '学生姓名',
      dataIndex: 'fullName',
      key: 'fullName',
      render: (name: string, record: any) => (
        <Space>
          <Avatar size="small" icon={<UserOutlined />} />
          <span>{name}</span>
        </Space>
      )
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email'
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone'
    },
    {
      title: '报名时间',
      dataIndex: 'enrolledAt',
      key: 'enrolledAt',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD')
    },
    {
      title: '出勤率',
      dataIndex: 'attendanceRate',
      key: 'attendanceRate',
      render: (rate: number) => (
        <Progress
          percent={rate}
          size="small"
          status={rate >= 80 ? 'success' : rate >= 60 ? 'normal' : 'exception'}
        />
      )
    },
    {
      title: '平均分',
      dataIndex: 'averageScore',
      key: 'averageScore',
      render: (score: number) => {
        let color = 'red';
        if (score >= 80) color = 'green';
        else if (score >= 60) color = 'orange';
        return <Tag color={color}>{score}分</Tag>;
      }
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const statusMap = {
          active: { color: 'green', text: '在读' },
          completed: { color: 'blue', text: '已完成' },
          dropped: { color: 'red', text: '退学' }
        };
        const config = statusMap[status as keyof typeof statusMap] || statusMap.active;
        return <Tag color={config.color}>{config.text}</Tag>;
      }
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record: any) => (
        <Space size="small">
          <Button type="link" size="small">
            查看
          </Button>
          <PermissionGuard requiredPermissions={['class:manage_students']}>
            <Popconfirm
              title="确定要移除这个学生吗？"
              onConfirm={() => handleRemoveStudent(record.id)}
            >
              <Button type="link" size="small" danger>
                移除
              </Button>
            </Popconfirm>
          </PermissionGuard>
        </Space>
      )
    }
  ];

  const assignmentColumns = [
    {
      title: '作业标题',
      dataIndex: 'title',
      key: 'title',
      render: (title: string, record: any) => (
        <Space>
          <BookOutlined />
          <span>{title}</span>
        </Space>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const statusMap = {
          draft: { color: 'default', text: '草稿' },
          published: { color: 'green', text: '已发布' },
          completed: { color: 'blue', text: '已完成' }
        };
        const config = statusMap[status as keyof typeof statusMap] || statusMap.draft;
        return <Tag color={config.color}>{config.text}</Tag>;
      }
    },
    {
      title: '截止时间',
      dataIndex: 'dueDate',
      key: 'dueDate',
      render: (date: string) => dayjs(date).format('YYYY-MM-DD')
    },
    {
      title: '提交情况',
      key: 'submission',
      render: (_, record: any) => {
        const rate = (record.submissionCount / record.totalStudents) * 100;
        return (
          <Space>
            <span>{record.submissionCount}/{record.totalStudents}</span>
            <Progress percent={rate} size="small" />
          </Space>
        );
      }
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record: any) => (
        <Space size="small">
          <Button type="link" size="small">
            查看
          </Button>
          <Button type="link" size="small">
            批改
          </Button>
        </Space>
      )
    }
  ];

  const handleRemoveStudent = async (studentId: string) => {
    try {
      // 实现移除学生逻辑
      message.success('学生移除成功');
      loadStudents(); // 重新加载学生列表
    } catch (error) {
      message.error('学生移除失败');
    }
  };

  const renderSchedule = () => {
    if (!classInfo.schedule || classInfo.schedule.length === 0) {
      return <span>暂无安排</span>;
    }

    const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    
    return (
      <Space wrap>
        {classInfo.schedule.map((item: any, index: number) => (
          <Tag key={index} color="blue">
            {dayNames[item.dayOfWeek]} {item.startTime}-{item.endTime}
          </Tag>
        ))}
      </Space>
    );
  };

  return (
    <div className="class-detail">
      <Card
        title={
          <Space>
            <CalendarOutlined />
            <span>{classInfo.name}</span>
            {getStatusTag(classInfo.status)}
          </Space>
        }
        extra={
          <Space>
            <PermissionGuard requiredPermissions={['class:edit']}>
              <Button
                type="primary"
                icon={<EditOutlined />}
                onClick={onEdit}
                loading={loading}
              >
                编辑
              </Button>
            </PermissionGuard>
            
            <PermissionGuard requiredPermissions={['class:manage']}>
              {classInfo.status === 'active' ? (
                <Button
                  icon={<PauseCircleOutlined />}
                  onClick={onDeactivate}
                  loading={loading}
                >
                  停用
                </Button>
              ) : (
                <Button
                  type="primary"
                  ghost
                  icon={<PlayCircleOutlined />}
                  onClick={onActivate}
                  loading={loading}
                >
                  激活
                </Button>
              )}
            </PermissionGuard>

            <Button
              icon={<CopyOutlined />}
              onClick={onCopy}
              loading={loading}
            >
              复制
            </Button>

            <PermissionGuard requiredPermissions={['class:delete']}>
              <Popconfirm
                title="确定要删除这个班级吗？"
                description="删除后将无法恢复，请谨慎操作。"
                onConfirm={onDelete}
                okText="确定"
                cancelText="取消"
              >
                <Button
                  danger
                  icon={<DeleteOutlined />}
                  loading={loading}
                >
                  删除
                </Button>
              </Popconfirm>
            </PermissionGuard>
          </Space>
        }
      >
        <Row gutter={[16, 16]}>
          <Col span={16}>
            <Descriptions column={2} bordered>
              <Descriptions.Item label="班级ID">{classInfo.id}</Descriptions.Item>
              <Descriptions.Item label="状态">{getStatusTag(classInfo.status)}</Descriptions.Item>
              <Descriptions.Item label="所属课程">
                <Space>
                  <BookOutlined />
                  {course?.title || '加载中...'}
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="授课教师">
                <Space>
                  <Avatar size="small" icon={<UserOutlined />} src={teacher?.avatar} />
                  {teacher?.fullName || '加载中...'}
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="所属门店">
                {store ? `${store.name} - ${store.address}` : '加载中...'}
              </Descriptions.Item>
              <Descriptions.Item label="最大学生数">{classInfo.maxStudents}人</Descriptions.Item>
              <Descriptions.Item label="开始日期">
                {classInfo.startDate ? dayjs(classInfo.startDate).format('YYYY-MM-DD') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="结束日期">
                {classInfo.endDate ? dayjs(classInfo.endDate).format('YYYY-MM-DD') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="公开状态">
                <Tag color={classInfo.isPublic ? 'green' : 'default'}>
                  {classInfo.isPublic ? '公开' : '私有'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="自主报名">
                <Tag color={classInfo.allowSelfEnroll ? 'green' : 'red'}>
                  {classInfo.allowSelfEnroll ? '允许' : '不允许'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {dayjs(classInfo.createdAt).format('YYYY-MM-DD HH:mm')}
              </Descriptions.Item>
              <Descriptions.Item label="更新时间">
                {dayjs(classInfo.updatedAt).format('YYYY-MM-DD HH:mm')}
              </Descriptions.Item>
            </Descriptions>
          </Col>
          
          <Col span={8}>
            <Card title="班级统计" size="small">
              <Row gutter={[16, 16]}>
                <Col span={12}>
                  <Statistic
                    title="总学生数"
                    value={stats.totalStudents}
                    suffix="人"
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="活跃学生"
                    value={stats.activeStudents}
                    suffix="人"
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="完成作业"
                    value={stats.completedAssignments}
                    suffix="份"
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="平均分"
                    value={stats.averageScore}
                    precision={1}
                    suffix="分"
                  />
                </Col>
              </Row>
              <Divider />
              <div>
                <div style={{ marginBottom: 8 }}>出勤率</div>
                <Progress
                  percent={stats.attendanceRate}
                  status={stats.attendanceRate > 80 ? 'success' : 'active'}
                  strokeColor={{
                    '0%': '#108ee9',
                    '100%': '#87d068',
                  }}
                />
              </div>
            </Card>
          </Col>
        </Row>

        <Divider orientation="left">班级描述</Divider>
        <Paragraph>
          {classInfo.description || '暂无描述'}
        </Paragraph>

        <Divider orientation="left">上课时间安排</Divider>
        {renderSchedule()}

        {classInfo.tags && classInfo.tags.length > 0 && (
          <>
            <Divider orientation="left">标签</Divider>
            <Space wrap>
              {classInfo.tags.map((tag, index) => (
                <Tag key={index} color="blue">{tag}</Tag>
              ))}
            </Space>
          </>
        )}
      </Card>

      <Card title="班级管理" style={{ marginTop: 16 }}>
        <Tabs 
          defaultActiveKey="students"
          items={[
            {
              key: 'students',
              label: '学生管理',
              children: (
                <>
                  <div style={{ marginBottom: 16 }}>
                    <PermissionGuard requiredPermissions={['class:manage_students']}>
                      <Button
                        type="primary"
                        icon={<UserAddOutlined />}
                        onClick={() => message.info('添加学生功能开发中')}
                      >
                        添加学生
                      </Button>
                    </PermissionGuard>
                  </div>
                  <Table
                    columns={studentColumns}
                    dataSource={students}
                    rowKey="id"
                    loading={loadingStudents}
                    pagination={{
                      pageSize: 10,
                      showSizeChanger: true,
                      showQuickJumper: true,
                      showTotal: (total) => `共 ${total} 名学生`
                    }}
                  />
                </>
              ),
            },
            {
              key: 'assignments',
              label: '作业管理',
              children: (
                <Table
                  columns={assignmentColumns}
                  dataSource={assignments}
                  rowKey="id"
                  loading={loadingAssignments}
                  pagination={{
                    pageSize: 10,
                    showSizeChanger: true,
                    showQuickJumper: true,
                    showTotal: (total) => `共 ${total} 个作业`
                  }}
                />
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
};

export default ClassDetail;
export { ClassDetail };