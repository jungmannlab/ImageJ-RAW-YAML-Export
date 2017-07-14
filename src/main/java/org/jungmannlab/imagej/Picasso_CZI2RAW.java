
package org.jungmannlab.imagej;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.*;
import ij.gui.*;
import ij.io.DirectoryChooser;

import java.awt.*;
import java.awt.image.*;
import ij.plugin.*;
import ij.plugin.frame.*;

/**

This plugin is developed for quadview images or imagestacks, where four different color channels ar recordet on a single camera chip.
The image gets quartered into four individual images or stacks with same size. Color channel info is appended to the filename. The color model stays the same.

Images needs to be square sized!

@author Auer Alexander <aauer@biochem.mpg.de>

created 150701
*/

public class Picasso_CZI2RAW implements PlugIn {

		
		// image property members
		private int width;
		private int height;
		private int frames;

		// plugin parameters
		public double value;
		public String name;
		

		public void run(String arg) {
			IJ.showMessage("dfd");
			ImagePlus ip=WindowManager.getCurrentImage();
			//check if file is loaded
			if(ip==null){
				IJ.noImage();
				return;
			}
			
			// get titel
			String ipFullTitle = ip.getTitle();
			String ipTitle = ipFullTitle;
			int i = ipFullTitle.indexOf(".");
			if(i!=-1){
				ipTitle = ipFullTitle.substring(0,i);
			}
		
			//extract file path
			String path = null;
			try {
				path = ip.getOriginalFileInfo().directory;
				
			} catch (NullPointerException name) {

				if(path==null){
					DirectoryChooser directoryChooser = new DirectoryChooser("File has no Location. Please select Output Location.");
					
		            String selectedDirectory = directoryChooser.getDirectory();
		            if(selectedDirectory == null){
		                IJ.showMessage("No Folder selected.");
		            }else{
		                path = selectedDirectory;
		            }
				}
			}
			
			//check if stack
			if(1==ip.getStackSize()){
				IJ.showMessage("jo");
				width = ip.getWidth();
				height = ip.getHeight();
				frames = ip.getNFrames();
				
				ImageStack stackCh1 = new ImageStack(width, height);
				ImageStack stackCh2 = new ImageStack(width, height);
				
				
				stackCh1 = ChannelSplitter.getChannel(ip,1);
				stackCh2 = ChannelSplitter.getChannel(ip,2);
				
				ImagePlus imageCh1 = new ImagePlus(ipTitle+"_Ch1",stackCh1);
				imageCh1.show();
				
			}
			else{
				IJ.showMessage("No Stack loaded");
			}
		}	


	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads an
	 * image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = Picasso_CZI2RAW.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
		System.setProperty("plugins.dir", pluginsDir);
		System.out.println(pluginsDir);
		// start ImageJ
		new ImageJ();
		
		
		// open the sample image
//		ImagePlus image = IJ.openImage("/Users/Alex/Desktop/2nM_p1_atto647n_561_50%.tif");
		ImagePlus image = IJ.createImage("test", 360, 360, 6, 16);
		image.show();
		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}

	
}


