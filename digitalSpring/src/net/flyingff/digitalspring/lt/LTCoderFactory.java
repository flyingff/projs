package net.flyingff.digitalspring.lt;

public class LTCoderFactory {
	private int dropLen = 256;
	public int getDropLen() {
		return dropLen;
	}
	public void setDropLen(int dropLen) {
		this.dropLen = dropLen;
	}
	public Encoder build(byte[] data){
		return new Encoder(dropLen, data);
	}
	
	public Decoder build(){
		return new Decoder(dropLen);
	}
}
