package web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.dao.CarDao;
import web.model.*;

@Component
public class CarService {
    @Autowired
    private CarDao carDao;

    public List<Car> getCars(int count) {
        return this.carDao.getCars(count);
    }

    public int getCarsCount () {
        return carDao.getCarsCount();
    }
}

