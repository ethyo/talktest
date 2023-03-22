package com.jhs.repository;

import com.jhs.entity.UserString;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserStringRepository extends JpaRepository<UserString, Long> {
}
