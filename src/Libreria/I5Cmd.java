/*
 * Classname I5Cmd 
 *
 * Version 0.1 info Object of Commands 
 *
 * Copyright notice
 */




package Libreria;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Enumeration;
import java.util.Hashtable;

public class I5Cmd {
	
	private String conf;
	private Hashtable<String, String> hst;
	
	public I5Cmd(String conf) {
		// TODO Auto-generated constructor stub
		this.conf=conf;
	}
	
	public I5Cmd(){
		this.conf=null;
	}
	
	public void setConfigFile(String conf){
		this.conf=conf;
	}
	
	public String getConfigFile(){
		return this.conf;
	}
	
	public boolean loadCommands(){
		try{
			File file = new File(this.conf);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc=db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nl = doc.getElementsByTagName("compiler");
			// Create the hashtable.
			if(hst!=null){
				hst.clear();
			}
			hst = new Hashtable<String,String>(nl.getLength());
			for(int s=0;s<nl.getLength();s++){
				Node Nodo = nl.item(s);
				if(Nodo.getNodeType()==Node.ELEMENT_NODE){
					Element element = (Element) Nodo;
					// Take the first value of element ID
					NodeList ListNd = element.getElementsByTagName("id");
					Element FstElement = (Element) ListNd.item(0);
					NodeList uniquenode = FstElement.getChildNodes();
					String auxid=uniquenode.item(0).getNodeValue();
					// Take the second value of element Name
					ListNd = element.getElementsByTagName("name");
					FstElement = (Element) ListNd.item(0);
					uniquenode = FstElement.getChildNodes();
					auxid=auxid+":"+uniquenode.item(0).getNodeValue();
					// take the Third value of element Sentence
					ListNd = element.getElementsByTagName("sentence");
					FstElement = (Element) ListNd.item(0);
					uniquenode = FstElement.getChildNodes();				
					auxid=FstElement.getAttribute("parm")+":"+auxid;
					String auxsentence=uniquenode.item(0).getNodeValue();
					hst.put(auxid,auxsentence);
				}
			}
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		return true;
	}
	
	public String[] getKeyCommands(){
		String mykeys[]= new String[hst.size()];
		int pos=0;
		for(Enumeration<String> enu= hst.keys();enu.hasMoreElements();){
			mykeys[pos]=enu.nextElement();
			pos++;
			
		}
		return mykeys;
	}
	
	public String getKeyCommand(String type){
		String mykey = new String("");
		String aux= new String("");
		for(Enumeration<String> enu= hst.keys();enu.hasMoreElements();){
			mykey=enu.nextElement();
			if(mykey.contains(type)==true){
				aux=mykey;
			}
			
		}
		return aux;
	}
	
	public String getCommand(String key,String ... parm){
		String mycommand=null;
		if(key.isEmpty()==false){
			int parms=Integer.parseInt(key.substring(0,key.indexOf(":")));
			if(parms==parm.length){
				mycommand=hst.get(key);
				if(mycommand!=null){
					for(int i=0;i<parms;i++){
						mycommand=mycommand.replace("#VAR"+(i+1)+"#",parm[i]);
					}
				}
			}
		}
		return mycommand;
	}
	
	public boolean createCommandsFile(){
		return true;
	}
	

}
