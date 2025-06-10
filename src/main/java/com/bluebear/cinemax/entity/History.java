package com.bluebear.cinemax.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "History")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HistoryID")
    private Integer historyId;

    @Column(name = "TableName", nullable = false, length = 100)
    private String tableName;

    @Column(name = "RecordID", nullable = false)
    private Integer recordId;

    @Column(name = "OldValue", nullable = false)
    private String oldValue;

    @Column(name = "NewValue", nullable = false)
    private String newValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "Action", nullable = false)
    private HistoryAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    public enum HistoryAction {
        Update, Delete
    }
}