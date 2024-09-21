package org.example.gc_coffee.order.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.gc_coffee.global.common.response.ApiResponse;
import org.example.gc_coffee.order.requestDto.CreateOrderRequestDto;
import org.example.gc_coffee.order.responseDto.OrderResponseDto;
import org.example.gc_coffee.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/order")
public class OrderApiController {

    private final OrderService orderService;

    //주문등록 컨트롤러
    @PostMapping("")
    public ApiResponse<OrderResponseDto> createOrder(@RequestBody @Valid CreateOrderRequestDto requestDto) {

        OrderResponseDto responseDto = orderService.createOrder(requestDto);

        return ApiResponse.ok(201, responseDto, "상품 주문 성공");
    }

    //pk를 이용한 주문 단건조회 컨트롤러
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponseDto> getOrder(@PathVariable UUID orderId) {

        OrderResponseDto responseDto = orderService.getOrder(orderId);

        return ApiResponse.ok(200, responseDto, "주문 조회 성공");
    }

    //주문취소 컨트롤러
    @DeleteMapping("/{orderId}")
    public ApiResponse<?> deleteOrder(@PathVariable UUID orderId) {

        orderService.deleteOrder(orderId);

        return ApiResponse.ok("주문 취소 성공");
    }

}
