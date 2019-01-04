/*
 * Classname I5Wrkenv 
 *
 * Version 0.1 info Object of the Working Envioment 
 *
 * Copyright notice
 */
package Libreria;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author ramon
 *
 */
public class I5Wrkenv {
	private String name_env;
	private ArrayList <String> libraries;
	private ArrayList <I5Source> sources;
	private I5Com as400;
	private I5Cmd commands;
	private int check[]= new int[5];
	// Constructor.
	public I5Wrkenv(){
		this.name_env=null;
		this.as400 = new I5Com();
		this.commands = new I5Cmd();
		this.sources= new ArrayList<I5Source>();
		this.libraries= new ArrayList<String>();
	}
	
	public I5Wrkenv(String config){
		check[0]=1;
		this.name_env=config;
		this.as400 = new I5Com();
		this.commands = new I5Cmd();
		this.sources= new ArrayList<I5Source>();
		this.libraries= new ArrayList<String>();
	}
	
	public void setEnvConfig(String config){
		check[0]=1;
		this.name_env=config;
		as400.setDirectory(config);
	}
		
	public void setServer(String server){
		check[1]=1;
		this.as400.setServer(server);
	}
	
	public void setLogin(String user, String pass){
		this.as400.setLogin(user, pass);
	}
	
	public void setCommandConf(String conf){
		check[2]=1;
		this.commands.setConfigFile(conf);
		this.commands.loadCommands();
	}
	
	public int connect(){
		int returns=0;
		if(check[1]==1){
			returns=this.as400.connect();
			// Load libraries of envioment.
			if(libraries!=null){
				int cant=libraries.size();
				for(int pos=0;pos<cant;pos++){
					as400.executeComand("ADDLIBLE LIB("+libraries.get(pos)+") POSITION(*LAST)");
				}
			}
			
			
		}else{
			returns=1;
		}
		
		return returns;
	}
	
	public int disconnect(){
		int returns=1;
		if(this.as400!=null){
			returns=this.as400.disconnect();
		}
		return returns; 
	}
	
	public boolean checkEnvioment(){
		boolean returns=false;
		if(check[0]==1){
			if((new File(this.name_env).mkdir())==false){
				returns=false;
			}
			else{
				returns=true;
			}
		}
		return returns;
	}
	
	public int createEnvioment(){
		int returns=0;
		if((check[0]+check[1]+check[2])==3){
			if((new File(this.name_env).isDirectory())==false){
				if((new File(this.name_env).mkdir())==false){
					returns=1;
				}
			}
			if(returns==0){
				try{
					if ((new File(this.name_env+ "/" + this.name_env+".PEV").createNewFile())==false){
						returns=1;
					}
				}catch(IOException e){
					returns=1;
				}
			}
			
		}
		
		return returns;
	}
	
	public boolean addSource(String lib, String file, String member,String type){
		boolean returns=false;
		if(as400.isSource(lib, file, member)==false){
			returns=as400.createSourceMember(lib, file, member, type);
		}else{
			returns=true;
		}
		if(returns==true){
			I5Source newsource = new I5Source();
			newsource.library=lib;
			newsource.file=file;
			newsource.member=member;
			newsource.type=type;
			newsource.registry_width=this.as400.getWidthSourceAS400(lib,file,member);
			if(sources.isEmpty()==true){
				sources.add(newsource);
				as400.setWidthSource(newsource.registry_width);
				as400.getSource(lib, file, member);
			}
			else{
				if(sources.contains(newsource)==false){
					sources.add(newsource);
					as400.setWidthSource(newsource.registry_width);
					as400.getSource(lib, file, member);
				}else{
					returns=false;
				}
			}
		}
	
		return returns;
	}
	
	public boolean addLibrary(String lib){
		boolean returns=false;
		if(libraries.contains(lib)==false){
			libraries.add(lib);
			try{
				if(as400!=null && as400.isConected()==true){
					returns=as400.executeComand("ADDLIBLE LIB("+lib+") POSITION(*LAST)");
				}else{
					returns=true;
				}
			}catch(Exception e){
				returns=false;
			}
			if(returns==false){
				libraries.remove(lib);
			}
		}else{
			returns=false;
		}
		return returns;
	}
	
	public boolean rmvLibrary(String lib){
		boolean returns=false;
		if(libraries.contains(lib)==true){
			libraries.remove(lib);
			as400.executeComand("ADDLIBLE LIB("+lib+") POSITION(*LAST)");
		}else{
			returns=false;
		}
		return returns;
	}
	
	
	public boolean loadEnvioment(){
		boolean returns=true;
		try{
			File file = new File(this.name_env+"/"+this.name_env+".PEV");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc=db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nl = doc.getElementsByTagName("configuration");
			Node Nodo = nl.item(0);
			if(Nodo.getNodeType()==Node.ELEMENT_NODE){
				Element element = (Element) Nodo;
				// Take the server direcction
				NodeList ListNd = element.getElementsByTagName("server");
				Element FstElement = (Element) ListNd.item(0);
				NodeList uniquenode = FstElement.getChildNodes();
				this.as400.setServer(uniquenode.item(0).getNodeValue());
				// Take the second value of element Name
				ListNd = element.getElementsByTagName("directory");
				FstElement = (Element) ListNd.item(0);
				uniquenode = FstElement.getChildNodes();
				this.name_env=uniquenode.item(0).getNodeValue();
				// take the Third value of element Sentence
				ListNd = element.getElementsByTagName("commandConfiguration");
				FstElement = (Element) ListNd.item(0);
				uniquenode = FstElement.getChildNodes();
				this.commands.setConfigFile(uniquenode.item(0).getNodeValue());
				//Verify if the file exist
				this.commands.loadCommands();
				// Take the four value of element Sentence
				ListNd = element.getElementsByTagName("library");
				if(this.libraries!=null){
					this.libraries.clear();
				}
				FstElement = (Element) ListNd.item(0);
				uniquenode = FstElement.getChildNodes();
				String libs=uniquenode.item(0).getNodeValue();
				if(libs.contains("null")!=true){
					String [] libraries=libs.split(";");
					int cant=libraries.length;
					for(int pos=0;pos<cant;pos++){
						this.addLibrary(libraries[pos]);
					}
				}
				check[0]=1;
				check[1]=1;
				check[2]=1;

			}
			nl = doc.getElementsByTagName("source");
			if(this.sources!=null){
				this.sources.clear();
			}
			for(int s=0;s<nl.getLength();s++){
				I5Source source = new I5Source();
				Nodo = nl.item(s);
				if(Nodo.getNodeType()==Node.ELEMENT_NODE){
					Element element = (Element) Nodo;
					// First Element
					NodeList ListNd = element.getElementsByTagName("library");
					Element FstElement = (Element) ListNd.item(0);
					NodeList uniquenode = FstElement.getChildNodes();
					source.library=uniquenode.item(0).getNodeValue();
					//Second Element
					ListNd = element.getElementsByTagName("file");
					FstElement = (Element) ListNd.item(0);
					uniquenode = FstElement.getChildNodes();
					source.file=uniquenode.item(0).getNodeValue();
					//Third Element
					ListNd = element.getElementsByTagName("member");
					FstElement = (Element) ListNd.item(0);
					uniquenode = FstElement.getChildNodes();
					source.member=uniquenode.item(0).getNodeValue();
					// Four Element
					ListNd = element.getElementsByTagName("type");
					FstElement = (Element) ListNd.item(0);
					uniquenode = FstElement.getChildNodes();
					source.type=uniquenode.item(0).getNodeValue();
					// Five Elment
					ListNd = element.getElementsByTagName("with");
					FstElement = (Element) ListNd.item(0);
					uniquenode = FstElement.getChildNodes();
					source.registry_width=Integer.parseInt(uniquenode.item(0).getNodeValue());
					sources.add(source);

				}
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
			return returns=false;	
		}
		return returns;
	}
	
	public int saveEnvioment(){
		
		if(check[0]==1 && check[1]==1){
			String to = new String(this.name_env+"/"+this.name_env+".PEV");
			try {
				FileWriter fileconf = new FileWriter(to);
				fileconf.write("<?xml version=\"1.0\"?>\n");
				fileconf.write("<envioment>\n");
				fileconf.write("<configuration>\n");
				fileconf.write("<server>"+this.as400.getServer()+"</server>\n");
				fileconf.write("<directory>"+this.name_env+"</directory>\n");
				fileconf.write("<commandConfiguration>"+this.commands.getConfigFile()+"</commandConfiguration>\n");
				fileconf.write("<library>");
				int cant=this.libraries.size();
				for(int pos=0;pos<cant;pos++){
					String lib=this.libraries.get(pos);
					fileconf.write(lib);
					if(pos!=(cant-1)){
						fileconf.write(";");
					}
				}
				if(cant==0){
					fileconf.write("null");
				}
				fileconf.write("</library>\n");
				fileconf.write("</configuration>\n");
				fileconf.write("<sources>\n");
				cant=this.sources.size();
				for(int pos=0;pos<cant;pos++){
					I5Source souraux=this.sources.get(pos);
					fileconf.write("<source>\n");
					fileconf.write("<library>"+souraux.library+"</library>\n");
					fileconf.write("<file>"+souraux.file+"</file>\n");
					fileconf.write("<member>"+souraux.member+"</member>\n");
					fileconf.write("<type>"+souraux.type+"</type>\n");
					fileconf.write("<with>"+souraux.registry_width+"</with>\n");
					fileconf.write("</source>\n");
				}
				fileconf.write("</sources>\n");
				fileconf.write("</envioment>\n");
				fileconf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		return 0;
	}
	
	public int getCantSources(){
		return this.sources.size();
	}
	
	public I5Source getInfoSource(int index){
		I5Source aux = new I5Source();
		if(index<=this.sources.size() && index>-1){
			aux=this.sources.get(index);
		}
		else{
			aux=null;
		}
		return aux;
	}
	
	public int loadSource(int index){
		I5Source aux = new I5Source();
		if(index<=this.sources.size() && index>-1){
			aux=this.sources.get(index);
			this.as400.setWidthSource(aux.registry_width);
			this.as400.getSource(aux.library,aux.file,aux.member);
		}
		return 0;
	}
	
	public int saveSource(int index){
		I5Source aux = new I5Source();
		if(index<=this.sources.size() && index>-1){
			aux=this.sources.get(index);
			this.as400.setWidthSource(aux.registry_width);
			this.as400.setSource(aux.library,aux.file,aux.member);
		}
		return 0;
	}
	
	public int deleteSource(int index){
		if(index<=this.sources.size() && index>-1){
			this.sources.remove(index);
		}
		return 0;
	}
	
	public boolean compileSource(int index){
		I5Source aux = new I5Source();
		boolean returns=false;
		if(index<=this.sources.size() && index>-1){
			aux=this.sources.get(index);
			returns=this.as400.executeComand(commands.getCommand(commands.getKeyCommand(aux.type),
					aux.library,aux.member,aux.library,aux.file,aux.member));
		}
		return returns;
	}
	
	public String getMessage(){
		String returns = new String("");
		if(this.as400!=null){
			returns=this.as400.getMessage();
		}
		if(returns==null){
			returns="There was a problem with compilatión, please check the configuration";
		}
		return returns;
	}

}
