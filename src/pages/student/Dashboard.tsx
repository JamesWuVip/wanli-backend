import React from 'react';
import { useErrorHandler } from '../../hooks/useErrorHandler';
import { Card, Row, Col, Statistic, List, Progress, Calendar, Badge } from 'antd';
import { BookOutlined, FileTextOutlined, TrophyOutlined, ClockCircleOutlined } from '@ant-design/icons';
import type { Dayjs } from 'dayjs';

interface StudentStats {
  totalCourses: number;
  completedAssignments: number;
  pendingAssignments: number;
  averageScore: number;
}

interface RecentActivity {
  id: string;
  type: 'assignment' | 'course' | 'grade';
  title: string;
  description: string;
  time: string;
}

const StudentDashboard: React.FC = () => {
  const handleError = useErrorHandler();
  
  // 模拟数据
  const stats: StudentStats = {
    totalCourses: 5,
    completedAssignments: 12,
    pendingAssignments: 3,
    averageScore: 85.6
  };

  const recentActivities: RecentActivity[] = [
    {
      id: '1',
      type: 'assignment',
      title: '数学作业提交',
      description: '已完成第三章练习题',
      time: '2小时前'
    },
    {
      id: '2',
      type: 'grade',
      title: '英语测试成绩',
      description: '获得92分',
      time: '1天前'
    },
    {
      id: '3',
      type: 'course',
      title: '新课程开始',
      description: '物理课程已开课',
      time: '2天前'
    }
  ];

  const getListData = (value: Dayjs) => {
    let listData;
    switch (value.date()) {
      case 8:
        listData = [
          { type: 'warning', content: '数学作业截止' },
          { type: 'success', content: '英语课程' },
        ];
        break;
      case 10:
        listData = [
          { type: 'warning', content: '物理实验' },
          { type: 'success', content: '化学课程' },
          { type: 'error', content: '历史测试' },
        ];
        break;
      case 15:
        listData = [
          { type: 'warning', content: '期中考试' },
          { type: 'success', content: '语文课程' },
          { type: 'error', content: '生物作业截止' },
        ];
        break;
      default:
    }
    
    return listData || [];
  };

  const cellRender = (value: Dayjs) => {
    const listData = getListData(value);
    return (
      <ul className="events">
        {listData.map((item, index) => (
          <li key={index}>
            <Badge status={item.type as any} text={item.content} />
          </li>
        ))}
      </ul>
    );
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6">学生仪表盘</h1>
      
      {/* 统计卡片 */}
      <Row gutter={[16, 16]} className="mb-6">
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="在读课程"
              value={stats.totalCourses}
              prefix={<BookOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="已完成作业"
              value={stats.completedAssignments}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="待完成作业"
              value={stats.pendingAssignments}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="平均成绩"
              value={stats.averageScore}
              precision={1}
              prefix={<TrophyOutlined />}
              suffix="分"
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        {/* 最近活动 */}
        <Col xs={24} lg={12}>
          <Card title="最近活动" className="h-96">
            <List
              itemLayout="horizontal"
              dataSource={recentActivities}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={
                      item.type === 'assignment' ? <FileTextOutlined /> :
                      item.type === 'course' ? <BookOutlined /> :
                      <TrophyOutlined />
                    }
                    title={item.title}
                    description={item.description}
                  />
                  <div className="text-gray-500 text-sm">{item.time}</div>
                </List.Item>
              )}
            />
          </Card>
        </Col>

        {/* 学习日历 */}
        <Col xs={24} lg={12}>
          <Card title="学习日历" className="h-96">
            <Calendar
              fullscreen={false}
              cellRender={cellRender}
            />
          </Card>
        </Col>
      </Row>

      {/* 学习进度 */}
      <Row gutter={[16, 16]} className="mt-6">
        <Col span={24}>
          <Card title="课程学习进度">
            <Row gutter={[16, 16]}>
              <Col xs={24} md={8}>
                <div className="text-center">
                  <Progress type="circle" percent={75} />
                  <div className="mt-2">数学</div>
                </div>
              </Col>
              <Col xs={24} md={8}>
                <div className="text-center">
                  <Progress type="circle" percent={60} status="active" />
                  <div className="mt-2">英语</div>
                </div>
              </Col>
              <Col xs={24} md={8}>
                <div className="text-center">
                  <Progress type="circle" percent={30} status="exception" />
                  <div className="mt-2">物理</div>
                </div>
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default StudentDashboard;