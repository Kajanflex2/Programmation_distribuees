package com.example.rent;

import com.example.rent.data.Car;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RentApplicationTests {

	@Test
	void contextLoads() {
		Car car = new Car("AA11BB", 200);
		assertEquals(car.getPlateNumber(), "AA11BB");
	}

}
