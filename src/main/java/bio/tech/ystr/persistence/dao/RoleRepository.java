package bio.tech.ystr.persistence.dao;

import bio.tech.ystr.persistence.model.Role;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {

    Role findByName(String name);

    @Override
    void delete(Role role);
}
