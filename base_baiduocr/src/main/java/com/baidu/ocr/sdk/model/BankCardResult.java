package com.baidu.ocr.sdk.model;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class BankCardResult extends ResponseResult {
    private String bankCardNumber;
    private String bankName;
    private BankCardType bankCardType;
    private String validDate;
    private String holderName;

    public BankCardResult() {
    }

    public String getBankName() {
        return this.bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public BankCardType getBankCardType() {
        return this.bankCardType;
    }

    public void setBankCardType(BankCardType bankCardType) {
        this.bankCardType = bankCardType;
    }

    public void setBankCardType(int bankCardTypeId) {
        this.bankCardType = BankCardResult.BankCardType.FromId(bankCardTypeId);
    }

    public String getBankCardNumber() {
        return this.bankCardNumber;
    }

    public void setBankCardNumber(String bankCardNumber) {
        this.bankCardNumber = bankCardNumber;
    }

    public String getValidDate() {
        return this.validDate;
    }

    public void setValidDate(String validDate) {
        this.validDate = validDate;
    }

    public String getHolderHame() {
        return this.holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public static enum BankCardType {
        Unknown(0),
        Debit(1),
        Credit(2);

        private final int id;

        private BankCardType(int id) {
            this.id = id;
        }

        public static BankCardType FromId(int id) {
            switch (id) {
                case 1:
                    return Debit;
                case 2:
                    return Credit;
                default:
                    return Unknown;
            }
        }
    }
}
