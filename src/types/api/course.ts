import type { BaseEntity, PaginatedResponse, PaginationParams, UserInfo } from '../index'

/**
 * 课程相关API类型定义
 */

// 课程状态枚举
export enum CourseStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED'
}

// 课程难度枚举
export enum CourseDifficulty {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED'
}

// 课程类型枚举
export enum CourseType {
  ONLINE = 'ONLINE',
  OFFLINE = 'OFFLINE',
  HYBRID = 'HYBRID'
}

// 课程基础信息
export interface Course extends BaseEntity {
  title: string
  description: string
  coverImage?: string
  status: CourseStatus
  type: CourseType
  difficulty: CourseDifficulty
  duration: number // 总时长（分钟）
  price: number
  originalPrice?: number
  tags: string[]
  category: string
  subCategory?: string
  creator: UserInfo
  instructor?: UserInfo
  isPublic: boolean
  maxStudents?: number
  currentStudents: number
  rating: number
  reviewCount: number
  lessonCount: number
  completionRate: number
  language: string
  prerequisites?: string[]
  learningObjectives: string[]
  targetAudience: string[]
  publishedAt?: string
  archivedAt?: string
}

// 课程列表项
export interface CourseListItem {
  id: string
  title: string
  description: string
  coverImage?: string
  status: CourseStatus
  type: CourseType
  difficulty: CourseDifficulty
  duration: number
  price: number
  originalPrice?: number
  category: string
  creator: {
    id: string
    fullName: string
    avatar?: string
  }
  currentStudents: number
  rating: number
  reviewCount: number
  lessonCount: number
  isPublic: boolean
  createdAt: string
  updatedAt: string
  publishedAt?: string
}

// 课程详情响应
export interface CourseDetailResponse {
  course: Course
  lessons: Array<{
    id: string
    title: string
    duration: number
    order: number
    isPreview: boolean
  }>
  instructor: UserInfo
  stats: {
    totalViews: number
    totalEnrollments: number
    completionRate: number
    averageRating: number
  }
  userProgress?: {
    enrolledAt: string
    progress: number
    completedLessons: number
    lastAccessedAt: string
  }
}

// 课程列表响应
export interface CourseListResponse extends PaginatedResponse<CourseListItem> {
  filters: {
    categories: Array<{ name: string; count: number }>
    difficulties: Array<{ name: CourseDifficulty; count: number }>
    types: Array<{ name: CourseType; count: number }>
    priceRanges: Array<{ min: number; max: number; count: number }>
  }
}

// 课程列表查询参数
export interface CourseListParams extends PaginationParams {
  search?: string
  category?: string
  subCategory?: string
  status?: CourseStatus
  type?: CourseType
  difficulty?: CourseDifficulty
  creatorId?: string
  instructorId?: string
  isPublic?: boolean
  priceMin?: number
  priceMax?: number
  tags?: string[]
  language?: string
  rating?: number
  hasDiscount?: boolean
  dateFrom?: string
  dateTo?: string
}

// 创建课程请求
export interface CreateCourseRequest {
  title: string
  description: string
  coverImage?: string
  type: CourseType
  difficulty: CourseDifficulty
  price: number
  originalPrice?: number
  category: string
  subCategory?: string
  instructorId?: string
  isPublic: boolean
  maxStudents?: number
  tags: string[]
  language: string
  prerequisites?: string[]
  learningObjectives: string[]
  targetAudience: string[]
}

// 更新课程请求
export interface UpdateCourseRequest {
  title?: string
  description?: string
  coverImage?: string
  type?: CourseType
  difficulty?: CourseDifficulty
  price?: number
  originalPrice?: number
  category?: string
  subCategory?: string
  instructorId?: string
  isPublic?: boolean
  maxStudents?: number
  tags?: string[]
  language?: string
  prerequisites?: string[]
  learningObjectives?: string[]
  targetAudience?: string[]
}

// 课程统计
export interface CourseStats {
  totalViews: number
  totalEnrollments: number
  activeStudents: number
  completionRate: number
  averageRating: number
  totalRevenue: number
  monthlyEnrollments: Array<{
    month: string
    count: number
  }>
  ratingDistribution: Array<{
    rating: number
    count: number
  }>
  topLessons: Array<{
    id: string
    title: string
    views: number
    completionRate: number
  }>
}

// 课程分类
export interface CourseCategory {
  id: string
  name: string
  description?: string
  icon?: string
  parentId?: string
  children?: CourseCategory[]
  courseCount: number
  order: number
  isActive: boolean
}

// 课程标签
export interface CourseTag {
  id: string
  name: string
  color?: string
  courseCount: number
  isActive: boolean
}

// 课程评价
export interface CourseReview {
  id: string
  courseId: string
  userId: string
  user: {
    id: string
    fullName: string
    avatar?: string
  }
  rating: number
  comment: string
  isRecommended: boolean
  createdAt: string
  updatedAt: string
}

// 课程评价列表响应
export interface CourseReviewListResponse extends PaginatedResponse<CourseReview> {
  stats: {
    averageRating: number
    totalReviews: number
    ratingDistribution: Array<{
      rating: number
      count: number
      percentage: number
    }>
  }
}