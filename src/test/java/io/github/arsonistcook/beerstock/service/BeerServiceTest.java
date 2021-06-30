package io.github.arsonistcook.beerstock.service;

import io.github.arsonistcook.beerstock.builder.BeerDTOBuilder;
import io.github.arsonistcook.beerstock.dto.BeerDTO;
import io.github.arsonistcook.beerstock.entity.Beer;
import io.github.arsonistcook.beerstock.exception.*;
import io.github.arsonistcook.beerstock.mapper.BeerMapper;
import io.github.arsonistcook.beerstock.repository.BeerRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BeerServiceTest {
    private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        //Given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

        //When
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        //Then
        BeerDTO savedBeer = beerService.createBeer(expectedBeerDTO);
        assertThat(savedBeer.getName(), is(equalTo(expectedSavedBeer.getName())));
        assertThat(savedBeer.getBrand(), is(equalTo(expectedSavedBeer.getBrand())));
        assertThat(savedBeer.getType(), is(equalTo(expectedSavedBeer.getType())));
    }

    @Test
    void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() {
        //Given
        BeerDTO expectedSavedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer beerAlreadySaved = beerMapper.toModel(expectedSavedBeerDTO);

        //When
        when(beerRepository.findByName(expectedSavedBeerDTO.getName())).thenReturn(Optional.of(beerAlreadySaved));

        //Then
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedSavedBeerDTO));
    }

    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
        //Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //When
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.of(expectedFoundBeer));

        //Then
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());
        assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() throws BeerNotFoundException {
        //Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //When
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());

        //Then
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
    }

    @Test
    void whenListBeerIsCalledThenReturnABeersList() {
        //Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //When
        when(beerRepository.findAll()).thenReturn(List.of(expectedFoundBeer));

        //Then
       List<BeerDTO> foundBeersList= beerService.listAll();
       assertThat(foundBeersList, is(not(empty())));
       assertThat(foundBeersList.get(0), is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
    void whenListBeerIsCalledThenReturnAnEmptyList() {
        //Given

        //When
        when(beerRepository.findAll()).thenReturn(Lists.emptyList());

        //Then
        List<BeerDTO> foundBeersList= beerService.listAll();
        assertThat(foundBeersList, is(empty()));
    }

    @Test
    void whenExclusionIsCalledWithValidIdThenBeerShouldBeDeleted() throws BeerNotFoundException {
        //Given
        BeerDTO expectedToBeDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedToBeDeletedBeer = beerMapper.toModel(expectedToBeDeletedBeerDTO);

        //When
        when(beerRepository.findById(expectedToBeDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedToBeDeletedBeer));
        doNothing().when(beerRepository).deleteById(expectedToBeDeletedBeerDTO.getId());

        //Then
        beerService.deleteById(expectedToBeDeletedBeerDTO.getId());
        verify(beerRepository, times(1)).findById(expectedToBeDeletedBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedToBeDeletedBeerDTO.getId());
    }

    @Test
    void whenExclusionIsCalledWithAnInvalidIdThenThrowAnException(){
        //Given

        //When
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        //Then
        assertThrows(BeerNotFoundException.class, () -> beerService.deleteById(INVALID_BEER_ID));
    }

    @Test
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException, BeerStockNegativeArgumentException {
        //Given
        BeerDTO beerToIncrementDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer beerToIncrement = beerMapper.toModel(beerToIncrementDTO);

        //When
        when(beerRepository.findById(beerToIncrementDTO.getId())).thenReturn(Optional.of(beerToIncrement));
        when(beerRepository.save(beerToIncrement)).thenReturn(beerToIncrement);

        //Then
        int incrementQuantity = 10;
        int expectedFinalQuantity = beerToIncrementDTO.getQuantity() + incrementQuantity;
        BeerDTO beerAfterIncrement = beerService.increment(beerToIncrementDTO.getId(), incrementQuantity);

        assertThat(expectedFinalQuantity, is(equalTo(beerAfterIncrement.getQuantity())));
        assertThat(expectedFinalQuantity, is(lessThan(beerToIncrementDTO.getMax())));
    }

    @Test
    void whenIncrementIsGreaterThanMaxThenThrowAnException() {
        //Given
        BeerDTO beerToIncrementDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer beerToIncrement = beerMapper.toModel(beerToIncrementDTO);

        //When
        when(beerRepository.findById(beerToIncrementDTO.getId())).thenReturn(Optional.of(beerToIncrement));

        //Then
        int incrementQuantity = 85;
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(beerToIncrementDTO.getId(),incrementQuantity));
    }

    @Test
    void whenIncrementIsCalledWithANegativeParameterThenThrowAnException(){
        //Given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        int decrementQuantity = -10;
        //When

        //Then
        assertThrows(BeerStockNegativeArgumentException.class, () -> beerService.increment(beerDTO.getId(), decrementQuantity));
    }

    @Test
    void whenIncrementSumIsGreaterThanMaxThenThrowAnException() {
        //Given
        BeerDTO beerToIncrementDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer beerToIncrement = beerMapper.toModel(beerToIncrementDTO);

        //When
        when(beerRepository.findById(beerToIncrementDTO.getId())).thenReturn(Optional.of(beerToIncrement));

        //Then
        int incrementQuantity = 45;
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(beerToIncrementDTO.getId(),incrementQuantity));
    }

    @Test
    void whenIncrementIsCalledWithAnInvalidIdThenThrowAnException(){
        //Given
        int quantityToIncrement = 10;
        //When
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        //Then
        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }

    //Decrement

    @Test
    void whenDecrementIsCalledThenDecrementBeerStock() throws BeerNotFoundException, BeerStockNegativeArgumentException, BeerStockMinimumException {
        //Given
        BeerDTO beerToDecrementDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer beerToDecrement = beerMapper.toModel(beerToDecrementDTO);

        //When
        when(beerRepository.findById(beerToDecrementDTO.getId())).thenReturn(Optional.of(beerToDecrement));
        when(beerRepository.save(beerToDecrement)).thenReturn(beerToDecrement);

        //Then
        int decrementQuantity = 10;
        int expectedFinalQuantity = beerToDecrementDTO.getQuantity() - decrementQuantity;
        BeerDTO beerAfterDecrement = beerService.decrement(beerToDecrementDTO.getId(), decrementQuantity);

        assertThat(expectedFinalQuantity, is(equalTo(beerAfterDecrement.getQuantity())));
        assertThat(expectedFinalQuantity, is(not(lessThan(0))));
    }

    @Test
    void whenDecrementIsLowerThanZeroThenThrowAnException() {
        //Given
        BeerDTO beerToDecrementDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        int decrementQuantity = -45;
        //When

        //Then
        assertThrows(BeerStockNegativeArgumentException.class, () -> beerService.decrement(beerToDecrementDTO.getId(),decrementQuantity));
    }

    @Test
    void whenDecrementSumIsLowerThanZeroThenThrowAnException() {
        //Given
        BeerDTO beerToDecrementDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer beerToDecrement = beerMapper.toModel(beerToDecrementDTO);

        //When
        when(beerRepository.findById(beerToDecrementDTO.getId())).thenReturn(Optional.of(beerToDecrement));

        //Then
        int decrementQuantity = 45;
        assertThrows(BeerStockMinimumException.class, () -> beerService.decrement(beerToDecrementDTO.getId(),decrementQuantity));
    }

    @Test
    void whenDecrementIsCalledWithAnInvalidIdThenThrowAnException(){
        //Given
        int quantityToDecrement = 10;
        //When
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        //Then
        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, quantityToDecrement));
    }
}