/*
 *                  BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 * 
 * Created on May 21, 2007
 * 
 */

package org.biojava.spice.manypanel.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biojava.spice.feature.HistogramFeature;
import org.biojava.spice.feature.HistogramSegment;
import org.biojava.spice.feature.Segment;


public class HistogramPainter {

	float     scale;
	CoordManager coordManager;
	int chainLength;
	

	
	Map cache;
	public HistogramPainter(CoordManager cm, float scale, int chainLength){
		this.scale        = scale;
		this.coordManager = cm;
		this.chainLength  = chainLength;
		
		
		// a cache to store max and min values
		cache = new HashMap();
		
	}



	

	public int getChainLength() {
		return chainLength;
	}





	public void setChainLength(int chainLength) {
		this.chainLength = chainLength;
	}





	public float getScale() {
		return scale;
	}





	public void setScale(float scale) {
		this.scale = scale;
	}

	
	



	protected void drawHistogramFeature(HistogramFeature feature,
			int featurePos, 
			int drawHeight,
			Graphics g,
			int y, Color c1, Color c2, 
			String histogramType){


		//if ( featurePos > 0)
		//	return;


		//System.out.println("drawing Histogram: " + histogramType);

		Graphics2D g2D =(Graphics2D) g;

		int aminosize = Math.round(1*scale);
		if ( aminosize < 1 )
			aminosize = 1;

		int zeroPos = drawHeight / 2; 
		int zeroY   = y + zeroPos;
		int maxY    = y + drawHeight;


		//System.out.println("hydrophobicity " + features.length + " " + histogramType );
		/**if ( histogramType.equals("lineplot")){
			//		 sort the features by their start position. 
			Comparator fcomp = new FeatureComparator();

			Arrays.sort(features,fcomp);
		}**/

		
		double max = feature.getMax();
		double min = feature.getMin();
		//System.out.println("max / min " + max + " " +min);
		if ( min < 0) {

			Color avC = DrawUtils.getColorGradient(c1, c2, 0.5);
			g2D.setColor(avC);

			// draw the "0" line ...
			if ( ! histogramType.equals("gradient"))
				g2D.drawLine(0,zeroY,coordManager.getPanelPos(chainLength),zeroY );
		}

		double shift   = 0 - min;

		int prevStart  = 0;
		int prevHeight = 0;


		List segments = feature.getSegments();
		
		
		for (int i =0 ; i< segments.size(); i++){
			Segment seg = (Segment)segments.get(i);
	
			if ( ! (seg instanceof HistogramSegment))
				continue;
			
			HistogramSegment s = (HistogramSegment)seg;
			
			
			double d     =  s.getScore();			
			double ratio =  ( d + shift ) / (max+shift) ;

			Color c = DrawUtils.getColorGradient(c1, c2, ratio);
			g2D.setColor(c);
			s.setColor(c);
			
			int start = s.getStart() -1 ;
			int end   = s.getEnd() -1 ;

			int xstart = coordManager.getPanelPos(start);            
			int width  = coordManager.getPanelPos(end) - xstart + aminosize +1;

			int height =  (int)Math.round(drawHeight * ratio)+1;

			//if ( d < 0 )
			//	height = drawHeight - height;

			// System.out.println("drawHeight:" +drawHeight + " zeroY:" + zeroY +  "score:" + score + " height:" + height + " ratio " + ratio + " " + start + " " + end);


			// switch based on the histogramtype:


			if ( histogramType.equalsIgnoreCase("histogram")) {
				//System.out.println("draw a histogram");
				// the inverse display:
				// g2D.fillRect(xstart+width-aminosize,y+ height,aminosize, drawHeight - height);

				int barH = Math.abs(height - zeroPos);
				if (min <0 ) {
					if ( height < zeroPos) {
						//	barH = height;
						g2D.fillRect(xstart+width-aminosize,zeroY,aminosize, zeroPos-height);
					} else {
						g2D.fillRect(xstart+width-aminosize,maxY-height,aminosize,barH);
					}
				} else {
					g2D.fillRect(xstart+width-aminosize,maxY-height,aminosize,height);
				}

				/*if ( d >= 0)
					g2D.fillRect(xstart+width-aminosize,zeroY-height,aminosize, height);
				else 
					g2D.fillRect(xstart+width-aminosize,zeroY,aminosize, height);
				 */

			} else if ( histogramType.equals("gradient")) {

				g2D.fillRect(xstart,y,width,drawHeight);

			}
			else if ( histogramType.equals("lineplot")){

				int ystart = maxY - prevHeight ;				
				int yend   = maxY - height  ; 				

				if (prevStart != (start - 1))				
					ystart = yend;

				g2D.drawLine(xstart,ystart,xstart + aminosize,yend);


			} else {
				System.err.println("unknow histogram style " + histogramType);
			}

			// draw a base line
			//g2D.setColor(Color.black);
			//g2D.drawLine(0,maxY,chainLength * aminosize,maxY);
			
			prevHeight = height;			
			prevStart = start;
		}
		

	}



}
