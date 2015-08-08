package com.linuxense.javadbf;

public enum DBFDataType {
	UNKNOWN         ((byte) 0),
	CHARACTER       ((byte) 'C'), 
	DATE            ((byte) 'D'), 
	FLOATING_POINT  ((byte) 'F'), 
	LOGICAL         ((byte) 'L'), 
	MEMO            ((byte) 'M'), 
	NUMERIC         ((byte) 'N');

	private byte code;

	private DBFDataType(byte code) {
		this.code = code;
	}
	
	public byte getCode() {
		return this.code;
	}
	
	public static DBFDataType fromCode(byte cod) {
		for (DBFDataType type: values()) {
			if (cod == type.code){
				return type;
			}
		}
		System.out.println("Unknown data type:" + cod);
		System.out.println("Unknown data type:" + (char) cod);
		throw new IllegalArgumentException("Unknown data type:" + cod);
	}
	

}