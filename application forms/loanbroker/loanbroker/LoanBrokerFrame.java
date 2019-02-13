package loanbroker.loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.*;

import javax.jms.*;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import messaging.MessageQueue;
import messaging.QueueNames;
import messaging.requestreply.RequestReply;
import model.bank.*;
import model.loan.LoanReply;
import model.loan.LoanRequest;


public class LoanBrokerFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;

	private MessageQueue messageQueue = new MessageQueue();

	// Mapping the message id's of BankInterestRequest with LoanRequest
	private Map<String, String> requestsWithMessageIds = new HashMap<>();
	private Map<String, LoanRequest> loanRequestWithMessageIds = new HashMap<>();
	private Map<String, BankInterestRequest> bankInterestRequestWithMessageIds = new HashMap<>();
	private List<RequestReply<BankInterestRequest, LoanReply>> requestReplies = new ArrayList<>();
	
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

		consumeLoanRequest();
		consumeBankInterestReply();
	}

	private void consumeLoanRequest() {
		Destination loanRequestDestination = messageQueue.createDestination(QueueNames.loanRequest);
		messageQueue.consume(loanRequestDestination, new MessageListener() {
			@Override
			public void onMessage(Message message) {
				LoanRequest loanRequest = null;
				String correlationId = null;

				try {
					loanRequest = (LoanRequest)((ObjectMessage) message).getObject();
					correlationId = message.getJMSMessageID();
					loanRequestWithMessageIds.put(correlationId, loanRequest);

					BankInterestRequest bankInterestRequest = new BankInterestRequest(loanRequest.getAmount(), loanRequest.getTime());
					RequestReply<BankInterestRequest, LoanReply> requestReply = new RequestReply<>(bankInterestRequest, null);

					requestReplies.add(requestReply);
					add(loanRequest);
					add(loanRequest, bankInterestRequest);

					Destination bankInterestRequestDestination = messageQueue.createDestination(QueueNames.bankInterestRequest);
					String messageId = messageQueue.produce(bankInterestRequest, bankInterestRequestDestination, null);

					bankInterestRequestWithMessageIds.put(messageId, bankInterestRequest);
					requestsWithMessageIds.put(messageId, correlationId);
				} catch (JMSException e) {
					e.printStackTrace();
				}

			}
		});
	}

	private void consumeBankInterestReply() {
		Destination bankInterestReplyDestination = messageQueue.createDestination(QueueNames.bankInterestReply);
		messageQueue.consume(bankInterestReplyDestination, new MessageListener() {
			@Override
			public void onMessage(Message message) {
				BankInterestReply bankInterestReply = null;
				String correlationId = null;

				try {
					bankInterestReply = (BankInterestReply) ((ObjectMessage) message).getObject();
					correlationId = message.getJMSCorrelationID();

					String messageId = requestsWithMessageIds.get(correlationId);
					BankInterestRequest bankInterestRequest = bankInterestRequestWithMessageIds.get(correlationId);
					LoanReply loanReply = new LoanReply(bankInterestReply.getInterest(), bankInterestReply.getQuoteId());

					RequestReply<BankInterestRequest, LoanReply> requestReply = requestReplies.stream().filter(o -> o.getRequest().equals(bankInterestRequest)).findFirst().get();
					requestReply.setReply(loanReply);
					LoanRequest loanRequest = loanRequestWithMessageIds.get(messageId);

					add(loanRequest, bankInterestReply);

					Destination loanReplyDestination = messageQueue.createDestination(QueueNames.loanReply);
					messageQueue.produce(loanReply, loanReplyDestination, messageId);
				} catch (JMSException e) {
					e.printStackTrace();
				}

			}
		});
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


}
