import React, { useState, useEffect } from 'react';
import { useErrorHandler } from '../../hooks/useErrorHandler';
import { Card, Row, Col, Button, Tag, List, Modal, Form, Input, Upload, message, Tabs, Progress, Descriptions } from 'antd';
import { FileTextOutlined, UploadOutlined, EyeOutlined, EditOutlined, ClockCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd/es/upload/interface';

interface Assignment {
  id: string;
  title: string;
  description: string;
  courseName: string;
  dueDate: string;
  status: 'pending' | 'submitted' | 'graded' | 'overdue';
  score?: number;
  maxScore: number;
  submissionDate?: string;
  feedback?: string;
  attachments?: string[];
}

interface Submission {
  id: string;
  content: string;
  attachments: UploadFile[];
  submittedAt: string;
}

const { TextArea } = Input;

const StudentAssignments: React.FC = () => {
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [selectedAssignment, setSelectedAssignment] = useState<Assignment | null>(null);
  const [submissionVisible, setSubmissionVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [activeTab, setActiveTab] = useState('pending');

  useEffect(() => {
    // 模拟获取作业数据
    const mockAssignments: Assignment[] = [
      {
        id: '1',
        title: '数学第三章练习题',
        description: '完成教材第三章所有练习题，包括证明题和计算题。要求过程清晰，步骤完整。',
        courseName: '高等数学基础',
        dueDate: '2024-01-20',
        status: 'pending',
        maxScore: 100
      },
      {
        id: '2',
        title: '英语作文：我的家乡',
        description: '写一篇关于家乡的英语作文，不少于500词，要求语法正确，表达流畅。',
        courseName: '英语听说读写',
        dueDate: '2024-01-18',
        status: 'submitted',
        maxScore: 100,
        submissionDate: '2024-01-17',
        score: 92,
        feedback: '作文内容丰富，语法基本正确，但个别词汇使用不够准确。'
      },
      {
        id: '3',
        title: '物理实验报告',
        description: '完成光学实验报告，包括实验目的、步骤、数据记录和结论分析。',
        courseName: '物理实验',
        dueDate: '2024-01-15',
        status: 'graded',
        maxScore: 100,
        submissionDate: '2024-01-14',
        score: 88,
        feedback: '实验数据记录完整，分析较为深入，但结论部分可以更加详细。'
      },
      {
        id: '4',
        title: '计算机编程作业',
        description: '使用Python编写一个简单的计算器程序，要求支持四则运算。',
        courseName: '计算机基础',
        dueDate: '2024-01-10',
        status: 'overdue',
        maxScore: 100
      }
    ];
    setAssignments(mockAssignments);
  }, []);

  const getStatusColor = (status: Assignment['status']) => {
    switch (status) {
      case 'pending': return 'orange';
      case 'submitted': return 'blue';
      case 'graded': return 'green';
      case 'overdue': return 'red';
      default: return 'default';
    }
  };

  const getStatusText = (status: Assignment['status']) => {
    switch (status) {
      case 'pending': return '待提交';
      case 'submitted': return '已提交';
      case 'graded': return '已批改';
      case 'overdue': return '已逾期';
      default: return '未知';
    }
  };

  const getStatusIcon = (status: Assignment['status']) => {
    switch (status) {
      case 'pending': return <ClockCircleOutlined />;
      case 'submitted': return <UploadOutlined />;
      case 'graded': return <CheckCircleOutlined />;
      case 'overdue': return <ClockCircleOutlined />;
      default: return <FileTextOutlined />;
    }
  };

  const filteredAssignments = assignments.filter(assignment => {
    if (activeTab === 'all') return true;
    return assignment.status === activeTab;
  });

  const handleSubmit = async (assignment: Assignment) => {
    setSelectedAssignment(assignment);
    setSubmissionVisible(true);
    form.resetFields();
    setFileList([]);
  };

  const handleViewDetails = (assignment: Assignment) => {
    setSelectedAssignment(assignment);
    setDetailVisible(true);
  };

  const handleSubmissionSubmit = async (values: any) => {
    try {
      // 模拟提交作业
      console.log('提交作业:', {
        assignmentId: selectedAssignment?.id,
        content: values.content,
        attachments: fileList
      });
      
      // 更新作业状态
      setAssignments(prev => prev.map(assignment => 
        assignment.id === selectedAssignment?.id 
          ? { ...assignment, status: 'submitted' as const, submissionDate: new Date().toISOString().split('T')[0] }
          : assignment
      ));
      
      message.success('作业提交成功！');
      setSubmissionVisible(false);
    } catch (error) {
      message.error('提交失败，请重试');
    }
  };

  const uploadProps = {
    fileList,
    onChange: ({ fileList: newFileList }: { fileList: UploadFile[] }) => {
      setFileList(newFileList);
    },
    beforeUpload: () => false, // 阻止自动上传
  };

  const getDaysUntilDue = (dueDate: string) => {
    const due = new Date(dueDate);
    const now = new Date();
    const diffTime = due.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">我的作业</h1>
      </div>

      <Tabs 
        activeKey={activeTab} 
        onChange={setActiveTab} 
        className="mb-6"
        items={[
          { key: 'pending', label: '待提交' },
          { key: 'submitted', label: '已提交' },
          { key: 'graded', label: '已批改' },
          { key: 'overdue', label: '已逾期' },
          { key: 'all', label: '全部' }
        ]}
      />

      <Row gutter={[16, 16]}>
        {filteredAssignments.map((assignment) => {
          const daysUntilDue = getDaysUntilDue(assignment.dueDate);
          
          return (
            <Col xs={24} sm={12} lg={8} key={assignment.id}>
              <Card
                hoverable
                title={
                  <div className="flex justify-between items-start">
                    <span className="text-lg font-semibold truncate">{assignment.title}</span>
                    <Tag color={getStatusColor(assignment.status)} icon={getStatusIcon(assignment.status)}>
                      {getStatusText(assignment.status)}
                    </Tag>
                  </div>
                }
                actions={[
                  <Button 
                    icon={<EyeOutlined />} 
                    onClick={() => handleViewDetails(assignment)}
                  >
                    查看详情
                  </Button>,
                  assignment.status === 'pending' && (
                    <Button 
                      type="primary" 
                      icon={<EditOutlined />}
                      onClick={() => handleSubmit(assignment)}
                    >
                      提交作业
                    </Button>
                  )
                ].filter(Boolean)}
              >
                <div className="space-y-3">
                  <p className="text-gray-600 text-sm line-clamp-2">{assignment.description}</p>
                  <div className="text-sm">
                    <p><strong>课程：</strong>{assignment.courseName}</p>
                    <p><strong>截止时间：</strong>{assignment.dueDate}</p>
                    {assignment.status === 'pending' && (
                      <p className={`font-semibold ${
                        daysUntilDue < 0 ? 'text-red-500' : 
                        daysUntilDue <= 3 ? 'text-orange-500' : 'text-green-500'
                      }`}>
                        {daysUntilDue < 0 ? `已逾期 ${Math.abs(daysUntilDue)} 天` : 
                         daysUntilDue === 0 ? '今天截止' : 
                         `还有 ${daysUntilDue} 天`}
                      </p>
                    )}
                    {assignment.score !== undefined && (
                      <p><strong>成绩：</strong>
                        <span className="ml-1">
                          {assignment.score}/{assignment.maxScore}
                          <Progress 
                            percent={(assignment.score / assignment.maxScore) * 100} 
                            size="small" 
                            className="ml-2 inline-block w-16"
                          />
                        </span>
                      </p>
                    )}
                  </div>
                </div>
              </Card>
            </Col>
          );
        })}
      </Row>

      {/* 作业提交模态框 */}
      <Modal
        title={`提交作业：${selectedAssignment?.title}`}
        open={submissionVisible}
        onCancel={() => setSubmissionVisible(false)}
        footer={null}
        width={800}
      >
        <Form form={form} onFinish={handleSubmissionSubmit} layout="vertical">
          <Form.Item
            name="content"
            label="作业内容"
            rules={[{ required: true, message: '请输入作业内容' }]}
          >
            <TextArea rows={8} placeholder="请输入您的作业内容..." />
          </Form.Item>
          
          <Form.Item label="附件上传">
            <Upload {...uploadProps} multiple>
              <Button icon={<UploadOutlined />}>选择文件</Button>
            </Upload>
          </Form.Item>
          
          <Form.Item>
            <div className="flex justify-end space-x-2">
              <Button onClick={() => setSubmissionVisible(false)}>取消</Button>
              <Button type="primary" htmlType="submit">提交作业</Button>
            </div>
          </Form.Item>
        </Form>
      </Modal>

      {/* 作业详情模态框 */}
      <Modal
        title={selectedAssignment?.title}
        open={detailVisible}
        onCancel={() => setDetailVisible(false)}
        footer={null}
        width={800}
      >
        {selectedAssignment && (
          <div className="space-y-6">
            <Descriptions column={2} bordered>
              <Descriptions.Item label="课程名称">{selectedAssignment.courseName}</Descriptions.Item>
              <Descriptions.Item label="作业状态">
                <Tag color={getStatusColor(selectedAssignment.status)} icon={getStatusIcon(selectedAssignment.status)}>
                  {getStatusText(selectedAssignment.status)}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="截止时间">{selectedAssignment.dueDate}</Descriptions.Item>
              <Descriptions.Item label="总分">{selectedAssignment.maxScore}分</Descriptions.Item>
              {selectedAssignment.submissionDate && (
                <Descriptions.Item label="提交时间">{selectedAssignment.submissionDate}</Descriptions.Item>
              )}
              {selectedAssignment.score !== undefined && (
                <Descriptions.Item label="得分">
                  <span className="text-lg font-semibold">
                    {selectedAssignment.score}/{selectedAssignment.maxScore}
                  </span>
                </Descriptions.Item>
              )}
            </Descriptions>

            <div>
              <h3 className="text-lg font-semibold mb-2">作业要求</h3>
              <p className="text-gray-700 bg-gray-50 p-4 rounded">{selectedAssignment.description}</p>
            </div>

            {selectedAssignment.feedback && (
              <div>
                <h3 className="text-lg font-semibold mb-2">教师反馈</h3>
                <p className="text-gray-700 bg-blue-50 p-4 rounded border-l-4 border-blue-400">
                  {selectedAssignment.feedback}
                </p>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default StudentAssignments;