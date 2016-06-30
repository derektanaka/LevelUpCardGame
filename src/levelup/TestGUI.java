package levelup;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class TestGUI extends JFrame implements KeyListener, MouseListener, ActionListener{

	
	private JLayeredPane jlp;
	private ArrayList<JToggleButton> cardList;
	private final int width = 12;
	private Thread fromGUIthread;
	private ClientModel2 cm2;
	private boolean running = true;
	
	public TestGUI(ClientModel2 cm2){
		this.cm2 = cm2;
		cardList = new ArrayList<JToggleButton>();
		JFrame frame = new JFrame();
		jlp = new JLayeredPane();
		JPanel panel = new JPanel();
		//frame.getContentPane().add(panel);
		frame.getContentPane().add(jlp);
		
		JPanel cards = new JPanel();
		cards.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.add(cards);
		cards.setBounds(0, 500, 700, 200);
		cards.setLayout(null);
		jlp.add(cards);
		//cards.add(jb1);
		panel.setLayout(null);
		
		JButton button1 = new JButton();
		button1.setName("button1");
		button1.setText("Play");
		button1.addActionListener(this);
		jlp.add(button1);
		button1.setBounds(700, 500, 100, 50);
		
		fromGUIthread = new Thread(){
			public void run(){
				while(running){
					String temp = "";
					synchronized(cm2.fromGUI){
						while(cm2.fromGUI.size() < 1){
							try {
								cm2.fromGUI.wait();
							} catch (InterruptedException e) {
							}
						}
						temp = cm2.fromGUI.get(0);
						cm2.fromGUI.remove(0);
					}
					runJob(temp);
				}
			}
		};

		for(int i = 0; i<20; i++){
			final int q = i;
			JToggleButton jb = new JToggleButton();
			jb.setIcon(getIcon(104+i));
			jb.setActionCommand("" + (104+i));
			jb.addActionListener(
					new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent arg0) {
							if(jb.isSelected()){
								jb.setLocation(jb.getLocation().x, 0);
							}
							else{
								jb.setLocation(jb.getLocation().x, 15);
							}
						}
					}
					);
			jb.setSize(71, 96);
			jb.setLocation(i*width, 15);
			jlp.add(jb, new Integer(104+i));
			cardList.add(jb);
		}
	
		frame.setLocation(200, 0);
		//frame.setUndecorated(true); //no borders, fullscreen effect
		//frame.setSize(800, 600);
		frame.setSize(1000, 800);
		frame.addKeyListener(this);
		frame.addMouseListener(this);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand().equals("Play")){
			String s = "";
			do{
				for(int i = 0; i < cardList.size(); i++){
					if(cardList.get(i).isSelected()){
						s += cardList.get(i).getActionCommand();
						jlp.remove(cardList.get(i));
						cardList.remove(i);
						i--;
					}
				}
			}while(cm2.isLegalPlay(s));
			synchronized(cm2.toGUI){
				cm2.toGUI.add(s);
				cm2.toGUI.notifyAll();
			}
		}
		updateUI();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public Icon getIcon(int card){
		String s = "cards/" + getMatchingIcon(card) + ".gif";
		ImageIcon image = new ImageIcon(s);
		return image;
	}
	
	public String getMatchingIcon(int card){
		String name = "";
		switch((card%100)/2){
		case 2: name += "Two"; break;
		case 3: name += "Three"; break;
		case 4: name += "Four"; break;
		case 5: name += "Five"; break;
		case 6: name += "Six"; break;
		case 7: name += "Seven"; break;
		case 8: name += "Eight"; break;
		case 9: name += "Nine"; break;
		case 10: name += "Ten"; break;
		case 11: name += "Jack"; break;
		case 12: name += "Queen"; break;
		case 13: name += "King"; break;
		case 14: name += "Ace"; break;
		}
		name += " of ";
		switch(card/100){
		case 1: name += "Clubs"; break;
		case 2: name += "Diamonds"; break;
		case 3: name += "Spades"; break;
		case 4: name += "Hearts"; break;
		case 5:
			if(card%500 < 2){
				name = "Small Joker";
			}
			else{
				name = "Big Joker";
			}
			break;
		}
		return name;
	}
	
	public void updateUI(){
		int x = (20-cardList.size())*width/2;
		for(int i = 0; i < cardList.size(); i++){
			cardList.get(i).setLocation(x, 15);
			x += width;
		}
		jlp.repaint();
	}
	
	public void runJob(String j){
		
	}
	
	public static void main(String[] args){
		TestGUI tgui = new TestGUI(new ClientModel2(5, 3));	
	}

}

/*center the cards after every play
 *when a card is selected, push it up a couple of pixels
 *when deselected, reset it vertically
 */