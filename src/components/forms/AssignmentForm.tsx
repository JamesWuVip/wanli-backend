import React, { useState, useEffect } from 'react';
import { useErrorHandler } from '../../hooks/useErrorHandler';
import {
  Form,
  Input,
  DatePicker,
  Select,
  InputNumber,
  Switch,
  Upload,
  Button,
  Card,
  Row,
  Col,
  message,
  Spin
} from 'antd';
import { UploadOutlined, PlusOutlined } from '@ant-design/icons';
import type { Assignment, CreateAssignmentRequest, UpdateAssignmentRequest } from '../../types';
import { UserRole } from '../../types';
import { userService } from '../../services';
import dayjs from 'dayjs';

const { TextArea } = Input;
const { Option } = Select;
const { RangePicker } = DatePicker;

interface AssignmentFormProps {
  assignment?: Assignment;
  onSubmit: (data: CreateAssignmentRequest | UpdateAssignmentRequest) => Promise<void>;
  onCancel: () => void;
  loading?: boolean;
}

const AssignmentForm: React.FC<AssignmentFormProps> = ({
  assignment,
  onSubmit,
  onCancel,
  loading = false,
}) => {
  const { handleError } = useErrorHandler();
  const [form] = Form.useForm();
  const [teachers, setTeachers] = useState<any[]>([]);
  const [loadingTeachers, setLoadingTeachers] = useState(false);
  const [attachments, setAttachments] = useState<any[]>([]);

  useEffect(() => {
    loadTeachers();
    if (assignment) {
      form.setFieldsValue({
        ...assignment,
        timeRange: assignment.startDate && assignment.endDate 
          ? [dayjs(assignment.startDate), dayjs(assignment.endDate)]
          : undefined,
        tags: assignment.tags || []
      });
      setAttachments(assignment.attachments || []);
    }
  }, [assignment, form]);

  const loadTeachers = async () => {
    try {
      setLoadingTeachers(true);
      const response = await userService.getUsers({ role: UserRole.TEACHER });
      setTeachers(response.content || []);
    } catch (error) {
      message.error('加载教师列表失败');
    } finally {
      setLoadingTeachers(false);
    }
  };

  const handleSubmit = async (values: any) => {
    try {
      const { timeRange, ...rest } = values;
      const submitData = {
        ...rest,
        startDate: timeRange?.[0]?.toISOString(),
        endDate: timeRange?.[1]?.toISOString(),
        attachments
      };
      
      await onSubmit(submitData);
      message.success(assignment ? '作业更新成功' : '作业创建成功');
    } catch (error) {
      message.error(assignment ? '作业更新失败' : '作业创建失败');
    }
  };

  const handleUploadChange = (info: any) => {
    const { fileList } = info;
    setAttachments(fileList.map((file: any) => ({
      uid: file.uid,
      name: file.name,
      url: file.response?.url || file.url,
      status: file.status
    })));
  };

  const uploadProps = {
    name: 'file',
    action: '/api/upload',
    multiple: true,
    fileList: attachments,
    onChange: handleUploadChange,
    beforeUpload: (file: File) => {
      const isValidSize = file.size / 1024 / 1024 < 10;
      if (!isValidSize) {
        message.error('文件大小不能超过10MB');
      }
      return isValidSize;
    }
  };

  return (
    <Card title={assignment ? '编辑作业' : '创建作业'} className="assignment-form">
      <Spin spinning={loading}>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            status: 'draft',
            maxScore: 100,
            allowLateSubmission: false,
            isPublic: false
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="title"
                label="作业标题"
                rules={[
                  { required: true, message: '请输入作业标题' },
                  { max: 100, message: '标题长度不能超过100个字符' }
                ]}
              >
                <Input placeholder="请输入作业标题" data-testid="assignment-title-input" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="courseId"
                label="所属课程"
                rules={[{ required: true, message: '请选择所属课程' }]}
              >
                <Select placeholder="请选择所属课程" loading={loadingTeachers} data-testid="assignment-course-select">
                  {/* 这里应该加载课程列表 */}
                  <Option value="course1">数学基础</Option>
                  <Option value="course2">英语阅读</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="teacherId"
                label="授课教师"
                rules={[{ required: true, message: '请选择授课教师' }]}
              >
                <Select placeholder="请选择授课教师" loading={loadingTeachers}>
                  {teachers.map(teacher => (
                    <Option key={teacher.id} value={teacher.id}>
                      {teacher.fullName}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="maxScore"
                label="满分"
                rules={[
                  { required: true, message: '请输入满分' },
                  { type: 'number', min: 1, max: 1000, message: '满分必须在1-1000之间' }
                ]}
              >
                <InputNumber
                  min={1}
                  data-testid="assignment-max-score-input"
                  max={1000}
                  placeholder="请输入满分"
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="作业描述"
            rules={[
              { required: true, message: '请输入作业描述' },
              { max: 500, message: '描述长度不能超过500个字符' }
            ]}
          >
            <TextArea
              rows={4}
              placeholder="请输入作业描述"
              showCount
              maxLength={500}
              data-testid="assignment-description-input"
            />
          </Form.Item>

          <Form.Item
            name="content"
            label="作业内容"
            rules={[
              { required: true, message: '请输入作业内容' },
              { max: 2000, message: '内容长度不能超过2000个字符' }
            ]}
          >
            <TextArea
              rows={8}
              placeholder="请输入详细的作业内容和要求"
              showCount
              maxLength={2000}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="type"
                label="作业类型"
                rules={[{ required: true, message: '请选择作业类型' }]}
              >
                <Select placeholder="请选择作业类型" data-testid="assignment-type-select">
                  <Option value="homework">作业</Option>
                  <Option value="exam">考试</Option>
                  <Option value="project">项目</Option>
                  <Option value="quiz">测验</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="dueDate"
                label="截止日期"
                rules={[{ required: true, message: '请选择截止日期' }]}
              >
                <DatePicker
                  showTime
                  format="YYYY-MM-DD HH:mm"
                  placeholder="选择截止日期"
                  style={{ width: '100%' }}
                  data-testid="assignment-due-date-picker"
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="startDate"
                label="开始时间"
                rules={[{ required: true, message: '请选择开始时间' }]}
              >
                <DatePicker
                  showTime
                  format="YYYY-MM-DD HH:mm"
                  placeholder="选择开始时间"
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="status"
                label="状态"
                rules={[{ required: true, message: '请选择状态' }]}
              >
                <Select placeholder="请选择状态">
                  <Option value="draft">草稿</Option>
                  <Option value="published">已发布</Option>
                  <Option value="completed">已完成</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="tags"
            label="标签"
          >
            <Select
              mode="tags"
              placeholder="请输入标签，按回车添加"
              style={{ width: '100%' }}
            >
              <Option value="基础">基础</Option>
              <Option value="进阶">进阶</Option>
              <Option value="练习">练习</Option>
              <Option value="考试">考试</Option>
            </Select>
          </Form.Item>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="allowLateSubmission"
                label="允许迟交"
                valuePropName="checked"
              >
                <Switch />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="isPublic"
                label="公开作业"
                valuePropName="checked"
              >
                <Switch />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="attachments"
            label="附件"
          >
            <Upload {...uploadProps}>
              <Button icon={<UploadOutlined />}>上传附件</Button>
            </Upload>
          </Form.Item>

          <Form.Item>
            <Row gutter={16}>
              <Col>
                <Button 
                  type="primary" 
                  htmlType="submit" 
                  loading={loading}
                  data-testid={assignment ? "assignment-update-submit-btn" : "assignment-create-submit-btn"}
                >
                  {assignment ? '更新作业' : '创建作业'}
                </Button>
              </Col>
              <Col>
                <Button onClick={onCancel}>
                  取消
                </Button>
              </Col>
            </Row>
          </Form.Item>
        </Form>
      </Spin>
    </Card>
  );
};

export default AssignmentForm;