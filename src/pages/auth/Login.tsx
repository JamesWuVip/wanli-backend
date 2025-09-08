import React, { useState } from 'react';
import { Form, Input, Button, Checkbox, message, Typography, Space, Divider } from 'antd';
import { UserOutlined, LockOutlined, EyeInvisibleOutlined, EyeTwoTone } from '@ant-design/icons';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../stores';
import { LoginRequest } from '../../types';

const { Title, Text } = Typography;

interface LocationState {
  from?: {
    pathname: string;
  };
}

/**
 * 登录页面组件
 */
export const Login: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuthStore();

  // 获取重定向路径
  const from = (
    typeof location.state?.from === 'string' 
      ? location.state.from 
      : typeof location.state?.from === 'object' && location.state.from?.pathname
        ? location.state.from.pathname
        : location.state?.from || '/dashboard'
  ) as string;
  
  console.log('Login component - location.state:', location.state);
  console.log('Login component - from:', from);

  // 处理登录提交
  const handleSubmit = async (values: LoginRequest & { remember: boolean }) => {
    setLoading(true);
    try {
      await login({
        username: values.username,
        password: values.password,
      }, values.remember);
      message.success('登录成功');
      navigate(from, { replace: true });
    } catch (error) {
      message.error('登录失败，请检查用户名和密码');
    } finally {
      setLoading(false);
    }
  };

  // 处理演示登录
  const handleDemoLogin = async (role: 'admin' | 'teacher' | 'student') => {
    const demoAccounts = {
      admin: { username: 'admin', password: 'admin123' },
      teacher: { username: 'teacher', password: 'teacher123' },
      student: { username: 'student', password: 'student123' },
    };

    const account = demoAccounts[role];
    form.setFieldsValue({ ...account, remember: true });

    setLoading(true);
    try {
      await login(account, true);
      message.success(`${role === 'admin' ? '管理员' : role === 'teacher' ? '教师' : '学生'}登录成功`);
      navigate(from, { replace: true });
    } catch (error) {
      message.error('演示登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md mx-auto">
      <div className="text-center mb-8">
        <Title level={2} className="mb-2">
          欢迎登录
        </Title>
        <Text type="secondary">
          加盟连锁门店作业管理系统
        </Text>
      </div>

      <Form
        form={form}
        name="login"
        onFinish={handleSubmit}
        autoComplete="off"
        size="large"
        className="space-y-4"
      >
        <Form.Item
          name="username"
          rules={[
            { required: true, message: '请输入用户名' },
            { min: 3, message: '用户名至少3个字符' },
          ]}
        >
          <Input
            data-testid="username-input"
            prefix={<UserOutlined className="text-gray-400" />}
            placeholder="用户名"
            autoComplete="username"
          />
        </Form.Item>

        <Form.Item
          name="password"
          rules={[
            { required: true, message: '请输入密码' },
            { min: 6, message: '密码至少6个字符' },
          ]}
        >
          <Input.Password
            data-testid="password-input"
            prefix={<LockOutlined className="text-gray-400" />}
            placeholder="密码"
            autoComplete="current-password"
            iconRender={(visible) => (visible ? <EyeTwoTone /> : <EyeInvisibleOutlined />)}
          />
        </Form.Item>

        <Form.Item>
          <div className="flex justify-between items-center">
            <Form.Item name="remember" valuePropName="checked" noStyle>
              <Checkbox data-testid="remember-me-checkbox">记住我</Checkbox>
            </Form.Item>
            <Link to="/auth/forgot-password" className="text-blue-600 hover:text-blue-500">
              忘记密码？
            </Link>
          </div>
        </Form.Item>

        <Form.Item>
          <Button
            data-testid="login-submit-btn"
            type="primary"
            htmlType="submit"
            loading={loading}
            className="w-full h-12 text-base font-medium"
          >
            登录
          </Button>
        </Form.Item>
      </Form>

      <Divider>
        <Text type="secondary" className="text-sm">
          演示账号
        </Text>
      </Divider>

      <div className="space-y-2">
        <Button
          block
          size="large"
          onClick={() => handleDemoLogin('admin')}
          loading={loading}
          className="h-10"
        >
          管理员演示
        </Button>
        <div className="grid grid-cols-2 gap-2">
          <Button
            size="large"
            onClick={() => handleDemoLogin('teacher')}
            loading={loading}
            className="h-10"
          >
            教师演示
          </Button>
          <Button
            size="large"
            onClick={() => handleDemoLogin('student')}
            loading={loading}
            className="h-10"
          >
            学生演示
          </Button>
        </div>
      </div>

      <div className="text-center mt-6">
        <Text type="secondary">
          还没有账号？{' '}
          <Link to="/auth/register" className="text-blue-600 hover:text-blue-500 font-medium">
            立即注册
          </Link>
        </Text>
      </div>

      <div className="mt-8 p-4 bg-blue-50 rounded-lg">
        <Text type="secondary" className="text-sm">
          <strong>演示说明：</strong>
          <br />
          • 管理员：可访问所有功能模块
          <br />
          • 教师：可管理课程和作业
          <br />
          • 学生：可查看和提交作业
        </Text>
      </div>
    </div>
  );
};

export default Login;