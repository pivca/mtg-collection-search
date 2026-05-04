package com.mtgcs.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoryRepository extends JpaRepository<HistoryEntry, Long> {

    @Query("SELECT h FROM HistoryEntry h WHERE h.user.id = :userId ORDER BY h.createdAt DESC LIMIT 10")
    List<HistoryEntry> findTop10ByUserId(@Param("userId") Long userId);

    /** Keep only the 10 most recent entries — delete the rest. */
    @Modifying
    @Query(value = """
            DELETE FROM history
            WHERE user_id = :userId
              AND id NOT IN (
                  SELECT id FROM history
                  WHERE user_id = :userId
                  ORDER BY created_at DESC
                  LIMIT 10
              )
            """, nativeQuery = true)
    void pruneOlderThan10(@Param("userId") Long userId);
}
