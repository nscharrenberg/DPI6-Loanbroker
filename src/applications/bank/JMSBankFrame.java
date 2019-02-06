package applications.bank;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.jms.*;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class JMSBankFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField tfReply;
	private DefaultListModel<RequestReply<BankInterestRequest, BankInterestReply>> listModel = new DefaultListModel<RequestReply<BankInterestRequest, BankInterestReply>>();

	/**
	 * JMS Variables
	 */
	private Connection connection = null;
	private Session session = null;

	private Destination sendDestination = null;

	private MessageProducer producer = null;
	private MessageConsumer consumer = null;

	// Queues
	private Queue bankInterestReplyQueue = null;
	private Queue bankInterestRequestQueue = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JMSBankFrame frame = new JMSBankFrame();
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
	public JMSBankFrame() {
		setTitle("JMS Bank - ABN AMRO");
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
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		JList<RequestReply<BankInterestRequest, BankInterestReply>> list = new JList<RequestReply<BankInterestRequest, BankInterestReply>>(listModel);
		scrollPane.setViewportView(list);
		
		JLabel lblNewLabel = new JLabel("type reply");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		tfReply = new JTextField();
		GridBagConstraints gbc_tfReply = new GridBagConstraints();
		gbc_tfReply.gridwidth = 2;
		gbc_tfReply.insets = new Insets(0, 0, 0, 5);
		gbc_tfReply.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfReply.gridx = 1;
		gbc_tfReply.gridy = 1;
		contentPane.add(tfReply, gbc_tfReply);
		tfReply.setColumns(10);
		
		JButton btnSendReply = new JButton("send reply");
		btnSendReply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RequestReply<BankInterestRequest, BankInterestReply> rr = list.getSelectedValue();
				double interest = Double.parseDouble((tfReply.getText()));
				BankInterestReply reply = new BankInterestReply(interest,"ABN AMRO");
				if (rr!= null && reply != null){
					rr.setReply(reply);
	                list.repaint();
					// todo: sent JMS message with the reply to Loan Broker
					LoanRequest request = rr.getRequest().getLoanRequest();
					System.out.println("BROKER: Get Request: " + request);
					reply.setLoanRequest(request);
					System.out.println("BROKER: Get Reply: " + reply);

					produce(reply);
					System.out.println("BROKER: Reply has been Send!");
				}
			}
		});
		GridBagConstraints gbc_btnSendReply = new GridBagConstraints();
		gbc_btnSendReply.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnSendReply.gridx = 4;
		gbc_btnSendReply.gridy = 1;
		contentPane.add(btnSendReply, gbc_btnSendReply);

		/**
		 * Process
		 * ======================================================================================
		 * 1. Client should request a loan, it sends the LoanRequest object to the Loan Broker.
		 * 2. The Loan Broker consumes the LoanRequest object from the client.
		 * 3. The Loan Broker converts the LoanRequest into a BankInterestRequest.
		 * 4. The Loan Broker sends the BankInterestRequest to the Bank.
		 * 5. The Bank consumes the BankInterestRequest from the Loan Broker.					 <===
		 * 6. An person sets an interest rate and converts it into a BankInterestReply.			 <===
		 * 7. The Bank sends the BankInterestReply back to the Loan Broker.						 <===
		 * 8. The Loan Broker consumes the BankInterestReply from the Bank.
		 * 9. The Loan Broker converts the BankInterestReply into a LoanReply.
		 * 10. The Loan Broker sends the LoanReply back to the Client.
		 * 11. The Client consumes the LoanReply.
		 * ======================================================================================
		 */
		consume("bankInterestRequestQueue", new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				if(msg instanceof ObjectMessage) {
					Serializable obj = null;

					try {
						obj = (Serializable)((ObjectMessage) msg).getObject();

						if(obj instanceof BankInterestRequest) {
							BankInterestRequest request = (BankInterestRequest) obj;

							System.out.println("BANK: Request: " + request);

							listModel.addElement(new RequestReply<BankInterestRequest, BankInterestReply>(request, null));
						}
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			}
		});
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

			if (obj instanceof  BankInterestReply) {
				producer = session.createProducer(bankInterestReplyQueue);
			}

			System.out.println("BANK: ObjectMessage Created");
			ObjectMessage msg = session.createObjectMessage(obj);
			producer.send(msg);
			System.out.println("BANK: ObjectMessage Send");
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

			if (queue.equals("bankInterestRequestQueue")) {
				System.out.println("BANK: INSIDE BankInterestRequestQueue");
				consumer = session.createConsumer(bankInterestRequestQueue);
				System.out.println("BANK: Consumer has been Set!");
			}

			System.out.println("BANK: After BankInterestRequestQueue");
			consumer.setMessageListener(listener);
			System.out.println("BANK: Listener has been set!");
			connection.start();
			System.out.println("BANK: Connection has been started!");
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

			bankInterestRequestQueue = session.createQueue("bankInterestRequestQueue");
			bankInterestReplyQueue = session.createQueue("bankInterestReplyQueue");

			System.out.println("BANK: Ready to connect!");

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
