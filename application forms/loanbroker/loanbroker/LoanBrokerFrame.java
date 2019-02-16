package loanbroker.loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import gateways.application.LoanBrokerApplicationGateway;
import javafx.application.Platform;
import messaging.requestreply.RequestReply;
import model.bank.*;
import model.loan.LoanReply;
import model.loan.LoanRequest;


public class LoanBrokerFrame extends JFrame implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;

	private LoanBrokerApplicationGateway loanBrokerApplicationGateway;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanBrokerFrame frame = new LoanBrokerFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LoanBrokerFrame() {
		this.loanBrokerApplicationGateway = new LoanBrokerApplicationGateway();

		this.loanBrokerApplicationGateway.addObserver(this);

		setTitle("Loan Broker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
		gbl_contentPane.rowHeights = new int[]{233, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 7;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		list = new JList<JListLine>(listModel);
		scrollPane.setViewportView(list);
	}
	
	 private JListLine getRequestReply(LoanRequest request){    
	     
	     for (int i = 0; i < listModel.getSize(); i++){
	    	 JListLine rr =listModel.get(i);
	    	 if (rr.getLoanRequest() == request){
	    		 return rr;
	    	 }
	     }
	     
	     return null;
	   }
	
	public void add(LoanRequest loanRequest){		
		listModel.addElement(new JListLine(loanRequest));		
	}
	

	public void add(LoanRequest loanRequest,BankInterestRequest bankRequest){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankRequest != null){
			rr.setBankRequest(bankRequest);
            list.repaint();
		}		
	}
	
	public void add(LoanRequest loanRequest, BankInterestReply bankReply){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankReply != null){
			rr.setBankReply(bankReply);
            list.repaint();
		}		
	}


	/**
	 * This method is called whenever the observed object is changed. An
	 * application calls an <tt>Observable</tt> object's
	 * <code>notifyObservers</code> method to have all the object's
	 * observers notified of the change.
	 *
	 * @param o   the observable object.
	 * @param arg an argument passed to the <code>notifyObservers</code>
	 */
	@Override
	public void update(Observable o, Object arg) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(arg != null) {
					String messageId = arg.toString();
					LoanRequest loanRequest = null;
					BankInterestReply bankInterestReply = null;
					BankInterestRequest bankInterestRequest = null;
					String tmpMessageId = null;

					try {
						tmpMessageId = loanBrokerApplicationGateway.getRequestsWithMessageIds().get(messageId);

						if(tmpMessageId != null) {
							loanRequest = loanBrokerApplicationGateway.getLoanRequestWithMessageIds().get(loanBrokerApplicationGateway.getRequestsWithMessageIds().get(messageId));
						} else {
							loanRequest = loanBrokerApplicationGateway.getLoanRequestWithMessageIds().get(messageId);
						}


						bankInterestReply = loanBrokerApplicationGateway.getBankInterestReplyWithMessageIds().get(messageId);
						bankInterestRequest = loanBrokerApplicationGateway.getBankInterestRequestWithMessageIds().get(messageId);

						System.out.println("loanRequest: " + loanRequest);
						System.out.println("bankInterestReply: " + bankInterestReply);
						System.out.println("bankInterestRequest: " + bankInterestRequest);

						if(loanRequest == null) {
							throw new Exception("LoanRequest can not be null on update!");
						}



						if(bankInterestReply != null) {
							add(loanRequest, bankInterestReply);
						} else if(bankInterestRequest != null) {
							add(loanRequest);
							add(loanRequest, bankInterestRequest);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				list.repaint();
			}
		});
	}
}
