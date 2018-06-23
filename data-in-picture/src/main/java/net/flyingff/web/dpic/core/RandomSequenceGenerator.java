package net.flyingff.web.dpic.core;

public interface RandomSequenceGenerator {
	byte[] generate(String password, int len);
}
