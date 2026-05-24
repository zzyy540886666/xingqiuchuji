package com.xingqiu.server.common.exception;

public enum ErrorCode {

    // 通用
    BAD_REQUEST("BAD_REQUEST", "请求参数错误"),
    UNAUTHORIZED("UNAUTHORIZED", "未认证或令牌过期"),
    FORBIDDEN("FORBIDDEN", "无权限"),
    NOT_FOUND("NOT_FOUND", "资源不存在"),
    CONFLICT("CONFLICT", "资源冲突"),
    INTERNAL_ERROR("INTERNAL_ERROR", "服务内部错误"),
    RATE_LIMITED("RATE_LIMITED", "请求过于频繁"),
    IDEMPOTENCY_CONFLICT("IDEMPOTENCY_CONFLICT", "重复请求"),

    // Auth — BE-01
    AUTH_INVALID_CODE("AUTH_INVALID_CODE", "微信code无效或过期"),
    AUTH_USER_DISABLED("AUTH_USER_DISABLED", "账号已被冻结"),

    // Catalog — BE-02
    SKU_NOT_FOUND("SKU_NOT_FOUND", "SKU不存在"),
    SKU_OFFLINE("SKU_OFFLINE", "SKU已下架"),

    // Order — BE-03
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "订单不存在"),
    ORDER_PRICE_CHANGED("ORDER_PRICE_CHANGED", "价格已变更，请重新试算"),
    ORDER_NOT_PAYABLE("ORDER_NOT_PAYABLE", "订单当前状态不可支付"),
    ORDER_STATUS_INVALID("ORDER_STATUS_INVALID", "订单状态迁移非法"),

    // Payment — BE-04
    PAY_SIGN_FAILED("PAY_SIGN_FAILED", "微信支付签名失败"),
    PAY_CALLBACK_VERIFY_FAIL("PAY_CALLBACK_VERIFY_FAIL", "支付回调验签失败"),

    // Contract — BE-05
    CONTRACT_NOT_FOUND("CONTRACT_NOT_FOUND", "合同不存在"),
    CONTRACT_GENERATE_FAILED("CONTRACT_GENERATE_FAILED", "合同生成失败"),

    // Member — BE-06
    MEMBERSHIP_NOT_FOUND("MEMBERSHIP_NOT_FOUND", "会员信息不存在"),
    PLANET_CARD_SOLD_OUT("PLANET_CARD_SOLD_OUT", "星球卡已售罄"),

    // Wallet — BE-07
    WALLET_NOT_FOUND("WALLET_NOT_FOUND", "钱包不存在"),
    WITHDRAW_AMOUNT_INVALID("WITHDRAW_AMOUNT_INVALID", "提现金额无效"),
    BALANCE_INSUFFICIENT("BALANCE_INSUFFICIENT", "余额不足"),

    // Asset — BE-08
    ASSET_NOT_FOUND("ASSET_NOT_FOUND", "资产不存在"),
    ASSET_SLOT_CONFLICT("ASSET_SLOT_CONFLICT", "托管时段冲突"),

    // Community — BE-09
    POST_NOT_FOUND("POST_NOT_FOUND", "帖子不存在"),
    POST_AUDIT_REJECTED("POST_AUDIT_REJECTED", "帖子内容审核未通过"),
    POST_RATE_LIMITED("POST_RATE_LIMITED", "发帖过于频繁"),

    // Distribution — BE-10
    DISTRIBUTION_SELF_BIND("DISTRIBUTION_SELF_BIND", "不可绑定自己为邀请人"),
    COMMISSION_NOT_FOUND("COMMISSION_NOT_FOUND", "佣金记录不存在"),

    // Config — BE-11
    CONFIG_NOT_FOUND("CONFIG_NOT_FOUND", "配置项不存在"),

    // Repair — BE-13
    DEVICE_NOT_FOUND("DEVICE_NOT_FOUND", "设备不存在"),
    WORK_ORDER_NOT_FOUND("WORK_ORDER_NOT_FOUND", "工单不存在"),
    INSPECTION_TASK_NOT_FOUND("INSPECTION_TASK_NOT_FOUND", "巡检任务不存在"),
    WORK_ORDER_ACTION_DENIED("WORK_ORDER_ACTION_DENIED", "当前角色不可执行此工单操作"),
    ;

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
