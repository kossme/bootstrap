package com.example.bootstrap.service;

import com.example.bootstrap.model.Role;

import java.util.List;

public interface RoleService {

    public List<Role> listRoles();

    Role getRoleById(int id);

    Role getRoleByName(String name);

    void save(Role role);
}
