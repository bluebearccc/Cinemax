package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Detail_Seat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "InvoiceID", nullable = false)
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "SeatID", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 10)
    private SeatStatus status;

    @ManyToOne
    @JoinColumn(name = "ScheduleID", nullable = false)
    private Schedule schedule;
}