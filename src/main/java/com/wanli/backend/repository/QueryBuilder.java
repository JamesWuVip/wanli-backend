package com.wanli.backend.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.wanli.backend.dto.BaseQueryDTO;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/** 数据库查询构建器 提供通用的查询条件构建功能 */
public class QueryBuilder {

  /**
   * 构建基础查询规范
   *
   * @param queryDTO 查询条件DTO
   * @param <T> 实体类型
   * @return 查询规范
   */
  public static <T> Specification<T> buildBaseSpecification(BaseQueryDTO queryDTO) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // 添加基础查询条件
      addBasePredicates(predicates, root, criteriaBuilder, queryDTO);

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  /**
   * 构建分页对象
   *
   * @param queryDTO 查询条件DTO
   * @return 分页对象
   */
  public static Pageable buildPageable(BaseQueryDTO queryDTO) {
    // 构建排序
    Sort sort = buildSort(queryDTO);

    // 构建分页
    return PageRequest.of(
        queryDTO.getPage() - 1, // Spring Data JPA的页码从0开始
        queryDTO.getSize(),
        sort);
  }

  /**
   * 构建排序对象
   *
   * @param queryDTO 查询条件DTO
   * @return 排序对象
   */
  public static Sort buildSort(BaseQueryDTO queryDTO) {
    if (!queryDTO.hasSorting()) {
      // 默认按创建时间倒序排列
      return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    Sort.Direction direction =
        "ASC".equalsIgnoreCase(queryDTO.getSortDirection())
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

    return Sort.by(direction, queryDTO.getSortBy());
  }

  /**
   * 添加基础查询条件
   *
   * @param predicates 查询条件列表
   * @param root 根对象
   * @param criteriaBuilder 条件构建器
   * @param queryDTO 查询条件DTO
   * @param <T> 实体类型
   */
  private static <T> void addBasePredicates(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      BaseQueryDTO queryDTO) {

    // 删除状态过滤
    addDeletedFilter(predicates, root, criteriaBuilder, queryDTO);

    // 创建时间范围过滤
    addCreatedAtRangeFilter(predicates, root, criteriaBuilder, queryDTO);

    // 更新时间范围过滤
    addUpdatedAtRangeFilter(predicates, root, criteriaBuilder, queryDTO);

    // 创建者过滤
    addCreatedByFilter(predicates, root, criteriaBuilder, queryDTO);
  }

  /** 添加删除状态过滤条件 */
  private static <T> void addDeletedFilter(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      BaseQueryDTO queryDTO) {
    if (Boolean.TRUE.equals(queryDTO.getOnlyDeleted())) {
      // 只查询已删除的记录
      predicates.add(criteriaBuilder.isTrue(root.get("deleted")));
    } else if (!Boolean.TRUE.equals(queryDTO.getIncludeDeleted())) {
      // 默认不包含已删除的记录
      predicates.add(
          criteriaBuilder.or(
              criteriaBuilder.isFalse(root.get("deleted")),
              criteriaBuilder.isNull(root.get("deleted"))));
    }
    // 如果includeDeleted为true，则不添加任何删除状态过滤条件
  }

  /** 添加创建时间范围过滤条件 */
  private static <T> void addCreatedAtRangeFilter(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      BaseQueryDTO queryDTO) {
    if (queryDTO.getCreatedAtStart() != null) {
      predicates.add(
          criteriaBuilder.greaterThanOrEqualTo(
              root.get("createdAt"), queryDTO.getCreatedAtStart()));
    }

    if (queryDTO.getCreatedAtEnd() != null) {
      predicates.add(
          criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), queryDTO.getCreatedAtEnd()));
    }
  }

  /** 添加更新时间范围过滤条件 */
  private static <T> void addUpdatedAtRangeFilter(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      BaseQueryDTO queryDTO) {
    if (queryDTO.getUpdatedAtStart() != null) {
      predicates.add(
          criteriaBuilder.greaterThanOrEqualTo(
              root.get("updatedAt"), queryDTO.getUpdatedAtStart()));
    }

    if (queryDTO.getUpdatedAtEnd() != null) {
      predicates.add(
          criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), queryDTO.getUpdatedAtEnd()));
    }
  }

  /** 添加创建者过滤条件 */
  private static <T> void addCreatedByFilter(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      BaseQueryDTO queryDTO) {
    if (queryDTO.hasCreatedByFilter()) {
      predicates.add(root.get("createdBy").in(queryDTO.getCreatedByIds()));
    }
  }

  /**
   * 添加关键词搜索条件
   *
   * @param predicates 查询条件列表
   * @param root 根对象
   * @param criteriaBuilder 条件构建器
   * @param keyword 关键词
   * @param searchFields 搜索字段
   * @param <T> 实体类型
   */
  public static <T> void addKeywordSearch(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      String keyword,
      String... searchFields) {
    if (keyword != null && !keyword.trim().isEmpty() && searchFields.length > 0) {
      String likePattern = "%" + keyword.trim() + "%";

      List<Predicate> keywordPredicates = new ArrayList<>();
      for (String field : searchFields) {
        keywordPredicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get(field)), likePattern.toLowerCase()));
      }

      predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
    }
  }

  /**
   * 添加精确匹配条件
   *
   * @param predicates 查询条件列表
   * @param root 根对象
   * @param criteriaBuilder 条件构建器
   * @param fieldName 字段名
   * @param value 值
   * @param <T> 实体类型
   */
  public static <T> void addEqualCondition(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      String fieldName,
      Object value) {
    if (value != null) {
      predicates.add(criteriaBuilder.equal(root.get(fieldName), value));
    }
  }

  /**
   * 添加IN条件
   *
   * @param predicates 查询条件列表
   * @param root 根对象
   * @param fieldName 字段名
   * @param values 值列表
   * @param <T> 实体类型
   */
  public static <T> void addInCondition(
      List<Predicate> predicates, Root<T> root, String fieldName, List<?> values) {
    if (values != null && !values.isEmpty()) {
      predicates.add(root.get(fieldName).in(values));
    }
  }

  /**
   * 添加日期范围条件
   *
   * @param predicates 查询条件列表
   * @param root 根对象
   * @param criteriaBuilder 条件构建器
   * @param fieldName 字段名
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @param <T> 实体类型
   */
  public static <T> void addDateRangeCondition(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      String fieldName,
      LocalDateTime startDate,
      LocalDateTime endDate) {
    if (startDate != null) {
      predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), startDate));
    }

    if (endDate != null) {
      predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), endDate));
    }
  }

  /**
   * 添加UUID条件
   *
   * @param predicates 查询条件列表
   * @param root 根对象
   * @param criteriaBuilder 条件构建器
   * @param fieldName 字段名
   * @param uuid UUID值
   * @param <T> 实体类型
   */
  public static <T> void addUUIDCondition(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      String fieldName,
      UUID uuid) {
    if (uuid != null) {
      predicates.add(criteriaBuilder.equal(root.get(fieldName), uuid));
    }
  }

  /**
   * 添加布尔条件
   *
   * @param predicates 查询条件列表
   * @param root 根对象
   * @param criteriaBuilder 条件构建器
   * @param fieldName 字段名
   * @param value 布尔值
   * @param <T> 实体类型
   */
  public static <T> void addBooleanCondition(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaBuilder criteriaBuilder,
      String fieldName,
      Boolean value) {
    if (value != null) {
      if (value) {
        predicates.add(criteriaBuilder.isTrue(root.get(fieldName)));
      } else {
        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.isFalse(root.get(fieldName)),
                criteriaBuilder.isNull(root.get(fieldName))));
      }
    }
  }
}
