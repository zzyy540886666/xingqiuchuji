package com.xingqiu.server.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingqiu.server.repair.domain.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    @Select("<script>" +
            "SELECT * FROM work_orders WHERE 1=1 " +
            "<if test='status != null and status != \"\"'>AND status = #{status}</if> " +
            "<if test='technicianId != null'>AND assigned_technician_id = #{technicianId}</if> " +
            "ORDER BY created_at DESC" +
            "</script>")
    List<WorkOrder> selectListByFilters(@Param("status") String status,
                                        @Param("technicianId") Long technicianId);
}
