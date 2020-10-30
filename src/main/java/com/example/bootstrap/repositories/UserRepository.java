package com.example.bootstrap.repositories;

import com.example.bootstrap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long>
{
/*   @Query("SELECT u FROM Role u WHERE u.name = :name")
   User findByUsername(@Param("name") String name);*/

   User findByUsername(String username);

}