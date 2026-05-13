package cd.beapi.service;

import cd.beapi.entity.User;

public interface UserService {
    CreatedStaffUser createStaffUser(String fullName, String staffCode, String roleCode);

    record CreatedStaffUser(User user, String temporaryPassword) {
    }
}
