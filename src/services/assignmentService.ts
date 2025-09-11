import { Assignment, CreateAssignmentRequest, UpdateAssignmentRequest, AssignmentQueryParams, AssignmentSubmission } from '../types/course';
import { SimplePageResponse, PageResponse, ApiResponse } from '../types/api';
import { httpClient } from '../utils/httpClient';

/**
 * 作业管理服务
 */
export class AssignmentService {
  private baseURL = '/api/assignments';

  /**
   * 获取作业列表
   */
  async getAssignments(params?: AssignmentQueryParams): Promise<PageResponse<Assignment>> {
    const queryParams = new URLSearchParams();
    if (params?.page !== undefined) queryParams.append('page', params.page.toString());
    if (params?.pageSize !== undefined) queryParams.append('pageSize', params.pageSize.toString());
    if (params?.courseId) queryParams.append('courseId', params.courseId);
    if (params?.status) queryParams.append('status', params.status);
    if (params?.search) queryParams.append('search', params.search);

    const url = queryParams.toString() ? `${this.baseURL}?${queryParams}` : this.baseURL;
    return httpClient.get<PageResponse<Assignment>>(url);
  }

  /**
   * 根据ID获取作业详情
   */
  async getAssignmentById(id: string): Promise<Assignment> {
    return httpClient.get<Assignment>(`${this.baseURL}/${id}`);
  }

  /**
   * 创建作业
   */
  async createAssignment(data: CreateAssignmentRequest): Promise<Assignment> {
    return httpClient.post<Assignment>(this.baseURL, data);
  }

  /**
   * 更新作业
   */
  async updateAssignment(id: string, data: UpdateAssignmentRequest): Promise<Assignment> {
    return httpClient.put<Assignment>(`${this.baseURL}/${id}`, data);
  }

  /**
   * 删除作业
   */
  async deleteAssignment(id: string): Promise<void> {
    return httpClient.post<void>(`${this.baseURL}/${id}/delete`);
  }

  /**
   * 发布作业
   */
  async publishAssignment(id: string): Promise<Assignment> {
    return httpClient.post<Assignment>(`${this.baseURL}/${id}/publish`);
  }

  /**
   * 关闭作业
   */
  async closeAssignment(id: string): Promise<Assignment> {
    return httpClient.post<Assignment>(`${this.baseURL}/${id}/close`);
  }

  /**
   * 获取作业提交列表
   */
  async getSubmissions(assignmentId: string, params?: { page?: number; pageSize?: number }): Promise<PageResponse<AssignmentSubmission>> {
    const queryParams = new URLSearchParams();
    if (params?.page !== undefined) queryParams.append('page', params.page.toString());
    if (params?.pageSize !== undefined) queryParams.append('pageSize', params.pageSize.toString());

    const url = queryParams.toString() 
      ? `${this.baseURL}/${assignmentId}/submissions?${queryParams}` 
      : `${this.baseURL}/${assignmentId}/submissions`;
    return httpClient.get<PageResponse<AssignmentSubmission>>(url);
  }

  /**
   * 获取作业统计信息
   */
  async getAssignmentStats(id: string): Promise<any> {
    return httpClient.get<any>(`${this.baseURL}/${id}/stats`);
  }

  /**
   * 导出作业数据
   */
  async exportAssignments(params?: AssignmentQueryParams): Promise<Blob> {
    const queryParams = new URLSearchParams();
    if (params?.courseId) queryParams.append('courseId', params.courseId);
    if (params?.status) queryParams.append('status', params.status);
    if (params?.search) queryParams.append('search', params.search);

    const url = queryParams.toString() 
      ? `${this.baseURL}/export?${queryParams}` 
      : `${this.baseURL}/export`;
    return httpClient.get<Blob>(url);
  }
}

export const assignmentService = new AssignmentService();