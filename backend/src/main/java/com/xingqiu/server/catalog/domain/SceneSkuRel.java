package com.xingqiu.server.catalog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("scene_sku_rel")
public class SceneSkuRel {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sceneId;

    private Long skuId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSceneId() { return sceneId; }
    public void setSceneId(Long sceneId) { this.sceneId = sceneId; }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
}
