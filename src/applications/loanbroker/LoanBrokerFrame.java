package applications.loanbroker;

import mix.messaging.GLOBALS;
import mix.messaging.MessageQueue;
import mix.messaging.requestreply.RequestReply;
import mix.model.bank.BankInterestReply;
import mix.model.bank.BankInterestRequest;
import mix.model.loan.LoanReply;
import mix.model.loan.LoanRequest;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;

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
		new MessageQueue().consume(GLOBALS.loanRequestQueue, new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				if(msg instanceof ObjectMessage) {
					Serializable obj = null;

					try {
						obj = (Serializable)((ObjectMessage) msg).getObject();
					} catch (JMSException e) {
						e.printStackTrace();
					}

					if(obj instanceof LoanRequest) {
						LoanRequest request = (LoanRequest) obj;

						System.out.println("BROKER: Request: " + request);

						BankInterestRequest bankInterestRequest = new BankInterestRequest(request.getAmount(), request.getTime());
						bankInterestRequest.setLoanRequest(request);

						add(request);
						add(request, bankInterestRequest);

						System.out.println("BROKER: Send BankInterestRequest");
						new MessageQueue().produce(bankInterestRequest);
					}
				}
			}
		});

		new MessageQueue().consume(GLOBALS.bankInterestReplyQueue, new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				if(msg instanceof ObjectMessage) {
					Serializable obj = null;

					try {
						obj = (Serializable)((ObjectMessage) msg).getObject();
					} catch (JMSException e) {
						e.printStackTrace();
					}

					if(obj instanceof BankInterestReply) {
						BankInterestReply reply = (BankInterestReply) obj;
						LoanRequest request = (LoanRequest) reply.getLoanRequest();

						System.out.println("BROKER: Reply: " + reply);
						System.out.println("BROKER: Request: " + request);

						add(request, reply);

						LoanReply loanReply = new LoanReply(reply.getInterest(), reply.getQuoteId());
						loanReply.setLoanRequest(request);

						System.out.println("BROKER: Send LoanReply");
						new MessageQueue().produce(loanReply);
					}
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
