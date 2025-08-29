import type { SubmissionResult, AssignmentItem, ApiResponse, PaginatedResponse } from '@/types/submission'
import { api } from './index'

// 作业提交相关API
export const submissionApi = {
  // 获取作业结果详情
  async getSubmissionResult(submissionId: string): Promise<SubmissionResult> {
    const response = await api.get<any>(`/submissions/${submissionId}/result`)
    
    // 转换后端返回的数据格式以匹配前端期望的格式
    return {
      submissionId: response.submissionId,
      homeworkId: response.assignmentId, // 后端返回assignmentId，前端期望homeworkId
      homeworkTitle: response.assignmentTitle, // 后端返回assignmentTitle，前端期望homeworkTitle
      studentId: response.studentId,
      studentName: response.studentUsername, // 后端返回studentUsername，前端期望studentName
      totalScore: response.score, // 后端返回score，前端期望totalScore
      maxScore: response.maxScore,
      teacherComment: response.feedback, // 后端返回feedback，前端期望teacherComment
      teacherFeedback: response.teacherFeedback,
      submittedAt: response.submittedAt,
      gradedAt: response.gradedAt,
      questions: (response.questions || []).map((q: any) => ({
        questionId: q.id, // 后端返回id，前端期望questionId
        content: q.content,
        studentAnswer: q.studentAnswer, // 使用后端返回的学生答案
        standardAnswer: q.standardAnswer,
        score: q.score, // 使用后端返回的得分
        maxScore: q.maxScore, // 使用后端返回的满分
        explanation: q.explanation,
        videoUrl: q.videoUrl
      }))
    }
  },

  // 获取学生的作业列表
  async getStudentAssignments(
    page: number = 1,
    pageSize: number = 10
  ): Promise<PaginatedResponse<AssignmentItem>> {
    const response = await api.get<any>(
      `/assignments/my-assignments?page=${page}&pageSize=${pageSize}`
    )
    
    // 转换后端返回的数据格式以匹配前端期望的格式
    if (response && Array.isArray(response)) {
      // 后端直接返回数组，需要转换为分页格式
      const transformedItems = response.map((item: any) => ({
        ...item,
        status: item.submissionStatus || 'PUBLISHED', // 将submissionStatus转换为status
        maxScore: item.totalScore // 将totalScore转换为maxScore
      }))
      
      return {
        items: transformedItems,
        total: transformedItems.length,
        page: page,
        pageSize: pageSize,
        totalPages: Math.ceil(transformedItems.length / pageSize)
      }
    } else if (response && response.items && Array.isArray(response.items)) {
      // 如果后端返回分页格式
      const transformedItems = response.items.map((item: any) => ({
        ...item,
        status: item.submissionStatus || 'PUBLISHED',
        maxScore: item.totalScore
      }))
      
      return {
        ...response,
        items: transformedItems
      }
    }
    
    // 如果数据结构不符合预期，返回空结果
    return {
      items: [],
      total: 0,
      page: 1,
      pageSize: 10,
      totalPages: 0
    }
  },

  // 提交作业答案
  async submitAssignment(
    homeworkId: string,
    answers: Record<string, string>
  ): Promise<{ submissionId: string }> {
    return api.post<{ submissionId: string }>(`/submissions`, {
      homeworkId,
      answers,
    })
  },

  // 保存作业草稿
  async saveDraft(
    homeworkId: string,
    answers: Record<string, string>
  ): Promise<{ submissionId: string }> {
    return api.post<{ submissionId: string }>(`/submissions/draft`, {
      homeworkId,
      answers,
    })
  },
}

// 认证相关API
export const authApi = {
  // 用户登录
  async login(username: string, password: string): Promise<any> {
    const response = await api.post<any>('/auth/login', {
      usernameOrEmail: username,
      password,
    })
    
    // 后端返回格式: { data: { accessToken, ... }, success, message }
    // 保存token
    if (response.data && response.data.accessToken) {
      localStorage.setItem('auth_token', response.data.accessToken)
    }
    
    return response
  },

  // 用户登出
  async logout(): Promise<void> {
    try {
      await api.post('/auth/logout')
    } finally {
      // 无论请求是否成功，都清除本地token
      localStorage.removeItem('auth_token')
    }
  },

  // 获取当前用户信息
  async getCurrentUser(): Promise<any> {
    return api.get('/auth/me')
  },
}

// 导出API客户端实例，供其他模块使用
export { api as apiClient }