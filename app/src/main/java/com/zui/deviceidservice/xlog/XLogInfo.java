package com.zui.deviceidservice.xlog;

import com.zui.xlog.sdk.ParamMap;

public class XLogInfo {
    private String category;
    private String action;
    private String label;
    private int value;
    private ParamMap params;

    public String getCategory() {
        return category;
    }

    public String getAction() {
        return action;
    }

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    public ParamMap getParams() {
        return params;
    }

    private XLogInfo(Builder builder) {
        this.category = builder.category;
        this.action = builder.action;
        this.label = builder.label;
        this.value = builder.value;
        this.params = builder.params;
    }

    public static class Builder {
        private String category;
        private String action;
        private String label;
        private int value;
        private ParamMap params;

        public Builder() {
            params = new ParamMap();
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder value(int value) {
            this.value = value;
            return this;
        }

        public Builder param(int index, String key, String value) {
            params.put(index, key, value);
            return this;
        }

        public XLogInfo build() {
            return new XLogInfo(this);
        }
    }
}
