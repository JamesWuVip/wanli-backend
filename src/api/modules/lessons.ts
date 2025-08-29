import { api } from '../index'
import type {
  Lesson,
  LessonListResponse,
  LessonDetailResponse,
  CreateLessonRequest,
  UpdateLessonRequest,
  LessonListParams,
  ReorderLessonsRequest
} from '@/types/api/lesson'

/**
 * 课时相关API
 */
export const lessonsApi = {
  /**
   * 获取课程的课时列表
   * @param courseId 课程ID
   * @param params 查询参数
   */
  getLessonsByCourse(courseId: string, params?: LessonListParams) {
    return api.get<LessonListResponse>(`/courses/${courseId}/lessons`, { params })
  },

  /**
   * 获取课时详情
   * @param courseId 课程ID
   * @param lessonId 课时ID
   */
  getLessonById(courseId: string, lessonId: string) {
    return api.get<LessonDetailResponse>(`/courses/${courseId}/lessons/${lessonId}`)
  },

  /**
   * 创建课时
   * @param courseId 课程ID
   * @param lessonData 课时数据
   */
  createLesson(courseId: string, lessonData: CreateLessonRequest) {
    return api.post<Lesson>(`/courses/${courseId}/lessons`, lessonData)
  },

  /**
   * 更新课时
   * @param courseId 课程ID
   * @param lessonId 课时ID
   * @param lessonData 更新数据
   */
  updateLesson(courseId: string, lessonId: string, lessonData: UpdateLessonRequest) {
    return api.put<Lesson>(`/courses/${courseId}/lessons/${lessonId}`, lessonData)
  },

  /**
   * 删除课时
   * @param courseId 课程ID
   * @param lessonId 课时ID
   */
  deleteLesson(courseId: string, lessonId: string) {
    return api.delete(`/courses/${courseId}/lessons/${lessonId}`)
  },

  /**
   * 批量删除课时
   * @param courseId 课程ID
   * @param lessonIds 课时ID数组
   */
  batchDeleteLessons(courseId: string, lessonIds: string[]) {
    return api.delete(`/courses/${courseId}/lessons/batch`, {
      data: { ids: lessonIds }
    })
  },

  /**
   * 重新排序课时
   * @param courseId 课程ID
   * @param reorderData 排序数据
   */
  reorderLessons(courseId: string, reorderData: ReorderLessonsRequest) {
    return api.post(`/courses/${courseId}/lessons/reorder`, reorderData)
  },

  /**
   * 复制课时
   * @param courseId 课程ID
   * @param lessonId 源课时ID
   * @param title 新课时标题
   */
  duplicateLesson(courseId: string, lessonId: string, title?: string) {
    return api.post<Lesson>(`/courses/${courseId}/lessons/${lessonId}/duplicate`, { title })
  },

  /**
   * 发布课时
   * @param courseId 课程ID
   * @param lessonId 课时ID
   */
  publishLesson(courseId: string, lessonId: string) {
    return api.post(`/courses/${courseId}/lessons/${lessonId}/publish`)
  },

  /**
   * 取消发布课时
   * @param courseId 课程ID
   * @param lessonId 课时ID
   */
  unpublishLesson(courseId: string, lessonId: string) {
    return api.post(`/courses/${courseId}/lessons/${lessonId}/unpublish`)
  },

  /**
   * 上传课时视频
   * @param courseId 课程ID
   * @param lessonId 课时ID
   * @param videoFile 视频文件
   */
  uploadLessonVideo(courseId: string, lessonId: string, videoFile: File) {
    const formData = new FormData()
    formData.append('video', videoFile)
    
    return api.post(`/courses/${courseId}/lessons/${lessonId}/video`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 删除课时视频
   * @param courseId 课程ID
   * @param lessonId 课时ID
   */
  deleteLessonVideo(courseId: string, lessonId: string) {
    return api.delete(`/courses/${courseId}/lessons/${lessonId}/video`)
  },

  /**
   * 获取课时学习进度
   * @param courseId 课程ID
   * @param lessonId 课时ID
   */
  getLessonProgress(courseId: string, lessonId: string) {
    return api.get(`/courses/${courseId}/lessons/${lessonId}/progress`)
  },

  /**
   * 标记课时为已完成
   * @param courseId 课程ID
   * @param lessonId 课时ID
   */
  markLessonComplete(courseId: string, lessonId: string) {
    return api.post(`/courses/${courseId}/lessons/${lessonId}/complete`)
  }
}