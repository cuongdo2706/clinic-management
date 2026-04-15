package cd.beapi.service.impl;

import cd.beapi.dto.request.UpdatePermissionRequest;
import cd.beapi.dto.response.PermissionResponse;
import cd.beapi.dto.response.PermissionResponse.PagePermission;
import cd.beapi.entity.Action;
import cd.beapi.entity.Page;
import cd.beapi.entity.Permission;
import cd.beapi.entity.Role;
import cd.beapi.enumerate.ActionType;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.RoleMapper;
import cd.beapi.repository.jpa.ActionRepository;
import cd.beapi.repository.jpa.PageRepository;
import cd.beapi.repository.jpa.PermissionRepository;
import cd.beapi.repository.jpa.RoleRepository;
import cd.beapi.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final RoleRepository roleRepository;
    private final PageRepository pageRepository;
    private final ActionRepository actionRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    @Override
    public PermissionResponse getPermissionMatrix(Long roleId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new AppException("Cannot find role with id: " + roleId, HttpStatus.BAD_REQUEST));
        List<Page> allPages = pageRepository.findAll();
        Set<String> grantedSet = role.getPermissions().stream()
                .map(p -> p.getPage().getCode().name() + ":" + p.getAction().getCode().name())
                .collect(Collectors.toSet());

        List<String> allActions = Arrays
                .stream(ActionType.values())
                .map(ActionType::name)
                .toList();

        List<PagePermission> pagePermissions = allPages.stream()
                .map(page -> {
                    Set<ActionType> allowedActions = page.getCode().getAllowedActions();
                    Map<String, Boolean> granted = new LinkedHashMap<>();
                    for (ActionType action : ActionType.values()) {
                        if (allowedActions.contains(action)) {
                            granted.put(action.name(), grantedSet.contains(page.getCode().name() + ":" + action.name()));
                        }
                    }
                    return new PagePermission(page.getCode().name(), page.getName(), granted);
                })
                .toList();
        return new PermissionResponse(
                roleMapper.toRoleResponse(role),
                allActions,
                pagePermissions
        );
    }

    @Transactional
    @Override
    public PermissionResponse updatePermissions(UpdatePermissionRequest request) {
        Role role = roleRepository.findByIdWithPermissions(request.getRoleId())
                .orElseThrow(() -> new AppException("Cannot find role with id: " + request.getRoleId(), HttpStatus.BAD_REQUEST));
        Map<String, Page> pageMap = pageRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getCode().name(), p -> p));
        Map<String, Action> actionMap = actionRepository.findAll().stream()
                .collect(Collectors.toMap(a -> a.getCode().name(), a -> a));
        role.getPermissions().clear();
        if (request.getPermissions() != null) {
            for (UpdatePermissionRequest.PermissionEntry entry : request.getPermissions()) {
                Page page = pageMap.get(entry.getPageCode());
                Action action = actionMap.get(entry.getActionCode());
                if (page == null) {
                    throw new AppException("Invalid page code: " + entry.getPageCode(), HttpStatus.BAD_REQUEST);
                }
                if (action == null) {
                    throw new AppException("Invalid action code: " + entry.getActionCode(), HttpStatus.BAD_REQUEST);
                }
                if (!page.getCode().getAllowedActions().contains(action.getCode())) {
                    throw new AppException(
                            "Action " + entry.getActionCode() + " is not allowed for page " + entry.getPageCode(),
                            HttpStatus.BAD_REQUEST);
                }
                Permission permission = permissionRepository
                        .findByPageAndAction(page.getCode(), action.getCode())
                        .orElseThrow(() -> new AppException(
                                "Permission not found: " + entry.getPageCode() + ":" + entry.getActionCode(),
                                HttpStatus.BAD_REQUEST));
                role.getPermissions().add(permission);
            }
        }
        roleRepository.save(role);
        return getPermissionMatrix(role.getId());
    }
}

