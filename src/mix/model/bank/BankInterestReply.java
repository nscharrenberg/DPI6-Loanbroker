package mix.model.bank;

import mix.model.loan.LoanRequest;

import java.io.Serializable;

/**
 * This class stores information about the bank reply
 *  to a loan request of the specific client
 * 
 */
public class BankInterestReply implements Serializable {

    private double interest; // the loan interest
    private String bankId; // the nunique quote Id
    private LoanRequest loanRequest;
    
    public BankInterestReply() {
        this.interest = 0;
        this.bankId = "";
        this.loanRequest = null;
    }
    
    public BankInterestReply(double interest, String quoteId) {
        this.interest = interest;
        this.bankId = quoteId;
        this.loanRequest = null;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getQuoteId() {
        return bankId;
    }

    public void setQuoteId(String quoteId) {
        this.bankId = quoteId;
    }

    public String toString() {
        return "quote=" + this.bankId + " interest=" + this.interest;
    }

    public LoanRequest getLoanRequest() {
        return loanRequest;
    }

    public void setLoanRequest(LoanRequest loanRequest) {
        this.loanRequest = loanRequest;
    }
}
