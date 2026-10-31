package se.iths.martin.productserviceprojekt2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.iths.martin.productserviceprojekt2.dto.ProductInfoDTO;
import se.iths.martin.productserviceprojekt2.service.StockReservationService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StockReservationHttpTests {
    @Autowired private MockMvc mvc;
    @MockitoBean private StockReservationService service;
    private static final String TOKEN = "internal-test-token-not-for-real-use-123456";

    // Kontrollerar att reservationen returnerar produktuppgifter bakom den interna säkerhetskedjan.
    @Test
    void reservesThroughProtectedEndpoint() throws Exception {
        var id = UUID.randomUUID();
        when(service.reserve(eq(id), eq("customer"), anyList()))
                .thenReturn(List.of(new ProductInfoDTO(42L, "Product", BigDecimal.TEN, 2)));
        mvc.perform(post("/internal/stock-reservations/" + id)
                        .header("X-Internal-Service-Token", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"customer\",\"items\":[{\"productId\":42,\"quantity\":2}]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(42));
    }

    // Kontrollerar att återställning anropas utan kund-JWT när korrekt serverhemlighet finns.
    @Test
    void releasesThroughProtectedEndpoint() throws Exception {
        var id = UUID.randomUUID();
        mvc.perform(post("/internal/stock-reservations/" + id + "/release")
                        .header("X-Internal-Service-Token", TOKEN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"customer\"}"))
                .andExpect(status().isNoContent());
        verify(service).release(id, "customer");
    }

    // Kontrollerar att ett försök utan serverhemlighet aldrig når reservationslogiken.
    @Test
    void rejectsReservationWithoutInternalToken() throws Exception {
        mvc.perform(post("/internal/stock-reservations/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"customer\",\"items\":[{\"productId\":42,\"quantity\":2}]}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }
}
