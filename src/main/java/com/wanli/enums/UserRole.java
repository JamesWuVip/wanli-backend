package com.wanli.enums;

/**
 * 用户角色枚举.
 * 
 * <p>定义系统中支持的用户角色类型，每种角色对应不同的权限和功能。</p>
 * 
 * @author AI Generated
 * @version 1.0
 */
public enum UserRole {
    
    /**
     * 学生用户.
     * 可以参与课程学习、提交作业、查看成绩等。
     */
    STUDENT,
    
    /**
     * 教师用户.
     * 可以创建课程、管理班级、批改作业、查看学生信息等。
     */
    TEACHER,
    
    /**
     * 门店管理员.
     * 可以管理门店信息、课程安排、教师和学生管理等。
     */
    STORE_MANAGER,
    
    /**
     * 系统管理员.
     * 拥有系统的最高权限，可以管理所有功能和数据。
     */
    ADMIN
}