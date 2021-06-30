package io.github.arsonistcook.beerstock.mapper;

import io.github.arsonistcook.beerstock.dto.BeerDTO;
import io.github.arsonistcook.beerstock.entity.Beer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BeerMapper {

    BeerMapper INSTANCE = Mappers.getMapper(BeerMapper.class);

    Beer toModel(BeerDTO beerDTO);

    BeerDTO toDTO(Beer beer);
}
