package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.enumtype.TheaterStock_Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;
@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Theater_Stock")
public class TheaterStock {

    @Id
    @Column(name = "Theater_StockID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stockID;

    @Column(name = "FoodName", length = 20, nullable = false)
    private String itemName;

    @Column(name = "Image", length = 255)
    private String image;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "UnitPrice", nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50, nullable = false)
    private TheaterStock_Status status;

    @OneToMany(mappedBy = "theaterStock")
    private List<Detail_FD> detail_FD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TheaterID", nullable = false, referencedColumnName = "TheaterID")
    private Theater theater;
}
