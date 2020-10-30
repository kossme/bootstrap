package com.example.bootstrap.service;

import com.example.bootstrap.model.Role;
import com.example.bootstrap.model.User;
import com.example.bootstrap.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@Service
public class UserService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    RoleService roleService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder bCryptPasswordEncoder;

    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Set<Role> roles = user.getRoles();
            if (user.getRoles() == null) {
                user.setRoles(Collections.singleton(new Role(1, "ROLE_USER")));
            }
            if(roleService.getRoleByName("ROLE_USER")==null) {
                roleService.save(new Role(1, "ROLE_USER"));
            }

        System.out.println(user.getRoles());
        System.out.println(user.getRoles());


        for (Role e : user.getRoles()) {
            if (e.getName().equals("ROLE_USER")) {
                e.setId(1);
            }
            if (e.getName().equals("ROLE_ADMIN")) {
                e.setId(2);
            }
        }
        System.out.println(user.getRoles());

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user==null) {
            throw new UsernameNotFoundException("not found");
        }
        return user;
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String[] userRoles = user.getRoles().stream().map((role) -> role.getName()).toArray(String[]::new);
        Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(userRoles);
        return authorities;
    }

    public User findUserById(long id) {
        return userRepository.getOne(id);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public void removeUser(long id) {
        userRepository.deleteById(id);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

}
