package com.xingqiu.server.distribution.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingqiu.server.distribution.domain.InviteRelation;
import com.xingqiu.server.distribution.dto.DistributionTeamResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DistributionMapper extends BaseMapper<InviteRelation> {

    /**
     * Count direct invitees (level 1) for a given inviter.
     */
    @Select("SELECT COUNT(*) FROM invite_relations WHERE inviter_user_id = #{userId} AND level = 1")
    int countLevel1(@Param("userId") Long userId);

    /**
     * Count indirect invitees (level 2) for a given inviter.
     */
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
}
