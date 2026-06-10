package com.xingqiu.server.distribution.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingqiu.server.distribution.domain.InviteRelation;
import com.xingqiu.server.distribution.dto.CommissionResponse;
import com.xingqiu.server.distribution.dto.DistributionTeamResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DistributionMapper extends BaseMapper<InviteRelation> {

    @Select("SELECT COUNT(*) FROM invite_relations WHERE inviter_user_id = #{userId} AND level = 1")
    int countLevel1(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM invite_relations WHERE inviter_user_id = #{userId} AND level = 2")
    int countLevel2(@Param("userId") Long userId);

    @Select("""
            SELECT r.invitee_user_id AS user_id, u.nickname, u.avatar_url, r.level, r.bound_at AS joined_at
            FROM invite_relations r
            JOIN users u ON u.id = r.invitee_user_id
            WHERE r.inviter_user_id = #{userId}
            ORDER BY r.bound_at DESC
            """)
    List<DistributionTeamResponse.Member> findTeamMembers(@Param("userId") Long userId);

    @Select("""
            <script>
            SELECT COALESCE(SUM(amount_minor), 0)
            FROM commission_entries
            WHERE beneficiary_user_id = #{userId}
              AND status IN
              <foreach item="status" collection="statuses" open="(" separator="," close=")">
                #{status}
              </foreach>
            </script>
            """)
    Long sumCommissionByStatuses(@Param("userId") Long userId, @Param("statuses") List<String> statuses);

    @Select("""
            SELECT COALESCE(SUM(amount_minor), 0)
            FROM withdraw_requests
            WHERE user_id = #{userId}
              AND status = 'SUCCESS'
            """)
    Long sumSuccessfulWithdrawnCommission(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM invite_relations
            WHERE inviter_user_id = #{userId}
              AND bound_at >= #{start}
              AND bound_at < #{end}
            """)
    int countTodayNew(@Param("userId") Long userId,
                      @Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end);

    @Select("""
            SELECT COUNT(DISTINCT invitee_user_id)
            FROM invite_relations
            WHERE inviter_user_id = #{userId}
            """)
    int countTotalInvite(@Param("userId") Long userId);

    @Select("""
            <script>
            SELECT
              ce.id,
              ce.order_id,
              o.order_no,
              ce.beneficiary_user_id,
              ce.source_user_id,
              ce.level,
              ce.amount_minor,
              ce.rate_percent,
              ce.status,
              ce.protect_until,
              ce.settled_at,
              ce.created_at,
              COALESCE(sm.url, '/static/icons/device-placeholder.svg') AS product_image,
              COALESCE(ol.sku_name, CONCAT('订单 ', o.order_no)) AS product_name,
              CASE
                WHEN o.order_type = 'RENT' THEN 'RENT'
                WHEN o.order_type = 'BUY' AND ol.sku_name LIKE '%配件%' THEN 'ACCESSORY'
                ELSE 'BUY'
              END AS source_type
            FROM commission_entries ce
            JOIN orders o ON o.id = ce.order_id
            LEFT JOIN (
              SELECT order_id, MIN(id) AS line_id
              FROM order_lines
              GROUP BY order_id
            ) first_line ON first_line.order_id = ce.order_id
            LEFT JOIN order_lines ol ON ol.id = first_line.line_id
            LEFT JOIN (
              SELECT sku_id, MIN(id) AS media_id
              FROM sku_media
              GROUP BY sku_id
            ) first_media ON first_media.sku_id = o.sku_id
            LEFT JOIN sku_media sm ON sm.id = first_media.media_id
            WHERE ce.beneficiary_user_id = #{userId}
            <if test="status != null and status != '' and status != 'ALL'">
              AND ce.status = #{status}
            </if>
            ORDER BY ce.created_at DESC, ce.id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<CommissionResponse> findCommissionPage(@Param("userId") Long userId,
                                                @Param("status") String status,
                                                @Param("offset") long offset,
                                                @Param("pageSize") long pageSize);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM commission_entries ce
            WHERE ce.beneficiary_user_id = #{userId}
            <if test="status != null and status != '' and status != 'ALL'">
              AND ce.status = #{status}
            </if>
            </script>
            """)
    long countCommissionPage(@Param("userId") Long userId, @Param("status") String status);
}
