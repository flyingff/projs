package net.flyingff.digitalspring.lt;

public class LTCoderFactory {
	private int dropLen = 256;
	public int getDropLen() {
		return dropLen;
	}
	public LTCoderFactory setDropLen(int dropLen) {
		this.dropLen = dropLen;
		return this;
	}
	public Encoder buildEnocder(){
		return new Encoder(dropLen);
	}
	
	public Decoder buildDecoder(){
		return new Decoder(dropLen);
	}
}
