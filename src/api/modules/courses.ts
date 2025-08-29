import { api } from '../index'
import type {
  Course,
  CourseListResponse,
  CourseDetailResponse,
  CreateCourseRequest,
  UpdateCourseRequest,
  CourseListParams
} from '@/types/api/course'

/**
 * 课程相关API
 */
export const coursesApi = {
  /**
   * 获取课程列表
   * @param params 查询参数
   */
  getCourses(params?: CourseListParams) {
    return api.get<CourseListResponse>('/courses', { params })
  },

  /**
   * 获取课程详情
   * @param id 课程ID
   */
  getCourseById(id: string) {
    return api.get<CourseDetailResponse>(`/courses/${id}`)
  },

  /**
   * 创建课程
   * @param courseData 课程数据
   */
  createCourse(courseData: CreateCourseRequest) {
    return api.post<Course>('/courses', courseData)
  },

  /**
   * 更新课程
   * @param id 课程ID
   * @param courseData 更新数据
   */
  updateCourse(id: string, courseData: UpdateCourseRequest) {
    return api.put<Course>(`/courses/${id}`, courseData)
  },

  /**
   * 删除课程
   * @param id 课程ID
   */
  deleteCourse(id: string) {
    return api.delete(`/courses/${id}`)
  },

  /**
   * 批量删除课程
   * @param ids 课程ID数组
   */
  batchDeleteCourses(ids: string[]) {
    return api.delete('/courses/batch', { data: { ids } })
  },

  /**
   * 复制课程
   * @param id 源课程ID
   * @param title 新课程标题
   */
  duplicateCourse(id: string, title?: string) {
    return api.post<Course>(`/courses/${id}/duplicate`, { title })
  },

  /**
   * 发布课程
   * @param id 课程ID
   */
  publishCourse(id: string) {
    return api.post(`/courses/${id}/publish`)
  },

  /**
   * 取消发布课程
   * @param id 课程ID
   */
  unpublishCourse(id: string) {
    return api.post(`/courses/${id}/unpublish`)
  },

  /**
   * 获取我创建的课程
   * @param params 查询参数
   */
  getMyCourses(params?: CourseListParams) {
    return api.get<CourseListResponse>('/courses/my', { params })
  },

  /**
   * 搜索课程
   * @param query 搜索关键词
   * @param params 其他查询参数
   */
  searchCourses(query: string, params?: Omit<CourseListParams, 'search'>) {
    return api.get<CourseListResponse>('/courses/search', {
      params: { q: query, ...params }
    })
  },

  /**
   * 获取课程统计信息
   * @param id 课程ID
   */
  getCourseStats(id: string) {
    return api.get(`/courses/${id}/stats`)
  }
}