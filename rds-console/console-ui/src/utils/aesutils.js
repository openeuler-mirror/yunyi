import CryptoJS from 'crypto-js'
export default {//加密

	//首先声明两个变量，加密的时候要用到，要和后台沟通，保持一致

  encrypt(params){
	  var AES_KEY = 'hj7x89H$yuBI0456';
	  var IV = 'NIfb&95GUY86Gfgh';
    var key = CryptoJS.enc.Utf8.parse(AES_KEY);
    var iv = CryptoJS.enc.Utf8.parse(IV);
    var jsonData = JSON.stringify(params); //下面的data参数要求是一个字符串，第一次用的时候我直接传递的是一个对象，出现了错误，要转换成字符串
    var data = CryptoJS.enc.Utf8.parse(params);
    var encrypted = CryptoJS.AES.encrypt(data, key, { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.ZeroPadding });
    return CryptoJS.enc.Base64.stringify(encrypted.ciphertext);
  },
  //解密
  decrypt(word){
	  var AES_KEY = 'hj7x89H$yuBI0456';
	  var IV = 'NIfb&95GUY86Gfgh';
	  var key = CryptoJS.enc.Utf8.parse(AES_KEY);
    var iv = CryptoJS.enc.Utf8.parse(IV);
    var base64 = CryptoJS.enc.Base64.parse(word);
    var src = CryptoJS.enc.Base64.stringify(base64);
	  var decrypt = CryptoJS.AES.decrypt(src, key, {iv:iv,mode:CryptoJS.mode.CBC,padding: CryptoJS.pad.ZeroPadding});
	  var decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
	  return decryptedStr.toString();
  }

}
