package com.sunyard.ecm.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyRep;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * 反序列化漏洞
 * @author asus
 * 重写resolveClass方法，对可序列化对象进行白名单设置
 */
public class AntObjectInputStream extends ObjectInputStream {
	
	public AntObjectInputStream(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	/**
	 * 只允许反序列化SerialObject class
	 */
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		boolean result = false;
		if (desc.getName().equals(KeyRep.class.getName())) {
			result = true;
		}else if (!desc.getName().equals(KeyPair.class.getName())) {
			result = true;
		}else if (!desc.getName().equals(Key.class.getName())) {
			result = true;
		}else if (!desc.getName().equals(RSAPrivateKey.class.getName())) {
			result = true;
		}else if (!desc.getName().equals(RSAPublicKey.class.getName())) {
			result = true;
		}
		if(result){
			return super.resolveClass(desc);
		}else{
			throw new InvalidClassException("Unauthorized deserialization attempt", desc.getName());
		}
	}
}
