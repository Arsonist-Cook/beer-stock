package io.github.arsonistcook.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockMinimumException extends Exception {

    public BeerStockMinimumException(Long id) {
        super(String.format("Beers with %s ID to decrement informed makes the quantity lower than 0.", id));
    }
}