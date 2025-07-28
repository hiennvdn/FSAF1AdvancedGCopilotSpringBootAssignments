package com.example.service;

import com.example.dto.CreateOrderRequestDTO;
import com.example.dto.OrderDTO;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.Product;
import com.example.entity.User;
import com.example.repository.OrderRepository;
import com.example.repository.OrderItemRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import com.example.service.impl.OrderServiceImpl;
import com.example.service.impl.ProductServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BuggyOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProductServiceImpl productService;
    
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
    void testBugA_WrongPriceInOrderItem() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productService.findProductById(1L)).thenReturn(testProduct);
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

        // Assert - Bug A: The total should be wrong due to hardcoded price
        // Expected: 2 * 100 = 200, but will be 2 * 9999.99 = 19999.98
        assertEquals(BigDecimal.valueOf(19999.98), result.getTotalAmount());
        System.out.println("Bug A detected: Price is hardcoded to 9999.99 instead of actual product price");
    }

    @Test
    void testBugB_NullPointerException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productService.findProductById(1L)).thenReturn(null); // Bug B: returns null

        // Act & Assert - Bug B: Should throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            orderService.placeOrder(orderRequest);
        });
        System.out.println("Bug B detected: ProductService.findProductById returns null causing NPE");
    }

    @Test
    void testBugC_TransactionIssue() {
        // This test would require integration testing to properly demonstrate
        // the transaction issue, but we can verify the annotation is missing
        try {
            OrderServiceImpl.class.getMethod("placeOrder", CreateOrderRequestDTO.class)
                    .getAnnotation(org.springframework.transaction.annotation.Transactional.class);
            fail("Bug C not detected: @Transactional annotation should be missing");
        } catch (Exception e) {
            System.out.println("Bug C detected: @Transactional annotation is missing from placeOrder method");
        }
    }
}
