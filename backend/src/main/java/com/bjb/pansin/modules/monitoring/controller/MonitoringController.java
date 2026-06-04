package com.bjb.pansin.modules.monitoring.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.monitoring.dto.DashboardStatsDto;
import com.bjb.pansin.modules.monitoring.dto.VaultMonitorDto;
import com.bjb.pansin.modules.monitoring.service.MonitoringService;
import com.bjb.pansin.modules.activity.entity.ActivityLog;
import com.bjb.pansin.modules.activity.repository.ActivityLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "Monitoring", description = "Dashboard monitoring endpoints")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final ActivityLogRepository activityLogRepository;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getStats() {
        log.info("GET /monitoring/stats called");
        DashboardStatsDto stats = monitoringService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Stats retrieved", stats));
    }

    @GetMapping("/vaults")
    public ResponseEntity<ApiResponse<List<VaultMonitorDto>>> getVaults() {
        log.info("GET /monitoring/vaults called");
        List<VaultMonitorDto> vaults = monitoringService.getAllVaultMonitors();
        return ResponseEntity.ok(ApiResponse.ok("Vaults retrieved", vaults));
    }

    @GetMapping("/vaults/{id}")
    public ResponseEntity<ApiResponse<VaultMonitorDto>> getVaultById(@PathVariable String id) {
        log.info("GET /monitoring/vaults/{} called", id);
        VaultMonitorDto vault = monitoringService.getVaultMonitorById(id);
        return ResponseEntity.ok(ApiResponse.ok("Vault retrieved", vault));
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("GET /monitoring/activities page={}, pageSize={}", page, pageSize);
        
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, 
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLog> activities = activityLogRepository.findAll(pageRequest);
        
        List<Map<String, Object>> data = activities.getContent().stream()
                .map(this::mapActivityToDto)
                .collect(Collectors.toList());
        
        Map<String, Object> response = Map.of(
                "data", data,
                "total", activities.getTotalElements(),
                "page", page,
                "pageSize", pageSize,
                "totalPages", activities.getTotalPages()
        );
        
        return ResponseEntity.ok(ApiResponse.ok("Activities retrieved", response));
    }

    private Map<String, Object> mapActivityToDto(ActivityLog log) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", log.getId() != null ? log.getId().toString() : "");
        map.put("timestamp", log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");
        map.put("type", log.getActivity() != null ? log.getActivity().toLowerCase() : "info");
        map.put("title", log.getActivity() != null ? log.getActivity() : "Activity");
        map.put("description", log.getDescription() != null ? log.getDescription() : "");
        map.put("severity", "info");
        map.put("ipAddress", log.getIpAddress());
        map.put("userId", log.getUserId() != null ? log.getUserId().toString() : null);
        return map;
    }
}
