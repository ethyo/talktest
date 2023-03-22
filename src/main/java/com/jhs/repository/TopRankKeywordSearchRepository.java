package com.jhs.repository;

import com.jhs.entity.UserString;
import com.jhs.entity.UserStringInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface TopRankKeywordSearchRepository extends JpaRepository<UserString, Long> {
    @Query(value = "SELECT userstring, COUNT(userstring) as wordcnt FROM userstring GROUP BY userstring ORDER BY wordcnt DESC", nativeQuery = true)
    List<UserStringInterface> findDistinctBy();
}
