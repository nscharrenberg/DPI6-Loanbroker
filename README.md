# The Loanbroker Application

The loanbroker application is an assignment from the DPI6 course at Fontys University of Applied Sciences.

## Technologies

This application makes use of the Java Messaging Service and displays the use of JMS within this loanbroker system.

## How does it work?

A client requests a loan and sends it to the loan broker as a `LoanRequest`. The Loanbroker receives the `LoanRequest` and converts it to a `BankInterestRequest` and sends this to the Bank.

The bank receives the `BankInterestRequest` and sends back a reply to the loan broker with the interst rate they want for this particular loan. They send this back as a `BankInterestReply` with the messageId from the `BankInterestRequest` so that the loan broker knows what `BankInterestRequest` it came from.

The loan broker receives the `BankInterestReply` and converts it to a `LoanReply` stating the loan and the interest for the client. The client receives the `LoanReply` and can see what the interest will be for the loan.

## Dependency Requirements
This project makes use of the following dependencies:
 - Java EE
 - Java Message Service
 - Apache ActiveMQ
 
Be sure to have these dependencies available.

