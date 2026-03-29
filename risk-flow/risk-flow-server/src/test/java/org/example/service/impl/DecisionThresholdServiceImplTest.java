package org.example.service.impl;

import org.example.entity.DecisionThreshold;
import org.example.repository.DecisionThresholdRepository;
import org.example.service.DecisionThresholdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionThresholdServiceImplTest {

    @Mock
    private DecisionThresholdRepository thresholdRepository;

    private DecisionThresholdServiceImpl thresholdService;

    @BeforeEach
    void setUp() {
        thresholdService = new DecisionThresholdServiceImpl(thresholdRepository);
    }

    @Test
    void resolve_shouldUseExactMatchFirst() {
        DecisionThreshold exact = threshold(100L, "login", "VIP", 95, 75, 55);
        when(thresholdRepository.findByWorkflowIdAndEventTypeAndUserLevelAndEnabledTrue(100L, "login", "VIP"))
                .thenReturn(Optional.of(exact));

        DecisionThresholdService.Threshold result = thresholdService.resolve(100L, "login", "VIP");

        assertEquals(95, result.getRejectThreshold());
        assertEquals(75, result.getReviewThreshold());
        assertEquals(55, result.getChallengeThreshold());
        verify(thresholdRepository, never()).findByWorkflowIdIsNullAndEventTypeAndUserLevelAndEnabledTrue("login", "VIP");
    }

    @Test
    void resolve_shouldFallbackToEventTypeAndUserLevel() {
        DecisionThreshold fallback = threshold(null, "login", "NEW", 60, 40, 20);
        when(thresholdRepository.findByWorkflowIdAndEventTypeAndUserLevelAndEnabledTrue(1L, "login", "NEW"))
                .thenReturn(Optional.empty());
        when(thresholdRepository.findByWorkflowIdIsNullAndEventTypeAndUserLevelAndEnabledTrue("login", "NEW"))
                .thenReturn(Optional.of(fallback));

        DecisionThresholdService.Threshold result = thresholdService.resolve(1L, "login", "NEW");

        assertEquals(60, result.getRejectThreshold());
        assertEquals(40, result.getReviewThreshold());
        assertEquals(20, result.getChallengeThreshold());
    }

    @Test
    void resolve_shouldReturnDefaultWhenNoConfigFound() {
        when(thresholdRepository.findByWorkflowIdAndEventTypeAndUserLevelAndEnabledTrue(1L, "payment", "NORMAL"))
                .thenReturn(Optional.empty());
        when(thresholdRepository.findByWorkflowIdIsNullAndEventTypeAndUserLevelAndEnabledTrue("payment", "NORMAL"))
                .thenReturn(Optional.empty());
        when(thresholdRepository.findByWorkflowIdIsNullAndEventTypeAndUserLevelIsNullAndEnabledTrue("payment"))
                .thenReturn(Optional.empty());
        when(thresholdRepository.findByWorkflowIdIsNullAndEventTypeIsNullAndUserLevelIsNullAndEnabledTrue())
                .thenReturn(Optional.empty());

        DecisionThresholdService.Threshold result = thresholdService.resolve(1L, "payment", "NORMAL");

        assertEquals(80, result.getRejectThreshold());
        assertEquals(50, result.getReviewThreshold());
        assertEquals(30, result.getChallengeThreshold());
    }

    private DecisionThreshold threshold(Long workflowId, String eventType, String userLevel,
                                        int reject, int review, int challenge) {
        DecisionThreshold threshold = new DecisionThreshold();
        threshold.setWorkflowId(workflowId);
        threshold.setEventType(eventType);
        threshold.setUserLevel(userLevel);
        threshold.setRejectThreshold(reject);
        threshold.setReviewThreshold(review);
        threshold.setChallengeThreshold(challenge);
        threshold.setEnabled(true);
        return threshold;
    }
}
