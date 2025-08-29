package com.wanli.controller;

import com.wanli.dto.InstitutionCreateDTO;
import com.wanli.dto.InstitutionResponseDTO;
import com.wanli.dto.InstitutionUpdateDTO;
import com.wanli.entity.Institution;
import com.wanli.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 机构管理控制器
 * 提供机构的CRUD操作接口
 */
@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;

    /**
     * 创建机构
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HQ_TEACHER')")
    public ResponseEntity<InstitutionResponseDTO> createInstitution(@Valid @RequestBody InstitutionCreateDTO createDTO) {
        InstitutionResponseDTO institution = institutionService.createInstitution(createDTO);
        return ResponseEntity.ok(institution);
    }

    /**
     * 根据ID获取机构信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HQ_TEACHER') or hasRole('TEACHER')")
    public ResponseEntity<InstitutionResponseDTO> getInstitution(@PathVariable String id) {
        InstitutionResponseDTO institution = institutionService.getInstitutionById(id);
        return ResponseEntity.ok(institution);
    }

    /**
     * 获取机构列表（分页）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HQ_TEACHER') or hasRole('TEACHER')")
    public ResponseEntity<Page<InstitutionResponseDTO>> getInstitutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InstitutionResponseDTO> institutions = institutionService.getAllInstitutions(pageable);
        return ResponseEntity.ok(institutions);
    }

    /**
     * 更新机构信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HQ_TEACHER')")
    public ResponseEntity<InstitutionResponseDTO> updateInstitution(
            @PathVariable String id, 
            @Valid @RequestBody InstitutionUpdateDTO updateDTO) {
        InstitutionResponseDTO institution = institutionService.updateInstitution(id, updateDTO);
        return ResponseEntity.ok(institution);
    }

    /**
     * 切换机构状态
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HQ_TEACHER')")
    public ResponseEntity<InstitutionResponseDTO> toggleInstitutionStatus(
            @PathVariable String id,
            @RequestParam boolean isActive) {
        InstitutionResponseDTO institution = institutionService.toggleInstitutionStatus(id, isActive);
        return ResponseEntity.ok(institution);
    }

    /**
     * 删除机构
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteInstitution(@PathVariable String id) {
        institutionService.deleteInstitution(id);
        return ResponseEntity.ok(Map.of("message", "机构删除成功"));
    }

    /**
     * 获取活跃机构列表
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HQ_TEACHER') or hasRole('TEACHER')")
    public ResponseEntity<List<InstitutionResponseDTO>> getActiveInstitutions() {
        List<InstitutionResponseDTO> institutions = institutionService.getActiveInstitutions();
        return ResponseEntity.ok(institutions);
    }

    /**
     * 获取机构统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HQ_TEACHER')")
    public ResponseEntity<Map<String, Object>> getInstitutionStatistics() {
        Map<String, Object> statistics = institutionService.getInstitutionStatistics();
        return ResponseEntity.ok(statistics);
    }
}