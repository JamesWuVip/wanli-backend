package com.wanli.service.impl;

import com.wanli.dto.InstitutionCreateDTO;
import com.wanli.dto.InstitutionResponseDTO;
import com.wanli.dto.InstitutionUpdateDTO;
import com.wanli.entity.Institution;
import com.wanli.entity.User;
import com.wanli.exception.ResourceNotFoundException;
import com.wanli.repository.InstitutionRepository;
import com.wanli.repository.UserRepository;
import com.wanli.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 机构服务实现类
 */
@Service
@Transactional
public class InstitutionServiceImpl implements InstitutionService {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public InstitutionResponseDTO createInstitution(InstitutionCreateDTO createDTO) {
        // 检查机构名称是否已存在
        if (institutionRepository.existsByInstitutionName(createDTO.getInstitutionName())) {
            throw new IllegalArgumentException("机构名称已存在");
        }

        Institution institution = new Institution();
        institution.setId(UUID.randomUUID().toString());
        institution.setInstitutionName(createDTO.getInstitutionName());
        institution.setInstitutionType(createDTO.getInstitutionType());
        institution.setAddress(createDTO.getAddress());
        institution.setContactPhone(createDTO.getContactPhone());
        institution.setContactEmail(createDTO.getContactEmail());
        institution.setDescription(createDTO.getDescription());
        institution.setIsActive(true);
        institution.setCreatedAt(LocalDateTime.now());
        institution.setUpdatedAt(LocalDateTime.now());
        
        // 设置创建者
        String currentUsername = getCurrentUsername();
        if (currentUsername != null) {
            User creator = userRepository.findByUsername(currentUsername).orElse(null);
            if (creator != null) {
                institution.setCreatedBy(creator.getId());
                institution.setUpdatedBy(creator.getId());
            }
        }

        Institution savedInstitution = institutionRepository.save(institution);
        return convertToResponseDTO(savedInstitution);
    }

    @Override
    @Transactional(readOnly = true)
    public InstitutionResponseDTO getInstitutionById(String id) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + id));
        return convertToResponseDTO(institution);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InstitutionResponseDTO> getAllInstitutions(Pageable pageable) {
        Page<Institution> institutions = institutionRepository.findAll(pageable);
        return institutions.map(this::convertToResponseDTO);
    }

    @Override
    public InstitutionResponseDTO updateInstitution(String id, InstitutionUpdateDTO updateDTO) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + id));

        // 检查机构名称是否已被其他机构使用
        if (!institution.getInstitutionName().equals(updateDTO.getInstitutionName()) &&
            institutionRepository.existsByInstitutionName(updateDTO.getInstitutionName())) {
            throw new IllegalArgumentException("机构名称已存在");
        }

        institution.setInstitutionName(updateDTO.getInstitutionName());
        institution.setInstitutionType(updateDTO.getInstitutionType());
        institution.setAddress(updateDTO.getAddress());
        institution.setContactPhone(updateDTO.getContactPhone());
        institution.setContactEmail(updateDTO.getContactEmail());
        institution.setDescription(updateDTO.getDescription());
        institution.setUpdatedAt(LocalDateTime.now());
        
        // 设置更新者
        String currentUsername = getCurrentUsername();
        if (currentUsername != null) {
            User updater = userRepository.findByUsername(currentUsername).orElse(null);
            if (updater != null) {
                institution.setUpdatedBy(updater.getId());
            }
        }

        Institution savedInstitution = institutionRepository.save(institution);
        return convertToResponseDTO(savedInstitution);
    }

    @Override
    public InstitutionResponseDTO toggleInstitutionStatus(String id, boolean isActive) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + id));

        institution.setIsActive(isActive);
        institution.setUpdatedAt(LocalDateTime.now());
        
        // 设置更新者
        String currentUsername = getCurrentUsername();
        if (currentUsername != null) {
            User updater = userRepository.findByUsername(currentUsername).orElse(null);
            if (updater != null) {
                institution.setUpdatedBy(updater.getId());
            }
        }

        Institution savedInstitution = institutionRepository.save(institution);
        return convertToResponseDTO(savedInstitution);
    }

    @Override
    public void deleteInstitution(String id) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("机构不存在，ID: " + id));

        // 软删除
        institution.setDeletedAt(LocalDateTime.now());
        institution.setUpdatedAt(LocalDateTime.now());
        
        // 设置删除者
        String currentUsername = getCurrentUsername();
        if (currentUsername != null) {
            User deleter = userRepository.findByUsername(currentUsername).orElse(null);
            if (deleter != null) {
                institution.setUpdatedBy(deleter.getId());
            }
        }

        institutionRepository.save(institution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstitutionResponseDTO> getActiveInstitutions() {
        List<Institution> institutions = institutionRepository.findByIsActiveTrueAndDeletedAtIsNull();
        return institutions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getInstitutionStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        long totalCount = institutionRepository.countByDeletedAtIsNull();
        long activeCount = institutionRepository.countByIsActiveTrueAndDeletedAtIsNull();
        long inactiveCount = institutionRepository.countByIsActiveFalseAndDeletedAtIsNull();
        
        statistics.put("totalCount", totalCount);
        statistics.put("activeCount", activeCount);
        statistics.put("inactiveCount", inactiveCount);
        
        return statistics;
    }

    /**
     * 转换为响应DTO
     */
    private InstitutionResponseDTO convertToResponseDTO(Institution institution) {
        InstitutionResponseDTO dto = new InstitutionResponseDTO();
        dto.setId(institution.getId());
        dto.setInstitutionName(institution.getInstitutionName());
        dto.setInstitutionType(institution.getInstitutionType());
        dto.setAddress(institution.getAddress());
        dto.setContactPhone(institution.getContactPhone());
        dto.setContactEmail(institution.getContactEmail());
        dto.setDescription(institution.getDescription());
        dto.setIsActive(institution.getIsActive());
        dto.setCreatedAt(institution.getCreatedAt());
        dto.setUpdatedAt(institution.getUpdatedAt());
        dto.setCreatedBy(institution.getCreatedBy());
        dto.setUpdatedBy(institution.getUpdatedBy());
        return dto;
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }
}