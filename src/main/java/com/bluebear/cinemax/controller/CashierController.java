// CashierController.java
package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.cashier.MovieDTO;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.sercurity.cashier.CashierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/cashier")
public class CashierController {

    private CashierService cashierService;

    @Autowired
    public CashierController(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("movies", cashierService.getMovieAvailable(Movie.MovieStatus.Active, 0, 5));
        model.addAttribute("selectedMovie", null);
        return "cashier-templates/cashier-booking";
    }

    @GetMapping("/{id}")
    public String selectMovie(@PathVariable Integer id, Model model) {
        try {
            var movies = cashierService.getMovieAvailable(Movie.MovieStatus.Active, 0, 5);
            var selectedMovie = cashierService.getMovieById(id);

            if (selectedMovie == null) {
                return "redirect:/cashier/";
            }

            model.addAttribute("movies", movies);
            model.addAttribute("selectedMovie", selectedMovie);
            return "cashier-templates/cashier-booking";
        } catch (Exception e) {
            e.printStackTrace(); // log lỗi ra để dễ debug
            return "redirect:/cashier/";
        }
    }

}