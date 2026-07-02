package pe.edu.vallegrande.mspedidos.infrastructure.adapter.out.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.mspedidos.application.port.out.IProductoClientPort;
import pe.edu.vallegrande.mspedidos.domain.model.Producto;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductoClientAdapter implements IProductoClientPort {

    private final WebClient.Builder webClientBuilder;

    @Value("${servicios.productos-url}")
    private String productosUrl;

    @Override
    public Mono<Producto> findById(Long id) {
        return webClientBuilder.build()
                .get()
                .uri(productosUrl + "/api/productos/" + id)
                .retrieve()
                .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> decreaseStock(Long id, Integer quantity) {
        return webClientBuilder.build()
                .put()
                .uri(productosUrl + "/api/productos/" + id + "/decrease-stock?quantity=" + quantity)
                .retrieve()
                .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> increaseStock(Long id, Integer quantity) {
        return webClientBuilder.build()
                .put()
                .uri(productosUrl + "/api/productos/" + id + "/increase-stock?quantity=" + quantity)
                .retrieve()
                .bodyToMono(Producto.class);
    }
}
