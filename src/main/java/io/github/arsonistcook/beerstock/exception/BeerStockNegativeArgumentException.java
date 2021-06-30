package io.github.arsonistcook.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockNegativeArgumentException extends Exception{
    public BeerStockNegativeArgumentException() {
        super("Not allowed negative values in this function call.");
    }
}
