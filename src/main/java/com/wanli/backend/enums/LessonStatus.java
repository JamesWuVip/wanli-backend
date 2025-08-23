package com.wanli.backend.enums;

/** 课时状态枚举 定义课时的各种状态 */
public enum LessonStatus {
  /** 草稿状态 - 课时正在编辑中，未发布 */
  DRAFT("DRAFT", "草稿"),

  /** 已发布状态 - 课时已发布，学生可以学习 */
  PUBLISHED("PUBLISHED", "已发布"),

  /** 已归档状态 - 课时已归档，不再显示给学生 */
  ARCHIVED("ARCHIVED", "已归档");

  private final String code;
  private final String description;

  LessonStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  /**
   * 根据代码获取枚举值
   *
   * @param code 状态代码
   * @return 对应的枚举值
   */
  public static LessonStatus fromCode(String code) {
    for (LessonStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("未知的课时状态代码: " + code);
  }

  /**
   * 检查状态是否可以发布
   *
   * @return 如果可以发布返回true
   */
  public boolean canPublish() {
    return this == DRAFT;
  }

  /**
   * 检查状态是否可以归档
   *
   * @return 如果可以归档返回true
   */
  public boolean canArchive() {
    return this == PUBLISHED;
  }

  /**
   * 检查状态是否可以编辑
   *
   * @return 如果可以编辑返回true
   */
  public boolean canEdit() {
    return this == DRAFT;
  }
}
