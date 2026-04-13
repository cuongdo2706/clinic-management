package cd.beapi.service.impl;

import cd.beapi.dto.response.RoleResponse;
import cd.beapi.mapper.RoleMapper;
import cd.beapi.repository.jpa.RoleRepository;
import cd.beapi.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    @Override
    public List<RoleResponse> getAllRoles() {
        return roleMapper.toRoleResponses(roleRepository.findAll());
    }
}
