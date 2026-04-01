package com.ecommerce.mobile.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.mobile.entity.Cart;
import com.ecommerce.mobile.entity.CartItem;
import com.ecommerce.mobile.entity.Customer;
import com.ecommerce.mobile.entity.Order;
import com.ecommerce.mobile.entity.OrderItem;
import com.ecommerce.mobile.entity.ProductVariant;
import com.ecommerce.mobile.enums.OrderStatus;
import com.ecommerce.mobile.enums.PaymentMethod;
import com.ecommerce.mobile.repository.OrderRepository;

@Service
public class OrderService {

    private final CartService cartService;
    private final CustomerService customerService;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    public OrderService(CartService cartService,
                        CustomerService customerService,
                        OrderRepository orderRepository,
                        PaymentService paymentService) {
        this.cartService = cartService;
        this.customerService = customerService;
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public Order placeOrder(String customerEmail,
                            String shippingName,
                            String shippingPhone,
                            String shippingAddress,
                            String shippingCity,
                            String voucherCode,
                            PaymentMethod paymentMethod,
                            Long cartId) {
        Customer customer = customerService.requireCustomerByEmail(customerEmail);
        Cart cart = cartService.getCartByCustomerEmail(customerEmail);

        if (cart.getCartId() == null || !cart.getCartId().equals(cartId)) {
            throw new RuntimeException("Giỏ hàng không hợp lệ");
        }
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethod == null ? PaymentMethod.COD : paymentMethod);
        order.setShippingName(shippingName);
        order.setShippingPhone(shippingPhone);
        order.setShippingAddress(shippingAddress);
        order.setShippingCity(shippingCity);
        order.setAppliedVoucher(voucherCode == null || voucherCode.isBlank() ? null : voucherCode.trim());
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();
            if (variant == null) {
                continue;
            }
            Integer quantity = cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
            if (quantity < 1) {
                continue;
            }

            if (variant.getStockQty() != null && quantity > variant.getStockQty()) {
                throw new RuntimeException("Sản phẩm " + cartItem.getProductName() + " không đủ tồn kho");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setQuantity(quantity);
            order.getItems().add(orderItem);

            if (cartItem.getSubtotal() != null) {
                total = total.add(cartItem.getSubtotal());
            }
        }

        order.setTotalAmount(total.add(order.getShippingFee()).subtract(order.getDiscountAmount()));
        order = orderRepository.save(order);
        order.getPayments().add(paymentService.createPayment(order, order.getPaymentMethod()));
        Order saved = orderRepository.save(order);
        cartService.clearCart(customerEmail);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerEmail(String customerEmail) {
        Customer customer = customerService.requireCustomerByEmail(customerEmail);
        return orderRepository.findByCustomerUserIDOrderByCreatedAtDesc(customer.getUserID());
    }

    @Transactional(readOnly = true)
    public Order getOrderDetailByCustomerEmail(String customerEmail, Long orderId) {
        Customer customer = customerService.requireCustomerByEmail(customerEmail);
        return orderRepository.findDetailedByOrderId(orderId)
                .filter(order -> order.getCustomer() != null
                        && order.getCustomer().getUserID().equals(customer.getUserID()))
                .orElse(null);
    }

    private String generateOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
