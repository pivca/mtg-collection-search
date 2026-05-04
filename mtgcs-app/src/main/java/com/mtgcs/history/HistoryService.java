package com.mtgcs.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtgcs.user.AppUser;
import com.mtgcs.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public List<HistoryEntryResponse> getHistory(Long userId) {
        return historyRepository.findTop10ByUserId(userId).stream()
                .map(e -> new HistoryEntryResponse(e.getId(), e.getActionType(), e.getSummary(), e.getPayload(), e.getCreatedAt()))
                .toList();
    }

    @Async
    @Transactional
    public void record(Long userId, ActionType actionType, String summary, Object payloadObject) {
        try {
            AppUser user = userRepository.getReferenceById(userId);
            String payload = null;
            if (payloadObject != null) {
                try {
                    payload = objectMapper.writeValueAsString(payloadObject);
                } catch (JsonProcessingException e) {
                    log.warn("Could not serialize history payload: {}", e.getMessage());
                }
            }
            historyRepository.save(new HistoryEntry(user, actionType, summary, payload));
            historyRepository.pruneOlderThan10(userId);
        } catch (Exception e) {
            log.warn("Failed to record history for user {}: {}", userId, e.getMessage());
        }
    }
}
