package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.catalog.domain.Brand;
import com.xingqiu.server.catalog.mapper.BrandMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/brands")
public class AdminBrandController {

    private final BrandMapper brandMapper;

    public AdminBrandController(BrandMapper brandMapper) {
        this.brandMapper = brandMapper;
    }

    @GetMapping
    public ApiResponse<List<Brand>> list() {
        return ApiResponse.ok(brandMapper.selectList(new LambdaQueryWrapper<Brand>().orderByAsc(Brand::getSortOrder)));
    }

    @PostMapping
    @Transactional
    public ApiResponse<Brand> create(@RequestBody Map<String, Object> body) {
        Brand brand = new Brand();
        apply(brand, body);
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());
        brandMapper.insert(brand);
        return ApiResponse.ok(brand);
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Brand> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Brand brand = requireBrand(id);
        apply(brand, body);
        brand.setUpdatedAt(LocalDateTime.now());
        brandMapper.updateById(brand);
        return ApiResponse.ok(brandMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireBrand(id);
        brandMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    private Brand requireBrand(Long id) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null) throw new BizException(ErrorCode.NOT_FOUND, "brand not found");
        return brand;
    }

    private void apply(Brand brand, Map<String, Object> body) {
        if (body.get("name") != null) brand.setName(String.valueOf(body.get("name")));
        if (body.get("logoUrl") != null) brand.setLogoUrl(String.valueOf(body.get("logoUrl")));
        if (body.get("sortOrder") instanceof Number number) brand.setSortOrder(number.intValue());
        if (brand.getSortOrder() == null) brand.setSortOrder(0);
    }
}
