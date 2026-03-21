package com.ecommerce.mobile.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ecommerce.mobile.entity.Address;
import com.ecommerce.mobile.entity.Customer;
import com.ecommerce.mobile.entity.Role;
import com.ecommerce.mobile.repository.AddressRepository;
import com.ecommerce.mobile.repository.CustomerRepository;
import com.ecommerce.mobile.repository.RoleRepository;
import jakarta.transaction.Transactional;

@Service
public class CustomerService {
    @Autowired 
    private CustomerRepository customerRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired 
    private AddressRepository addressRepository;

    @Transactional

    // Luồng đăng ký kiểm tra email -> lấy role -> tạo customer -> lưu DB
    public Customer register(String email, String password, String fullName, String phone){
        // BƯỚC 1: duyệt email tài khoản
        if (customerRepository.existsByEmail(email)){
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Bước 1.5 Duyệt xem, role gì? (Phải là CUSTOMER.)
        Role role = roleRepository.findByNameRole("CUSTOMER").orElseThrow(() -> new RuntimeException("Lỗi, không tìm thấy role CUSTOMER"));

        // Bước 2: set các thứ
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setHashPassword(passwordEncoder.encode(password));
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setRole(role);
        customer.setIsActive(true);  

         // trả về tài khoản customer đã dă ký và lưu vào DB
    return customerRepository.save(customer);

    }
   // cập nhật thông tin
    public Customer updateCustomerInfo(Long userId, String fullName, String phone){
        // gọi Customer từ DB (repository)
        @SuppressWarnings("null")
        Customer customer = customerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan!"));
            customer.setFullName(fullName);
            customer.setPhone(phone);
        
        return customerRepository.save(customer);

    }
        // ===== U11: DOI MAT KHAU =====
    // U11 ngoai le 6.1: phai kiem tra mat khau cu truoc khi doi
    @Transactional
    public Customer changePassword(Long userId, String oldPass, String newPass){
        Customer customer = customerRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

            if (!passwordEncoder.matches(oldPass, customer.getHashPassword())) {
                throw new RuntimeException("Mat khau hien tai khong chinh xac!");
            }
            customer.setHashPassword(passwordEncoder.encode(newPass));
            return customerRepository.save(customer);
        }

     // Thêm địa chỉ
     @Transactional
     public Address addAddress(String street, String city, Long customerId, String phone){
        // lấy id của người thay đổi địa chỉ để thay đổi cho họ. 
          Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setPhone(phone);
        address.setCustomer(customer);
        address.setIsDefault(false);
        return addressRepository.save(address);}
            
        
        
        // Set địa chỉ mặc định   
    
    @Transactional
    public void setDefaultAddress(Long customerId, Long addressId){

///  bỏ nhãn mặc định cho tất cả các địa chỉ
        List<Address> all = addressRepository.findByCustomerUserID(customerId);
        all.forEach(a -> a.setIsDefault(false)); // chỗ này chưa thạo
        addressRepository.saveAll(all);

        @SuppressWarnings("null")
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
        address.setIsDefault(true);
        addressRepository.save(address);
    }
}
