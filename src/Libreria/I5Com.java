
/*
 * Classname I5Com 
 *
 * Version 0.1 info Object of comunication 
 *
 * Copyright notice
 */

package Libreria;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileReader;
import com.ibm.as400.access.IFSFileWriter;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400FileRecordDescription;
import com.ibm.as400.access.RecordFormat;
import com.ibm.as400.access.FieldDescription;

public class I5Com {
	private String server;
	private String user;
	private String pass;
	private String dir;
	private int max_width_regist;
	private String msg;
	private AS400 conexion;
	
	public I5Com(){
		this.server=null;
		this.user=null;
		this.pass=null;
		this.dir=null;
		this.max_width_regist=108; //This is the standar.
	}
	
	// Constructor function
	public I5Com(String server) {
		// TODO Auto-generated constructor stub
		this.server=server;
		this.user=null;
		this.pass=null;
		this.dir=null;
		this.max_width_regist=108; //This is the standar.
	}
	
	public I5Com(String server, String user, String pass) {
		// TODO Auto-generated constructor stub
		this.server=server;
		this.user=user;
		this.pass=pass;
		this.dir=null;
	}
	
	public void setServer(String server){
		this.server=server;
	}
	
	public String getServer(){
		return this.server;
	}
	
	public void setLogin(String user, String pass){
		this.user=user;
		this.pass=pass;
	}
	
	public short connect(){
		short returns=0;
		if(this.user==null && this.pass==null){ 
			conexion = new AS400(this.server);
		}else{
			conexion = new AS400(this.server,this.user,this.pass);
		}
		try {
			conexion.connectService(AS400.COMMAND);
			conexion.connectService(AS400.FILE);
		} catch (AS400SecurityException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			returns=1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			returns=1;
		} catch(Exception e){
			returns=1;
		}
		return returns;
	}
	
	public boolean isConected(){
		boolean returns;
		returns=this.conexion.isConnected();
		return returns;
	}
	
	public short disconnect(){
		short returns=1;
		if(conexion!=null){
			returns=0;
			if(conexion.isConnected()==true){
				conexion.disconnectAllServices();
			}
		}
		return returns;
	}
	
	// Set the directory of the source file
	public void setDirectory(String dir){
		this.dir=dir;
	}
	// get the directory of the source file
	public String getDirectory(){
		return this.dir;
	}
	
	public void setWidthSource(int width){
		this.max_width_regist=width;
	}
	
	public short getWidthSourceAS400(String lib, String file, String member){
		short recordWitdh=0;
		if(conexion.isConnected()){
			String from = new String("/QSYS.LIB/"+lib+".LIB/"+file+".FILE/"+member+".MBR");
			AS400FileRecordDescription myfile = new AS400FileRecordDescription(conexion,from);
			try {
				RecordFormat[] format= myfile.retrieveRecordFormat();
				FieldDescription field=format[format.length-1].getFieldDescription("SRCDTA");
				recordWitdh=(short) field.getLength();
			} catch (AS400Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
				recordWitdh=-1;
			} catch (AS400SecurityException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				recordWitdh=-1;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				recordWitdh=-1;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				recordWitdh=-1;
			}
		}else{
			recordWitdh=-1;
		}
		return recordWitdh;
	}
	
	public boolean isSource(String lib, String file, String member){
		boolean returns=false;
		if(conexion.isConnected()){
			String from = new String("/QSYS.LIB/"+lib+".LIB/"+file+".FILE/"+member+".MBR");
			IFSFile fromfile = new IFSFile(conexion,from);
			try {
				returns=fromfile.isFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				returns=false;
			}
		}
		return returns;
	}
	
	// Get the source from the AS400
	public boolean getSource(String lib, String file, String member){
		String []libp=new String[1];
		String []filep=new String[1];;
		String []memberp=new String[1];;
		libp[0]=new String(lib);
		filep[0]=new String(file);
		memberp[0]= new String(member);
		return getSource(libp,filep,memberp);
	}
	
	public boolean getSource(String lib[], String file[], String member[]){
		boolean returns=true;
		char registry[]= new char[max_width_regist];
		if(conexion.isConnected()){
			for(int cant=0;cant<lib.length;cant++){
				String from = new String("/QSYS.LIB/"+lib[cant]+".LIB/"+file[cant]+".FILE/"+member[cant]+".MBR");
				String to=new String("");
				// check if the folder exist.
				if(new java.io.File(file[cant]).exists()==false){
					if(this.dir!=null){
						String folder = this.dir+"/"+file[cant];
						new java.io.File(folder).mkdir();		
					}else{
						new java.io.File(file[cant]).mkdir();
					}
				}
				
				if(this.dir!=null){
					to = new String(this.dir+"/"+file[cant]+"/"+member[cant]+"."+file[cant]);
				}else{
					to = new String(file[cant]+"/"+member[cant]+"."+file[cant]);
				}
				try{
					IFSFile fromfile = new IFSFile(conexion,from);
					
					BufferedReader source = new BufferedReader(new IFSFileReader(fromfile));
					FileWriter destiny = new FileWriter(to);
					while(source.read(registry,0,max_width_regist)!=-1){
						destiny.write(this.rightTrim(String.valueOf(registry))+"\n");
					}

					destiny.close();
					source.close();
					msg="The Source have been Load";
				}catch (AS400SecurityException e) {
					// TODO Auto-generated catch block
					//e.printStackTramiembroce();
					if(conexion.isConnected()==false){
						msg="The conexión was canceled";
					}
					returns=false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					if(conexion.isConnected()==false){
						msg="The conexión was canceled";
					}else{
						msg="There is a problem with the source file";
					}
					returns=false;
				}
			}	
		}else{
			msg="There is a problem with the source file";
			returns=false;
		}
		return returns;
	}
	
	// Write the Source from the AS400
	public boolean setSource(String lib, String file, String member){
		String []libp=new String[1];
		String []filep=new String[1];;
		String []memberp=new String[1];;
		libp[0]=new String(lib);
		filep[0]=new String(file);
		memberp[0]= new String(member);
		return setSource(libp,filep,memberp);
	}
	
	public boolean setSource(String []lib, String []file, String []member){
		boolean returns=true;
		// Chance the conection.	
		if(conexion.isConnected()){
			for(int cant=0;cant<lib.length;cant++){
				String to = new String("/QSYS.LIB/"+lib[cant]+".LIB/"+file[cant]+".FILE/"+member[cant]+".MBR");
				String from="";
				// check if the folder exist.
				if(new java.io.File(file[cant]).exists()==false){
					if(this.dir!=null){
						String folder = this.dir+"/"+file[cant];
						new java.io.File(folder).mkdir();		
					}else{
						new java.io.File(file[cant]).mkdir();
					}
				}
				if(this.dir!=null){
					from = new String(this.dir+"/"+file[cant]+"/"+member[cant]+"."+file[cant]);
				}else{
					from = new String(file[cant]+"/"+member[cant]+"."+file[cant]);
				}
				String registry = new String();
				char []regisaux= new char[max_width_regist];
				try{
					IFSFile tofile = new IFSFile(conexion,to);
					PrintWriter escritor = new PrintWriter(new BufferedWriter(new IFSFileWriter(tofile)));
					FileReader filesource = new FileReader(from);
					BufferedReader source = new BufferedReader(filesource);
					registry=source.readLine();
					while(registry!=null){
						this.copychar(regisaux,registry);
						escritor.write(regisaux);
						registry=source.readLine();
					}
					escritor.close();
					source.close();
					msg="The Source have been save";
				}catch (AS400SecurityException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					if(conexion.isConnected()==false){
						msg="The conexión was canceled";
					}
					returns=false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					if(conexion.isConnected()==false){
						msg="The conexión was canceled";
					}else{
						msg="There is a problem with the source file";
					}
					returns=false;
				}
			}
		}
		else{
			msg="There is a problem with the source file";
			returns=false;
		}
		return returns;
	}
	
	public boolean executeComand(String command){
		String []commandp=new String[1];
		boolean returns=false;
		if(command!=null){
		commandp[0]=new String(command);
			returns=executeComand(commandp);
		}else{
			returns=false;
		}
		return returns;
	}
	
	
	public boolean executeComand(String []command){
		boolean returns=true;
		if(conexion.isConnected()){
			CommandCall cmd = new CommandCall(conexion);
			for(int cant=0;cant<command.length;cant++){
				try {
					returns=cmd.run(command[cant]);
					AS400Message[] messageList = cmd.getMessageList();
					msg=messageList[0].toString();
					//System.out.println(msg);
				} catch (AS400SecurityException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					if(conexion.isConnected()==false){
						msg="The conexión was canceled";
					}
					returns=false;
				} catch (ErrorCompletingRequestException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					returns=false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					returns=false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					returns=false;
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					returns=false;
				}
			}
		}
		else{
			msg="The conexión was canceled";
			returns=false;
		}
		return returns;
	}
	
	public boolean createSourceMember(String lib, String file, String member,String tipo){
		String comand = new String("ADDPFM FILE("+lib+"/"+file+") MBR("+member+") TEXT("+member+")" +
				        " SRCTYPE("+tipo+")");
		return this.executeComand(comand);
	}
	
	public boolean dropSourceMember(String lib, String file, String member){
		String comand = new String("RMVM FILE("+lib+"/"+file+") MBR("+member+")");	
		return this.executeComand(comand);
	}
	
	public String getMessage(){
		return this.msg;
	}
	
	private void copychar(char []a,String b){
		for(int i=0;i<a.length;i++){
			if(i<b.length()){
				a[i]=b.charAt(i);
			}
			else{
				a[i]=' ';
			}
		}
	}
	
	// I cant use it right now.
	private String rightTrim(String text){
		short len=(short) (text.length()-1);
		short pos=(short) (text.length()-1);
		while(len>0){
			//System.out.println(text.charAt(len));
			if(Character.isSpaceChar(text.charAt(len))==false){
				pos=len;
				len=1;
			}
			len--;
		}
		return text.substring(0,pos+1);
	}

}
