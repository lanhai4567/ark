/* DateUtils.java	@date 2009-5-31
 * @JDk 1.6
 * @encoding UTF-8
 * <p> 版权所有：宜通世纪
 * <p> 未经本公司许可，不得以任何方式复制或使用本程序任何部分 <p>
 */
package com.etone.ark.kernel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 提共对日期的一些操作的方法
 * @author WuHongyun
 */
public final class DateUtils {
	private static final Log logger = LogFactory.getLog(DateUtils.class);
	private static DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");
	private static DateFormat dateFormatyyyyMMDD = new SimpleDateFormat(
			"yyyy-MM-dd");

	private DateUtils() {
	}
	

	/**
	 * yyyy-MM-DD 转换后格式为：2012-03-08
	 * 
	 * @param date
	 * @return
	 */
	public static synchronized String convertDateToYYYYMMDDStr(Date date) {
		return dateFormatyyyyMMDD.format(date);
	}

	/**
	 * yyyy-MM-DD 转换后格式为：2012-03-08
	 * 
	 * @param date
	 * @return
	 */
	public static synchronized Date convertDateToYYYYMMDDDate(Date date) {
		try {
			return dateFormatyyyyMMDD.parse(dateFormatyyyyMMDD.format(date));
		} catch (ParseException e) {
			logger.error("时间转换出错：" + date, e);
		}
		return date;
	}

	public static synchronized Boolean compareToCurYYYYMMDDDateStr(Date oldDate) {
		String oldDateStr = convertDateToYYYYMMDDStr(oldDate);
		String currentDateStr = convertDateToYYYYMMDDStr(new Date());
		logger.info("上次执行日期：" + oldDateStr + ",当前日期：" + currentDateStr);
		if (oldDateStr.equals(currentDateStr)) {
			return true;
		}
		return false;
	}

	/**
	 * yyyy-MM-DD 转换后格式为：2012-03-08
	 * 
	 * @param date
	 * @return
	 */
	public static synchronized DateFormat getDateFormatyyyyMMDD() {
		return dateFormatyyyyMMDD;
	}

	/**
	 * 将Date类型的日期转换为String类型的日期
	 * 
	 * @param date
	 *            ate类型的日期
	 * @return String类型的日期
	 */
	public static synchronized String convertDateToString(Date date) {
		if(null == date) return "";
		return dateFormat.format(date);
	}

	/**
	 * 将String类型的日期转换为Date类型的日期
	 * 
	 * @param date
	 *            String类型的日期
	 * @return Date类型的日期
	 * @throws Exception
	 */
	public static synchronized Date convertStringToDate(String date)
			throws Exception {
		return dateFormat.parse(date);
	}

	/**
	 * 取得系统的当前时间
	 * 
	 * @return String类型的日期
	 */
	public static synchronized String getCurrentTime() {
		return dateFormat.format(new Date(System.currentTimeMillis()));
	}

	/**
	 * 取得系统的当前时间
	 * 
	 * @return String类型的日期
	 * @throws java.text.ParseException
	 * @throws java.text.ParseException
	 */
	public static synchronized Date getCurrentDateTime() throws ParseException {
		return dateFormat.parse(getCurrentTime());
	}

	/**
	 * 取得系统的当前时间
	 * 
	 * @return String类型的日期
	 */
	public static Date getCurrentDate() {
		return new Date(System.currentTimeMillis());
	}

	/**
	 * 取得系统当前时间的年份
	 * 
	 * @return 系统时间的当前年份
	 */
	public static int getCurrentYear() {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(new Date(System.currentTimeMillis()));

		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 取得这个时间的年份
	 * 
	 * @param date
	 *            时间
	 * @return 年份
	 */
	public static int getYear(Date date) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 取得系统当前时间的月份
	 * 
	 * @return
	 */
	public static int getCurrentMonth() {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(new Date(System.currentTimeMillis()));

		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * 取得这个时间的月份
	 * 
	 * @param date
	 *            时间
	 * @return 月份
	 */
	public static int getMonth(Date date) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * 取得这个时间的小时
	 * 
	 * @param date
	 *            时间
	 * @return 小时
	 */
	public static int getHour(Date date) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 取得这个时间的分钟
	 * 
	 * @param date
	 *            时间
	 * @return 分钟
	 */
	public static int getMinute(Date date) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		return calendar.get(Calendar.MINUTE);
	}

	/**
	 * 取得这个时间的毫秒
	 * 
	 * @param date
	 *            时间
	 * @return 毫秒
	 */
	public static int getMsecond(Date date) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		return calendar.get(Calendar.MILLISECOND);
	}

	/**
	 * 取得这个时间的秒
	 * 
	 * @param date
	 *            时间
	 * @return 秒
	 */
	public static int getSecond(Date date) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		return calendar.get(Calendar.SECOND);
	}

	/**
	 * 取得系统当前时间的日
	 * 
	 * @return
	 */
	public synchronized static int getCurrentDay() {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(new Date(System.currentTimeMillis()));

		return calendar.get(Calendar.DATE);
	}

	/**
	 * 取得这个时间的日
	 * 
	 * @param date
	 *            时间
	 * @return 日
	 */
	public synchronized static int getDay(Date date) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		return calendar.get(Calendar.DATE);
	}

	/**
	 * 取得系统当前时间的月份，一共有多少天
	 * 
	 * @return
	 */
	public static int getCurrentDayOfMonth() {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(new Date(System.currentTimeMillis()));

		return calendar.getActualMaximum(Calendar.DATE);
	}

	/**
	 * 指定年月，取得该月一共有多少天
	 * 
	 * @param year
	 *            年
	 * @param month
	 *            月
	 * @return 该月一共有多少天
	 */
	public static int getDayOfMonth(int year, int month) {
		Calendar calendar = new GregorianCalendar();

		calendar.set(Calendar.YEAR, year);
		/**
		 * Java月份才0开始算
		 */
		calendar.set(Calendar.MONTH, month - 1);

		return calendar.getActualMaximum(Calendar.DATE);
	}

	/**
	 * 把该date类型的时间变为0时0刻的时间
	 * 
	 * @param date
	 *            需要转变的时间
	 * @return 0时0刻的时间
	 */
	public static Date getDateStart(Date date) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime();
	}

	/**
	 * 把该date类型的时间变为23时59分59秒999毫秒的时间
	 * 
	 * @param date
	 *            需要转变的时间
	 * @return 0时0刻的时间
	 */
	public static Date getDateEnd(Date date) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);

		return calendar.getTime();
	}

	/**
	 * 把该date类型的时间增加n天
	 * 
	 * @param date
	 *            需要转变的时间
	 * @param amount
	 *            需要增加的天数
	 * @return 时间
	 */
	public static Date addDay(Date date, int amount) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		calendar.add(Calendar.DATE, amount);

		return calendar.getTime();
	}

	/**
	 * 把该date类型的时间的天设置为指定值
	 * 
	 * @param date
	 *            需要转变的时间
	 * @param amount
	 *            需要设置的值
	 * @return 时间
	 */
	public static Date setDay(Date date, int amount) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		calendar.set(Calendar.DATE, amount);

		return calendar.getTime();
	}

	/**
	 * 把该date类型的时间增加n个月
	 * 
	 * @param date
	 *            需要转变的时间
	 * @param amount
	 *            需要增加的天数
	 * @return 时间
	 */
	public static Date addMonth(Date date, int amount) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		calendar.add(Calendar.MONTH, amount);

		return calendar.getTime();
	}

	/**
	 * 把该date类型的时间的月份设置为指定值
	 * 
	 * @param date
	 *            需要转变的时间
	 * @param amount
	 *            需要设置的值
	 * @return 时间
	 */
	public static Date setMonth(Date date, int amount) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		calendar.set(Calendar.MONTH, amount - 1);

		return calendar.getTime();
	}

	/**
	 * 把该date类型的时间增加n年
	 * 
	 * @param date
	 *            需要转变的时间
	 * @param amount
	 *            需要增加的年数
	 * @return 时间
	 */
	public static Date addYear(Date date, int amount) {
		Calendar calendar = new GregorianCalendar();

		calendar.setTime(date);

		calendar.add(Calendar.YEAR, amount);

		return calendar.getTime();
	}
	
	/**
	 * 把该date类型的时间增加n个分钟
	 * 
	 * @param date
	 *            需要转变的时间
	 * @param amount
	 *            需要增加的天数
	 * @return 时间
	 */
	public static Date addMinute(Date date, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, minute);
		return calendar.getTime();
	}
	
	/**
	 * 把该date类型的时间增加n个分钟
	 * 
	 * @param date
	 *            需要转变的时间
	 * @param amount
	 *            需要增加的天数
	 * @return 时间
	 */
	public static Date addSecond(Date date,int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
 		calendar.add(Calendar.SECOND, second);
		return calendar.getTime();
	}

	/**
	 * 求两个时间之间的间隔
	 * 
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @return 两个时间之间的间隔天，以一个Date类型的list返回
	 * @throws Exception
	 */
	public static ArrayList<Date> getDateInterval(Date startDate, Date endDate)
			throws Exception {
		Calendar calendar = new GregorianCalendar();
		ArrayList<Date> reslut = new ArrayList<Date>();
		Date intervalDate = null;

		/**
		 * 当结束时间小时开始时间时
		 */
		if (endDate.compareTo(startDate) < 0)
			throw new Exception("求取时间间隔出错,结束时间小于开始时间！");
		/**
		 * 当结束时间大于等于开始时间时
		 */
		if (endDate.compareTo(startDate) >= 0) {
			/**
			 * 将时间设为整0时
			 */
			calendar.setTime(startDate);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			startDate = calendar.getTime();

			calendar.setTime(endDate);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			endDate = calendar.getTime();

			calendar.setTime(startDate);
			/**
			 * 不断地增加开始时间的天数，与结束的日期进行对比
			 */
			intervalDate = startDate;
			while (true) {
				reslut.add(intervalDate);

				calendar.add(Calendar.DATE, 1);

				intervalDate = calendar.getTime();

				if (endDate.compareTo(intervalDate) < 0)
					return reslut;
			}
		}

		return reslut;
	}

	/**
	 * 判断两个时间是否在同一天
	 * 
	 * @param Date
	 *            时间1
	 * @param otherDate
	 *            时间2
	 * @return 是否
	 */
	public static boolean isSameDay(Date date, Date otherDate) {
		Calendar calendar = new GregorianCalendar();
		boolean result = false;

		/**
		 * 将时间设为整0时
		 */
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		date = calendar.getTime();

		calendar.setTime(otherDate);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		otherDate = calendar.getTime();

		if (date.compareTo(otherDate) == 0)
			result = true;

		return result;
	}

	/**
	 * 比较传入时间和当前是否是同一天
	 * @param oldDate
	 * @param currentDate
	 * @return
	 */
	public static boolean compareDay(Date oldDate, Date currentDate) {
		String oldDateStr = convertDateToYYYYMMDDStr(oldDate);
		String currentDateStr = convertDateToYYYYMMDDStr(currentDate);
		logger.info("上次执行日期：" + oldDateStr + ",当前日期：" + currentDateStr);
		if (oldDateStr.equals(currentDateStr)) {
			return true;
		}
		return false;
	}
	
	public static String getCurrentTimeInMillisToString(){
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");		
		return dateFormat.format(Calendar.getInstance().getTimeInMillis());
	}
	
	/**
	 * 
	 * 功能描述：
	 * 比较 两个时间相差的分钟数
	 * @param beginTime 开始时间
	 * @param endTime 结束时间
	 * @return 返回两个时间相差的分钟数
	 * @author <a href="kuanghaibo@gmail.com">匡海波 </a>
	 * @version 1.0.0
	 * @since 1.0.0
	 * create on: 2012-9-29
	 */
	public static long  compareMinute(Date beginTime,Date endTime){
		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数   
		long nh = 1000 * 60 * 60;// 一小时的毫秒数   
		long nm = 1000 * 60;// 一分钟的毫秒数   
		long minute = 0;
		long diff = endTime.getTime() - beginTime.getTime(); 
		long day = diff / nd;// 计算差多少天   
		minute = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟   
		return minute;
	}
	
	/**
	 * 
	 * 功能描述：
	 * 比较 两个时间相差的秒数数
	 * @param beginTime 开始时间
	 * @param endTime 结束时间
	 * @return 返回两个时间相差的分钟数
	 * @author <a href="kuanghaibo@gmail.com">匡海波 </a>
	 * @version 1.0.0
	 * @since 1.0.0
	 * create on: 2012-9-29
	 */
	public static long compareSecond(Date beginTime,Date endTime){
		return (endTime.getTime()-beginTime.getTime())/1000;
	}
}
