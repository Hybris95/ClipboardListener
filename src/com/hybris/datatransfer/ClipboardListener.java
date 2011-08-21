package com.hybris.datatransfer;

import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Clipboard;
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

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

// Gui
import javax.swing.JFrame;

public class ClipboardListener implements Runnable{

	public static void main(String[] args){
		
		System.out.println("[ClipboardListener] Loading...");
		
		// Model
		System.out.println("[ClipboardListener] Loading Model...");
		ClipboardListener clipboard = null;
		try{
			clipboard = new ClipboardListener();
			System.out.println("[ClipboardListener] (Model) Loaded Clipboard");
		}
		catch(HeadlessException e){
			System.err.println("[ClipboardListener-Error] (Model) Loading failed!");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		catch(UnsupportedFlavorException e){
			System.err.println("[ClipboardListener-Error] (Model) Loading failed!");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		if(clipboard == null){
			System.err.println("[ClipboardListener-Error] (Model) Loading failed!");
			System.exit(-1);
		}
		
		Thread listening = new Thread(clipboard, "ClipboardListener");
		listening.start();
		System.out.println("[ClipboardListener] Loaded Model!");
		
		// View/Controller
		System.out.println("[ClipboardListener] Loading Controller...");
		if(args.length > 0){
			String maybeText = args[0];
			if(maybeText.equalsIgnoreCase("-text")){
				System.out.println("[ClipboardListener] (Controller) Loading text controller");
				clipboard.loadTextController();
			}
			else{
				System.out.println("[ClipboardListener] (Controller) Loading gui controller");
				clipboard.loadGuiController();
			}
		}
		else{
			System.out.println("[ClipboardListener] (Controller) Loading gui controller");
			clipboard.loadGuiController();
		}
		System.out.println("[ClipboardListener] Loaded Controller!");
		System.out.println("[ClipboardListener] Loaded succesfully!");
		System.out.print("[ClipboardParser]>");
		// Something else to do after loading the controller/view ?
	}
	
	private String lastSentence = "";
	private ArrayList<String> sentences = null;
	private Clipboard clipboard = null;
	
	private ClipboardListener() throws HeadlessException, UnsupportedFlavorException{
		sentences = new ArrayList<String>();
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try{
			Object sentence = clipboard.getData(DataFlavor.stringFlavor);
			if(!sentence.equals(lastSentence)){
				sentences.add(sentence.toString());
			}
		}
		catch(IOException e){}
	}
	
	public void run(){
		while(true){
			try{
				Object sentence = clipboard.getData(DataFlavor.stringFlavor);
				if(!sentence.equals(lastSentence)){
					lastSentence = sentence.toString();
					sentences.add(lastSentence);
				}
			}
			catch(UnsupportedFlavorException e){}
			catch(IOException e){}
			catch(IllegalStateException e){}
			catch(NullPointerException e){}
		}
	}
	
	String[] getTexts(){
		String[] toReturn = new String[sentences.size()];
		for(int i = 0; i < toReturn.length; i++){
			toReturn[i] = sentences.get(i);
		}
		return toReturn;
	}
	
	private void loadTextController(){
		InputStream in = System.in;
		InputStreamReader iSR = new InputStreamReader(in);
		BufferedReader bR = new BufferedReader(iSR);
		TextController tC = new TextController(bR, this);
		tC.start();
	}
	
	class TextController extends Thread{
		private BufferedReader bR;
		private ClipboardListener listener;
		private boolean quitting;
		
		public TextController(BufferedReader bR, ClipboardListener listener){
			super("ClipboardListener-TextController");
			this.bR = bR;
			this.listener = listener;
			boolean quitting = false;
		}
		
		/**
		* #answer -1 = undefined, 0 = save, 1 = quit, 2 = usage
		*/
		public void run(){
			while(!quitting){
				try{
					if(bR.ready()){
						System.out.println();
						String line = bR.readLine();
						int answer = -1;
						if(line.startsWith("save ")){
							answer = save(line);
							if(answer == 1) answer = 2;
						}
						else if(line.startsWith("saveandquit ")){
							answer = saveAndQuit(line);
							if(answer == 1) answer = 2;
						}
						else if(line.startsWith("quit")){
							answer = quit(line);
							if(answer == 1) answer = 2;
							if(answer == 3) answer = 1;
						}
						// TODO Empty command, Load command ?
						
						switch(answer){
							case 0:
								System.out.println("Your clipboard was saved !");
								break;
							case 1:
								break;
							case 2:
							case -1:
							default:
								System.out.println("[ClipboardParser-USAGE]");
								System.out.println("save <filename>");
								System.out.println("saveandquit <filename>");
								System.out.println("quit (-f[orce]|<filename>)");
								break;
						}
						System.out.print("[ClipboardParser]>");
					}
				}
				catch(IOException e){
					System.err.println("[ClipboardListener]" + e.getMessage());
				}
			}
			quitting = false;
		}
	
		/**
		* @return 0 = Saved, 1 = Cancelled, 2 = Failed
		*/
		private int save(String line){
			try{
				String fileName = line.split(" ", 2)[1];
				File toSave = new File(fileName);
				toSave.createNewFile();
				if(toSave.canWrite()){
					FileWriter out = new FileWriter(toSave);
					String[] texts = listener.getTexts();
					boolean wroteSomething = false;
					for(int i = 0; i < texts.length; i++){
						try{
							out.write(texts[i]+'\n');
							wroteSomething = true;
						}
						catch(IOException e){
							continue;
						}
						// TODO Add the cancel system
					}
					
					if(wroteSomething){
						out.flush();
					}
					else if(texts.length != 0){
						return 2;
					}
					
					out.close();
					return 0;
				}
				else{
					return 2;
				}
			}
			catch(NullPointerException e){
				return 2;
			}
			catch(IOException e){
				return 2;
			}
		}
		
		/**
		* @return 0 = Saved, 1 = Cancelled, 2 = Failed
		*/
		private int saveAndQuit(String line){
			line = line.replaceFirst("saveandquit", "save");
			int saveAnswer = save(line);
			switch(saveAnswer){
				case 0:
					quitting = true;
				case 1:
				case 2:
				default:
					break;
			}
			return saveAnswer;
		}
		
		/**
		* @return 0 = SaveAndQuitted, 1 = Cancelled, 2 = Failed, 3 = Quitted
		*/
		private int quit(String line){
			boolean force = false;
			String[] args = line.split(" ", 2);
			if(args.length == 1){
				return 2;
			}
			
			if(args[1].equals("-f") ||args[1].equals("-force")){
				force = true;
			}
			
			if(!force){
				line = line.replaceFirst("quit", "save");
				int saveAnswer = save(line);
				switch(saveAnswer){
					case 0:
						quitting = true;
					case 1:
					case 2:
					default:
						break;
				}
				return saveAnswer;
			}
			else{
				quitting = true;
				return 3;
			}
		}
		
	}
	
	private void loadGuiController(){
		JFrame frame = null;
		try{
			frame = new JFrame("ClipboardListener");
		}
		catch(HeadlessException e){
			loadTextController();
			return;
		}
		
		if(frame == null){
			loadTextController();
			return;
		}
		
		// TODO
	}

}
