// Name                 Cameron Reid
// Student ID           S2455533
// Programme of Study   Software Development

package com.example.reid_cameron_s2455533;

public class CurrencyItem {

    private String code; //Currency code - "GPB", "USD"
    private double rate; //Exchange rate
    private String title; //Full title
    private String publishDate; //Date the rate was published

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    @Override
    public String toString() {
        return code + " : " + rate;
    }
}
