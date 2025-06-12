//package com.bluebear.cinemax.repository.cashier;
//
//import com.bluebear.cinemax.entity.TheaterStock;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface TheaterStockRepository extends JpaRepository<TheaterStock, Integer> {
//    // Tìm đồ ăn/thức uống theo rạp
//    List<TheaterStock> findByTheater_TheaterIdAndStatusOrderByFoodName(Integer theaterId, TheaterStock.StockStatus status);
//
//    // Tìm món có sẵn (còn hàng)
//    List<TheaterStock> findByTheater_TheaterIdAndStatusAndQuantityGreaterThan(Integer theaterId, TheaterStock.StockStatus status, Integer minQuantity);
//
//    // Tìm theo tên món
//    List<TheaterStock> findByTheater_TheaterIdAndFoodNameContainingIgnoreCaseAndStatus(Integer theaterId, String foodName, TheaterStock.StockStatus status);
//
//    // Tìm món sắp hết hàng
//    List<TheaterStock> findByTheater_TheaterIdAndQuantityLessThanAndStatus(Integer theaterId, Integer threshold, TheaterStock.StockStatus status);
//
//    List<TheaterStock> findByTheaterIdAndStatus(Integer theaterId, TheaterStock.StockStatus stockStatus);
//
//    TheaterStock findByFoodName(String foodName);
//}