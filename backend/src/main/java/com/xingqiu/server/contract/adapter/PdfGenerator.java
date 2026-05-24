package com.xingqiu.server.contract.adapter;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.xingqiu.server.order.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

/**
 * PDF 合同生成器（真实实现 — OpenPDF）
 */
@Service
public class PdfGenerator {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerator.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    public byte[] generate(Order order) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", 18, Font.BOLD);
            Font bodyFont = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", 12, Font.NORMAL);
            Font boldFont = FontFactory.getFont("STSong-Light", "UniGB-UCS2-H", 12, Font.BOLD);

            Paragraph title = new Paragraph("星球出机 - 设备租赁合同", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("合同编号：" + order.getOrderNo(), bodyFont));
            document.add(new Paragraph("签订日期：" + (order.getCreatedAt() != null
                    ? order.getCreatedAt().format(DATE_FMT) : "--"), bodyFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("一、租赁设备信息", boldFont));
            document.add(new Paragraph("    订单编号：" + order.getOrderNo(), bodyFont));

            if (order.getRentStartDate() != null && order.getRentEndDate() != null) {
                document.add(new Paragraph("    租期：" + order.getRentStartDate() + " 至 " + order.getRentEndDate(), bodyFont));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("二、费用说明", boldFont));
            BigDecimal payable = BigDecimal.valueOf(order.getPayableMinor() != null ? order.getPayableMinor() : 0)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            document.add(new Paragraph("    应付金额：￥" + payable.toPlainString(), bodyFont));

            if (order.getDepositMinor() != null && order.getDepositMinor() > 0) {
                BigDecimal deposit = BigDecimal.valueOf(order.getDepositMinor())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                document.add(new Paragraph("    押金金额：￥" + deposit.toPlainString() + "（退还条件见合同条款）", bodyFont));
            }

            log.info("PDF generated for order: {}", order.getOrderNo());
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed for order {}: {}", order.getOrderNo(), e.getMessage(), e);
            throw new RuntimeException("合同生成失败", e);
        }
    }
}
