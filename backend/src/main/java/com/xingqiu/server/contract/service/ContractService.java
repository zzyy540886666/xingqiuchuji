package com.xingqiu.server.contract.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingqiu.server.common.exception.BizException;
import com.xingqiu.server.common.exception.ErrorCode;
import com.xingqiu.server.contract.adapter.CosAdapter;
import com.xingqiu.server.contract.adapter.PdfGenerator;
import com.xingqiu.server.contract.domain.Contract;
import com.xingqiu.server.contract.domain.Contract.SignStatus;
import com.xingqiu.server.contract.mapper.ContractMapper;
import com.xingqiu.server.order.domain.Order;
import com.xingqiu.server.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractService.class);

    private static final long DOWNLOAD_URL_EXPIRY_SECONDS = 3600; // 1 hour

    private final ContractMapper contractMapper;
    private final PdfGenerator pdfGenerator;
    private final CosAdapter cosAdapter;
    private final OrderService orderService;

    public ContractService(ContractMapper contractMapper,
                           PdfGenerator pdfGenerator,
                           CosAdapter cosAdapter,
                           OrderService orderService) {
        this.contractMapper = contractMapper;
        this.pdfGenerator = pdfGenerator;
        this.cosAdapter = cosAdapter;
        this.orderService = orderService;
    }

    /**
     * Generate a contract for the given order asynchronously.
     * Idempotent: if a contract already exists for this order, returns it.
     */
    @Async
    @Transactional
    public Contract generateContract(Long orderId) {
        // 1. Check if contract already exists (idempotent)
        Contract existing = findByOrderId(orderId);
        if (existing != null) {
            log.info("Contract already exists for order {}, returning existing", orderId);
            return existing;
        }

        // 2. Get order
        Order order = orderService.getOrderEntity(orderId);

        // 3. Generate PDF
        byte[] pdfBytes = pdfGenerator.generate(order);
        String pdfHash = sha256(pdfBytes);

        // 4. Upload to COS
        String cosKey = "contracts/" + order.getOrderNo() + ".pdf";
        cosAdapter.upload(cosKey, pdfBytes);

        // 5. Save contract record
        Contract contract = new Contract();
        contract.setOrderId(orderId);
        contract.setTemplateVersion("v1");
        contract.setCosKey(cosKey);
        contract.setPdfHash(pdfHash);
        contract.setSignStatus(SignStatus.PENDING);
        contract.setCreatedAt(LocalDateTime.now());

        contractMapper.insert(contract);

        log.info("Contract generated for order {}: cosKey={}, hash={}", orderId, cosKey, pdfHash);
        return contract;
    }

    /**
     * Get a presigned download URL for the order's contract.
     * Returns a map with "url" and "expiresAt" (epoch seconds).
     */
    public Map<String, Object> getDownloadUrl(Long orderId, Long currentUserId) {
        Order order = orderService.getOrderEntity(orderId);
        if (!order.getUserId().equals(currentUserId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "No permission to download this contract");
        }

        Contract contract = findByOrderId(orderId);
        if (contract == null) {
            throw new BizException(ErrorCode.CONTRACT_NOT_FOUND);
        }

        String url = cosAdapter.generatePresignedUrl(contract.getCosKey(), DOWNLOAD_URL_EXPIRY_SECONDS);
        long expiresAt = System.currentTimeMillis() / 1000 + DOWNLOAD_URL_EXPIRY_SECONDS;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("url", url);
        result.put("expiresAt", expiresAt);
        return result;
    }

    private Contract findByOrderId(Long orderId) {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contract::getOrderId, orderId);
        return contractMapper.selectOne(wrapper);
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
