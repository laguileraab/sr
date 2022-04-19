package com.ce.sr.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ce.sr.models.ERole;
import com.ce.sr.models.Role;

public interface RoleRepository extends MongoRepository<Role, String> {
  Optional<Role> findByName(ERole name);
}
