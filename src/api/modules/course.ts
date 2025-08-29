import apiClient from '../index'
import type {
  Course,
  CourseListResponse,
  CreateCourseRequest,
  UpdateCourseRequest,
  CourseListParams
} from '@/types/api/course'

/**
 * 课程相关API
 */
export default {
  /**
   * 获取课程列表
   */
  async getCourses(params: CourseListParams): Promise<CourseListResponse> {
    const response = await apiClient.get('/admin/courses', { params })
    return response.data
  },

  /**
   * 创建课程
   */
  async createCourse(data: CreateCourseRequest): Promise<Course> {
    const response = await apiClient.post('/admin/courses', data)
    return response.data
  },

  /**
   * 更新课程
   */
  async updateCourse(id: string, data: UpdateCourseRequest): Promise<Course> {
    const response = await apiClient.put(`/admin/courses/${id}`, data)
    return response.data
  },

  /**
   * 删除课程
   */
  async deleteCourse(id: string): Promise<void> {
    await apiClient.delete(`/admin/courses/${id}`)
  },

  /**
   * 获取课程详情
   */
  async getCourse(id: string): Promise<Course> {
    const response = await apiClient.get(`/admin/courses/${id}`)
    return response.data
  }
}

export * from '@/types/api/admin'