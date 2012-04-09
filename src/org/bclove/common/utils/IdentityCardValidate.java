package org.bclove.common.utils;

/**
 * 身份证识别工具类
 * @author soryokurin
 *
 */
public class IdentityCardValidate {
	
	private static final int[] wi = {7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2,1}; //wi = 2(n - 1)(mod 11)
	
	private static final int[] vi = {1,0,'X',9,8,7,6,5,4,3,2};
	
	public static boolean isEmpty(String identityCard){
		if(identityCard == null || "".equals(identityCard)){
			return true;
		}
		else{
			return false;
		}
	}
	
	public static boolean verifyLength(String identityCard){
		if(identityCard.length() == 15 || identityCard.length() == 18){
			return true;
		}
		else{
			return false;
		}
	}
	
	public static boolean haslawlessWord(String identityCard){
		if(identityCard.length() == 18){
			identityCard = identityCard.substring(0,17);
		}
		for(int i = 0;i < identityCard.length();i++){
			char c = identityCard.charAt(i);
			if(c < 48 || c > 57){
				return true;
			}
		}
		return false;
	}
	
	public static boolean verifyFormat(String identityCard){
		if(identityCard.length() != 15 && identityCard.length() != 18){
			return false;
		}
		if(identityCard.length() == 15){
			identityCard = upToEighteen(identityCard);
		}
		String birthYear = identityCard.substring(6,10);
		//System.out.println("birthYear = " + birthYear);
		String birthMonth = identityCard.substring(10,12);
		//System.out.println("birthMonth = " + birthMonth);
		String birthDay = identityCard.substring(12,14);
		//System.out.println("birthDay = " + birthDay);
		if(Integer.parseInt(birthYear) < 1949){
			return false;
		}
		if(Integer.parseInt(birthMonth) > 12 || Integer.parseInt(birthMonth) < 1){
			return false;
		}
		if(Integer.parseInt(birthDay) > 31 || Integer.parseInt(birthDay) < 1){
			return false;
		}
		String verifyCode = identityCard.substring(17);
		verifyCode = verifyCode.toUpperCase();
		if(verifyCode.equals(getVerify(identityCard))){
			return true;
		}
		else{
			return false;
		}
	}
	
	public static String getVerify(String eightcardid) { 
		int remaining = 0; 

		if (eightcardid.length() == 18) { 
			eightcardid = eightcardid.substring(0, 17); 
		} 


		if (eightcardid.length() == 17) { 
			int sum = 0; 

			for (int i = 0; i < 17; i++) {
				String k = eightcardid.substring(i, i + 1); 
				sum = sum + wi[i] * Integer.parseInt(k); 
			} 
			remaining = sum % 11; 
		}
		return remaining == 2?"X":String.valueOf(vi[remaining]);
	}
	
	public static String upToEighteen(String fifteenCardId){
		String eightCardId = fifteenCardId.substring(0,6);
		eightCardId = eightCardId + "19";
		eightCardId = eightCardId + fifteenCardId.substring(6,15);
		eightCardId = eightCardId + getVerify(eightCardId);
		return eightCardId;
	}
	
	public static void main(String[] args){
		String idCard = "231004198807230962";
		if(IdentityCardValidate.isEmpty(idCard)){
			System.out.println("idCard is empty");
		}
		else if(!IdentityCardValidate.verifyLength(idCard)){
			System.out.println("idCard is error length:" + idCard.length());
		}
		else if(IdentityCardValidate.haslawlessWord(idCard)){
			System.out.println("idCard has lawless word");
		}
		else if(!IdentityCardValidate.verifyFormat(idCard)){
			System.out.println("idCard is error format");
		}
		else{
			System.out.println("idCard is right");
		}
		System.out.println("idCard = " + idCard);
	}
}
