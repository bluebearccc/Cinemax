    package com.bluebear.cinemax.dto;

    import com.bluebear.cinemax.enumtype.Theater_Status;
    import lombok.*;

    import java.util.List;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class TheaterDTO {
        private Integer theaterID;
        private String theaterName;
        private String address;
        private String image;
        private int roomQuantity;
        private Theater_Status status;

        private List<RoomDTO> rooms;
    }
