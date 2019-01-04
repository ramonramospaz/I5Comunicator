package Libreria;
import java.util.Scanner;
import java.lang.NumberFormatException;

public class Main {

	/**
	 * @param args
	 */
	
	// Validemos que tan factible es utilizar arrays como parametros.
	// El servidor de prueba es PUB1.RZKH.DE
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner teclado = new Scanner(System.in);
		String aux = new String("");
		short op;
		do{
			System.out.println("Sistema de administración de fuentes");
			System.out.println("favor, seleccione la acción a tomar");
			System.out.println("1-Crear un nuevo ambiente de trabajo");
			System.out.println("2-Cargar un nuveo ambiente de trabajo");
			System.out.println("3-Salir");
			do{
				System.out.print("Opcion:");
				try{
					aux=teclado.next();
					op=Short.valueOf(aux);
				}catch(NumberFormatException e){
					op=0;
				}
			}while(op<1 || op>3);
			switch(op){
				case 1:
					crearNuevoAmbiente();
				break;
				case 2:
					cargarAmbiente();
				break;
			}
		}while(op!=3);
		System.exit(0);
		
	}
	
	private static void crearNuevoAmbiente(){
		I5Wrkenv env = new I5Wrkenv();
		Scanner teclado = new Scanner(System.in);
		String aux = new String("");
		int res=0;
		short cant;
		boolean res2;
		System.out.println("Creación del ambiente:");
		do{
			System.out.println("Ingrese el ambiente que desea crear");
			aux=teclado.next();
			env.setEnvConfig(aux);
			if((res2=env.checkEnvioment())==false){
				System.out.println("El ambiente ya existe, escoge otro nombre");
			}
		}while(res2==false);
		env.setCommandConf("Commands.xml");
		do{
			System.out.println("Ingrese el nómbre del servidor a conectarse");
			aux=teclado.next();
			env.setServer(aux);
			System.out.println("Intentando conectar");
			res=env.connect();
			if(res==1){
				System.out.println("No se pudo conectar al servidor, favor volver a intentar");
			}else{
				System.out.println("Sistema conectado con exito");
				// Ahora viene la parte de las librerias
				String op= new String("");
				System.out.println("A continuación, favor ingresar la librerias que quiera cargar:");
				do{
					System.out.println("Ingrese la libreria a cargar en el sistema:");
					String libreria=teclado.next();
					res2=env.addLibrary(libreria);
					if(res2==false){
						System.out.println("No se pudo cargar la libreria intente otra");
						op="s";
					}else{
						System.out.println("La libreria fue agregada. ¿Desea hacerlo con otro (s/n)?");
						op=teclado.next();
					}
				}while(op.contains("s")==true || op.contains("S")==true);
				cant=1;
				
				do{
					System.out.println("Ingresando el fuente "+cant);
					System.out.println("Ingrese la libreria:");
					String libreria=teclado.next();
					System.out.println("Ingrese el archivo");
					String archivo=teclado.next();
					System.out.println("Ingrese el miembro");
					String miembro=teclado.next();
					System.out.println("Ingrese el tipo");
					String tipo=teclado.next();
					res2=env.addSource(libreria,archivo,miembro,tipo);
					if(res2==false){
						System.out.println("No se puede crear el fuente, intente con otro");
						op="s";
					}else{
						System.out.println("El fuente fue agregado. ¿Desea hacerlo con otro (s/n)?");
						op=teclado.next();
					}
				}while(op.contains("s")==true || op.contains("S")==true);
				System.out.println("El ambiente se esta salvando.");
				env.saveEnvioment();
				System.out.println("El ambiente se encuentra salvando, favor cargar el ambiente para trabajar con el\n");
			}
		}while(res==1);
		if(res==0){
			System.out.println("Favor ingresar los fuentes que se van a trabajar en el ambiente");
			
		}else{
			System.out.println("No se puede conectar al servidor.");
		}
		env.disconnect();
		
	}
	
	private static void cargarAmbiente(){
		I5Wrkenv env = new I5Wrkenv();
		Scanner teclado = new Scanner(System.in);
		String aux = new String("");
		int cant=0;
		int res=0;
		boolean res2, res3;
		do{
			System.out.println("Ingrese el ambiente que desea cargar");
			aux=teclado.next();
			env.setEnvConfig(aux);
			if((res2=env.checkEnvioment())==true){
				System.out.println("El ambiente no existe, favor selecionar otro");
			}
		}while(res2==true);
		System.out.println("Cargando ambiente");
		if(env.loadEnvioment()==true){
			System.out.println("El ambiente fue cargado con exito");
			cant=env.getCantSources();
			if(cant>0){
				res=env.connect();
				if(res==0){
					I5Source fnt = new I5Source();
					int aux2=0;
					do{
						System.out.println("Favor seleccione el fuente a manipular");
						for(int pos=0;pos<cant;pos++){
							fnt=env.getInfoSource(pos);
							System.out.println((pos+1)+"-"+fnt.member);
						}
						System.out.println("A-Agregar fuente");
						System.out.println((cant+1)+"-Salir");
						System.out.println("Favor seleccione el fuente al que debe trabajar:");
						aux=teclado.next();
						try{
							aux2=Integer.parseInt(aux);
						}catch(NumberFormatException e){
							aux2=0;
						}
						// Si la selección es numerica
						if(aux2>0 && aux2<=(cant)){
							fnt=env.getInfoSource(aux2-1);
							System.out.println("---------------------------------------");
							System.out.println("El fuente seleccionado es:"+fnt.member);
							System.out.println("1-Cargar fuente");
							System.out.println("2-Salvar fuente");
							System.out.println("3-Compilar fuente");
							System.out.println("4-Borrar fuente");
							System.out.println("Seleccione:");
							aux=teclado.next();
							int aux3=0;
							try{
								aux3=Integer.parseInt(aux);
							}catch(NumberFormatException e){
								aux3=0;
							}
							switch(aux3){
								case 1:
									env.loadSource(aux2-1);
								break;
								case 2:
									env.saveSource(aux2-1);
								break;
								case 3:
									res3=env.compileSource(aux2-1);
								break;
								case 4:
									env.deleteSource(aux2-1);
									env.saveEnvioment();
									env.loadEnvioment();
									cant=env.getCantSources();
									aux2=0;
								break;
							}
							System.out.println(env.getMessage());
						}
						// si la selección es texto
						if(aux.equals("A")||aux.equals("a")){
							System.out.println("A continuación coloque el fuente a ingresar");
							System.out.println("Ingrese la libreria:");
							String libreria=teclado.next();
							System.out.println("Ingrese el archivo");
							String archivo=teclado.next();
							System.out.println("Ingrese el miembro");
							String miembro=teclado.next();
							System.out.println("Ingrese el tipo");
							String tipo=teclado.next();
							res2=env.addSource(libreria,archivo,miembro,tipo);
							if(res2==false){
								System.out.println("No se puede crear el fuente, intente con otro");
							}else{
								System.out.println("El fuente fue agregado.");
								env.saveEnvioment();
								env.loadEnvioment();
								cant=env.getCantSources();
								
							}
						}
					}while(aux2!=cant+1);
				}else{
					System.out.println("Existen problemas en la conexión");
				}
				env.disconnect();
			}
		}else{
			System.out.println("El archivo de configuración tiene problemas");
		}
		
		
	}
}
