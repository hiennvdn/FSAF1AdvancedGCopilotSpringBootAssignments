package com.example.service;

import com.example.dto.CreateOrderRequestDTO;
import com.example.dto.OrderDTO;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.Product;
import com.example.entity.User;
import com.example.entity.OrderStatus;
import com.example.repository.OrderRepository;
import com.example.repository.OrderItemRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import com.example.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private OrderServiceImpl orderService;
    
    private User testUser;
    private Product testProduct;
    private CreateOrderRequestDTO orderRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.00));
        testProduct.setStock(10);
        
        CreateOrderRequestDTO.OrderItemRequest itemRequest = new CreateOrderRequestDTO.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);
        
        orderRequest = new CreateOrderRequestDTO();
        orderRequest.setUserId(1L);
        orderRequest.setShippingAddress("123 Test Street");
        orderRequest.setItems(Arrays.asList(itemRequest));
    }

    @Test
    void testPlaceOrderSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> {
            OrderItem item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        // Act
        OrderDTO result = orderService.placeOrder(orderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(BigDecimal.valueOf(200.00), result.getTotalAmount());
        assertEquals("123 Test Street", result.getShippingAddress());
        
        // Verify stock was reduced
        assertEquals(8, testProduct.getStock());
        
        // Verify repository interactions
        verify(userRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void testPlaceOrderUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.placeOrder(orderRequest));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testPlaceOrderProductNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.placeOrder(orderRequest));
        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    void testPlaceOrderInsufficientStock() {
        // Arrange
        testProduct.setStock(1); // Less than requested quantity of 2
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.placeOrder(orderRequest));
        assertTrue(exception.getMessage().contains("InsufficientStockException"));
    }
}
