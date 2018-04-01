
package org.jungmannlab.imagej;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;

import ij.io.DirectoryChooser;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import ij.plugin.*;

/**
This plugin helps to convert image data for example .czi (Zeiss file format) files to .raw with the addition of an YAML file for Picasso processing.
The image files need to be loaded into ImageJ.
Multi-channel files will be saved as ongoing files _Ch{i}

@author Auer Alexander <aauer@biochem.mpg.de>

created 170713
*/

@SuppressWarnings("unused")
public class Raw_Yaml_export implements PlugIn {

		
		// image property members
		private int width;
		private int height;
		private int frames;
		private int slices;
		private int channels;
		private boolean isStack;

		// plugin parameters
		public double value;
		public String name;
		

		public void run(String arg) {
			
			
			ImagePlus ip = WindowManager.getCurrentImage();
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
				if(path.length()==0) {
					throw new Exception();
				}
			} catch (Exception name) {

				DirectoryChooser directoryChooser = new DirectoryChooser("File has no path. Please select output folder.");
				
	            String selectedDirectory = directoryChooser.getDirectory();
	            if(selectedDirectory == null){
	                IJ.showMessage("No Folder selected. Try again.");
	            }else{
	                path = selectedDirectory;
	            }
			}
		
			isStack = (1!=ip.getStackSize());
			channels = ip.getNChannels();
			width = ip.getWidth();
			height = ip.getHeight();
			frames = ip.getNFrames();
			slices = ip.getNSlices();
			
//			solves an issue when stack data is loaded as slices (Z slices) and not as time points (frames)
//			make substack function produces z slices not time points e.g. frames
			if(slices >= frames) {
				frames = slices;
			}
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("Byte Order", '<');
			data.put("Data Type", "uint16");
			data.put("Frames",frames);
			data.put("Height",height);
			data.put("Width",width);
			
		
			if(channels==1){
				
				saveRaw(ip,path,System.getProperty("file.separator")+ipTitle);
				
				saveYaml(data,path,ipTitle);
				
				
			}else if(channels>1){
				//iterate through the channel stack
				
				for(int k=1; k<=channels; k++){
					
					ImageStack stackCh = new ImageStack(width, height);
					stackCh = ChannelSplitter.getChannel(ip,k);
					String ipTitleCh = null;
					ipTitleCh = ipTitle+"_Ch"+Integer.toString(k);
					
					ImagePlus imageCh = new ImagePlus(ipTitleCh,stackCh);
					imageCh.show();
					
					saveRaw(imageCh,path,System.getProperty("file.separator")+ipTitleCh);
					
					saveYaml(data,path,ipTitleCh);
					
				}
			
			}
				
		}	
		
		private void saveRaw(ImagePlus im, String path, String filename){
			
			Prefs.intelByteOrder = true;
			if(path != null && path.length() > 0){
				IJ.saveAs(im, "Raw Data", path+filename+".raw");  
			}
		}
		
		private void saveYaml(Map<String, Object> data, String path, String ipTitle ) {
			
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			Yaml yaml = new Yaml(options);
			
			FileWriter writer = null;
			try {
				writer = new FileWriter(path+ipTitle+".yaml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			yaml.dump(data, writer);
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
		Class<?> clazz = Raw_Yaml_export.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
		System.setProperty("plugins.dir", pluginsDir);
		// System.out.println(pluginsDir);
		// start ImageJ
		//new ImageJ();
		
		
		// open the sample image
//		 ImagePlus image = IJ.openImage("/Users/Alex/Desktop/1.png");
		//ImagePlus image = IJ.createImage("test", 360, 360, 6, 16);
		//image.show();
		// run the plugin
		//IJ.runPlugIn(clazz.getName(), "");
	}

	
}


