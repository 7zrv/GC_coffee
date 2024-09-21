package org.example.gc_coffee.order.service;


import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.gc_coffee.global.configure.aop.TimeTrace;
import org.example.gc_coffee.global.exception.BadRequestException;
import org.example.gc_coffee.order.entity.Order;
import org.example.gc_coffee.order.repository.OrderRepository;
import org.example.gc_coffee.order.requestDto.CreateOrderRequestDto;
import org.example.gc_coffee.order.responseDto.OrderResponseDto;
import org.example.gc_coffee.order.type.OrderStatus;
import org.example.gc_coffee.orderItem.entity.OrderItem;
import org.example.gc_coffee.orderItem.requestDto.CreateOrderItemRequestDto;
import org.example.gc_coffee.orderItem.responseDto.OrderItemResponse;
import org.example.gc_coffee.orderItem.service.OrderItemService;
import org.example.gc_coffee.product.entity.Product;
import org.example.gc_coffee.product.repository.ProductRepository;
import org.example.gc_coffee.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.gc_coffee.global.exception.ExceptionCode.NOT_FOUND_ORDER_ID;
import static org.example.gc_coffee.global.exception.ExceptionCode.NOT_FOUND_PRODUCT_ID;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final ProductService productService;


    //주문 등록 메서드
    @TimeTrace
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto requestDto) {

        Order order = requestDto.toEntity();
        orderRepository.save(order);  // Order는 즉시 저장

        // productId 목록을 먼저 수집
        List<UUID> productIds = extractionProductIds(requestDto);

        // productId 목록으로 한번에 제품 조회
        Map<UUID, Product> productMap = getProducts(productIds);

        // orderItems 및 응답 객체 생성
        List<OrderItem> orderItems = getOrderItems(requestDto, productMap, order);

        // orderItems 한번에 저장 (saveAll)
        orderItemService.createOrderItems(orderItems);

        //responseDto 변환 메서드
        List<OrderItemResponse> orderItemResponses = convertToOrderItemResponses(orderItems);

        // 응답 반환
        return OrderResponseDto.of(order, orderItemResponses);
    }

    private static List<OrderItemResponse> convertToOrderItemResponses(List<OrderItem> orderItems) {
        List<OrderItemResponse> orderItemResponses = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            orderItemResponses.add(OrderItemResponse.from(orderItem));
        }
        return orderItemResponses;
    }

    private static List<OrderItem> getOrderItems(CreateOrderRequestDto requestDto, Map<UUID, Product> productMap, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        

        for (CreateOrderItemRequestDto itemDto : requestDto.getItems()) {

            // 미리 조회한 Product 리스트에서 해당 제품 찾기
            Product product = productMap.get(itemDto.getProductId());

            OrderItem orderItem = itemDto.of(product, order);
            orderItems.add(orderItem);
            
        }
        return orderItems;
    }

    private static List<UUID> extractionProductIds(CreateOrderRequestDto requestDto) {
        List<UUID> productIds = requestDto.getItems().stream()
                .map(CreateOrderItemRequestDto::getProductId)
                .collect(Collectors.toList());
        return productIds;
    }

    private Map<UUID, Product> getProducts(List<UUID> productIds) {
        Map<UUID, Product> productMap = productService.getProductByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        return productMap;
    }


    //주문 단건 조회
    public OrderResponseDto getOrder(UUID orderId) {

        Order order = findOrderById(orderId);

        return OrderResponseDto.from(order);
    }

    //PK를 통한 주문 검색
    public Order findOrderById(UUID orderId) {

        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_ORDER_ID));
    }


    //주문 삭제 메서드
    public void deleteOrder(UUID orderId) {

        Order order = findOrderById(orderId);

        orderRepository.deleteById(order.getOrderId());
    }


    public void updateOrderStatus(LocalDateTime startOfDay, LocalDateTime endOfDay) {

        orderRepository.updateOrderStatus(OrderStatus.SHIPPED, startOfDay, endOfDay);
    }
}
