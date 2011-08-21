package com.hybris.datatransfer;

import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
//import java.io.UnsupportedEncodingException;

import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.awt.Toolkit;
import java.awt.HeadlessException;

import java.util.ArrayList;

// Gui
import javax.swing.JFrame;

public class ClipboardListener implements FlavorListener{

	public static void main(String[] args){
		
		System.out.println("[ClipboardListener] Loading...");
		
		// Model
		System.out.println("[ClipboardListener] Loading Model...");
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = null;
		try{
			clipboard = toolkit.getSystemClipboard();
		}
		catch(HeadlessException e){
			try{
				clipboard = toolkit.getSystemSelection();
			}
			catch(HeadlessException f){
				System.err.println("[ClipboardListener] Model loading failed!");
				System.err.println(f.getMessage());
				System.exit(-1);
			}
		}
		
		if(clipboard == null){
			System.err.println("[ClipboardListener] Model loading failed!");
			System.exit(-1);
		}
		
		ClipboardListener cL = new ClipboardListener();
		clipboard.addFlavorListener(cL);
		System.out.println("[ClipboardListener] Loaded Model!");
		
		// View/Controller
		System.out.println("[ClipboardListener] Loading Controller...");
		if(args.length > 0){
			String maybeText = args[0];
			if(maybeText.equalsIgnoreCase("-text")){
				cL.loadTextController(clipboard/*, cL*/);
			}
			else{
				cL.loadGuiController(clipboard/*, cL*/);
			}
		}
		else{
			cL.loadGuiController(clipboard/*, cL*/);
		}
		System.out.println("[ClipboardListener] Loaded Controller!");
		// Something else to do after loading the controller/view ?
		System.out.println("[ClipboardListener] Loaded succesfully!");
	}
	
	private ArrayList<String> texts;
	
	private ClipboardListener(){
		texts = new ArrayList<String>();
	}
	
	public void flavorsChanged(FlavorEvent event){
		Object source = event.getSource();
		if(source instanceof Clipboard){
			Clipboard clipboard = (Clipboard)source;
			try{
				Transferable transfered = clipboard.getContents(this);
				DataFlavor dF = new DataFlavor();
				Reader reader = dF.getReaderForText(transfered);
				BufferedReader bR = new BufferedReader(reader);
				
				while(bR.ready()){
					try{
						texts.add(bR.readLine());
					}
					catch(IOException e){}
				}
			}
			catch(IllegalStateException e){
				texts.add("[ClipboardListener]" + e.getMessage());
			}
			catch(UnsupportedFlavorException e){
				texts.add("[ClipboardListener]" + e.getMessage());
			}
			catch(IOException e){
				texts.add("[ClipboardListener]" + e.getMessage());
			}
			catch(IllegalArgumentException e){
				texts.add("[ClipboardListener]" + e.getMessage());
			}
			catch(NullPointerException e){
				texts.add("[ClipboardListener]" + e.getMessage());
			}
		}
	}
	
	private String[] getTexts(){
		Object[] array = texts.toArray();
		if(array instanceof String[]){
			return (String[])array;
		}
		else{
			return new String[0];
		}
	}
	
	private void loadTextController(Clipboard clipboard/*, ClipboardListener cL*/){
		InputStream in = System.in;
		InputStreamReader iSR = new InputStreamReader(in);
		BufferedReader bR = new BufferedReader(iSR);
		TextController tC = new TextController(bR);
		tC.start();
	}
	
	class TextController extends Thread{
		private BufferedReader bR;
		
		public TextController(BufferedReader bR){
			super("ClipboardListener-TextController");
			this.bR = bR;
		}
		
		public void run(){
			while(true){
				try{
					if(bR.ready()){
						String line = bR.readLine();
						// TODO Command parser (save <file>, saveandquit <file>, quit [-f[orce]])
						System.out.println("[ClipboardParser]" + line);
					}
				}
				catch(IOException e){
					System.err.println("[ClipboardListener]" + e.getMessage());
				}
			}
		}
	}
	
	private void loadGuiController(Clipboard clipboard/*, ClipboardListener cL*/){
		JFrame frame = null;
		try{
			frame = new JFrame("ClipboardListener");
		}
		catch(HeadlessException e){
			loadTextController(clipboard/*, cL*/);
			return;
		}
		
		if(frame == null){
			loadTextController(clipboard/*, cL*/);
			return;
		}
		
		// TODO
	}

}
