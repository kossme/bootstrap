package com.example.bootstrap.web.controllers;


import com.example.bootstrap.model.Role;
import com.example.bootstrap.model.User;
import com.example.bootstrap.service.RoleService;
import com.example.bootstrap.service.UserService;
import com.example.bootstrap.web.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private RoleService roleService;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/admin/userList")
    public String userList(Model model) {
        System.out.println("roleService.listAllRoles()");


        model.addAttribute("usersList", userService.listUsers());
        return "/admin/userList";
    }

    @GetMapping("/admin/addNewUserForm")
    public String showSignUpForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.listRoles());
        return "admin/new";
    }

    @PostMapping("/admin/create")
    public String create(@ModelAttribute("user") User userForm,
                         //@ModelAttribute("roles") ArrayList<Integer> rolesInt,
                         BindingResult bindingResult,
                         Model model) {
        //System.out.println(rolesInt);
        System.out.println(userForm.getAuthorities());
        System.out.println(userForm.getRoles());
        System.out.println(userForm);
        userValidator.validate(userForm, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleService.listRoles());
            return "/admin/new";
        }
        userService.save(userForm);
        return "redirect:/admin/userList";
    }

    @GetMapping("admin/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.findUserById(id));
        model.addAttribute("allRoles", roleService.listRoles());

        boolean isAdmin = false;
        boolean isUser = false;
        if (userService.findUserById(id).getRoles().stream().anyMatch(a -> a.toString().contains("ROLE_ADMIN"))) {
            isAdmin = true;
        }
        model.addAttribute("confirmAdmin", isAdmin);
        if (userService.findUserById(id).getRoles().stream().anyMatch(a -> a.toString().contains("ROLE_USER"))) {
            isUser = true;
        }

        model.addAttribute("confirmUser", isUser);
        return "admin/updateForm";
    }

    @PostMapping("/admin/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @ModelAttribute("user") User userForm,
                         @RequestParam(name = "ROLE_ADMIN", required = false) boolean confirmAdmin,
                         @RequestParam(name = "ROLE_USER", required = false) boolean confirmUser,
                         BindingResult bindingResult,
                         Model model) {

        Validator editValidator = new Validator() {
            @Override
            public boolean supports(Class<?> aClass) {
                return User.class.equals(aClass);
            }

            @Override
            public void validate(Object o, Errors errors) {
                User user = (User) o;
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "NotEmpty.appUserForm.username");
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "NotEmpty.appUserForm.firstName");
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "NotEmpty.appUserForm.lastName");
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty.appUserForm.email");

                if((!userForm.getPassword().equals("")) && (!userForm.getConfirmPassword().equals(""))) {
                    if (userForm.getPassword().length() < 8 || userForm.getPassword().length() > 16) {
                        errors.rejectValue("password", "Size.userForm.password");
                    }
                    if (!userForm.getConfirmPassword().equals(userForm.getPassword())) {
                        errors.rejectValue("confirmPassword", "Different.userForm.password");
                    }
                }

                if (!userForm.getUsername().equals(userService.findUserById(id).getUsername())) {
                    if (userService.loadUserByUsername(user.getUsername()) != null) {
                        errors.rejectValue("username", "Duplicate.userForm.username");
                    }
                    if (user.getUsername().length() < 4 || user.getUsername().length() > 16) {
                        errors.rejectValue("username", "Size.userForm.username");
                    }
                }
            }
        };

        editValidator.validate(userForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return "/admin/updateForm";
        }

        //Checking if password was changed
        if(userForm.getPassword().equals("")) {
            userForm.setPassword(userService.findUserById(id).getPassword());
        } else {
            userForm.setPassword(bCryptPasswordEncoder.encode(userForm.getPassword()));
        }

        //Cgecking Roles
        User userRole = new User();
        Set<Role> newroles = new HashSet<>();

        if (confirmAdmin) {
            newroles.add(roleService.getRoleByName("ROLE_ADMIN"));
        }
        if (confirmUser) {
            newroles.add(roleService.getRoleByName("ROLE_USER"));
        }

        userRole.setRoles(newroles);

        //updating user's roles
        userForm.setRoles(userRole.getRoles());

        userService.updateUser(userForm);
        return "redirect:/admin/userList";
    }


    @GetMapping("/admin/deleteConfirm/{id}")
    public String deleteConfirm(@PathVariable("id") Long id, Model model) {
        User user = userService.findUserById(id);
        model.addAttribute("user", userService.findUserById(id));
        return "/admin/deleteConfirm";
    }

    @GetMapping("/admin/delete/{id}")
    public String deleteUserById(@PathVariable(value = "id", required = true) long id, Model model) {
        userService.removeUser(id);
        return "redirect:/admin/userList";
    }
}