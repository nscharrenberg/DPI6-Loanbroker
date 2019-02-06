package applications.loanclient;
import mix.messaging.requestreply.RequestReply;
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

public class LoanClientFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField tfSSN;
	private DefaultListModel<RequestReply<LoanRequest,LoanReply>> listModel = new DefaultListModel<RequestReply<LoanRequest,LoanReply>>();
	private JList<RequestReply<LoanRequest,LoanReply>> requestReplyList;

	private JTextField tfAmount;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JTextField tfTime;

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

	/**
	 * Create the frame.
	 */
	public LoanClientFrame() {
		setTitle("Loan Client");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 684, 619);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] {0, 0, 30, 30, 30, 30, 0};
		gbl_contentPane.rowHeights = new int[] {30,  30, 30, 30, 30};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblBody = new JLabel("ssn");
		GridBagConstraints gbc_lblBody = new GridBagConstraints();
		gbc_lblBody.insets = new Insets(0, 0, 5, 5);
		gbc_lblBody.gridx = 0;
		gbc_lblBody.gridy = 0;
		contentPane.add(lblBody, gbc_lblBody);
		
		tfSSN = new JTextField();
		GridBagConstraints gbc_tfSSN = new GridBagConstraints();
		gbc_tfSSN.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfSSN.insets = new Insets(0, 0, 5, 5);
		gbc_tfSSN.gridx = 1;
		gbc_tfSSN.gridy = 0;
		contentPane.add(tfSSN, gbc_tfSSN);
		tfSSN.setColumns(10);
		
		lblNewLabel = new JLabel("amount");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		tfAmount = new JTextField();
		GridBagConstraints gbc_tfAmount = new GridBagConstraints();
		gbc_tfAmount.anchor = GridBagConstraints.NORTH;
		gbc_tfAmount.insets = new Insets(0, 0, 5, 5);
		gbc_tfAmount.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfAmount.gridx = 1;
		gbc_tfAmount.gridy = 1;
		contentPane.add(tfAmount, gbc_tfAmount);
		tfAmount.setColumns(10);
		
		lblNewLabel_1 = new JLabel("time");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		contentPane.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		tfTime = new JTextField();
		GridBagConstraints gbc_tfTime = new GridBagConstraints();
		gbc_tfTime.insets = new Insets(0, 0, 5, 5);
		gbc_tfTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfTime.gridx = 1;
		gbc_tfTime.gridy = 2;
		contentPane.add(tfTime, gbc_tfTime);
		tfTime.setColumns(10);
		
		JButton btnQueue = new JButton("send loan request");
		btnQueue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int ssn = Integer.parseInt(tfSSN.getText());
				int amount = Integer.parseInt(tfAmount.getText());
				int time = Integer.parseInt(tfTime.getText());				
				
				LoanRequest request = new LoanRequest(ssn,amount,time);
				listModel.addElement( new RequestReply<LoanRequest,LoanReply>(request, null));

				// todo:  send the JMS with request to Loan Broker
				produce(request);
			}
		});
		GridBagConstraints gbc_btnQueue = new GridBagConstraints();
		gbc_btnQueue.insets = new Insets(0, 0, 5, 5);
		gbc_btnQueue.gridx = 2;
		gbc_btnQueue.gridy = 2;
		contentPane.add(btnQueue, gbc_btnQueue);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 7;
		gbc_scrollPane.gridwidth = 6;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		requestReplyList = new JList<RequestReply<LoanRequest,LoanReply>>(listModel);
		scrollPane.setViewportView(requestReplyList);

		/**
		 * Proces
		 * ======================================================================================
		 * 1. Client should request a loan, it sends the LoanRequest object to the Loan Broker.
		 * 2. The Loan Broker consumes the LoanRequest object from the client.
		 * 3. The Loan Broker converts the LoanRequest into a BankInterestRequest.
		 * 4. The Loan Broker sends the BankInterestRequest to the Bank.
		 * 5. The Bank consumes the BankInterestRequest from the Loan Broker.
		 * 6. An person sets an interest rate and converts it into a BankInterestReply.
		 * 7. The Bank sends the BankInterestReply back to the Loan Broker.
		 * 8. The Loan Broker consumes the BankInterestReply from the Bank.
		 * 9. The Loan Broker converts the BankInterestReply into a LoanReply.
		 * 10. The Loan Broker sends the LoanReply back to the Client.
		 * 11. The Client consumes the LoanReply.
		 * ======================================================================================
		 */

		consume("loanReplyQueue", new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				if(msg instanceof ObjectMessage) {
					Serializable obj = null;
					System.out.println("Passed onMessage");

					try {
						obj = (Serializable)((ObjectMessage) msg).getObject();
						System.out.println("Passed Object");

						if(obj instanceof LoanReply) {
							LoanReply reply = (LoanReply) obj;
							LoanRequest request = (LoanRequest)reply.getLoanRequest();

							System.out.println("Reply: " + reply);

							RequestReply<LoanRequest, LoanReply> requestReply = getRequestReply(request);
							requestReply.setReply(reply);
							requestReplyList.repaint();
							System.out.println("Repainted RequestReplyList");
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

			if(obj instanceof LoanRequest) {
				System.out.println("Producer set");
				producer = session.createProducer(loanRequestQueue);
			} else if (obj instanceof LoanReply) {
				System.out.println("Producer set");
				producer = session.createProducer(loanReplyQueue);
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

			System.out.println("Connection Opened");

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method returns the RequestReply line that belongs to the request from requestReplyList (JList). 
	 * You can call this method when an reply arrives in order to add this reply to the right request in requestReplyList.
	 * @param request
	 * @return
	 */
   private RequestReply<LoanRequest,LoanReply> getRequestReply(LoanRequest request){
     for (int i = 0; i < listModel.getSize(); i++){
    	 RequestReply<LoanRequest,LoanReply> rr =listModel.get(i);
    	 if (rr.getRequest() == request){
    		 return rr;
    	 }
     }
     
     return null;
   }
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanClientFrame frame = new LoanClientFrame();
					
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
