package applications.loanbroker;

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
				System.out.println("Producer set");
				producer = session.createProducer(loanRequestQueue);
			} else if (obj instanceof LoanReply) {
				System.out.println("Producer set");
				producer = session.createProducer(loanReplyQueue);
			} else if (obj instanceof BankInterestRequest) {
				producer = session.createProducer(bankInterestRequestQueue);
			} else if (obj instanceof  BankInterestReply) {
				producer = session.createProducer(bankInterestReplyQueue);
			}

			System.out.println("ObjectMessage Created");
			ObjectMessage msg = session.createObjectMessage(obj);
			producer.send(msg);
			System.out.println("ObjectMessage Send");
		} catch (JMSException e) {
			e.printStackTrace();
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

			if(queue.equals("loanRequestQueue")) {
				System.out.println("Consume set");
				consumer = session.createConsumer(loanRequestQueue);
			} else if(queue.equals("loanReplyQueue")) {
				System.out.println("Consume set");
				consumer = session.createConsumer(loanReplyQueue);
			} else if (queue.equals("bankInterestRequestQueue")) {
				System.out.println("Consume set");
				consumer = session.createConsumer(bankInterestRequestQueue);
			} else if (queue.equals("bankInterestReplyQueue")) {
				System.out.println("Consume set");
				consumer = session.createConsumer(bankInterestReplyQueue);
			}

			System.out.println("Get ObjectListener");
			consumer.setMessageListener(listener);
			connection.start();
			System.out.println("Start Connection");
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
			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Trust all serializable classes
			factory.setTrustAllPackages(true);

			loanRequestQueue = session.createQueue("loanRequestQueue");
			loanReplyQueue = session.createQueue("loanReplyQueue");
			bankInterestRequestQueue = session.createQueue("bankInterestRequestQueue");
			bankInterestReplyQueue = session.createQueue("bankInterestReplyQueue");

			System.out.println("Connection Opened");

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}


}
