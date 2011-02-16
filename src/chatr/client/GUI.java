package chatr.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener {

	private JTextArea messagesLog;
	private JTextField inputField;

	private final Application app;

	public GUI(final Application app) {
		this.app = app;

		display();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				app.close();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == inputField) {
			app.handleInput(inputField.getText());
			inputField.setText("");
		}
	}
	
	public void addNotice(String msg) {
	  addMessage(String.format("# %s", msg));
	}
	
	public void addError(String msg) {
	  addMessage(String.format("! %s", msg));
	}

	public void addMessage(String msg) {
		messagesLog.append(String.format("(%s) %s\n",
		    new SimpleDateFormat("HH:mm").format(new Date()),
		    msg));
		messagesLog.setCaretPosition(messagesLog.getText().length());
	}
	
	public void blockInput() {
	  inputField.setEnabled(false);
	}

	private void display() {
		setTitle("chatr");
		setSize(300, 450);
		Container content = getContentPane();
		content.setLayout(new BorderLayout());

		messagesLog = new JTextArea(7, 7);
		messagesLog.setEditable(false);
		messagesLog.setLineWrap(true);
		messagesLog.setWrapStyleWord(true);
		content.add(new JScrollPane(messagesLog), "Center");

		inputField = new JTextField(25);
		inputField.addActionListener(this);
		JPanel inputPanel = new JPanel(new FlowLayout());
		inputPanel.add(inputField);
		content.add(inputPanel, "South");

		setVisible(true);
		inputField.requestFocus();
	}

  public void setStatus(String status) {
    if (status != null && !status.isEmpty()) {
      setTitle(String.format("chatr (%s)", status));
    } else {
      setTitle("chatr");
    }
  }

	// private void error(Exception e, String msg) {
	// msg = (msg != null) ? msg + "\n(" + e + ")" : e.toString();
	// JOptionPane.showMessageDialog(this,
	// msg, "Ups, wystąpił błąd...",
	// JOptionPane.ERROR_MESSAGE);
	// }
}
