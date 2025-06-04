package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.HistoryAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDTO {
    private Integer historyId;
    private String tableName;
    private Integer recordId;
    private String oldValue;
    private String newValue;
    private HistoryAction action;
    private Integer accountId;
    private LocalDateTime updatedAt;
    private AccountDTO account;
}