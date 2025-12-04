package io.github.zaina01.request;

public class Params {
    private String op;
    private String value;
    private String qw;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getQw() {
        return qw;
    }

    public void setQw(String qw) {
        this.qw = qw;
    }

    @Override
    public String toString() {
        return "Params{" +
                "op='" + op + '\'' +
                ", value='" + value + '\'' +
                ", qw='" + qw + '\'' +
                '}';
    }
}
