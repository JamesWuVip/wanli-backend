import React, { useState, useEffect } from 'react';
import { useErrorHandler } from '../../hooks/useErrorHandler';
import {
  Form,
  Input,
  Button,
  Select,
  Checkbox,
  message,
  Typography,
  Steps,
  Card,
  Row,
  Col,
} from 'antd';
import {
  UserOutlined,
  LockOutlined,
  MailOutlined,
  PhoneOutlined,
  EyeInvisibleOutlined,
  EyeTwoTone,
  ShopOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { RegisterRequest, UserRole, Store } from '../../types';
import { StoreStatus } from '../../types/store';
import { authService, storeService } from '../../services';

const { Title, Text } = Typography;
const { Option } = Select;
const { Step } = Steps;

interface RegisterFormData extends RegisterRequest {
  confirmPassword: string;
  agreement: boolean;
  role: UserRole;
  storeId?: string;
}

/**
 * 注册页面组件
 */
export const Register: React.FC = () => {
  const { handleError } = useErrorHandler();
  const [form] = Form.useForm<RegisterFormData>();
  const [loading, setLoading] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [stores, setStores] = useState<Store[]>([]);
  const [loadingStores, setLoadingStores] = useState(false);
  const navigate = useNavigate();

  // 加载门店列表
  const loadStores = async () => {
    setLoadingStores(true);
    try {
      const response = await storeService.getStores({ status: StoreStatus.ACTIVE });
      setStores(response.content);
    } catch (error) {
      handleError(error);
    } finally {
      setLoadingStores(false);
    }
  };

  useEffect(() => {
    loadStores();
  }, []);

  // 处理下一步
  const handleNext = async () => {
    try {
      if (currentStep === 0) {
        await form.validateFields(['username', 'email', 'password', 'confirmPassword']);
      } else if (currentStep === 1) {
        await form.validateFields(['fullName', 'phone', 'role', 'storeId']);
      }
      setCurrentStep(currentStep + 1);
    } catch (error) {
      // 验证失败，不执行下一步
    }
  };

  // 处理上一步
  const handlePrev = () => {
    setCurrentStep(currentStep - 1);
  };

  // 处理注册提交
  const handleSubmit = async (values: RegisterFormData) => {
    if (!values.agreement) {
      message.error('请同意用户协议和隐私政策');
      return;
    }

    setLoading(true);
    try {
      const registerData: RegisterRequest = {
        username: values.username,
        email: values.email,
        password: values.password,
        fullName: values.fullName,
        phone: values.phone,
        // role: values.role, // 注册时不需要指定角色
        // storeId: values.role === UserRole.STUDENT || values.role === UserRole.TEACHER ? values.storeId : undefined,
      };

      await authService.register(registerData);
      message.success('注册成功！请登录您的账号。');
      navigate('/auth/login');
    } catch (error: any) {
      const errorMessage = error?.message || '注册失败，请稍后重试';
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  // 验证密码确认
  const validateConfirmPassword = (_: any, value: string) => {
    const password = form.getFieldValue('password');
    if (value && value !== password) {
      return Promise.reject(new Error('两次输入的密码不一致'));
    }
    return Promise.resolve();
  };

  // 验证用户名唯一性
  const validateUsername = async (_: any, value: string) => {
    if (!value || value.length < 3) {
      return Promise.resolve();
    }
    
    try {
      const isAvailable = await authService.checkUsernameAvailability(value);
      if (!isAvailable) {
        return Promise.reject(new Error('用户名已被使用'));
      }
    } catch (error) {
      // 网络错误时不阻止提交
    }
    return Promise.resolve();
  };

  // 验证邮箱唯一性
  const validateEmail = async (_: any, value: string) => {
    if (!value || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
      return Promise.resolve();
    }
    
    try {
      const isAvailable = await authService.checkEmailAvailability(value);
      if (!isAvailable) {
        return Promise.reject(new Error('邮箱已被使用'));
      }
    } catch (error) {
      // 网络错误时不阻止提交
    }
    return Promise.resolve();
  };

  // 处理角色变化
  const handleRoleChange = (role: UserRole) => {
    if (role === UserRole.HEADQUARTERS_ADMIN || role === UserRole.STORE_ADMIN) {
      form.setFieldValue('storeId', undefined);
    }
  };

  // 获取角色选项
  const getRoleOptions = () => {
    return [
      {
        value: UserRole.STUDENT,
        label: '学生',
        icon: <UserOutlined />,
        description: '可以查看课程和提交作业',
      },
      {
        value: UserRole.TEACHER,
        label: '教师',
        icon: <TeamOutlined />,
        description: '可以创建课程和管理作业',
      },
      {
        value: UserRole.STORE_ADMIN,
        label: '门店经理',
        icon: <ShopOutlined />,
        description: '可以管理门店的课程和用户',
      },
    ];
  };

  // 步骤内容
  const steps = [
    {
      title: '账号信息',
      content: (
        <div className="space-y-4">
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名' },
              { min: 3, max: 20, message: '用户名长度为3-20个字符' },
              { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' },
              { validator: validateUsername },
            ]}
            hasFeedback
          >
            <Input
              prefix={<UserOutlined className="text-gray-400" />}
              placeholder="用户名（3-20个字符）"
              autoComplete="username"
              data-testid="username-input"
            />
          </Form.Item>

          <Form.Item
            name="email"
            rules={[
              { required: true, message: '请输入邮箱地址' },
              { type: 'email', message: '请输入有效的邮箱地址' },
              { validator: validateEmail },
            ]}
            hasFeedback
          >
            <Input
              prefix={<MailOutlined className="text-gray-400" />}
              placeholder="邮箱地址"
              autoComplete="email"
              data-testid="email-input"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 6, message: '密码至少6个字符' },
              {
                pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/,
                message: '密码必须包含大小写字母和数字',
              },
            ]}
            hasFeedback
          >
            <Input.Password
              prefix={<LockOutlined className="text-gray-400" />}
              placeholder="密码（至少6位，包含大小写字母和数字）"
              autoComplete="new-password"
              iconRender={(visible) => (visible ? <EyeTwoTone /> : <EyeInvisibleOutlined />)}
              data-testid="password-input"
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            dependencies={['password']}
            rules={[
              { required: true, message: '请确认密码' },
              { validator: validateConfirmPassword },
            ]}
            hasFeedback
          >
            <Input.Password
              prefix={<LockOutlined className="text-gray-400" />}
              placeholder="确认密码"
              autoComplete="new-password"
              iconRender={(visible) => (visible ? <EyeTwoTone /> : <EyeInvisibleOutlined />)}
              data-testid="confirm-password-input"
            />
          </Form.Item>
        </div>
      ),
    },
    {
      title: '个人信息',
      content: (
        <div className="space-y-4">
          <Form.Item
            name="fullName"
            rules={[
              { required: true, message: '请输入真实姓名' },
              { min: 2, max: 20, message: '姓名长度为2-20个字符' },
            ]}
          >
            <Input
              prefix={<UserOutlined className="text-gray-400" />}
              placeholder="真实姓名"
              autoComplete="name"
              data-testid="full-name-input"
            />
          </Form.Item>

          <Form.Item
            name="phone"
            rules={[
              { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码' },
            ]}
          >
            <Input
              prefix={<PhoneOutlined className="text-gray-400" />}
              placeholder="手机号码（可选）"
              autoComplete="tel"
            />
          </Form.Item>

          <Form.Item
            name="role"
            rules={[{ required: true, message: '请选择用户角色' }]}
          >
            <div className="space-y-3">
              <Text strong>选择您的角色：</Text>
              {getRoleOptions().map((option) => (
                <Card
                  key={option.value}
                  size="small"
                  className="cursor-pointer hover:border-blue-500 transition-colors"
                  onClick={() => {
                    form.setFieldValue('role', option.value);
                    handleRoleChange(option.value);
                  }}
                  style={{
                    borderColor: form.getFieldValue('role') === option.value ? '#1890ff' : undefined,
                  }}
                >
                  <div className="flex items-center space-x-3">
                    <div className="text-lg text-blue-500">{option.icon}</div>
                    <div className="flex-1">
                      <div className="font-medium">{option.label}</div>
                      <div className="text-sm text-gray-500">{option.description}</div>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          </Form.Item>

          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) => prevValues.role !== currentValues.role}
          >
            {({ getFieldValue }) => {
              const role = getFieldValue('role');
              if (role === UserRole.STUDENT || role === UserRole.TEACHER) {
                return (
                  <Form.Item
                    name="storeId"
                    rules={[{ required: true, message: '请选择所属门店' }]}
                  >
                    <Select
                      placeholder="选择所属门店"
                      loading={loadingStores}
                      showSearch
                      filterOption={(input, option) =>
                        (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
                      }
                    >
                      {stores.map((store) => (
                        <Option key={store.id} value={store.id}>
                          <div className="flex items-center space-x-2">
                            <ShopOutlined />
                            <span>{store.name}</span>
                            <Text type="secondary" className="text-xs">
                              ({store.address})
                            </Text>
                          </div>
                        </Option>
                      ))}
                    </Select>
                  </Form.Item>
                );
              }
              return null;
            }}
          </Form.Item>
        </div>
      ),
    },
    {
      title: '完成注册',
      content: (
        <div className="space-y-6">
          <div className="text-center">
            <Title level={4}>确认注册信息</Title>
            <Text type="secondary">请确认以下信息无误后完成注册</Text>
          </div>

          <Card>
            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Text strong>用户名：</Text>
                <div>{form.getFieldValue('username')}</div>
              </Col>
              <Col span={12}>
                <Text strong>邮箱：</Text>
                <div>{form.getFieldValue('email')}</div>
              </Col>
              <Col span={12}>
                <Text strong>姓名：</Text>
                <div>{form.getFieldValue('fullName')}</div>
              </Col>
              <Col span={12}>
                <Text strong>手机：</Text>
                <div>{form.getFieldValue('phone') || '未填写'}</div>
              </Col>
              <Col span={12}>
                <Text strong>角色：</Text>
                <div>
                  {getRoleOptions().find(opt => opt.value === form.getFieldValue('role'))?.label}
                </div>
              </Col>
              <Col span={12}>
                <Text strong>门店：</Text>
                <div>
                  {form.getFieldValue('storeId')
                    ? stores.find(store => store.id === form.getFieldValue('storeId'))?.name
                    : '总部'
                  }
                </div>
              </Col>
            </Row>
          </Card>

          <Form.Item
            name="agreement"
            valuePropName="checked"
            rules={[
              {
                validator: (_, value) =>
                  value ? Promise.resolve() : Promise.reject(new Error('请同意用户协议')),
              },
            ]}
          >
            <Checkbox>
              我已阅读并同意{' '}
              <Link to="/terms" target="_blank" className="text-blue-600">
                用户协议
              </Link>{' '}
              和{' '}
              <Link to="/privacy" target="_blank" className="text-blue-600">
                隐私政策
              </Link>
            </Checkbox>
          </Form.Item>
        </div>
      ),
    },
  ];

  return (
    <div className="w-full max-w-2xl mx-auto">
      <div className="text-center mb-8">
        <Title level={2} className="mb-2">
          用户注册
        </Title>
        <Text type="secondary">
          加入加盟连锁门店作业管理系统
        </Text>
      </div>

      <Card>
        <Steps current={currentStep} className="mb-8">
          {steps.map((step) => (
            <Step key={step.title} title={step.title} />
          ))}
        </Steps>

        <Form
          form={form}
          name="register"
          onFinish={handleSubmit}
          autoComplete="off"
          size="large"
          layout="vertical"
        >
          <div className="min-h-[400px]">
            {steps[currentStep].content}
          </div>

          <div className="flex justify-between mt-8">
            <div>
              {currentStep > 0 && (
                <Button onClick={handlePrev}>
                  上一步
                </Button>
              )}
            </div>
            <div className="space-x-2">
              {currentStep < steps.length - 1 && (
                <Button type="primary" onClick={handleNext}>
                  下一步
                </Button>
              )}
              {currentStep === steps.length - 1 && (
                <Button type="primary" htmlType="submit" data-testid="register-submit-btn" loading={loading}>
                  完成注册
                </Button>
              )}
            </div>
          </div>
        </Form>
      </Card>

      <div className="text-center mt-6">
        <Text type="secondary">
          已有账号？{' '}
          <Link to="/auth/login" className="text-blue-600 hover:text-blue-500 font-medium">
            立即登录
          </Link>
        </Text>
      </div>
    </div>
  );
};

export default Register;