package bio.tech.catalog.spring;

import bio.tech.catalog.persistence.dao.PrivilegeRepository;
import bio.tech.catalog.persistence.dao.RoleRepository;
import bio.tech.catalog.persistence.dao.UserRepository;
import bio.tech.catalog.persistence.model.Privilege;
import bio.tech.catalog.persistence.model.Role;
import bio.tech.catalog.persistence.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        final Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        final Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
        final Privilege dbacPrivilege = createPrivilegeIfNotFound("UDBAC_PRIVILEGE");
        final Privilege uploadPrivilege = createPrivilegeIfNotFound("UPLOAD_PRIVILEGE");
        final Privilege passwordPrivilege = createPrivilegeIfNotFound("CHANGE_PASSWORD_PRIVILEGE");

        final List<Privilege> adminPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, writePrivilege, passwordPrivilege));
        final List<Privilege> bioArchPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, passwordPrivilege, uploadPrivilege));
        final List<Privilege> dbacPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, passwordPrivilege, dbacPrivilege));
        final List<Privilege> userPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, passwordPrivilege));
        final Role adminRole = createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        final Role bioRole = createRoleIfNotFound("ROLE_BIOBANK", bioArchPrivileges);
        final Role arcRole = createRoleIfNotFound("ROLE_ARCHIVE", bioArchPrivileges);
        final Role dbaRole = createRoleIfNotFound("ROLE_DBAC", dbacPrivileges);
        final Role userRole = createRoleIfNotFound("ROLE_USER", userPrivileges);

        createUserIfNotFound("admin@catalogue.ac.za", "Admin", "Admin", "SANBI","admin", "123",
                new ArrayList<Role>(Arrays.asList(adminRole)));

        createUserIfNotFound("cls@catalogue.ac.za", "Biobank", "Biobank", "CLS","clsbio", "123",
                new ArrayList<Role>(Arrays.asList(bioRole)));

        createUserIfNotFound("archive@catalogue.ac.za", "Archive", "Archive", "ARCHIVE","cbioarch", "123",
                new ArrayList<Role>(Arrays.asList(arcRole)));

        createUserIfNotFound("dbac@catalogue.ac.za", "Dbac", "DBAC", "DBAC","dbacom", "123",
                new ArrayList<Role>(Arrays.asList(dbaRole)));

        alreadySetup = true;
    }

    private Privilege createPrivilegeIfNotFound(final String name) {
        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilege = privilegeRepository.save(privilege);
        }
        return privilege;
    }

    private Role createRoleIfNotFound(final String name, final List<Privilege> privileges) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
        }
        role.setPrivileges(privileges);
        role = roleRepository.save(role);
        return role;
    }

    private User createUserIfNotFound(final String email, final String firstName, final String lastName,
                                      final String institutionName, final String username, final String password,
                                      final Collection<Role> roles) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setInstitutionName(institutionName);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setEnabled(true);
        }
        user.setRoles(roles);
        user = userRepository.save(user);

        return user;
    }
}
