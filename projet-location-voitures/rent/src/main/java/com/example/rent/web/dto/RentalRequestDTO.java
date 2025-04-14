package com.example.rent.web.dto;

public class RentalRequestDTO {

    private String begin;
    private String end;
    private String personName;

    public RentalRequestDTO() {
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    @Override
    public String toString() {
        return "RentalRequestDTO{" +
                "begin='" + begin + '\'' +
                ", end='" + end + '\'' +
                ", personName='" + personName + '\'' +
                '}';
    }
}