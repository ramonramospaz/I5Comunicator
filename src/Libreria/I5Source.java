package Libreria;

public class I5Source {
	public String member;
	public String file;
	public String library;
	public String type;
	public int	registry_width;
	public boolean equals(Object o){
		boolean returns=false;
		I5Source source = (I5Source)o;
		if(this.member==source.member && this.file==source.file && this.library==source.library &&
				this.type==source.type && this.registry_width==source.registry_width){
			returns=true;
		}
		return returns;
	}
}
