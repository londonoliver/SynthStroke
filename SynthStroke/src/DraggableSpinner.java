import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;

public class DraggableSpinner extends JSpinner implements MouseListener, MouseMotionListener {
	private float multiplier = 0.0f;
	Point mousePoint = new Point();
	boolean vertical = false;

	public DraggableSpinner(double value, double min, double max, double step, boolean vertical) {
		vertical = this.vertical;
		SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
		this.setModel(model);
		DefaultEditor defaultEditor = (DefaultEditor) getEditor();
		JFormattedTextField textField = defaultEditor.getTextField();

		textField.setEditable(false);
		textField.getCaret().setSelectionVisible(false);
		textField.setHighlighter(null);
		textField.setColumns(5);
		
		this.setUI(new javax.swing.plaf.basic.BasicSpinnerUI(){
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
	  
		
		if(vertical)
		{
			textField.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		}
		else
		{
			textField.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		}
		textField.addMouseListener(this);
		textField.addMouseMotionListener(this);

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
			
			SpinnerModel model = getModel();
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
	
	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		DraggableSpinner spinner= new DraggableSpinner(0.0, -10.0, 10.0, .1, true);
		
		//Install the draggable JSpinner
		
		frame.add(spinner, BorderLayout.NORTH);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

	
	
	

