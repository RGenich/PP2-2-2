package web.dao;

import org.springframework.stereotype.Component;
import web.model.Car;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CarDao {

    private static List<Car> cars = Arrays.asList(
            new Car("Ru", "Lada", 2006),
            new Car("Jp", "Toyota", 2010),
            new Car("Ge", "BMW", 2020),
            new Car("Us", "Lada", 2030),
            new Car("Uk", "LandRover", 2011)
    );

    public List<Car> getCars(int count) {
        return cars.stream().limit(count).collect(Collectors.toList());
    }

    public int getCarsCount () {
        return cars.size();
    }

}
