    package com.bluebear.cinemax.dto;

    import com.bluebear.cinemax.enumtype.TheaterStock_Status;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class TheaterStockDTO {
        private Integer id;
        private Integer theaterId;
        private String itemName;
        private String image;
        private Integer quantity;
        private Double price;
        private TheaterStock_Status status;
    }
