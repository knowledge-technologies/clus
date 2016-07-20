/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

/*
 * Copyright (c) 1996 by Jan Andersson, Torpa Konsult AB.
 *
 * Permission to use, copy, and distribute this software for
 * NON-COMMERCIAL purposes and without fee is hereby granted
 * provided that this copyright notice appears in all copies.
 *
 */

package jeans.graph;

import java.awt.image.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

import jeans.graph.image.GrayScaleFilter;

/**
 * A class that implements an image and/or string labelled button.
 * No fancy animations are supported. It's just a simple button with
 * image support. <p>
 *
 * It is (supposed to be) compatible with the awt.Button class, regarding
 * generated actions event etc. Image does not have to be preloaded and
 * the button is sized when the image size is known.<p>
 *
 * Note: not using preloaded images may cause problems with the layout,
 * depending on the layout manager used for the parent of the button.
 * When the image size is know the layout() function of the parent is
 * called. You might have to resize and/or reshape the parent when
 * the layout() function is called. See how this is done in the class
 * JanneToolbar.
 *

 * Sub-class of Canvas due to the awt design.
 *
 *
 * @version     1.11 96/08/27
 * @author      Jan Andersson, Torpa Konsult AB. (janne@torpa.se)
 */

public class ImageButton extends Canvas {

	public final static long serialVersionUID = 1;

   /**
    * The image
    */
   protected Image image = null;
   protected Image gray_image = null;

   /**
    * Flag to keep track of if image size (yet) known
    */
   protected boolean imageSizeKnown = false;

   /**
    * The label string (also used in action event for image buttons)
    */
   protected String label;

   /**
    * Button shadow border width
    */
   protected int shadow = 2;

   /**
    * Button border width
    */
   protected int border = 2;

   /**
    * The button state.
    */
   protected boolean selected = false;
   protected boolean enable = true;

   /**
    * Resize image to actual size of button.
    */
   protected boolean resizeImage = true;

   /**
    * Show label as well as image
    */
   protected boolean showLabel = true;

   /**
    * Minimum width
    */
   protected int  minWidth = 10;

   protected Vector listeners = new Vector();

   /**
    * Constructs a Button with a string label and/or an image.
    * @param image the button image
    * @param label the button label (used in action events)
    * @param shadow the button shadow width
    * @param border the button border width
    * @param resizeImage true if image to be resized to actual width
    * of button.
    * @param showLabel if label to be displayed as well as image.
    * @param minWidth  minimum width (pixels). Useful if you want
    *                  to have many buttons with the same width.
    */
   public ImageButton(Image image, Image gray, String label, boolean resizeImage, boolean showLabel) {
      this.image = image;
      this.gray_image = gray;
      this.label = label;
      this.resizeImage = resizeImage;
      this.showLabel = showLabel;
      this.shadow = 2;
      this.border = 2;
      this.minWidth = 10;
      if (image == null) imageSizeKnown = true;	// kind of ;-)
      addMouseListener(new CBMouseListener());
   }

   public ImageButton(Image image, String label, boolean resizeImage, boolean showLabel) {
      	this(image, null, label, resizeImage, showLabel);
   }

   public ImageButton(Image image, Image gray, String label) {
      	this(image, gray, label, false, true);
   }

   public ImageButton(Image image, String label) {
      	this(image, null, label, false, true);
   }

   public ImageButton(Image image, Image gray) {
   	this(image, gray, "", false, false);
   }

   public ImageButton(Image image) {
   	this(image, null, "", false, false);
   }

   public void addActionListener(ActionListener listener) {
	listeners.addElement(listener);
   }

   /**
    * Gets the string label of the button.
    * @see #setLabel
    */
   public String getLabel() {
        return label;
    }

   /**
    * Sets the string label of the button.
    * @param label the label to set the button with
    * @see #getLabel
    */
   public void setLabel(String label) {
      this.label = label;
      layoutParent();
      repaint();
    }

   /**
    * Gets the image of the button.
    * @see #setImage
    */
   public Image getImage() {
      return image;
   }

   /**
    * Sets the image of the button.
    * @param image the image to set the button with
    * @see #getImage
    */
   public void setImage(Image image) {
      this.image = image;
      layoutParent();
      repaint();
   }

   /**
    * Gets the resizeImage flag of the button.
    * @see #setResizeImage
    */
   public boolean getResizeImage() {
      return resizeImage;
   }

   /**
    * Sets the resizeImage flag of the button.
    * @param resizeImage true if image to be resized to actual width
    * of button.
    */
   public void setResizeImage(boolean resizeImage) {
      this.resizeImage = resizeImage;
      layoutParent();
      repaint();
   }

   /**
    * Gets the showLabel flag of the button.
    * @see #setShowLabel
    */
   public boolean getShowLabel() {
      return showLabel;
   }

   /**
    * Sets the showLabel flag of the button.
    * @param showLabel true if label to be displayed as well as image.
    */
   public void setShowLabel(boolean showLabel) {
      this.showLabel = showLabel;
      layoutParent();
      repaint();
   }

   /**
    * Check if image size (yet) known
    */
   public boolean imageSizeKnown() {
      return imageSizeKnown;
   }

   /**
    * Returns the parameter String of this button.
    */
   protected String paramString() {
      return super.paramString() + ",label=" + label;
   }

   /**
    * Repaints the button when the image has changed.
    * Set flag if some bits loaded.
    * @return true if image has changed; false otherwise.
    */
   public boolean imageUpdate(Image img, int flags,
			      int x, int y, int w, int h) {
      boolean ok = (img == image);
      if (ok && (flags & (HEIGHT|WIDTH)) != 0) {
	 // got the size; make sure we (re-) layout parent.
	 imageSizeKnown = true;
	 layoutParent();
      }
      return super.imageUpdate(img, flags, x, y, w, h);
   }

   /**
    * Re-layout parent. Called when a button changes image,
    * size etc.
    */
   protected void layoutParent() {
      Container parent = getParent();
      if (parent != null) {
	 parent.doLayout();
      }
   }

   /**
    * Paints the button.
    * @param g the specified Graphics window
    */
   public void paint(Graphics g) {
      Dimension size = getSize();
      if (isVisible()) {
	 if (enable) {
	    if (selected)
	       paintSelected(g, size);
	    else
	       paintUnSelected(g, size);
	 } else {
	    paintDisabled(g, size);
	 }
      }
   }

   /**
    * Paints the button when selected.
    * @param g the specified Graphics window
    * @param size the button size
    * @see #paint
    */
   protected void paintSelected(Graphics g, Dimension size) {
      Color c = getBackground();
      g.setColor(c);
      ImageUtil.draw3DRect(g, 0, 0, size.width, size.height, shadow, false);
      drawBody(g, size);
   }

   /**
    * Paints the button when not selected.
    * @param g the specified Graphics window
    * @param size the button size
    * @see #paint
    */
   protected void paintUnSelected(Graphics g, Dimension size) {
      Color c = getBackground();
      g.setColor(c);
      g.fillRect(0, 0, size.width, size.height);
      ImageUtil.draw3DRect(g, 0, 0, size.width, size.height, shadow, true);
      drawBody(g, size);
   }

   /**
    * Paints the button when disabled.
    * @param g the specified Graphics window
    * @param size the button size
    * @see #paint
    */
   protected void paintDisabled(Graphics g, Dimension size) {
      Color c = getBackground();
      g.setColor(c);
      g.fillRect(0, 0, size.width, size.height);
      g.setColor(c.darker());
      g.drawRect(0, 0, size.width-1, size.height-1);
      //ImageUtil.draw3DRect(g, 0, 0, size.width, size.height, shadow, true);
      drawBody(g, size);
   }

   /**
    * Draw body of button. I.e image and/or label string
    * @param g the specified Graphics window
    * @param size the button size
    * @see #paint
    */
   protected void drawBody(Graphics g, Dimension size) {
      int selOff = selected ? 1 : 0;
      int labelX = 0;
      int labelY = 0;
      int labelW = 0;
      int labelH = 0;
      FontMetrics fm = null;
      if (image == null || showLabel) {
	 // calculate size and x/y pos. of label string
	 Font f = getFont();
	 fm = getFontMetrics(f);
	 labelH = fm.getAscent() + fm.getDescent();
	 labelW = fm.stringWidth(label)+3;
	 labelX = size.width/2 - labelW/2 + selOff;
	 labelY = size.height/2 - labelH/2 + fm.getAscent() + selOff;
      }
      if (image != null) {
	 // draw image
	 int x, y, w, h;
	 if (resizeImage) {
	    // image resized to actual button size
	    x = shadow + border + selOff;
	    y = shadow + border + selOff;
	    w = size.width - 2*(shadow+border) - selOff;
	    h = size.height - 2*(shadow+border) - selOff;
	    if (showLabel)
	       h -= (labelH + border);
	 } else {
	    // image centered in button
	    Dimension d = new Dimension();
	    d.width = image.getWidth(this);
	    d.height = image.getHeight(this);
	    if (d.width > 0 && d.height > 0)
	       imageSizeKnown = true;
	    w = d.width - selOff;
	    h = d.height - selOff;
	    if (showLabel) {
	       x = border+selOff;
	       y = size.height/2 - d.height/2 + selOff;
	    } else {
	       x = size.width/2 - d.width/2 + selOff;
	       y = size.height/2 - d.height/2 + selOff;
	    }
	 }
         if (enable) {
	       g.drawImage(image, x, y, w, h, this);
	       if (showLabel) {
			g.setColor(getForeground());
			labelX = x+w+border+selOff;
			g.drawString(label, labelX, labelY);
               }
	 } else {
	       if (gray_image == null && image != null) {
	       		gray_image = getGrayScale(image);
	       }
	       g.drawImage(gray_image, x, y, w, h, this);
	       if (showLabel) {
			g.setColor(Color.gray);
			labelX = x+w+border;
			g.drawString(label, labelX, labelY);
               }
         }
      } else {
	 // no image; draw label string
	 if (!enable)
	    g.setColor(Color.gray);
	 else
	    g.setColor(getForeground());
	 g.drawString(label,  labelX, labelY);
      }
   }

   /**
    * Returns the preferred size of this component.
    * @see #minimumSize
    * @see LayoutManager
    */
   public Dimension getPreferredSize() {
      return getMinimumSize();
   }

  /**
    * Returns the minimum size of this component.
    * @see #preferredSize
    * @see LayoutManager
    */
   public synchronized Dimension getMinimumSize() {
      Dimension d = new Dimension();
      Dimension labelDimension = new Dimension();
      if (image == null || showLabel) {
	 // get size of label
	 FontMetrics fm = getFontMetrics(getFont());
	 labelDimension.width =
	    Math.max(fm.stringWidth(label) + 2*(shadow+border),
		     minWidth);
	 labelDimension.height = fm.getAscent() + fm.getDescent() +
	    2*(shadow+border);
	 if (image == null)
	    d = labelDimension;
      }
      if (image != null) {
	 // image used; get image size (If the height is not known
	 // yet then the ImageObserver (this) will be notified later
	 // and -1 will be returned).
	 d.width = image.getWidth(this) ;
	 d.height = image.getHeight(this);
	 if (d.width > 0 && d.height > 0) {
	    // size known; adjust for shadow and border
	    d.width += 2*(shadow+border);
	    d.height += 2*(shadow+border);
	    d.width = Math.max(d.width, minWidth);
	    if (showLabel) {
	       // show label as well as image; adjust for label size
	       if (labelDimension.height > d.height)
		  d.height = labelDimension.height;
	       d.width += labelDimension.width - 2*shadow - border;
	    }
	 }
      }
      return d;
   }

   public synchronized void setEnabled(boolean enable) {
	if (this.enable != enable) {
	      this.enable = enable;
	      repaint();		// as suggested by Robert Neundlinger
	}
   }

   public void myNotifyListeners() {
	for (Enumeration enum1 = listeners.elements(); enum1.hasMoreElements(); ) {
		ActionListener listener = (ActionListener)enum1.nextElement();
		listener.actionPerformed(new ActionEvent(this, 0, getLabel()));
	}
   }

   private Image getGrayScale(Image image) {
	ImageFilter filter = new GrayScaleFilter();
	ImageProducer prod = new FilteredImageSource(image.getSource(), filter);
	return createImage(prod);
   }

   private class CBMouseListener extends MouseAdapter {

	public void mouseExited(MouseEvent e) {
		if (selected) {
			// mark as un-selected and repaint
			selected = false;
			repaint();
	      	}
	}

	public void mousePressed(MouseEvent e) {
		if (enable) {
			selected = true;
			repaint();
		}
	}

	public void mouseReleased(MouseEvent e) {
      		if (selected) {
			// mark as un-selected and repaint
			selected = false;
			repaint();
			if (enable) myNotifyListeners();
		}
	}

   }

}
