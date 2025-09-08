import React from 'react';
import { useErrorHandler } from '../hooks/useErrorHandler';
import { Button, Card, Typography, Space, Row, Col } from 'antd';
import { LoginOutlined, UserAddOutlined, DashboardOutlined } from '@ant-design/icons';
import { Link } from 'react-router-dom';

const { Title, Paragraph } = Typography;

export default function Home() {
  return (
    <div style={{ minHeight: '100vh', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', padding: '40px 20px' }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: '60px' }}>
          <Title level={1} style={{ color: 'white', fontSize: '3rem', marginBottom: '20px' }}>
            加盟连锁门店作业管理系统
          </Title>
          <Paragraph style={{ color: 'rgba(255,255,255,0.9)', fontSize: '1.2rem', maxWidth: '600px', margin: '0 auto' }}>
            专为加盟连锁门店设计的智能作业管理平台，提供完整的教学管理解决方案
          </Paragraph>
        </div>

        <Row gutter={[32, 32]} justify="center">
          <Col xs={24} sm={12} lg={8}>
            <Card
              hoverable
              style={{ height: '280px', textAlign: 'center', borderRadius: '12px' }}
              styles={{ body: { padding: '40px 24px' } }}
            >
              <DashboardOutlined style={{ fontSize: '48px', color: '#1890ff', marginBottom: '20px' }} />
              <Title level={3} style={{ marginBottom: '16px' }}>智能仪表盘</Title>
              <Paragraph style={{ color: '#666', marginBottom: '24px' }}>
                实时监控门店运营数据，掌握教学进度和学员表现
              </Paragraph>
              <Link to="/dashboard">
                <Button type="primary" size="large">
                  进入仪表盘
                </Button>
              </Link>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={8}>
            <Card
              hoverable
              style={{ height: '280px', textAlign: 'center', borderRadius: '12px' }}
              styles={{ body: { padding: '40px 24px' } }}
            >
              <LoginOutlined style={{ fontSize: '48px', color: '#52c41a', marginBottom: '20px' }} />
              <Title level={3} style={{ marginBottom: '16px' }}>用户登录</Title>
              <Paragraph style={{ color: '#666', marginBottom: '24px' }}>
                已有账户？立即登录开始使用系统功能
              </Paragraph>
              <Link to="/auth/login">
                <Button type="primary" size="large" style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}>
                  立即登录
                </Button>
              </Link>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={8}>
            <Card
              hoverable
              style={{ height: '280px', textAlign: 'center', borderRadius: '12px' }}
              styles={{ body: { padding: '40px 24px' } }}
            >
              <UserAddOutlined style={{ fontSize: '48px', color: '#fa8c16', marginBottom: '20px' }} />
              <Title level={3} style={{ marginBottom: '16px' }}>新用户注册</Title>
              <Paragraph style={{ color: '#666', marginBottom: '24px' }}>
                还没有账户？快速注册加入我们的教学平台
              </Paragraph>
              <Link to="/auth/register">
                <Button type="primary" size="large" style={{ backgroundColor: '#fa8c16', borderColor: '#fa8c16' }}>
                  免费注册
                </Button>
              </Link>
            </Card>
          </Col>
        </Row>

        <div style={{ textAlign: 'center', marginTop: '80px' }}>
          <Card style={{ background: 'rgba(255,255,255,0.1)', border: 'none', borderRadius: '12px' }}>
            <Title level={2} style={{ color: 'white', marginBottom: '24px' }}>核心功能特色</Title>
            <Row gutter={[24, 24]}>
              <Col xs={24} sm={12} md={6}>
                <div style={{ color: 'white', textAlign: 'center' }}>
                  <Title level={4} style={{ color: 'white' }}>多角色管理</Title>
                  <Paragraph style={{ color: 'rgba(255,255,255,0.8)' }}>
                    支持总部、门店、教师、学生多角色权限管理
                  </Paragraph>
                </div>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <div style={{ color: 'white', textAlign: 'center' }}>
                  <Title level={4} style={{ color: 'white' }}>课程管理</Title>
                  <Paragraph style={{ color: 'rgba(255,255,255,0.8)' }}>
                    完整的课程创建、发布、管理流程
                  </Paragraph>
                </div>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <div style={{ color: 'white', textAlign: 'center' }}>
                  <Title level={4} style={{ color: 'white' }}>作业系统</Title>
                  <Paragraph style={{ color: 'rgba(255,255,255,0.8)' }}>
                    智能作业布置、提交、批改一体化
                  </Paragraph>
                </div>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <div style={{ color: 'white', textAlign: 'center' }}>
                  <Title level={4} style={{ color: 'white' }}>数据分析</Title>
                  <Paragraph style={{ color: 'rgba(255,255,255,0.8)' }}>
                    实时数据统计和可视化分析报告
                  </Paragraph>
                </div>
              </Col>
            </Row>
          </Card>
        </div>
      </div>
    </div>
  );
}