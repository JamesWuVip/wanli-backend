import React, { useState, useEffect } from 'react';
import { useErrorHandler } from '../hooks/useErrorHandler';
import {
  Card,
  Row,
  Col,
  Statistic,
  Progress,
  List,
  Avatar,
  Button,
  Space,
  Tag,
  Table,
  Select,
  DatePicker,
  Divider,
  Alert,
  Badge,
  Tooltip
} from 'antd';
import {
  UserOutlined,
  BookOutlined,
  FileTextOutlined,
  HomeOutlined,
  TeamOutlined,
  TrophyOutlined,
  ClockCircleOutlined,
  RiseOutlined,
  FallOutlined,
  EyeOutlined,
  EditOutlined,
  PlusOutlined,
  BarChartOutlined,
  LineChartOutlined,
  PieChartOutlined
} from '@ant-design/icons';
import { LineChart, Line, AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, Legend, ResponsiveContainer } from 'recharts';
import type { User } from '../types/user';
import type { Course, Assignment } from '../types';
import type { Class } from '../types/class';
import type { Store } from '../types/store';
import { PermissionGuard } from '../components/common';
import { useNavigate } from 'react-router-dom';

const { RangePicker } = DatePicker;
const { Option } = Select;

interface DashboardStats {
  totalUsers: number;
  totalCourses: number;
  totalAssignments: number;
  totalClasses: number;
  totalStores: number;
  activeUsers: number;
  publishedCourses: number;
  activeClasses: number;
  completedAssignments: number;
  userGrowth: number;
  courseGrowth: number;
  assignmentGrowth: number;
  classGrowth: number;
}

interface RecentActivity {
  id: string;
  type: 'user' | 'course' | 'assignment' | 'class';
  title: string;
  description: string;
  time: string;
  user: string;
  status: string;
}

const Dashboard: React.FC = () => {
  const { handleError } = useErrorHandler();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState<DashboardStats>({
    totalUsers: 0,
    totalCourses: 0,
    totalAssignments: 0,
    totalClasses: 0,
    totalStores: 0,
    activeUsers: 0,
    publishedCourses: 0,
    activeClasses: 0,
    completedAssignments: 0,
    userGrowth: 0,
    courseGrowth: 0,
    assignmentGrowth: 0,
    classGrowth: 0
  });
  const [recentActivities, setRecentActivities] = useState<RecentActivity[]>([]);
  const [chartData, setChartData] = useState({
    userTrend: [],
    courseTrend: [],
    assignmentStats: [],
    storeDistribution: []
  });
  const [selectedTimeRange, setSelectedTimeRange] = useState('7d');

  useEffect(() => {
    loadDashboardData();
  }, [selectedTimeRange]);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      // 模拟加载统计数据
      const mockStats: DashboardStats = {
        totalUsers: Math.floor(Math.random() * 1000) + 500,
        totalCourses: Math.floor(Math.random() * 100) + 50,
        totalAssignments: Math.floor(Math.random() * 200) + 100,
        totalClasses: Math.floor(Math.random() * 150) + 80,
        totalStores: Math.floor(Math.random() * 20) + 10,
        activeUsers: Math.floor(Math.random() * 300) + 200,
        publishedCourses: Math.floor(Math.random() * 60) + 30,
        activeClasses: Math.floor(Math.random() * 80) + 40,
        completedAssignments: Math.floor(Math.random() * 150) + 80,
        userGrowth: Math.floor(Math.random() * 20) + 5,
        courseGrowth: Math.floor(Math.random() * 15) + 3,
        assignmentGrowth: Math.floor(Math.random() * 25) + 8,
        classGrowth: Math.floor(Math.random() * 18) + 6
      };
      setStats(mockStats);

      // 模拟最近活动
      const mockActivities: RecentActivity[] = [
        {
          id: '1',
          type: 'user',
          title: '新用户注册',
          description: '张三注册了新账户',
          time: '2分钟前',
          user: '张三',
          status: 'success'
        },
        {
          id: '2',
          type: 'course',
          title: '课程发布',
          description: '李老师发布了《高等数学》课程',
          time: '5分钟前',
          user: '李老师',
          status: 'success'
        },
        {
          id: '3',
          type: 'assignment',
          title: '作业提交',
          description: '王同学提交了数学作业',
          time: '10分钟前',
          user: '王同学',
          status: 'info'
        },
        {
          id: '4',
          type: 'class',
          title: '班级创建',
          description: '创建了《英语口语班》',
          time: '15分钟前',
          user: '管理员',
          status: 'success'
        }
      ];
      setRecentActivities(mockActivities);

      // 模拟图表数据
      const mockChartData = {
        userTrend: [
          { date: '01-01', users: 120, newUsers: 20 },
          { date: '01-02', users: 132, newUsers: 12 },
          { date: '01-03', users: 145, newUsers: 13 },
          { date: '01-04', users: 158, newUsers: 13 },
          { date: '01-05', users: 170, newUsers: 12 },
          { date: '01-06', users: 185, newUsers: 15 },
          { date: '01-07', users: 200, newUsers: 15 }
        ],
        courseTrend: [
          { date: '01-01', courses: 45, published: 30 },
          { date: '01-02', courses: 47, published: 32 },
          { date: '01-03', courses: 50, published: 35 },
          { date: '01-04', courses: 52, published: 37 },
          { date: '01-05', courses: 55, published: 40 },
          { date: '01-06', courses: 58, published: 42 },
          { date: '01-07', courses: 60, published: 45 }
        ],
        assignmentStats: [
          { name: '已完成', value: 65, color: '#52c41a' },
          { name: '进行中', value: 25, color: '#1890ff' },
          { name: '未开始', value: 10, color: '#faad14' }
        ],
        storeDistribution: [
          { name: '北京', value: 5, color: '#1890ff' },
          { name: '上海', value: 4, color: '#52c41a' },
          { name: '广州', value: 3, color: '#faad14' },
          { name: '深圳', value: 3, color: '#f5222d' },
          { name: '其他', value: 5, color: '#722ed1' }
        ]
      };
      setChartData(mockChartData);
    } catch (error) {
      handleError(error);
    } finally {
      setLoading(false);
    }
  };

  const getActivityIcon = (type: string) => {
    const icons = {
      user: <UserOutlined />,
      course: <BookOutlined />,
      assignment: <FileTextOutlined />,
      class: <TeamOutlined />
    };
    return icons[type as keyof typeof icons] || <UserOutlined />;
  };

  const getActivityColor = (type: string) => {
    const colors = {
      user: '#1890ff',
      course: '#52c41a',
      assignment: '#faad14',
      class: '#722ed1'
    };
    return colors[type as keyof typeof colors] || '#1890ff';
  };

  const quickActions = [
    {
      title: '创建用户',
      icon: <UserOutlined />,
      color: '#1890ff',
      path: '/management/users/create',
      permission: 'user:create'
    },
    {
      title: '创建课程',
      icon: <BookOutlined />,
      color: '#52c41a',
      path: '/management/courses/create',
      permission: 'course:create'
    },
    {
      title: '创建作业',
      icon: <FileTextOutlined />,
      color: '#faad14',
      path: '/management/assignments/create',
      permission: 'assignment:create'
    },
    {
      title: '创建班级',
      icon: <TeamOutlined />,
      color: '#722ed1',
      path: '/management/classes/create',
      permission: 'class:create'
    },
    {
      title: '创建门店',
      icon: <HomeOutlined />,
      color: '#f5222d',
      path: '/management/stores/create',
      permission: 'store:create'
    }
  ];

  const topPerformers = [
    { name: '李老师', courses: 12, students: 156, rating: 4.9 },
    { name: '王老师', courses: 10, students: 142, rating: 4.8 },
    { name: '张老师', courses: 8, students: 128, rating: 4.7 },
    { name: '刘老师', courses: 7, students: 115, rating: 4.6 },
    { name: '陈老师', courses: 6, students: 98, rating: 4.5 }
  ];

  const performerColumns = [
    {
      title: '教师',
      dataIndex: 'name',
      key: 'name',
      render: (name: string) => (
        <Space>
          <Avatar icon={<UserOutlined />} />
          {name}
        </Space>
      )
    },
    {
      title: '课程数',
      dataIndex: 'courses',
      key: 'courses',
      render: (courses: number) => <Tag color="blue">{courses}</Tag>
    },
    {
      title: '学生数',
      dataIndex: 'students',
      key: 'students',
      render: (students: number) => <Tag color="green">{students}</Tag>
    },
    {
      title: '评分',
      dataIndex: 'rating',
      key: 'rating',
      render: (rating: number) => (
        <Space>
          <TrophyOutlined style={{ color: '#faad14' }} />
          {rating}
        </Space>
      )
    }
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面标题和快捷操作 */}
      <Row justify="space-between" align="middle" style={{ marginBottom: 24 }}>
        <Col>
          <h1 style={{ margin: 0, fontSize: '24px', fontWeight: 600 }}>
            仪表盘
          </h1>
          <p style={{ margin: '8px 0 0 0', color: '#666' }}>
            欢迎回来！这里是您的工作概览
          </p>
        </Col>
        <Col>
          <Space>
            <Select
              value={selectedTimeRange}
              onChange={setSelectedTimeRange}
              style={{ width: 120 }}
            >
              <Option value="7d">最近7天</Option>
              <Option value="30d">最近30天</Option>
              <Option value="90d">最近90天</Option>
            </Select>
            <Button icon={<BarChartOutlined />}>导出报告</Button>
          </Space>
        </Col>
      </Row>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="总用户数"
              value={stats.totalUsers}
              prefix={<UserOutlined />}
              suffix={
                <Space>
                  <span>人</span>
                  <Tag color={stats.userGrowth > 0 ? 'green' : 'red'}>
                    {stats.userGrowth > 0 ? <RiseOutlined /> : <FallOutlined />}
                    {Math.abs(stats.userGrowth)}%
                  </Tag>
                </Space>
              }
            />
            <Progress
              percent={(stats.activeUsers / stats.totalUsers) * 100}
              size="small"
              showInfo={false}
              strokeColor="#1890ff"
              style={{ marginTop: 8 }}
            />
            <p style={{ margin: '4px 0 0 0', fontSize: '12px', color: '#666' }}>
              活跃用户: {stats.activeUsers}
            </p>
          </Card>
        </Col>
        
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="总课程数"
              value={stats.totalCourses}
              prefix={<BookOutlined />}
              suffix={
                <Space>
                  <span>门</span>
                  <Tag color={stats.courseGrowth > 0 ? 'green' : 'red'}>
                    {stats.courseGrowth > 0 ? <RiseOutlined /> : <FallOutlined />}
                    {Math.abs(stats.courseGrowth)}%
                  </Tag>
                </Space>
              }
            />
            <Progress
              percent={(stats.publishedCourses / stats.totalCourses) * 100}
              size="small"
              showInfo={false}
              strokeColor="#52c41a"
              style={{ marginTop: 8 }}
            />
            <p style={{ margin: '4px 0 0 0', fontSize: '12px', color: '#666' }}>
              已发布: {stats.publishedCourses}
            </p>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="总作业数"
              value={stats.totalAssignments}
              prefix={<FileTextOutlined />}
              suffix={
                <Space>
                  <span>个</span>
                  <Tag color={stats.assignmentGrowth > 0 ? 'green' : 'red'}>
                    {stats.assignmentGrowth > 0 ? <RiseOutlined /> : <FallOutlined />}
                    {Math.abs(stats.assignmentGrowth)}%
                  </Tag>
                </Space>
              }
            />
            <Progress
              percent={(stats.completedAssignments / stats.totalAssignments) * 100}
              size="small"
              showInfo={false}
              strokeColor="#faad14"
              style={{ marginTop: 8 }}
            />
            <p style={{ margin: '4px 0 0 0', fontSize: '12px', color: '#666' }}>
              已完成: {stats.completedAssignments}
            </p>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="总班级数"
              value={stats.totalClasses}
              prefix={<TeamOutlined />}
              suffix={
                <Space>
                  <span>个</span>
                  <Tag color={stats.classGrowth > 0 ? 'green' : 'red'}>
                    {stats.classGrowth > 0 ? <RiseOutlined /> : <FallOutlined />}
                    {Math.abs(stats.classGrowth)}%
                  </Tag>
                </Space>
              }
            />
            <Progress
              percent={(stats.activeClasses / stats.totalClasses) * 100}
              size="small"
              showInfo={false}
              strokeColor="#722ed1"
              style={{ marginTop: 8 }}
            />
            <p style={{ margin: '4px 0 0 0', fontSize: '12px', color: '#666' }}>
              活跃班级: {stats.activeClasses}
            </p>
          </Card>
        </Col>
      </Row>

      {/* 快捷操作 */}
      <Card title="快捷操作" style={{ marginBottom: 24 }}>
        <Row gutter={[16, 16]}>
          {quickActions.map((action, index) => (
            <Col key={index} xs={24} sm={12} md={8} lg={4.8}>
              <PermissionGuard permission={action.permission}>
                <Card
                  hoverable
                  size="small"
                  onClick={() => navigate(action.path)}
                  style={{
                    textAlign: 'center',
                    borderColor: action.color,
                    cursor: 'pointer'
                  }}
                >
                  <div style={{ color: action.color, fontSize: '24px', marginBottom: 8 }}>
                    {action.icon}
                  </div>
                  <div style={{ fontWeight: 500 }}>{action.title}</div>
                </Card>
              </PermissionGuard>
            </Col>
          ))}
        </Row>
      </Card>

      <Row gutter={[16, 16]}>
        {/* 用户趋势图 */}
        <Col xs={24} lg={12}>
          <Card title="用户增长趋势" extra={<LineChartOutlined />}>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={chartData.userTrend}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <RechartsTooltip />
                <Legend />
                <Area
                  type="monotone"
                  dataKey="users"
                  stackId="1"
                  stroke="#1890ff"
                  fill="#1890ff"
                  fillOpacity={0.6}
                  name="总用户"
                />
                <Area
                  type="monotone"
                  dataKey="newUsers"
                  stackId="2"
                  stroke="#52c41a"
                  fill="#52c41a"
                  fillOpacity={0.6}
                  name="新增用户"
                />
              </AreaChart>
            </ResponsiveContainer>
          </Card>
        </Col>

        {/* 课程趋势图 */}
        <Col xs={24} lg={12}>
          <Card title="课程发布趋势" extra={<BarChartOutlined />}>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData.courseTrend}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <RechartsTooltip />
                <Legend />
                <Bar dataKey="courses" fill="#1890ff" name="总课程" />
                <Bar dataKey="published" fill="#52c41a" name="已发布" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>

        {/* 作业完成情况 */}
        <Col xs={24} lg={12}>
          <Card title="作业完成情况" extra={<PieChartOutlined />}>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={chartData.assignmentStats}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {chartData.assignmentStats.map((entry: any, index: number) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <RechartsTooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>

        {/* 门店分布 */}
        <Col xs={24} lg={12}>
          <Card title="门店分布" extra={<PieChartOutlined />}>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={chartData.storeDistribution}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, value }) => `${name} ${value}`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {chartData.storeDistribution.map((entry: any, index: number) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <RechartsTooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        {/* 最近活动 */}
        <Col xs={24} lg={12}>
          <Card title="最近活动" extra={<ClockCircleOutlined />}>
            <List
              itemLayout="horizontal"
              dataSource={recentActivities}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={
                      <Avatar
                        style={{ backgroundColor: getActivityColor(item.type) }}
                        icon={getActivityIcon(item.type)}
                      />
                    }
                    title={
                      <Space>
                        <span>{item.title}</span>
                        <Badge status={item.status as any} />
                      </Space>
                    }
                    description={
                      <div>
                        <div>{item.description}</div>
                        <div style={{ fontSize: '12px', color: '#999', marginTop: 4 }}>
                          {item.user} · {item.time}
                        </div>
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>

        {/* 优秀教师排行 */}
        <Col xs={24} lg={12}>
          <Card title="优秀教师排行" extra={<TrophyOutlined />}>
            <Table
              dataSource={topPerformers}
              columns={performerColumns}
              pagination={false}
              size="small"
              rowKey="name"
            />
          </Card>
        </Col>
      </Row>

      {/* 系统提醒 */}
      <Row style={{ marginTop: 16 }}>
        <Col span={24}>
          <Alert
            message="系统提醒"
            description="您有3个待处理的课程审核请求，2个新的用户注册申请需要审批。"
            type="info"
            showIcon
            action={
              <Space>
                <Button size="small" type="ghost">
                  查看详情
                </Button>
                <Button size="small" type="primary">
                  立即处理
                </Button>
              </Space>
            }
            closable
          />
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;