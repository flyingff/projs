package net.flyingff.digitalspring.lt;

public class LTCoderFactory {
	private int dropLen = 256;
	public int getDropLen() {
		return dropLen;
	}
	public void setDropLen(int dropLen) {
		this.dropLen = dropLen;
	}
	public Encoder buildEnocder(){
		return new Encoder(dropLen);
	}
	
	public Decoder buildDecoder(){
		return new Decoder(dropLen);
	}
}
