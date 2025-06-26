package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Detail_Seat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @ManyToOne
    @JoinColumn(name = "ScheduleID", nullable = false)
    private Schedule schedule;

    @Column(name = "Status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DetailSeat_Status status;
}
