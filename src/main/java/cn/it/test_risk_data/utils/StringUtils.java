/*
package cn.it.test_risk_data.utils;

import com.sinaif.king.common.ErrorCode;
import com.sinaif.king.model.common.DetailAddressModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

*/
/**
 * @Description : 对字符串进行操作
 *
 *//*

public class StringUtils {

	private static Logger logger = LoggerFactory.getLogger(StringUtils.class);

	// 用于匹配手机号码
	private final static String REGEX_MOBILEPHONE = "^0?1[34578]\\d{9}$";

	// 用于匹配固定电话号码
	private final static String REGEX_FIXEDPHONE = "^(010|02\\d|0[3-9]\\d{2})?\\d{6,8}$";
	// 判断url地址
	private final static String REGEX_HTTPURL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";

	private static Pattern PATTERN_MOBILEPHONE;
	private static Pattern PATTERN_FIXEDPHONE;
	private static Pattern PATTERN_URL;

	static {
		PATTERN_FIXEDPHONE = Pattern.compile(REGEX_FIXEDPHONE);
		PATTERN_MOBILEPHONE = Pattern.compile(REGEX_MOBILEPHONE);
		PATTERN_URL = Pattern.compile(REGEX_HTTPURL);
	}

	*/
/** 对字符串的首字母大写转小写 *//*

	public static String firstStrToLowerCase(final String str) {
		char[] chars = new char[1];
		chars[0] = str.charAt(0);
		String temp = new String(chars);
		if (chars[0] >= 'A' && chars[0] <= 'Z') {// 当为字母时，则转换为小写
			return str.replaceFirst(temp, temp.toLowerCase());
		}
		return str;
	}

	*/
/**
	 * 判断姓名是否正确（2到15位汉字）
	 * 
	 * @param name
	 *            姓名
	 * @return
	 *//*

	public static boolean checkChineseName(String name) {
		if (isEmpty(name)) {// 空
			return false;
		}
		if (name.matches("^[\\u4e00-\\u9fa5·•]{2,15}")) {
			return true;
		} else {
			return false;
		}
	}

	*/
/**
	 * 判断是否为存数字
	 * 
	 * @param str
	 * @return
	 *//*

	public static boolean isNumeric(String str) {
		if (isEmpty(str)) {// 空
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	*/
/**
	 * 判断是空：包括null，空。参考示例：<br>
	 * 
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty("")        = true
	 * StringUtils.isEmpty(" ")       = false
	 * StringUtils.isEmpty("bob")     = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * </pre>
	 *//*

	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static boolean isEmpty(Object str) {
		return (str == null || "".equals(str));
	}

	*/
/**
	 * 判断非空：包括null，空。与isEmpty()正好相反。参考示例：<br>
	 * 
	 * <pre>
	 * StringUtils.isNotEmpty(null)      = false
	 * StringUtils.isNotEmpty("")        = false
	 * StringUtils.isNotEmpty(" ")       = true
	 * StringUtils.isNotEmpty("bob")     = true
	 * StringUtils.isNotEmpty("  bob  ") = true
	 * </pre>
	 *//*

	public static boolean isNotEmpty(final CharSequence cs) {
		return !isEmpty(cs);
	}

	*/
/**
	 * 判断是空：包括null，空，空格。参考示例：<br>
	 * 
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *//*

	public static boolean isBlank(final CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	*/
/**
	 * 判断非空：包括null，空，空格。与isNotBlank()正好相反。参考示例：<br>
	 * 
	 * <pre>
	 * StringUtils.isNotBlank(null)      = false
	 * StringUtils.isNotBlank("")        = false
	 * StringUtils.isNotBlank(" ")       = false
	 * StringUtils.isNotBlank("bob")     = true
	 * StringUtils.isNotBlank("  bob  ") = true
	 * </pre>
	 *//*

	public static boolean isNotBlank(final CharSequence cs) {
		return !isBlank(cs);
	}

	*/
/**
	 * 判断是否为手机号码
	 * 
	 * @param number
	 *            手机号码
	 * @return true为手机号码
	 *//*

	public static boolean isCellPhone(String number) {
		if (!isEmpty(number)) {
			number = replacePhoneParam(number);
		}
		Matcher match = PATTERN_MOBILEPHONE.matcher(number);
		return match.matches();
	}

	*/
/**
	 * 判断url是否正确
	 * 
	 * @param url
	 * @return
	 *//*

	public static boolean chekUrl(String url) {
		Matcher match = PATTERN_URL.matcher(url);
		return match.matches();
	}

	*/
/**
	 * 去除号码的不必要的值
	 * 
	 * @param phone
	 * @return
	 *//*

	public static String replacePhoneParam(String phone) {
		if (!isEmpty(phone)) {
			phone = phone.replaceAll(" ", "").replaceAll("-", "");
			// 去除0开头的号码
			if (phone.startsWith("0")) {
				phone = phone.substring(1, phone.length());
				// 把前面的0全部去掉
				phone = replacePhoneParam(phone);
			}
			if (phone.startsWith("86")) {
				phone = phone.substring(2, phone.length());
			}
			if (phone.startsWith("+86")) {
				phone = phone.substring(3, phone.length());
			}
			if (phone.startsWith("0086")) {
				phone = phone.substring(4, phone.length());
			}
			if (phone.startsWith("+0086")) {
				phone = phone.substring(5, phone.length());
			}
		}
		return phone;
	}

	*/
/**
	 * 判断是否为固定电话号码
	 * 
	 * @param number
	 *            固定电话号码
	 * @return true为固话
	 *//*

	public static boolean isFixedPhone(String number) {
		if (!isEmpty(number)) {
			number = number.replaceAll(" ", "").replaceAll("-", "");
		}
		Matcher match = PATTERN_FIXEDPHONE.matcher(number);
		return match.matches();
	}

	*/
/**
	 * 判断是否包含的某些字符的
	 * 
	 * @param ls
	 * @param str
	 * @return
	 *//*

	public static boolean isExistType(List<String> ls, String str) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}
		for (String s : ls) {
			Pattern pattern = Pattern.compile(s);
			Matcher matcher = pattern.matcher(str);
			if (matcher.find()) {
				return true;
			}
		}
		return false;
	}

	*/
/**
	 * 队列比较
	 * 
	 * @return false，则不相同，true全部相同
	 *//*

	public static <T> boolean compareListSame(List<T> a, List<T> b) {
		if (a == null || b == null) {
			return false;
		}
		if (a.size() != b.size()) {
			return false;
		}
		int same = 0;
		// 循环判断
		for (int i = 0; i < a.size(); i++) {
			for (int j = 0; j < b.size(); j++) {
				if (a.get(i).hashCode() == b.get(j).hashCode()) {
					same++;
					break;
				}
			}
		}
		if (same != a.size()) {
			return false;
		}
		return true;
	}

	*/
/**
	 * 根据身份证号码判断获取用户的年龄
	 * 
	 * @param idCard
	 * @return int
	 *//*

	public static int idCardToAge(String idCard) {
		if (isEmpty(idCard)) {
			return 0;
		}
		int leh = idCard.length();
		String dates = "";
		if (leh == 18) {
			// int se = Integer.valueOf(idCard.substring(leh - 1)) % 2;
			dates = idCard.substring(6, 10);
			String month = idCard.substring(10, 14);
			SimpleDateFormat df = new SimpleDateFormat("yyyy");
			SimpleDateFormat dm = new SimpleDateFormat("MMdd");
			String year = df.format(new Date());
			int u = Integer.parseInt(year) - Integer.parseInt(dates);
			String thisMonth = dm.format(new Date());
			String birthMonth = dm.format(DateUtils.format(month, "MMdd"));
			// 如果未到出生月份，则age - 1
			if (thisMonth.compareTo(birthMonth) < 0) {
				u -= 1;
			}
			return u;
		} else {
			dates = idCard.substring(6, 8);
			return Integer.parseInt(dates);
		}
	}

	*/
/**
	 * 根据身份证号码判断年份
	 * 
	 * @param idCard
	 * @return int
	 *//*

	public static int idCardToYear(String idCard) {
		if (isEmpty(idCard)) {
			return 0;
		}
		int leh = idCard.length();
		String year = "";
		if (leh == 18) {
			// int se = Integer.valueOf(idCard.substring(leh - 1)) % 2;
			year = idCard.substring(6, 10);
		} else if (leh == 15) {
			// 只有1980年以前才存在15位身份证
			year = "19" + idCard.substring(6, 8);
		} else {
			return 0;
		}
		return Integer.parseInt(year);
	}
	
	*/
/**
	 * 根据身份证号码判断年份
	 * 
	 * @param idCard
	 * @return int
	 *//*

	public static String idCardToBirthday(String idCard) {
		String birthday = "";
		if (isEmpty(idCard)) {
			return "";
		}
		if (idCard.length() == 18) {
			birthday = idCard.substring(6, 14);
		}
		if (!isEmpty(birthday)){
			Date date = DateUtils.string2DateSuper(birthday);
			birthday = DateUtils.dateToString(date, DateUtils.DATE_FORMAT_4);
		}
		return birthday;
	}

	*/
/**
	 * 获取文件内容
	 * 
	 * @param in
	 * @param encode
	 * @return
	 *//*

	public static String getContent(InputStream in, String encode) {
		String mesage = "";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			int len = 0;
			byte[] data = new byte[1024];
			while ((len = in.read(data)) != -1) {
				outputStream.write(data, 0, len);
			}
			mesage = new String(outputStream.toByteArray(), encode);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mesage;
	}
	
	*/
/**
	 * @Description : 回调返回结果
	 * @author : 陈惟鲜 danger
	 * @Date : 2019年7月8日 上午11:09:36
	 * @param result 返回内容
	 * @param response 
	 *//*

	public static void sendResponse(String msgInfo, String result, HttpServletResponse response){
		try {
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			OutputStream ps = response.getOutputStream();
			// 这句话的意思，使得放入流的数据是utf8格式
			ps.write(result.getBytes("UTF-8"));
		} catch (IOException e) {
			logger.error(msgInfo + "返回信息异常" + e.getMessage(), e);
		}
	}

	*/
/**
	 * 获取URL的文件名称
	 * 
	 * @param url
	 * @return
	 *//*

	public static String getUrlName(String url) {
		String name = HNUtil.getId() + Thread.currentThread().getId();
		if (!StringUtils.isEmpty(url)) {
			String[] parts = url.split("/");
			if (parts != null) {
				name = name + parts[parts.length - 1];
			}
		}
		return name;
	}

	*/
/**
	 * 获取URL的文件名称
	 *
	 * @param productid
	 * @param url
	 * @return
	 *//*

	public static String getUrlNameWithProductId( String productid, String url) {
		String name = productid;
		if (!StringUtils.isEmpty(url)) {
			String[] parts = url.split("/");
			if (parts != null) {
				name = name + parts[parts.length - 1];
			}
		}
		return name;
	}

	*/
/**
	 * 下载文件到本地
	 * 
	 * @param urlString
	 *            被下载的文件地址
	 * @param filename
	 *            本地文件名
	 * @throws Exception
	 *             各种异常
	 *//*

	public static File downloadFile(String urlString) {
		String filePath = "";
		InputStream is = null;
		FileOutputStream outputStream = null;
		try {
			long time = System.currentTimeMillis();
			logger.info(String.format("下载文件[%s]开始了", urlString));
			String folder = System.getProperty("java.io.tmpdir");
			filePath = folder + File.separator + getUrlName(urlString);

			if (urlString.startsWith("https")) {
				TrustManager[] tm = { new MyX509TrustManager() };
				SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
				sslContext.init(null, tm, new java.security.SecureRandom());
				// 从上述SSLContext对象中得到SSLSocketFactory对象
				SSLSocketFactory ssf = sslContext.getSocketFactory();
				// 创建URL对象
				URL myURL = new URL(urlString);
				// 创建HttpsURLConnection对象，并设置其SSLSocketFactory对象
				HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
				httpsConn.setSSLSocketFactory(ssf);

				outputStream = new FileOutputStream(filePath);
				// 输入流
				is = httpsConn.getInputStream();
				int len = 0;
				byte[] data = new byte[1024];
				while ((len = is.read(data)) != -1) {
					outputStream.write(data, 0, len);
				}
			} else {
				// 构造URL
				URL url = new URL(urlString);
				URLConnection con = url.openConnection();
				outputStream = new FileOutputStream(filePath);
				// 输入流
				is = con.getInputStream();
				int len = 0;
				byte[] data = new byte[1024];
				while ((len = is.read(data)) != -1) {
					outputStream.write(data, 0, len);
				}
			}

			logger.info(String.format("下载文件[%s]结束了花费时间(毫秒)：[%s]，下载文件：%s", urlString,
					(System.currentTimeMillis() - time), filePath));
			if (new File(filePath).length() <= 0) {
				throw new RuntimeException("文件大小异常=================");
			}

		} catch (FileNotFoundException e) {
			logger.error("文件未找到异常：", e);
			throw new RuntimeException(ErrorCode.UPLOAD_FILE_NOT_FOUND + "", e);
		} catch (Exception e) {
			logger.error("error", e);
			throw new RuntimeException("文件下载异常=================");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new File(filePath);
	}

	*/
/**
	 * 下载文件到本地完整下载
	 *
	 * @param urlString
	 *            被下载的文件地址
	 * @throws Exception
	 *             各种异常
	 *//*

	public static File downloadFileFull(String urlString) {
		String filePath = "";
		InputStream is = null;
		FileOutputStream outputStream = null;
		try {
			long time = System.currentTimeMillis();
			logger.info(String.format("下载文件[%s]开始了", urlString));
			String folder = System.getProperty("java.io.tmpdir");
			filePath = folder + File.separator + getUrlName(urlString);
			// 文件流大小
			int requestCount = 0;
			// 读取文件大小
			int readCount = 0;

			if (urlString.startsWith("https")) {
				TrustManager[] tm = { new MyX509TrustManager() };
				SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
				sslContext.init(null, tm, new java.security.SecureRandom());
				// 从上述SSLContext对象中得到SSLSocketFactory对象
				SSLSocketFactory ssf = sslContext.getSocketFactory();
				// 创建URL对象
				URL myURL = new URL(urlString);
				// 创建HttpsURLConnection对象，并设置其SSLSocketFactory对象
				HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
				httpsConn.setSSLSocketFactory(ssf);

				outputStream = new FileOutputStream(filePath);
				// 输入流
				requestCount = httpsConn.getContentLength();
				is = httpsConn.getInputStream();
				int len = 0;
				byte[] data = new byte[1024];
				while ((len = is.read(data)) != -1) {
					readCount = readCount + len;
					outputStream.write(data, 0, len);
				}
			} else {
				// 构造URL
				URL url = new URL(urlString);
				URLConnection con = url.openConnection();
				outputStream = new FileOutputStream(filePath);
				// 输入流
				requestCount = con.getContentLength();
				is = con.getInputStream();
				int len = 0;
				byte[] data = new byte[1024];
				while ((len = is.read(data)) != -1) {
					readCount = readCount + len;
					outputStream.write(data, 0, len);
				}
			}

			logger.info(MarkerFactory.getMarker("[specific_log]"),"下载文件【"+urlString+"】读取【"+readCount+"】与传输【"+requestCount+"】，下载"+(requestCount != readCount?"不":"")+"完整");
//			if (requestCount != readCount) {
//
//			}

			if (new File(filePath).length() <= 0) {
				throw new RuntimeException("文件大小异常");
			}

		} catch (FileNotFoundException e) {
			logger.error("文件未找到异常：", e);
			throw new RuntimeException(ErrorCode.UPLOAD_FILE_NOT_FOUND + "", e);
		} catch (Exception e) {
			logger.error("error", e);
			throw new RuntimeException("文件下载异常，原因：" + e.getMessage());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new File(filePath);
	}

	*/
/**
	 * 根据用户名的不同长度，来进行替换 ，达到保密效果
	 *
	 * @param userName
	 *            用户名
	 * @return 替换后的用户名
	 *//*

	public static String userNameReplace(String userName) {

		if (userName == null) {
			userName = "";
		}
		int nameLength = userName.length();
		if (nameLength <= 1) {
		} else if (nameLength == 2) {
			userName = userName.substring(0, 1) + "*";
		} else if (nameLength >= 3) {
			userName = userName.substring(0, 1) + "*" + userName.substring(userName.length() - 1, userName.length());
		}
		return userName;
	}

	*/
/**
	 * 从地址中截取城市名
	 * 
	 * @param area
	 * @return
	 *//*

	public static String getCityName(String area) {
		if (!StringUtils.isEmpty(area)) {
			try {
				int index = area.indexOf("省");
				if (index == -1) {
					index = area.indexOf("自治区");
				}
				int endIndex = area.indexOf("市", index < 0 ? 0 : index);
				if (endIndex <= index) {
					if (index < 0) {
						area = area.substring(0);
					} else {
						area = area.substring(index + 1);
					}
				} else {
					if (index < 0) {
						area = area.substring(0, endIndex);
					} else {
						area = area.substring(index + 1, endIndex);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return area;
	}

	*/
/**
	 * 替换xml中不能使用的特殊字符
	 * 
	 * @param txt
	 * @return
	 *//*

	public static String filterSpecialCharOfXml(String txt) {
		if (isEmpty(txt)) {
			return "";
		}
		String res = "";
		for (int i = 0; i < txt.length(); ++i) {
			char ch = txt.charAt(i);
			if (Character.isDefined(ch) && ch != '&' && ch != '<' && ch != '>' && ch != '\'' && ch != '\"'
					&& !Character.isHighSurrogate(ch) && !Character.isISOControl(ch) && !Character.isLowSurrogate(ch)) {
				res = res + ch;
			}
		}
		return res;
	}

	*/
/**
	 * 替换模板内容中的关键字
	 * 
	 * @param tmplcontent
	 * @param paramMap
	 * @return
	 *//*

	public static String replaceKeyword(String tmplcontent, Map<String, String> paramMap) {
		if (StringUtils.isEmpty(tmplcontent)) {
			return "";
		}
		if (paramMap == null || paramMap.isEmpty()) {
			return tmplcontent;
		}
		// 替换[关键字]，组装真实文本内容
		for (Entry<String, String> entry : paramMap.entrySet()) {
			if (StringUtils.isEmpty(entry.getKey())) {
				continue;
			}
			String searchKeyA = "#" + entry.getKey() + "#";// 格式：#code#
			if (tmplcontent.indexOf(searchKeyA.toLowerCase()) != -1) {
				tmplcontent = tmplcontent.toLowerCase().replaceAll(escapeExprSpecialWord(searchKeyA.toLowerCase()),
						entry.getValue());
			}
			String searchKeyB = "${" + entry.getKey() + "}";// 格式：${code}
			if (tmplcontent.indexOf(searchKeyB.toLowerCase()) != -1) {
				tmplcontent = tmplcontent.toLowerCase().replaceAll(escapeExprSpecialWord(searchKeyB.toLowerCase()),
						entry.getValue());
			}
		}
		return tmplcontent;
	}

	*/
/**
	 * 转义正则特殊字符：\$()*+.[]?^{},|
	 * 
	 * @param keyword
	 * @return
	 *//*

	public static String escapeExprSpecialWord(String keyword) {
		if (StringUtils.isEmpty(keyword)) {
			return "";
		}
		String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
		for (String key : fbsArr) {
			if (keyword.contains(key)) {
				keyword = keyword.replace(key, "\\" + key);
			}
		}
		return keyword;
	}

	*/
/**
	 * 获取随机数
	 * 
	 * @param startNum
	 * @param endNum
	 * @return
	 *//*

	public static Integer getRandomNum(Integer startNum, Integer endNum) {
		Random rand = new Random();
		return rand.nextInt(endNum) + startNum;
	}

	*/
/**
	 * 对象转map
	 * 
	 * @param startNum
	 * @param endNum
	 * @return
	 *//*

	public static Map<String, Object> objectToMap(Object obj) throws Exception {
		if (obj == null) {
			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();

		Field[] declaredFields = obj.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			field.setAccessible(true);
			map.put(field.getName(), field.get(obj));
		}

		return map;
	}

	*/
/**
	 * 邮箱校验
	 * 
	 * @param email
	 * @return
	 *//*

	public static boolean checkEmail(String email) {
		String regex = "^\\w+(\\.\\w+)*@\\w+((\\.\\w+)+)$";
		return Pattern.matches(regex, email);
	}

	*/
/**
	 * 过滤表情符号及特殊字符
	 * 
	 * @param str
	 * @return
	 *//*

	public static String filterEmojiSpecialChar(String str) {
		if (str.trim().isEmpty()) {
			return str;
		}
		String regex = "[^·•a-zA-Z0-9\u4E00-\u9FA5]";
		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(str);
		str = match.replaceAll("");
		return str;
	}

	*/
/**
	 * 在URL之后追加请求参数
	 *//*

	public static String urlAppend(String url, String value) {
		if (isBlank(url) || isBlank(value)) {
			return url;
		}
		// 在URL后面拼接请求参数
		if (url.indexOf('?') == -1) {
			return new String(url + "?" + value);
		} else {
			return new String(url + "&" + value);
		}
	}

	*/
/** 替换指定的关键字 *//*

	public static String replaceKey(String url, String key, String value) {
		if (isBlank(url)) {
			return "";
		}
		if (isBlank(key)) {
			return url;
		}
		if (url.indexOf(key) != -1) {
			return new String(url.replaceAll(escapeExprSpecialWord(key), value == null ? "" : value));
		}
		return url;
	}

	*/
/**
	 * 返回随机数
	 * 
	 * @param str
	 * @return
	 *//*

	public static int getRandomIndex(int total) {
		int index = (int) (Math.random() * total);
		return index;
	}

	*/
/**
	 * @param url
	 * @param params
	 * @return
	 *//*

	public static String urlAppendParams(String url, Map<String, String> params) {
		if (null == params || params.isEmpty()) {
			return url;
		}
		StringBuilder sb = new StringBuilder(url);
		if (url.indexOf("?") == -1) {
			sb.append("?");
		} else {
			sb.append("&");
		}
		boolean first = true;
		for (Entry<String, String> entry : params.entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append("&");
			}
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key).append("=");
			if (StringUtils.isNotEmpty(value)) {
				sb.append(value);
			}
		}
		return sb.toString();
	}

	*/
/**
	 * 过滤表情符号及特殊字符
	 *
	 * @param str
	 * @return
	 *//*

	public static boolean compleRegex(String str,String regex) {

		try {
			if (str.trim().isEmpty()) {
				return false;
			}

			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(str);
			// 字符串是否与正则表达式相匹配
			boolean rs = matcher.matches();

			return rs;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}

	}
	
	*/
/**
	 * 解析地址省市区
	 * @param address
	 * @return
	 *//*

	public static DetailAddressModel analysisDetailAddress(String address) {
		String regex="(?<province>[^省]+自治区|.*?省|.*?行政区|.*?市)(?<city>[^市]+自治州|.*?地区|.*?行政单位|.+盟|市辖区|.*?市|.*?县)(?<district>[^县]+县|.+区|.+市|.+旗|.+海域|.+岛)?(?<detail>.*)";
        Matcher m=Pattern.compile(regex).matcher(address);
        DetailAddressModel detailAddress = null;
        while(m.find()){
        	detailAddress = new DetailAddressModel();
        	detailAddress.setProvince(m.group("province"));
        	detailAddress.setCity(m.group("city"));
        	detailAddress.setDistrict(m.group("district"));
        	detailAddress.setDetail(m.group("detail"));
        }
        
        return detailAddress;
	}

	*/
/**
	 * 获取身份证掩码，隐藏后5位
	 * @param idCard
	 * @return
	 *//*

	public static String getIdCardMask(String idCard){
		String str = idCard.substring(0, idCard.length() - 5);
		return str + "*****";
	}

	*/
/**
	 * 获取手机号掩码，隐藏后4位
	 * @param phoneNumber
	 * @return
	 *//*

	public static String getPhoneNumberMask(String phoneNumber){
		String str = phoneNumber.substring(0, phoneNumber.length() - 4);
		return str + "****";
	}
	
	*/
/**
	 * 截取过长的日志
	 * @param logStr
	 * @return
	 *//*

	public static String subStr4Log(String logStr) {
		if (logStr == null) {
			return logStr;
		}
		
		int strLength = logStr.length();
		if (strLength > 1000) {
			return logStr.substring(0, 1000);
		}
		
		return logStr;
	}
	

	
	*/
/**
	 * @Description : 压缩信息
	 * @author : 陈惟鲜 danger
	 * @Date : 2018年11月8日 下午6:00:01
	 * @param data
	 * @return
	 * @throws Exception
	 *//*

	public static byte[] gzipString(byte[] data) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		gzip.write(data);
		gzip.finish();
		gzip.close();
		byte[] ret = bos.toByteArray();
		bos.close();
		return ret;
	}
 
	*/
/**
	 * @Description : 解压信息
	 * @author : 陈惟鲜 danger
	 * @Date : 2018年11月8日 下午6:00:13
	 * @param data
	 * @return
	 * @throws Exception
	 *//*

	public static byte[] ungzipString(byte[] data) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		GZIPInputStream gzip = new GZIPInputStream(bis);
		byte[] buf = new byte[1024];
		int num = -1;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((num = gzip.read(buf, 0, buf.length)) != -1) {
			bos.write(buf, 0, num);
		}
		gzip.close();
		bis.close();
		byte[] ret = bos.toByteArray();
		bos.flush();
		bos.close();
		return ret;
	}

	public static boolean checkRegex(String regex,String checkStr) {
		return Pattern.matches(regex, checkStr);
	}
	
	*/
/**
	 * 秒下款产品，获取申请的百分比
	 * @param min 最小初始值
	 * @param max 最大值
	 * @param minstep 没分上浮值
	 * @return
	 *//*

	public static float getApplyPercent(Float min, Float max, Float minstep) {
		// 获取秒数
		int differentSeconds = DateUtils.differentTime(DateUtils.getStartDate(new Date()), new Date());

		float currentPercent = min + minstep * (differentSeconds / 60);
		
		logger.info("getApplyPercent min:{},max:{},minstep:{},currentPercent:{}", min, max, minstep, currentPercent);
		if (currentPercent < max) {
			return currentPercent;
		} else {
			return max;
		}
	}
	
	
	public static String getSubString(String msg,String defaultmsg,int length)
	{
		if(msg==null)
			return defaultmsg;
		
		return msg.length()>=length?msg.substring(0,length):msg;
	}
	public static void main(String[] args) {
//		Map<String,String> paramsMap=new HashMap<String,String>();
//		paramsMap.put("status", "1");
//		System.out.println(urlAppendParams("http://192.168.1.20:8084/pages/home/index.html#/level3",paramsMap));
		String idcard = "360111199507133038";
		String phone = "13755603945";
		String idCardMask = getIdCardMask(idcard);
		String phoneNumberMask = getPhoneNumberMask(phone);
		System.out.println(idCardMask);
		System.out.println(phoneNumberMask);
	}
	*/
/**
	 * 获取URL的文件名称
	 *
	 * @param url
	 * @return
	 *//*

	public static String getUrlName1(String url) {
		String name = HNUtil.getId() + Thread.currentThread().getId();
		if (!StringUtils.isEmpty(url)) {
			String[] parts = url.split("/");
			if (parts != null) {
				if(parts[parts.length - 1].contains("?")||parts[parts.length - 1].contains("}")){
					return name+SnowFlakeUtil.generateId();
				}
				name = name + parts[parts.length - 1];
			}
		}
		return name;
	}

	*/
/**
	 * 下载文件到本地
	 *
	 * @param urlString
	 *            被下载的文件地址
	 * @param filename
	 *            本地文件名
	 * @throws Exception
	 *             各种异常
	 *//*

	public static File downloadFile1(String urlString) {
		String filePath = "";
		InputStream is = null;
		FileOutputStream outputStream = null;
		try {
			long time = System.currentTimeMillis();
			logger.info(String.format("下载文件[%s]开始了", urlString));
			String folder = System.getProperty("java.io.tmpdir");
			filePath = folder + File.separator + getUrlName1(urlString);

			if (urlString.startsWith("https")) {
				TrustManager[] tm = { new MyX509TrustManager() };
				SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
				sslContext.init(null, tm, new java.security.SecureRandom());
				// 从上述SSLContext对象中得到SSLSocketFactory对象
				SSLSocketFactory ssf = sslContext.getSocketFactory();
				// 创建URL对象
				URL myURL = new URL(urlString);
				// 创建HttpsURLConnection对象，并设置其SSLSocketFactory对象
				HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
				httpsConn.setSSLSocketFactory(ssf);

				outputStream = new FileOutputStream(filePath);
				// 输入流
				is = httpsConn.getInputStream();
				int len = 0;
				byte[] data = new byte[1024];
				while ((len = is.read(data)) != -1) {
					outputStream.write(data, 0, len);
				}
			} else {
				// 构造URL
				URL url = new URL(urlString);
				URLConnection con = url.openConnection();
				outputStream = new FileOutputStream(filePath);
				// 输入流
				is = con.getInputStream();
				int len = 0;
				byte[] data = new byte[1024];
				while ((len = is.read(data)) != -1) {
					outputStream.write(data, 0, len);
				}
			}

			logger.info(String.format("下载文件[%s]结束了花费时间(毫秒)：[%s]，下载文件：%s", urlString,
					(System.currentTimeMillis() - time), filePath));
			if (new File(filePath).length() <= 0) {
				throw new RuntimeException("文件大小异常=================");
			}

		} catch (FileNotFoundException e) {
			logger.error("文件未找到异常：", e);
			throw new RuntimeException(ErrorCode.UPLOAD_FILE_NOT_FOUND + "", e);
		} catch (Exception e) {
			logger.error("error", e);
			throw new RuntimeException("文件下载异常=================");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new File(filePath);
	}

}
*/
