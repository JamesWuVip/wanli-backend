import React from 'react';
import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import { UserRole } from '../types';
import { MainLayout, AuthLayout } from '../components/layouts';
import { Login, Register } from '../pages/auth';
import { NotFound, Forbidden } from '../pages/error';
import Dashboard from '../pages/Dashboard';
import { UserManagement } from '../pages/management';

// 懒加载页面组件
const CourseManagement = React.lazy(() => import('../pages/management/CourseManagement'));
const AssignmentManagement = React.lazy(() => import('../pages/management/AssignmentManagement'));
const ClassManagement = React.lazy(() => import('../pages/management/ClassManagement'));
const StoreManagement = React.lazy(() => import('../pages/management/StoreManagement'));

// 总部管理页面
const HeadquartersOverview = React.lazy(() => import('../pages/headquarters/Overview'));
const StoreMonitoring = React.lazy(() => import('../pages/headquarters/StoreMonitoring'));
const SystemSettings = React.lazy(() => import('../pages/headquarters/SystemSettings'));

// 门店管理页面
const StoreOverview = React.lazy(() => import('../pages/store/Overview'));
const StoreStudents = React.lazy(() => import('../pages/store/Students'));
const StoreTeachers = React.lazy(() => import('../pages/store/Teachers'));
const StoreCourses = React.lazy(() => import('../pages/store/Courses'));
const StoreClasses = React.lazy(() => import('../pages/store/Classes'));
const StoreAssignments = React.lazy(() => import('../pages/store/Assignments'));

// 教师工作台页面
const TeacherDashboard = React.lazy(() => import('../pages/teacher/Dashboard'));
const TeacherCourses = React.lazy(() => import('../pages/teacher/Courses'));
const TeacherClasses = React.lazy(() => import('../pages/teacher/Classes'));
const TeacherAssignments = React.lazy(() => import('../pages/teacher/Assignments'));
const TeacherStudents = React.lazy(() => import('../pages/teacher/Students'));

// 学生学习平台页面
const StudentDashboard = React.lazy(() => import('../pages/student/Dashboard'));
const StudentCourses = React.lazy(() => import('../pages/student/Courses'));
const StudentAssignments = React.lazy(() => import('../pages/student/Assignments'));
const StudentProgress = React.lazy(() => import('../pages/student/Progress'));

// 懒加载包装组件
const LazyWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <React.Suspense fallback={
    <div className="flex items-center justify-center min-h-screen">
      <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
    </div>
  }>
    {children}
  </React.Suspense>
);

// 路由配置
export const router = createBrowserRouter([
  {
    path: '/auth',
    element: <AuthLayout />,
    children: [
      {
        path: 'login',
        element: <Login />,
      },
      {
        path: 'register',
        element: <Register />,
      },
      {
        index: true,
        element: <Navigate to="/auth/login" replace />,
      },
    ],
  },
  {
    path: '/',
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />,
      },
      // 仪表盘
      {
        path: 'dashboard',
        element: <Dashboard />,
      },
      
      // 系统管理模块
      {
        path: 'management',
        element: <Outlet />,
        children: [
          {
            path: 'users',
            element: <UserManagement />,
          },
          {
            path: 'courses',
            element: (
              <LazyWrapper>
                <CourseManagement />
              </LazyWrapper>
            ),
          },
          {
            path: 'assignments',
            element: (
              <LazyWrapper>
                <AssignmentManagement />
              </LazyWrapper>
            ),
          },
          {
            path: 'classes',
            element: (
              <LazyWrapper>
                <ClassManagement />
              </LazyWrapper>
            ),
          },
          {
            path: 'stores',
            element: (
              <LazyWrapper>
                <StoreManagement />
              </LazyWrapper>
            ),
          },
        ],
      },
      
      // 总部管理模块
      {
        path: 'headquarters',
        element: <Outlet />,
        children: [
          {
            path: 'overview',
            element: (
              <LazyWrapper>
                <HeadquartersOverview />
              </LazyWrapper>
            ),
          },
          {
            path: 'stores',
            element: (
              <LazyWrapper>
                <StoreMonitoring />
              </LazyWrapper>
            ),
          },
          {
            path: 'settings',
            element: (
              <LazyWrapper>
                <SystemSettings />
              </LazyWrapper>
            ),
          },
        ],
      },
      
      // 门店管理模块
      {
        path: 'store',
        element: <Outlet />,
        children: [
          {
            path: 'overview',
            element: (
              <LazyWrapper>
                <StoreOverview />
              </LazyWrapper>
            ),
          },
          {
            path: 'students',
            element: (
              <LazyWrapper>
                <StoreStudents />
              </LazyWrapper>
            ),
          },
          {
            path: 'teachers',
            element: (
              <LazyWrapper>
                <StoreTeachers />
              </LazyWrapper>
            ),
          },
          {
            path: 'courses',
            element: (
              <LazyWrapper>
                <StoreCourses />
              </LazyWrapper>
            ),
          },
          {
            path: 'classes',
            element: (
              <LazyWrapper>
                <StoreClasses />
              </LazyWrapper>
            ),
          },
          {
            path: 'assignments',
            element: (
              <LazyWrapper>
                <StoreAssignments />
              </LazyWrapper>
            ),
          },
        ],
      },
      
      // 教师工作台模块
      {
        path: 'teacher',
        element: <Outlet />,
        children: [
          {
            index: true,
            element: <Navigate to="/teacher/dashboard" replace />,
          },
          {
            path: 'dashboard',
            element: (
              <LazyWrapper>
                <TeacherDashboard />
              </LazyWrapper>
            ),
          },
          {
            path: 'courses',
            element: (
              <LazyWrapper>
                <TeacherCourses />
              </LazyWrapper>
            ),
          },
          {
            path: 'classes',
            element: (
              <LazyWrapper>
                <TeacherClasses />
              </LazyWrapper>
            ),
          },
          {
            path: 'assignments',
            element: (
              <LazyWrapper>
                <TeacherAssignments />
              </LazyWrapper>
            ),
          },
          {
            path: 'students',
            element: (
              <LazyWrapper>
                <TeacherStudents />
              </LazyWrapper>
            ),
          },
        ],
      },
      
      // 学生学习平台模块
      {
        path: 'student',
        element: <Outlet />,
        children: [
          {
            index: true,
            element: <Navigate to="/student/dashboard" replace />,
          },
          {
            path: 'dashboard',
            element: (
              <LazyWrapper>
                <StudentDashboard />
              </LazyWrapper>
            ),
          },
          {
            path: 'courses',
            element: (
              <LazyWrapper>
                <StudentCourses />
              </LazyWrapper>
            ),
          },
          {
            path: 'assignments',
            element: (
              <LazyWrapper>
                <StudentAssignments />
              </LazyWrapper>
            ),
          },
          {
            path: 'progress',
            element: (
              <LazyWrapper>
                <StudentProgress />
              </LazyWrapper>
            ),
          },
        ],
      },
    ],
  },
  
  // 错误页面
  {
    path: '/403',
    element: <Forbidden />,
  },
  {
    path: '*',
    element: <NotFound />,
  },
]);

export default router;