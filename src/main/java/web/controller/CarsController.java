package web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import web.model.Car;
import web.service.CarService;

import java.util.List;

@Controller
public class CarsController {
    @Autowired
    CarService carService;

    @GetMapping(value = "/car")
    public String Car(@RequestParam("count") int count, Model carModel) {
        List<Car> requestedList = carService.getCars(count);
        carModel.addAttribute("cars", requestedList);

        return "car";
    }
}
