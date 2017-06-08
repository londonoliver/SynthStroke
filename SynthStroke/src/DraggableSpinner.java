import javax.swing.AbstractSpinnerModel;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Point;

import javax.swing.JSpinner;

import java.awt.GridBagConstraints;

import javax.swing.JButton;
import javax.swing.JSpinner.DefaultEditor;

import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.JLabel;


public class DraggableSpinner extends JPanel implements MouseListener, MouseMotionListener{
	private float multiplier = 0.0f;
	Point mousePoint = new Point();
	boolean vertical;
	JSpinner spinner;
	JLabel button, units;
	JFormattedTextField textField;
	AbstractSpinnerModel model;
	boolean text;
	volatile double lastValue; //used in setEnabled()
	
	
	/**
	 * Create the panel.
	 */	
	public DraggableSpinner(double value, double min, double max, double step, boolean vert, boolean text) {
		vertical = vert;
		this.text = text;
		
		requestFocus();
		setLayout(null);
		
		spinner = new JSpinner();
		spinner.setBounds(0, 0, 71, 16);
		add(spinner);
		String[] list = {"none", "lowpass", "bandpass", "highpass"};
		if(text){
			model = new SpinnerListModel(list);
			
		}
		else
		{
			model = new SpinnerNumberModel(value, min, max, step);
		}
		spinner.setModel(model);
		
		DefaultEditor defaultEditor = (DefaultEditor) spinner.getEditor();
		textField = defaultEditor.getTextField();
		textField.setBorder(javax.swing.BorderFactory.createEmptyBorder());

		
		textField.setFont(new Font("Courier", Font.PLAIN, 13));
		textField.setForeground(Color.white);
		
		
		if(text){
			textField.getCaret().setSelectionVisible(false);
			textField.setHighlighter(null);
			textField.setFocusable(false);
		}
		else
		{
		    textField.addFocusListener(new FocusListener() {
		    	
				public void focusGained(FocusEvent e) {
					JFormattedTextField textField = (JFormattedTextField)e.getSource();
					textField.getCaret().setSelectionVisible(true);
					textField.setHighlighter(new DefaultHighlighter());
				}
				
				public void focusLost(FocusEvent e) {
					JFormattedTextField textField = (JFormattedTextField)e.getSource();
					textField.getCaret().setSelectionVisible(false);
					textField.setHighlighter(null);
					
				}
	        });
		}
		
		spinner.setUI(new BasicSpinnerUI(){
			  protected Component createNextButton(){
			    Component c = new JButton();
			    c.setPreferredSize(new Dimension(0,0));
			    c.setFocusable(false);
			    return c;
			  }
			  protected Component createPreviousButton(){
			    Component c = new JButton();
			    c.setPreferredSize(new Dimension(0,0));
			    c.setFocusable(false);
			    return c;
			  }
			});
		
		
		units = new JLabel();
		units.setFont(new Font("Courier", Font.PLAIN, 13));
		units.setForeground(Color.white);
		units.setBounds(78, 0, 24, 16);
		add(units);
		units.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				JLabel label = (JLabel)e.getSource();
				label.requestFocus();
			}
			
			public void mousePressed(MouseEvent e){
				JLabel label = (JLabel)e.getSource();
				label.requestFocus();
			}
		});
		
		button = new JLabel();
		button.setIcon(new ImageIcon(getClass().getResource("resources/vertical_spinner_white.png")));
		button.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		button.setBounds(110, 0, 8, 16);
		add(button);
		button.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				JLabel label = (JLabel)e.getSource();
				label.requestFocus();
			}
			
			public void mousePressed(MouseEvent e){
				JLabel label = (JLabel)e.getSource();
				label.requestFocus();
			}
		});
		
		button.addMouseListener(this);
		button.addMouseMotionListener(this);
		
		makeTransparent(this);
	}
	
	boolean enabled;

	
	public void mousePressed(MouseEvent e) {
		enabled = false;
		mousePoint.setLocation(e.getPoint());
	}
	
	public void mouseDragged(MouseEvent e) {
	
		int delta = 0;
		if(vertical) {
			delta = mousePoint.y - e.getPoint().y;
		} else {
			delta = -(mousePoint.x - e.getPoint().x);
		}

		
		int absDelta = Math.abs(delta);
		if(!enabled && absDelta < 5) {
			return;
		}
		
		if(!enabled) {
			enabled = true;
			mousePoint.setLocation(e.getPoint());
			return;
		}

		if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
			absDelta = absDelta * 10;
		}
		
		if(multiplier > 0) {
			absDelta = (int) (absDelta * multiplier);
		}
		
		if(absDelta == 0) {
			return;
		}
		
		SpinnerModel model = spinner.getModel();
		Object value = model.getValue();
		for (int i = 0; i < absDelta; i++) {
			if(delta < 0) {
				value = model.getPreviousValue();
			} else {
				value = model.getNextValue();
			}
			
			if(value != null) {
				model.setValue(value);
			} else {
				break;
			}
		}
		
		mousePoint.setLocation(e.getPoint());
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	public JSpinner getSpinner()
	{
		return spinner;
	}
	
	public double getValue()
	{
		double d = (double)(Double)spinner.getValue();
		DecimalFormat df = new DecimalFormat("#.#");
		if( ( (d == 0) || (d * Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY) ){
			df.setRoundingMode(RoundingMode.CEILING);
		}else if( (d * Double.POSITIVE_INFINITY) == Double.NEGATIVE_INFINITY){
			df.setRoundingMode(RoundingMode.FLOOR);
		}else{
			System.out.println("Something wrong in draggablespinner.getValue: d * inf = " + (d * Double.POSITIVE_INFINITY));
		}
		return (double)Double.parseDouble(df.format(d));
	}
	
	public void setValue(double value)
	{
		spinner.setValue(value);
	}

	public void setMax(double max)
	{
		if(!text){
			((SpinnerNumberModel) model).setMaximum(max);
		}
	}
	
	public void setEnabled(boolean enabled)
	{
		spinner.setEnabled(enabled);
		if(!enabled)
		{
			lastValue = getValue();
			spinner.setValue(0.0);
			textField.setColumns(0);
			lastValue = getValue();
			textField.setValue(-1.0);
			button.setVisible(false);
		}
		else
		{
			spinner.setValue(lastValue);
			textField.setColumns(5);
			setValue(lastValue);
			button.setVisible(true);
		}
	}
	
	public static void makeTransparent(Component c) {
	    if(c instanceof Container) {
	        for(Component child : ((Container) c).getComponents())
	            makeTransparent(child);
	    }
	    if(c instanceof JComponent) {
	        JComponent c2 = (JComponent) c;
	        c2.setOpaque(false);
	        c2.setBackground(new Color(0,0,0,0));
	    }
	}
	
	public Dimension getPreferredSize(){
		return new Dimension(111,16);
	}
	
	
	public void setUnits(String unit)
	{
		units.setText(unit);
	}
	
	
	
	public static void main(String[] args){
		DraggableSpinner spinner = new DraggableSpinner(0.0, -10.0, 10000.0, .1, true, false);
		JFrame frame = new JFrame();
		frame.getContentPane().add(spinner, BorderLayout.NORTH);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
