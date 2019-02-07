package applications.loanbroker;

import mix.messaging.GLOBALS;
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

	/**
	 * JMS Variables
	 */
	private Connection connection = null;
	private Session session = null;

	private Destination sendDestination = null;

	private MessageProducer producer = null;
	private MessageConsumer consumer = null;

	// Queues
	private Queue loanRequestQueue = null;
	private Queue loanReplyQueue = null;
	private Queue bankInterestReplyQueue = null;
	private Queue bankInterestRequestQueue = null;

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
		consume(GLOBALS.loanRequestQueue, new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				if(msg instanceof ObjectMessage) {
					Serializable obj = null;

					try {
						obj = (Serializable)((ObjectMessage) msg).getObject();

						if(obj instanceof LoanRequest) {
							LoanRequest request = (LoanRequest) obj;

							System.out.println("BROKER: Request: " + request);

							BankInterestRequest bankInterestRequest = new BankInterestRequest(request.getAmount(), request.getTime());
							bankInterestRequest.setLoanRequest(request);

							add(request);
							add(request, bankInterestRequest);

							System.out.println("BROKER: Send BankInterestRequest");
							produce(bankInterestRequest);
						}
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			}
		});

		consume(GLOBALS.bankInterestReplyQueue, new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				if(msg instanceof ObjectMessage) {
					Serializable obj = null;

					try {
						obj = (Serializable)((ObjectMessage) msg).getObject();

						if(obj instanceof BankInterestReply) {
							BankInterestReply reply = (BankInterestReply) obj;
							LoanRequest request = (LoanRequest) reply.getLoanRequest();

							System.out.println("BROKER: Reply: " + reply);
							System.out.println("BROKER: Request: " + request);

							add(request, reply);

							LoanReply loanReply = new LoanReply(reply.getInterest(), reply.getQuoteId());
							loanReply.setLoanRequest(request);

							System.out.println("BROKER: Send LoanReply");
							produce(loanReply);
						}
					} catch (JMSException e) {
						e.printStackTrace();
					}
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
			rr.setBankReply(bankReply);;
			list.repaint();
		}
	}

	/**
	 * This method produces a JMS message and sends it to the Queue.
	 * @param obj
	 */
	private void produce(Serializable obj) {
		try {
			if(connection == null) {
				openJMSConnection();
			}

			if(obj instanceof LoanRequest) {
				System.out.println("BROKER: Inside LoanRequest");
				producer = session.createProducer(loanRequestQueue);
			} else if (obj instanceof LoanReply) {
				System.out.println("BROKER: Inside LoanReply");
				producer = session.createProducer(loanReplyQueue);
			} else if (obj instanceof BankInterestRequest) {
				System.out.println("BROKER: Inside BankInterestRequest");
				producer = session.createProducer(bankInterestRequestQueue);
			} else if (obj instanceof  BankInterestReply) {
				System.out.println("BROKER: Inside BankInterestReply");
				producer = session.createProducer(bankInterestReplyQueue);
			}

			System.out.println("BROKER: Producer is Set!");
			ObjectMessage msg = session.createObjectMessage(obj);
			producer.send(msg);
			System.out.println("BROKER: Message Send!");
		} catch (JMSException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method consumes a JMS message and handles it's request.
	 *
	 * @param queue
	 * @param listener
	 */
	private void consume(String queue, MessageListener listener) {
		try {
			if(connection == null) {
				openJMSConnection();
			}

			System.out.println("Queue: " + queue);
			if(queue.equals(GLOBALS.loanRequestQueue)) {
				System.out.println("BROKER: Inside LoanRequest");
				consumer = session.createConsumer(loanRequestQueue);
			} else if(queue.equals(GLOBALS.loanReplyQueue)) {
				System.out.println("BROKER: Inside LoanReply");
				consumer = session.createConsumer(loanReplyQueue);
			} else if (queue.equals(GLOBALS.bankInterestRequestQueue)) {
				System.out.println("BROKER: Inside BankInterestRequest");
				consumer = session.createConsumer(bankInterestRequestQueue);
			} else if (queue.equals(GLOBALS.bankInterestReplyQueue)) {
				System.out.println("BROKER: Inside BankInterestReply");
				consumer = session.createConsumer(bankInterestReplyQueue);
			}

			System.out.println("BROKER: Consumer Set!");
			consumer.setMessageListener(listener);
			System.out.println("BROKER: Listener Set!");
			connection.start();
			System.out.println("BROKER: Connection started!");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method initiates the JMS connection for the Queue's.
	 * It establishes an ActiveMQ connection and the corresponding Queue's.
	 */
	private void openJMSConnection() {
		try {
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();

			// Trust all serializable classes
			factory.setTrustAllPackages(true);

			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);



			loanRequestQueue = session.createQueue(GLOBALS.loanRequestQueue);
			loanReplyQueue = session.createQueue(GLOBALS.loanReplyQueue);
			bankInterestRequestQueue = session.createQueue(GLOBALS.bankInterestRequestQueue);
			bankInterestReplyQueue = session.createQueue(GLOBALS.bankInterestReplyQueue);

			System.out.println("BROKER: Ready to Connect!");

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}


}
