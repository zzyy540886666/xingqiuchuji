package com.xingqiu.server.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.catalog.domain.CatalogFilterGroup;
import com.xingqiu.server.catalog.domain.CatalogFilterOption;
import com.xingqiu.server.catalog.mapper.CatalogFilterGroupMapper;
import com.xingqiu.server.catalog.mapper.CatalogFilterOptionMapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.common.response.ApiResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/catalog-filters")
public class AdminCatalogFilterController {

    private final CatalogFilterGroupMapper groupMapper;
    private final CatalogFilterOptionMapper optionMapper;

    public AdminCatalogFilterController(CatalogFilterGroupMapper groupMapper,
                                        CatalogFilterOptionMapper optionMapper) {
        this.groupMapper = groupMapper;
        this.optionMapper = optionMapper;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        List<CatalogFilterGroup> groups = groupMapper.selectList(new LambdaQueryWrapper<CatalogFilterGroup>()
                .orderByAsc(CatalogFilterGroup::getSortOrder));
        return ApiResponse.ok(groups.stream().map(this::toRow).toList());
    }

    @PostMapping
    @Transactional
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        CatalogFilterGroup group = new CatalogFilterGroup();
        applyGroup(group, body);
        groupMapper.insert(group);
        saveOptions(group.getId(), listBody(body.get("options")));
        return ApiResponse.ok(toRow(groupMapper.selectById(group.getId())));
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        CatalogFilterGroup group = requireGroup(id);
        applyGroup(group, body);
        groupMapper.updateById(group);
        if (body.containsKey("options")) {
            saveOptions(id, listBody(body.get("options")));
        }
        return ApiResponse.ok(toRow(groupMapper.selectById(id)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requireGroup(id);
        optionMapper.delete(new LambdaQueryWrapper<CatalogFilterOption>().eq(CatalogFilterOption::getGroupId, id));
        groupMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    private CatalogFilterGroup requireGroup(Long id) {
        CatalogFilterGroup group = groupMapper.selectById(id);
        if (group == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "catalog filter group not found");
        }
        return group;
    }

    private Map<String, Object> toRow(CatalogFilterGroup group) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", group.getId());
        row.put("code", group.getCode());
        row.put("title", group.getTitle());
        row.put("filterField", group.getFilterField());
        row.put("sortOrder", group.getSortOrder());
        row.put("enabled", group.getEnabled());
        row.put("options", optionMapper.selectList(new LambdaQueryWrapper<CatalogFilterOption>()
                .eq(CatalogFilterOption::getGroupId, group.getId())
                .orderByAsc(CatalogFilterOption::getSortOrder)));
        return row;
    }

    private void applyGroup(CatalogFilterGroup group, Map<String, Object> body) {
        group.setCode(text(body.get("code"), group.getCode()));
        group.setTitle(text(body.get("title"), group.getTitle()));
        group.setFilterField(text(body.get("filterField"), group.getFilterField()));
        group.setSortOrder(number(body.get("sortOrder"), group.getSortOrder() == null ? 0L : group.getSortOrder().longValue()).intValue());
        group.setEnabled(bool(body.get("enabled"), group.getEnabled() == null || group.getEnabled()));
    }

    private void saveOptions(Long groupId, List<Map<String, Object>> options) {
        optionMapper.delete(new LambdaQueryWrapper<CatalogFilterOption>().eq(CatalogFilterOption::getGroupId, groupId));
        int index = 0;
        for (Map<String, Object> item : options) {
            CatalogFilterOption option = new CatalogFilterOption();
            option.setGroupId(groupId);
            option.setLabel(text(item.get("label"), ""));
            option.setValue(text(item.get("value"), ""));
            option.setMinPriceMinor(number(item.get("minPriceMinor"), null));
            option.setMaxPriceMinor(number(item.get("maxPriceMinor"), null));
            option.setSortOrder(number(item.get("sortOrder"), (long) index).intValue());
            option.setEnabled(bool(item.get("enabled"), true));
            optionMapper.insert(option);
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listBody(Object value) {
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private String text(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private Long number(Object value, Long fallback) {
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Boolean bool(Object value, Boolean fallback) {
        if (value == null) return fallback;
        if (value instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
