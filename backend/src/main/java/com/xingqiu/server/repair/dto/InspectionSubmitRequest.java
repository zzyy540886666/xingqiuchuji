package com.xingqiu.server.repair.dto;

import java.util.List;

public class InspectionSubmitRequest {

    private List<RecordItem> records;

    public List<RecordItem> getRecords() { return records; }
    public void setRecords(List<RecordItem> records) { this.records = records; }

    public static class RecordItem {
        private String itemName;
        private String result;
        private String issueDescription;
        private String images;

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }

        public String getIssueDescription() { return issueDescription; }
        public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

        public String getImages() { return images; }
        public void setImages(String images) { this.images = images; }
    }
}
