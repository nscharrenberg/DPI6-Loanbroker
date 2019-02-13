package applications.loanbroker;

import mix.messaging.MessageQueue;
import mix.messaging.requestreply.RequestReply;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import sun.misc.Request;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.*;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;


public class LoanBrokerFrame extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;
	private MessageQueue messageQueue = MessageQueue.getInstance();

	private List<RequestReply<BankInterestRequest, LoanReply>> requestReplies = new ArrayList<>();
	private Map<String, String> loanInterestWithMessageIds = new HashMap<>();
	private Map<String, LoanRequest> loanRequestWithMessageIds = new HashMap<>();
	private Map<String, BankInterestRequest> bankInterestRequestWithMessageIds = new HashMap<>();

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
		/**
		 * Process
		 * ======================================================================================
		 * 1. Client should request a loan, it sends the LoanRequest object to the Loan Broker.
		 * 2. The Loan Broker consumes the LoanRequest object from the client. 					 <===
		 * 3. The Loan Broker converts the LoanRequest into a BankInterestRequest.				 <===
		 * 4. The Loan Broker sends the BankInterestRequest to the Bank.						 <===
		 * 5. The Bank consumes the BankInterestRequest from the Loan Broker.
		 * 6. An person sets an interest rate and converts it into a BankInterestReply.
		 * 7. The Bank sends the BankInterestReply back to the Loan Broker.
		 * 8. The Loan Broker consumes the BankInterestReply from the Bank.						 <===
		 * 9. The Loan Broker converts the BankInterestReply into a LoanReply.					 <===
		 * 10. The Loan Broker sends the LoanReply back to the Client.							 <===
		 * 11. The Client consumes the LoanReply.
		 * ======================================================================================
		 */
		messageQueue.consume(MessageQueue.loanRequest, new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				LoanRequest request = null;
				String messageId = null;

				try {
					request = (LoanRequest)((ObjectMessage) msg).getObject();
					messageId = msg.getJMSMessageID();
					loanRequestWithMessageIds.put(messageId, request);
				} catch (JMSException e) {
					e.printStackTrace();
				}

				BankInterestRequest bankInterestRequest = new BankInterestRequest(request.getAmount(), request.getTime());

				RequestReply<BankInterestRequest, LoanReply> requestReply = new RequestReply<>(bankInterestRequest, null);
				requestReplies.add(requestReply);

				add(request);
				add(request, bankInterestRequest);

				String interestMessageId = messageQueue.produce(bankInterestRequest, messageQueue.getBankInterestRequestDestination(), null);

				if(interestMessageId != null) {
					bankInterestRequestWithMessageIds.put(interestMessageId, bankInterestRequest);
				}

				loanInterestWithMessageIds.put(interestMessageId, messageId);
			}
		});

		messageQueue.consume(MessageQueue.bankInterestReply, new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				BankInterestReply bankInterestReply = null;
				LoanRequest loanRequest = null;
				String correlationId = null;

				System.out.println("Received: " + msg);

				try {
					bankInterestReply = (BankInterestReply) ((ObjectMessage) msg).getObject();
					correlationId = msg.getJMSCorrelationID();
					add(loanRequest, bankInterestReply);

					String loanMessageId = loanInterestWithMessageIds.get(correlationId);
					BankInterestRequest bankInterestRequest = bankInterestRequestWithMessageIds.get(correlationId);
					LoanReply loanReply = new LoanReply(bankInterestReply.getInterest(), bankInterestReply.getQuoteId());

					RequestReply<BankInterestRequest, LoanReply> requestReply = requestReplies.stream().filter(rr -> rr.getRequest().equals(bankInterestRequest)).findFirst().get();
					requestReply.setReply(loanReply);
					messageQueue.produce(loanReply, messageQueue.getLoanReplyDestination(), loanMessageId);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});

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
			JListLine rr = listModel.get(i);

			if(rr.getLoanRequest().getSsn() == request.getSsn() && rr.getLoanRequest().getTime() == request.getTime() && rr.getLoanRequest().getAmount() == request.getAmount()) {
				return rr;
			}

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
			rr.setBankReply(bankReply);;
			list.repaint();
		}
	}
}
