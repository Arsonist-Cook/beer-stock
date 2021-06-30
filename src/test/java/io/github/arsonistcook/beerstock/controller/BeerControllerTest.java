package io.github.arsonistcook.beerstock.controller;

import io.github.arsonistcook.beerstock.builder.BeerDTOBuilder;
import io.github.arsonistcook.beerstock.dto.BeerDTO;
import io.github.arsonistcook.beerstock.dto.QuantityDTO;
import io.github.arsonistcook.beerstock.exception.BeerNotFoundException;
import io.github.arsonistcook.beerstock.exception.BeerStockExceededException;
import io.github.arsonistcook.beerstock.exception.BeerStockNegativeArgumentException;
import io.github.arsonistcook.beerstock.service.BeerService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.List;

import static io.github.arsonistcook.beerstock.utils.JSONConvertionUtils.asJSONString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

    private static final String BEER_API_URL_PATH = "/api/v1/beers";
    private static final long VALID_BEER_ID = 1L;
    private static final long INVALID_BEER_ID = 2l;
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

    private MockMvc mockMVC;

    @Mock
    private BeerService beerService;

    @InjectMocks
    private BeerController beerController;

    @BeforeEach
    void setup() {
        //MockMvc belongs to Spring...
        mockMVC = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
    void whenPOSTIsCalledThenBeerIsCreated() throws Exception {
        //Given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //When
        when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);

        //Then
        mockMVC.perform(
                post(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(beerDTO))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    @Test
    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {
        //Given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setBrand(null);
        //When
        //Then
        mockMVC.perform(
                post(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(beerDTO))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
        //Given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //When
        when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);

        //Then
        mockMVC.perform(
                get(BEER_API_URL_PATH + "/" + beerDTO.getName())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    @Test
    void whenGETIsCalledWithoutValidNameThenNotFoundStatusIsReturned() throws Exception {
        //Given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //When
        when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);

        //Then
        mockMVC.perform(
                get(BEER_API_URL_PATH + "/" + beerDTO.getName())
        )
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGETListWithBeersIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
        //Given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //When
        when(beerService.listAll()).thenReturn(List.of(beerDTO));

        //Then
        mockMVC.perform(
                get(BEER_API_URL_PATH)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(beerDTO.getName())))
                .andExpect(jsonPath("$[0].brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$[0].type", is(beerDTO.getType().toString())));
    }

    @Test
    void whenGETListWithoutBeersIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
        //Given

        //When
        when(beerService.listAll()).thenReturn(Lists.emptyList());

        //Then
        mockMVC.perform(
                get(BEER_API_URL_PATH)
        )
                .andExpect(status().isOk());
    }

    @Test
    void whenDELETEIsCalledWithValidIdThenNotContentStatusIsReturned() throws Exception {
        //Given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //When
        doNothing().when(beerService).deleteById(beerDTO.getId());

        //Then
        mockMVC.perform(
                delete(BEER_API_URL_PATH + "/" + beerDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDELETEIsCalledWithAnInvalidIdThenNotFoundStatusIsReturned() throws Exception {
        //When
        doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);

        //Then
        mockMVC.perform(
                delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHIsCalledToIncrementBeerThenOkStatusIsReturned() throws Exception {
        //Given
        QuantityDTO quantityToIncrementDTO = QuantityDTO.builder()
                .quantity(10)
                .build();
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setQuantity(beerDTO.getQuantity() + quantityToIncrementDTO.getQuantity());

        //When
        when(beerService.increment(VALID_BEER_ID, quantityToIncrementDTO.getQuantity())).thenReturn(beerDTO);

        //Then
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(quantityToIncrementDTO))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
                .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
    }

    @Test
    void whenPATCHIsCalledToIncrementBeerMoreThanMaxThenABadRequestStatusIsReturned() throws Exception {
        //Given
        QuantityDTO quantityToIncrementDTO = QuantityDTO.builder()
                .quantity(50)
                .build();

        //When
        when(beerService.increment(VALID_BEER_ID, quantityToIncrementDTO.getQuantity())).thenThrow(BeerStockExceededException.class);
        //Then
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(quantityToIncrementDTO))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHIsCalledToIncrementBeerWithNegativeQuantityThenABadRequestStatusIsReturned() throws Exception {
        //Given
        QuantityDTO quantityToIncrementDTO = QuantityDTO.builder()
                .quantity(-10)
                .build();

        //When
        when(beerService.increment(VALID_BEER_ID, quantityToIncrementDTO.getQuantity())).thenThrow(BeerStockNegativeArgumentException.class);
        //Then
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(quantityToIncrementDTO))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHIsCalledToIncrementBeerWithAnInvalidIdThenANotFoundStatusIsReturned() throws Exception {
        //Given
        QuantityDTO quantityToIncrementDTO = QuantityDTO.builder()
                .quantity(10)
                .build();

        //When
        when(beerService.increment(INVALID_BEER_ID, quantityToIncrementDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
        //Then
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(quantityToIncrementDTO))
        )
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHIsCalledToIncrementBeerWithoutRequestBodyThenABadRequestStatusIsReturned() throws Exception {
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }


    @Test
    void whenPATCHIsCalledToDecrementBeerThenOkStatusIsReturned() throws Exception {
        //Given
        QuantityDTO quantityToDecrementDTO = QuantityDTO.builder()
                .quantity(10)
                .build();
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setQuantity(beerDTO.getQuantity() - quantityToDecrementDTO.getQuantity());

        //When
        when(beerService.decrement(VALID_BEER_ID, quantityToDecrementDTO.getQuantity())).thenReturn(beerDTO);

        //Then
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(quantityToDecrementDTO))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
                .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
    }

    @Test
    void whenPATCHIsCalledToDecrementBeerLessThanZeroThenABadRequestStatusIsReturned() throws Exception {
        //Given
        QuantityDTO quantityToDecrementDTO = QuantityDTO.builder()
                .quantity(50)
                .build();

        //When
        when(beerService.decrement(VALID_BEER_ID, quantityToDecrementDTO.getQuantity())).thenThrow(BeerStockNegativeArgumentException.class);
        //Then
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(quantityToDecrementDTO))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHIsCalledToDecrementBeerWithNegativeQuantityThenABadRequestStatusIsReturned() throws Exception {
        //Given
        QuantityDTO quantityToDecrementDTO = QuantityDTO.builder()
                .quantity(-10)
                .build();

        //When
        when(beerService.decrement(VALID_BEER_ID, quantityToDecrementDTO.getQuantity())).thenThrow(BeerStockNegativeArgumentException.class);
        //Then
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(quantityToDecrementDTO))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHIsCalledToDecrementBeerWithAnInvalidIdThenANotFoundStatusIsReturned() throws Exception {
        //Given
        QuantityDTO quantityToDecrementDTO = QuantityDTO.builder()
                .quantity(10)
                .build();

        //When
        when(beerService.decrement(INVALID_BEER_ID, quantityToDecrementDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
        //Then
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJSONString(quantityToDecrementDTO))
        )
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHIsCalledToDecrementBeerWithoutRequestBodyThenABadRequestStatusIsReturned() throws Exception {
        mockMVC.perform(
                patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest());
    }

}
