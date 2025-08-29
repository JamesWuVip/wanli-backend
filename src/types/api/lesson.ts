import type { BaseEntity, PaginatedResponse, PaginationParams, UserInfo } from '../index'

/**
 * 课时相关API类型定义
 */

// 课时类型枚举
export enum LessonType {
  VIDEO = 'VIDEO',
  DOCUMENT = 'DOCUMENT',
  QUIZ = 'QUIZ',
  ASSIGNMENT = 'ASSIGNMENT',
  LIVE = 'LIVE',
  INTERACTIVE = 'INTERACTIVE'
}

// 课时状态枚举
export enum LessonStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED'
}

// 视频质量枚举
export enum VideoQuality {
  SD = '480p',
  HD = '720p',
  FHD = '1080p',
  UHD = '4K'
}

// 课时基础信息
export interface Lesson extends BaseEntity {
  courseId: string
  title: string
  description: string
  type: LessonType
  status: LessonStatus
  order: number
  duration: number // 时长（秒）
  isPreview: boolean // 是否可预览
  isMandatory: boolean // 是否必修
  content: LessonContent
  resources: LessonResource[]
  prerequisites: string[] // 前置课时ID
  learningObjectives: string[]
  tags: string[]
  viewCount: number
  completionCount: number
  averageRating: number
  reviewCount: number
  publishedAt?: string
  archivedAt?: string
}

// 课时内容
export interface LessonContent {
  // 视频内容
  videoUrl?: string
  videoThumbnail?: string
  videoQuality?: VideoQuality[]
  subtitles?: Array<{
    language: string
    url: string
  }>
  
  // 文档内容
  documentUrl?: string
  documentType?: 'pdf' | 'doc' | 'ppt' | 'html'
  
  // 测验内容
  questions?: QuizQuestion[]
  passingScore?: number
  timeLimit?: number // 时间限制（分钟）
  allowRetake?: boolean
  
  // 作业内容
  assignmentInstructions?: string
  submissionFormat?: 'text' | 'file' | 'both'
  maxFileSize?: number
  allowedFileTypes?: string[]
  dueDate?: string
  
  // 直播内容
  liveUrl?: string
  scheduledAt?: string
  duration?: number
  maxParticipants?: number
  
  // HTML内容
  htmlContent?: string
  
  // 交互式内容
  interactiveContent?: any // 具体结构根据交互类型定义
}

// 课时资源
export interface LessonResource {
  id: string
  name: string
  type: 'document' | 'video' | 'audio' | 'image' | 'archive' | 'link'
  url: string
  size?: number
  description?: string
  isDownloadable: boolean
  order: number
}

// 测验问题
export interface QuizQuestion {
  id: string
  type: 'single_choice' | 'multiple_choice' | 'true_false' | 'fill_blank' | 'essay'
  question: string
  options?: string[] // 选择题选项
  correctAnswers: string[] // 正确答案
  explanation?: string
  points: number
  order: number
}

// 课时列表项
export interface LessonListItem {
  id: string
  courseId: string
  title: string
  description: string
  type: LessonType
  status: LessonStatus
  order: number
  duration: number
  isPreview: boolean
  isMandatory: boolean
  viewCount: number
  completionCount: number
  averageRating: number
  createdAt: string
  updatedAt: string
  publishedAt?: string
}

// 课时详情响应
export interface LessonDetailResponse {
  lesson: Lesson
  course: {
    id: string
    title: string
    instructor: UserInfo
  }
  previousLesson?: {
    id: string
    title: string
  }
  nextLesson?: {
    id: string
    title: string
  }
  userProgress?: {
    isCompleted: boolean
    progress: number // 0-100
    timeSpent: number // 已学习时间（秒）
    lastPosition: number // 上次学习位置（秒）
    completedAt?: string
    rating?: number
    review?: string
  }
  stats: {
    totalViews: number
    uniqueViews: number
    averageWatchTime: number
    completionRate: number
  }
}

// 课时列表响应
export interface LessonListResponse extends PaginatedResponse<LessonListItem> {
  courseInfo: {
    id: string
    title: string
    totalLessons: number
    totalDuration: number
  }
}

// 课时列表查询参数
export interface LessonListParams extends PaginationParams {
  search?: string
  type?: LessonType
  status?: LessonStatus
  isPreview?: boolean
  isMandatory?: boolean
  tags?: string[]
  dateFrom?: string
  dateTo?: string
}

// 创建课时请求
export interface CreateLessonRequest {
  title: string
  description: string
  type: LessonType
  order?: number
  duration?: number
  isPreview?: boolean
  isMandatory?: boolean
  content: Partial<LessonContent>
  resources?: Omit<LessonResource, 'id'>[]
  prerequisites?: string[]
  learningObjectives?: string[]
  tags?: string[]
}

// 更新课时请求
export interface UpdateLessonRequest {
  title?: string
  description?: string
  type?: LessonType
  order?: number
  duration?: number
  isPreview?: boolean
  isMandatory?: boolean
  content?: Partial<LessonContent>
  resources?: Omit<LessonResource, 'id'>[]
  prerequisites?: string[]
  learningObjectives?: string[]
  tags?: string[]
}

// 重新排序课时请求
export interface ReorderLessonsRequest {
  lessonOrders: Array<{
    id: string
    order: number
  }>
}

// 更新课时进度请求
export interface UpdateLessonProgressRequest {
  progress: number // 0-100
  timeSpent: number // 学习时间（秒）
  currentPosition: number // 当前位置（秒）
  isCompleted?: boolean
}

// 评价课时请求
export interface RateLessonRequest {
  rating: number // 1-5
  review?: string
}

// 课时评价
export interface LessonReview {
  id: string
  lessonId: string
  userId: string
  user: {
    id: string
    fullName: string
    avatar?: string
  }
  rating: number
  review: string
  createdAt: string
  updatedAt: string
}

// 课时评价列表响应
export interface LessonReviewListResponse extends PaginatedResponse<LessonReview> {
  stats: {
    averageRating: number
    totalReviews: number
    ratingDistribution: Array<{
      rating: number
      count: number
    }>
  }
}

// 课时统计
export interface LessonStats {
  totalViews: number
  uniqueViews: number
  totalWatchTime: number
  averageWatchTime: number
  completionRate: number
  dropOffPoints: Array<{
    position: number // 秒
    dropOffRate: number // 0-1
  }>
  dailyViews: Array<{
    date: string
    views: number
    uniqueViews: number
  }>
  deviceStats: Array<{
    device: string
    count: number
    percentage: number
  }>
}

// 课时笔记
export interface LessonNote {
  id: string
  lessonId: string
  userId: string
  content: string
  position: number // 视频位置（秒）
  isPublic: boolean
  createdAt: string
  updatedAt: string
}

// 课时讨论
export interface LessonDiscussion {
  id: string
  lessonId: string
  userId: string
  user: {
    id: string
    fullName: string
    avatar?: string
  }
  content: string
  parentId?: string // 回复的讨论ID
  replies?: LessonDiscussion[]
  likeCount: number
  isLiked: boolean
  createdAt: string
  updatedAt: string
}