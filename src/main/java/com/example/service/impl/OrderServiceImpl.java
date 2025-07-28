package com.example.service.impl;

import com.example.dto.CreateOrderRequestDTO;
import com.example.dto.OrderDTO;
import com.example.dto.OrderItemDTO;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.Product;
import com.example.entity.User;
import com.example.entity.OrderStatus;
import com.example.repository.OrderRepository;
import com.example.repository.OrderItemRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import com.example.service.OrderService;
import com.example.service.impl.ProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductServiceImpl productService;

    @Override
    @Transactional // Fixed Bug C: Restored transaction management
    public OrderDTO placeOrder(CreateOrderRequestDTO request) {
        User user = verifyUserExists(request.getUserId());
        Order order = buildOrder(user, request);
        List<OrderItem> items = processOrderItems(order, request.getItems());
        BigDecimal total = calculateTotal(items);
        order.setTotalAmount(total);
        persistOrder(order, items);
        return toDTO(order);
    }

    private User verifyUserExists(Long userId) {
        return userRepository.findById(userId).orElseThrow(
            () -> new RuntimeException("User not found with id: " + userId)
        );
    }

    private Order buildOrder(User user, CreateOrderRequestDTO request) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());
        return order;
    }

    private List<OrderItem> processOrderItems(Order order, List<CreateOrderRequestDTO.OrderItemRequest> itemRequests) {
        List<OrderItem> items = new ArrayList<>();
        for (CreateOrderRequestDTO.OrderItemRequest itemReq : itemRequests) {
            Product product = getProductAndUpdateStock(itemReq.getProductId(), itemReq.getQuantity());
            OrderItem item = createOrderItem(order, product, itemReq.getQuantity());
            items.add(item);
        }
        return items;
    }

    private Product getProductAndUpdateStock(Long productId, Integer quantity) {
        Product product = productService.findProductById(productId); // Bug B: This will return null
        validateStockAvailability(product, quantity);
        deductStock(product, quantity);
        return product;
    }

    private void validateStockAvailability(Product product, Integer quantity) {
        if (product.getStock() < quantity) {
            throw new RuntimeException("InsufficientStockException for product: " + product.getName());
        }
    }

    private void deductStock(Product product, Integer quantity) {
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    private OrderItem createOrderItem(Order order, Product product, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPrice(product.getPrice()); // Fixed Bug A: Use actual product price
        return item;
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void persistOrder(Order order, List<OrderItem> items) {
        orderRepository.save(order);
        for (OrderItem item : items) {
            orderItemRepository.save(item);
        }
        order.getItems().addAll(items);
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        // Implement as needed
        return new ArrayList<>();
    }

    @Override
    public List<OrderDTO> getOrdersByUser(Long userId) {
        // Implement as needed
        return new ArrayList<>();
    }

    @Override
    public void cancelOrder(Long orderId) {
        // Implement as needed
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        List<OrderItemDTO> itemDTOs = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getName());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());
            itemDTOs.add(itemDTO);
        }
        dto.setItems(itemDTOs);
        return dto;
    }
}
