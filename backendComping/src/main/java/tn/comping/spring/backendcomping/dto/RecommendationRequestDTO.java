package tn.comping.spring.backendcomping.dto;

import java.util.List;

public class RecommendationRequestDTO {

    private List<String> cart;

    public RecommendationRequestDTO() {}

    public RecommendationRequestDTO(List<String> cart) {
        this.cart = cart;
    }

    public List<String> getCart() {
        return cart;
    }

    public void setCart(List<String> cart) {
        this.cart = cart;
    }
}