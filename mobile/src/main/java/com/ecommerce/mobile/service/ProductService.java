package com.ecommerce.mobile.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*; //phân trang thư viện
import org.springframework.stereotype.Service;

import com.ecommerce.mobile.entity.Category;
import com.ecommerce.mobile.entity.Product;
import com.ecommerce.mobile.enums.ProductStatus;
import com.ecommerce.mobile.repository.CategoryRepository;
import com.ecommerce.mobile.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private CategoryRepository categoryRepository;


    /// claude dạy?

    // searchProducts(),
    public Page<Product> searchProducts(String keyword, int page, int size, Long categoryId){
        // PageRequest.of(page, size): tạo Pageable từ số trang và kích thước 
        Pageable pageable = PageRequest.of(page,size); // ko cần code phân trang
        return productRepository.searchByStatusAndKeyword(ProductStatus.ACTIVE, keyword, pageable); // k hề phưucs tạp, tìm rồi kết quả là hiển thị, ok? repo truy vấn hộ rồi.
        // service chỉ cần phân tragn để hiện sản phẩm hộ repo?
    }
    
    // toi vẫn thấy nó giống ma thuật. 
    // dù tôi hiểu pageable nó làm hết tất cả cv phân trang, còn repo tập trung tìm sản phẩm. 
    // Tóm lại mình k code thuật toán mà dùng thuật toán của CSDL? và các từ ngữ để két nối với csdl ?

    //  findById(),
    public Optional<Product> findById(Long productId){
        return productRepository.findById(productId);
    }


    //  getAllCategories()
    public List<Category> getAllCategories(){
        return categoryRepository.findByIsActiveTrue();
    }
}
