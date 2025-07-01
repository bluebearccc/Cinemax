package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "History")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HistoryID", nullable = false)
    private Integer historyId;

    @Column(name = "TableName", nullable = false, length = 100)
    private String tableName;

    @Column(name = "RecordID", nullable = false)
    private Integer recordId;

    @Column(name = "OldValue", nullable = false, length = 255)
    private String oldValue;

    @Column(name = "NewValue", nullable = false, length = 255)
    private String newValue;

    @Column(name = "Action", nullable = false, length = 20)
    private String action;

    @Column(name = "AccountID", nullable = false)
    private Integer accountId;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
